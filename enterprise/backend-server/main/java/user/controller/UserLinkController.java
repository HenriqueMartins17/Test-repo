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

package com.apitable.enterprise.user.controller;

import com.apitable.core.support.ResponseData;
import com.apitable.enterprise.user.ro.DingTalkBindOpRo;
import com.apitable.enterprise.user.ro.UserLinkOpRo;
import com.apitable.enterprise.user.service.IUserLinkService;
import com.apitable.shared.captcha.ValidateCodeProcessorManage;
import com.apitable.shared.captcha.ValidateCodeType;
import com.apitable.shared.captcha.ValidateTarget;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.PostResource;
import com.apitable.shared.context.SessionContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Account Link Management Interface.
 */
@Slf4j
@RestController
@Tag(name = "User - Social Link")
@ApiResource
public class UserLinkController {

    @Resource
    private IUserLinkService iUserLinkService;

    /**
     * Associated DingTalk.
     */
    @PostResource(name = "Associated DingTalk", path = "/user/bindDingTalk", requiredPermission =
        false)
    @Operation(summary = "Associated DingTalk", description = "Associated DingTalk")
    public ResponseData<Void> bindDingTalk(@RequestBody @Valid DingTalkBindOpRo opRo) {
        ValidateTarget target = ValidateTarget.create(opRo.getPhone(), opRo.getAreaCode());
        ValidateCodeProcessorManage.me().findValidateCodeProcessor(ValidateCodeType.SMS)
            .verifyIsPass(target.getRealTarget());
        iUserLinkService.bindDingTalk(opRo);
        return ResponseData.success();
    }

    /**
     * Unbind the third-party account.
     */
    @PostResource(name = "Unbind the third-party account", path = "/user/unbind",
        requiredPermission = false)
    @Operation(summary = "Unbind the third-party account")
    public ResponseData<Void> unbind(@RequestBody @Valid UserLinkOpRo opRo) {
        Long userId = SessionContext.getUserId();
        iUserLinkService.unbind(userId, opRo.getType());
        return ResponseData.success();
    }
}
