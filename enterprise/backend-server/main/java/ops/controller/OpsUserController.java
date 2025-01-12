/*
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up
 * license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its
 * subdirectories does not constitute permission to use this code or APITable Enterprise Edition
 * features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

package com.apitable.enterprise.ops.controller;

import static com.apitable.user.enums.UserException.USER_NOT_EXIST;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.BooleanUtil;
import com.apitable.auth.ro.RegisterRO;
import com.apitable.core.support.ResponseData;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.enterprise.ops.ro.OpsAuthRo;
import com.apitable.enterprise.ops.service.IOpsService;
import com.apitable.shared.cache.bean.LoginUserDto;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.PostResource;
import com.apitable.shared.constants.InternalConstants;
import com.apitable.shared.context.LoginContext;
import com.apitable.shared.context.SessionContext;
import com.apitable.user.entity.UserEntity;
import com.apitable.user.enums.UserException;
import com.apitable.user.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * Product Operation System - User API.
 * </p>
 */
@Slf4j
@RestController
@ApiResource(path = "/ops")
@Tag(name = "Product Operation System - User API")
public class OpsUserController {

    @Resource
    private IOpsService iOpsService;

    @Resource
    private IUserService iUserService;

    @Value("${ENABLED_MANAGEMENT_BACKGROUND:false}")
    private Boolean enabledManagementBackground;

    @Value("${SUPER_ADMINISTRATORS:125,126}")
    private String superAdministratorsStr;

    @PostResource(path = "/user/updatePwd", requiredPermission = false)
    @Operation(summary = "Update Appoint Account Password",
        description = "Only supply to customized customers")
    public ResponseData<Void> updatePwd(@RequestBody @Valid final RegisterRO data) {
        if (BooleanUtil.isFalse(enabledManagementBackground)) {
            return ResponseData.error("Not Enabled.");
        }
        LoginUserDto loginUser = LoginContext.me().getLoginUser();
        if (loginUser == null || !superAdministratorsStr.contains(loginUser.getUuid())) {
            return ResponseData.error("You aren't super administrator.");
        }
        Long targetUserId = Validator.isEmail(data.getUsername())
            ? iUserService.getUserIdByEmail(data.getUsername())
            : iUserService.getUserIdByMobile(data.getUsername());
        ExceptionUtil.isNotNull(targetUserId, USER_NOT_EXIST);
        iUserService.updatePwd(targetUserId, data.getCredential());
        return ResponseData.success();
    }

    /**
     * Ban account.
     */
    @PostResource(path = "/users/{userId}/ban", requiredLogin = false)
    @Operation(summary = "Ban account", description = "Restrict login and force logout.")
    public ResponseData<Void> banUser(@PathVariable("userId") Long userId,
                                      @RequestBody OpsAuthRo body) {
        log.info("The operator「{}」ban the account「{}」",
            SessionContext.getUserIdWithoutException(), userId);
        // Check permissions.
        iOpsService.auth(body.getToken());
        // Query whether the user exist.
        UserEntity entity = iUserService.getById(userId);
        ExceptionUtil.isNotNull(entity, UserException.USER_NOT_EXIST);
        // Ban account.
        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setRemark(InternalConstants.BAN_ACCOUNT_REMARK);
        iUserService.updateById(user);
        // Close other login session of account.
        iUserService.closeMultiSession(userId, false);
        return ResponseData.success();
    }

    /**
     * Close paused account.
     */
    @PostResource(path = "/users/{uuid}/close", requiredLogin = false)
    @Operation(summary = "Close paused account")
    public ResponseData<Void> closeAccountDirectly(@PathVariable(name = "uuid") String userUuid,
                                                   @RequestBody OpsAuthRo body) {
        iOpsService.auth(body.getToken());
        Long logoutUserId = iUserService.getUserIdByUuidWithCheck(userUuid);
        UserEntity user = iUserService.getById(logoutUserId);
        // The account does not exist or has been logged out
        if (user == null) {
            return ResponseData.error("The account does not exist or has been logged out.");
        }
        // An exception is displayed if the account is not in the logout cooling-off period.
        if (!user.getIsPaused()) {
            return ResponseData.error(
                "The account cannot be deleted because it does not send a logout request.");
        }
        // Close the account and clear the account data
        iUserService.closeAccount(user);
        // Clear the cookie information of the user
        iUserService.closeMultiSession(logoutUserId, false);
        return ResponseData.success();
    }

}
