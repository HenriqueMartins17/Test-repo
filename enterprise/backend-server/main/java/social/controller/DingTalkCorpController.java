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

package com.apitable.enterprise.social.controller;

import cn.hutool.core.util.ObjectUtil;
import com.apitable.core.support.ResponseData;
import com.apitable.enterprise.social.service.IDingTalkService;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.PostResource;
import com.apitable.shared.context.SessionContext;
import com.vikadata.social.dingtalk.exception.DingTalkApiException;
import com.vikadata.social.dingtalk.model.DingTalkUserDetail;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * DingTalk related interface.
 */
@RestController
@Tag(name = "DingTalk enterprise internal application related service interface")
@ApiResource(path = "/dingtalkCorp")
@Slf4j
public class DingTalkCorpController {

    @Resource
    private IDingTalkService dingTalkService;

    /**
     * dingtalk user password free login.
     */
    @PostResource(path = "/login", name = "dingtalk user password free login", requiredLogin =
        false, requiredPermission = false)
    @Operation(summary = "dingtalk user password free login", description = "After the login is "
        + "completed, the system saves the user session by default, and calls other business "
        + "interfaces to automatically bring the cookie")
    @Parameters({
        @Parameter(name = "code", description = "temporary authorization code, uploaded by the "
            + "client", schema = @Schema(type = "string"), required = true, in = ParameterIn.QUERY)
    })
    public ResponseData<DingTalkUserDetail> login(
        @RequestParam(value = "code") String requestAuthCode) throws DingTalkApiException {
        log.info("DingTalk user login,code:{}", requestAuthCode);
        // query whether there is user login information in the session
        String userId = dingTalkService.getUserInfoV2ByCode(requestAuthCode).getUserid();
        DingTalkUserDetail userInfo = dingTalkService.getUserInfoByUserId(userId);
        // query system users and save them to the session, no need to log in again next time
        if (ObjectUtil.isNotNull(userInfo)) {
            SessionContext.setDingTalkUserId(userId, userInfo.getName());
        }
        return ResponseData.success(userInfo);
    }

}
