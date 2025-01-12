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

import static com.apitable.base.enums.ParameterException.INCORRECT_ARG;
import static com.apitable.core.constants.RedisConstants.GENERAL_LOCKED;

import cn.hutool.core.util.StrUtil;
import com.apitable.core.exception.BusinessException;
import com.apitable.core.support.ResponseData;
import com.apitable.enterprise.social.service.IDingTalkService;
import com.apitable.enterprise.user.mapper.UserLinkMapper;
import com.apitable.enterprise.user.service.IUserLinkService;
import com.apitable.interfaces.eventbus.facade.EventBusFacade;
import com.apitable.interfaces.eventbus.model.UserLoginEvent;
import com.apitable.shared.cache.bean.SocialAuthInfo;
import com.apitable.shared.cache.service.SocialAuthInfoCacheService;
import com.apitable.shared.component.TaskManager;
import com.apitable.shared.component.redis.RedisLockHelper;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.GetResource;
import com.apitable.shared.context.SessionContext;
import com.apitable.shared.util.information.ClientOriginInfo;
import com.apitable.shared.util.information.InformationUtil;
import com.apitable.user.enums.LinkType;
import com.vikadata.social.dingtalk.exception.DingTalkApiException;
import com.vikadata.social.dingtalk.model.UserInfo;
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
@Tag(name = "DingTalk service interface")
@ApiResource(path = "/dingtalk")
@Slf4j
public class DingTalkController {

    @Resource
    private IDingTalkService dingTalkService;

    @Resource
    private SocialAuthInfoCacheService socialAuthInfoCacheService;

    @Resource
    private IUserLinkService iUserLinkService;

    @Resource
    private UserLinkMapper userLinkMapper;

    @Resource
    private EventBusFacade eventBusFacade;

    /**
     * DingTalk scan code login callback.
     */
    @GetResource(path = "/login/callback", requiredLogin = false)
    @Operation(summary = "DingTalk scan code login callback")
    @Parameters({
        @Parameter(name = "type", description = "Type (0: scan code to log in; 1: account "
            + "binding;)", schema = @Schema(type = "integer"), in = ParameterIn.QUERY, example =
            "0"),
        @Parameter(name = "code", description = "coding. JS gets the login Tmp Code, redirects "
            + "and returns after jumping to the specified connection", schema = @Schema(type =
            "string"), required = true, in = ParameterIn.QUERY, example = "ABC123"),
        @Parameter(name = "state", description = "declare value. Used to prevent replay attacks",
            schema = @Schema(type = "string"), required = true, in = ParameterIn.QUERY, example =
            "STATE")
    })
    public ResponseData<String> callback(
        @RequestParam(value = "type", required = false, defaultValue = "0") Integer type,
        @RequestParam(name = "code") String code, @RequestParam(name = "state") String state) {
        // prevent duplicate requests
        RedisLockHelper.me()
            .preventDuplicateRequests(StrUtil.format(GENERAL_LOCKED, "dingtalk:code", code));
        log.info("DingTalk scan code login callback, type:{},code:{},state:{}", type, code, state);
        UserInfo userInfo;
        // get personal information of authorized users through temporary authorization codes
        try {
            userInfo = dingTalkService.getUserInfoByCode(code);
        } catch (DingTalkApiException e) {
            log.info(
                "Failed to get user information through temporary authorization code Code, "
                    + "code:{},msg:{}",
                e.getCode(), e.getMessage());
            throw new BusinessException(INCORRECT_ARG);
        }
        // Account binding processing
        if (type == 1) {
            final Long userId = SessionContext.getUserId();
            SocialAuthInfo authInfo = new SocialAuthInfo();
            authInfo.setUnionId(userInfo.getUnionid());
            authInfo.setOpenId(userInfo.getOpenid());
            authInfo.setNickName(userInfo.getNick());
            iUserLinkService.createUserLink(userId, authInfo, LinkType.DINGTALK.getType());
            return ResponseData.success(null);
        }
        // Query whether the account is associated
        Long linkUserId = userLinkMapper.selectUserIdByUnionIdAndType(userInfo.getUnionid(),
            LinkType.DINGTALK.getType());
        if (linkUserId == null) {
            // When the associated account cannot be found, save the information in the cache
            // authorized by the user, complete the association and log in after completing the
            // user information on the PC side
            SocialAuthInfo authInfo = new SocialAuthInfo();
            authInfo.setType(LinkType.DINGTALK.getType());
            authInfo.setUnionId(userInfo.getUnionid());
            authInfo.setOpenId(userInfo.getOpenid());
            authInfo.setNickName(userInfo.getNick());
            return ResponseData.success(socialAuthInfoCacheService.saveAuthInfoToCache(authInfo));
        }
        // Successful login, save session
        SessionContext.setUserId(linkUserId);
        // Sensor - Login
        ClientOriginInfo origin =
            InformationUtil.getClientOriginInfoInCurrentHttpContext(false, true);
        TaskManager.me().execute(() ->
            eventBusFacade.onEvent(new UserLoginEvent(linkUserId,
                "DingTalk scan code", false, origin)));
        return ResponseData.success(null);
    }

}
