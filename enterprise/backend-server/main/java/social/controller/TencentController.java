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
import static com.apitable.user.enums.UserException.AUTH_FAIL;

import cn.hutool.core.util.StrUtil;
import com.apitable.core.exception.BusinessException;
import com.apitable.core.support.ResponseData;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.enterprise.user.mapper.UserLinkMapper;
import com.apitable.enterprise.user.service.IUserLinkService;
import com.apitable.enterprise.wechat.mapper.ThirdPartyMemberMapper;
import com.apitable.enterprise.wechat.service.IThirdPartyMemberService;
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
import com.apitable.user.enums.ThirdPartyMemberType;
import com.vikadata.social.qq.QQException;
import com.vikadata.social.qq.QQTemplate;
import com.vikadata.social.qq.model.AccessTokenInfo;
import com.vikadata.social.qq.model.TencentUserInfo;
import com.vikadata.social.qq.model.WebAppAuthInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * Tencent QQ related service interface.
 * </p>
 */
@RestController
@Tag(name = "Tencent QQ module_Tencent QQ related service interface")
@ApiResource(path = "/tencent")
@Slf4j
public class TencentController {

    @Autowired(required = false)
    private QQTemplate qqTemplate;

    @Resource
    private SocialAuthInfoCacheService socialAuthInfoCacheService;

    @Resource
    private IThirdPartyMemberService iThirdPartyMemberService;

    @Resource
    private ThirdPartyMemberMapper thirdPartyMemberMapper;

    @Resource
    private IUserLinkService iUserLinkService;

    @Resource
    private UserLinkMapper userLinkMapper;

    @Resource
    private EventBusFacade eventBusFacade;

    /**
     * Website application callback.
     */
    @GetResource(path = "/web/callback", requiredLogin = false)
    @Operation(summary = "Website application callback", description = "code、accessToken, At "
        + "least one is passed in")
    @Parameters({
        @Parameter(name = "type", description = "Type (0: Scan code for login; 1: Account "
            + "binding;)", schema = @Schema(type = "integer"), in = ParameterIn.QUERY, example =
            "0"),
        @Parameter(name = "code", description = "Code (build the request yourself and call back "
            + "the parameter)", schema = @Schema(type = "string"), in = ParameterIn.QUERY,
            example = "ABC123"),
        @Parameter(name = "accessToken", description = "Authorization token (use the JS SDK to "
            + "call back this parameter)", schema = @Schema(type = "string"), in =
            ParameterIn.QUERY, example = "05C5374834"),
        @Parameter(name = "expiresIn", description = "access token's TERM OF VALIDITY", schema =
        @Schema(type = "string"), in = ParameterIn.QUERY, example = "7776000")
    })
    public ResponseData<String> callback(
        @RequestParam(value = "type", required = false, defaultValue = "0") Integer type,
        @RequestParam(name = "code", required = false) String code,
        @RequestParam(name = "accessToken", required = false) String accessToken)
        throws QQException {
        log.info("QQ Website application callback，type:{},code:{},accessToken:{}", type, code,
            accessToken);
        ExceptionUtil.isTrue(code != null || accessToken != null, INCORRECT_ARG);
        if (qqTemplate == null) {
            throw new BusinessException("QQ connection service is not opened");
        }
        if (code != null) {
            // Prevent duplicate requests
            RedisLockHelper.me()
                .preventDuplicateRequests(StrUtil.format(GENERAL_LOCKED, "qq:code", code));
            AccessTokenInfo tokenInfo = qqTemplate.authOperations().getAccessToken(code);
            ExceptionUtil.isNotNull(tokenInfo, AUTH_FAIL);
            accessToken = tokenInfo.getAccessToken();
        }
        WebAppAuthInfo webAppAuthInfo = qqTemplate.authOperations().getAuthInfo(accessToken);
        ExceptionUtil.isNotNull(webAppAuthInfo, AUTH_FAIL);
        // Query the member's nickname
        String nickName =
            thirdPartyMemberMapper.selectNickNameByUnionIdAndType(webAppAuthInfo.getClientId(),
                webAppAuthInfo.getUnionId(), ThirdPartyMemberType.TENCENT.getType());
        // If there is no record, save the member information
        if (nickName == null) {
            TencentUserInfo userInfo = qqTemplate.authOperations()
                .getTencentUserInfo(accessToken, webAppAuthInfo.getClientId(),
                    webAppAuthInfo.getOpenId());
            ExceptionUtil.isNotNull(userInfo, AUTH_FAIL);
            iThirdPartyMemberService.createTencentMember(webAppAuthInfo, userInfo);
            nickName = userInfo.getNickname();
        }
        // Account binding processing
        if (type == 1) {
            final Long userId = SessionContext.getUserId();
            SocialAuthInfo authInfo = new SocialAuthInfo();
            authInfo.setUnionId(webAppAuthInfo.getUnionId());
            authInfo.setOpenId(webAppAuthInfo.getOpenId());
            authInfo.setNickName(nickName);
            iUserLinkService.createUserLink(userId, authInfo, LinkType.TENCENT.getType());
            return ResponseData.success(null);
        }
        // Query whether the vika account is associated
        Long linkUserId = userLinkMapper.selectUserIdByUnionIdAndType(webAppAuthInfo.getUnionId(),
            LinkType.TENCENT.getType());
        if (linkUserId == null) {
            // If the associated vika account cannot be found, the information will be saved in
            // the cache authorized by the user,
            // and the association will be completed and logged in after the PC completes the
            // user information
            SocialAuthInfo authInfo = new SocialAuthInfo();
            authInfo.setType(LinkType.TENCENT.getType());
            authInfo.setUnionId(webAppAuthInfo.getUnionId());
            authInfo.setOpenId(webAppAuthInfo.getOpenId());
            authInfo.setNickName(nickName);
            return ResponseData.success(socialAuthInfoCacheService.saveAuthInfoToCache(authInfo));
        }
        // Login succeeded, save session
        SessionContext.setUserId(linkUserId);
        // Shence Burial Point - Login
        ClientOriginInfo origin =
            InformationUtil.getClientOriginInfoInCurrentHttpContext(false, true);
        TaskManager.me().execute(() ->
            eventBusFacade.onEvent(new UserLoginEvent(linkUserId,
                "QQ scanning code", false, origin)));
        return ResponseData.success(null);
    }
}
