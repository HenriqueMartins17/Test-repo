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

package com.apitable.enterprise.wechat.controller;

import static com.apitable.enterprise.wechat.cache.RedisKey.WECHAT_MP_CODE_MARK;
import static com.apitable.enterprise.wechat.cache.RedisKey.WECHAT_MP_QRCODE_MARK;
import static com.apitable.shared.constants.WechatConstants.MARK_PRE;
import static com.apitable.shared.constants.WechatConstants.TIMEOUT;
import static com.apitable.user.enums.UserException.NOT_SCANNED;
import static com.apitable.user.enums.UserException.QR_CODE_GET_ERROR;
import static com.apitable.user.enums.UserException.QR_CODE_INVALID;
import static com.apitable.user.enums.UserException.WECHAT_NO_EXIST;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.apitable.core.exception.BusinessException;
import com.apitable.core.support.ResponseData;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.enterprise.user.mapper.UserLinkMapper;
import com.apitable.enterprise.user.service.IUserLinkService;
import com.apitable.enterprise.wechat.autoconfigure.mp.WxMpProperties;
import com.apitable.enterprise.wechat.dto.ThirdPartyMemberInfo;
import com.apitable.enterprise.wechat.ro.MpSignatureRo;
import com.apitable.enterprise.wechat.service.IThirdPartyMemberService;
import com.apitable.enterprise.wechat.vo.QrCodeVo;
import com.apitable.interfaces.eventbus.facade.EventBusFacade;
import com.apitable.interfaces.eventbus.model.UserLoginEvent;
import com.apitable.shared.cache.bean.SocialAuthInfo;
import com.apitable.shared.cache.service.SocialAuthInfoCacheService;
import com.apitable.shared.component.TaskManager;
import com.apitable.shared.component.redis.RedisLockHelper;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.GetResource;
import com.apitable.shared.component.scanner.annotation.PostResource;
import com.apitable.shared.context.SessionContext;
import com.apitable.shared.util.RandomExtendUtil;
import com.apitable.shared.util.information.ClientOriginInfo;
import com.apitable.shared.util.information.InformationUtil;
import com.apitable.user.enums.LinkType;
import com.apitable.user.enums.ThirdPartyMemberType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxJsapiSignature;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * WeChat Mp API.
 * </p>
 */
@RestController
@Tag(name = "WeChat Mp API")
@ApiResource(path = "/wechat/mp")
@Slf4j
public class WechatMpController {

    @Autowired(required = false)
    private WxMpProperties wxMpProperties;

    @Autowired(required = false)
    private WxMpService wxMpService;

    @Resource
    private IUserLinkService iUserLinkService;

    @Resource
    private UserLinkMapper userLinkMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private SocialAuthInfoCacheService socialAuthInfoCacheService;

    @Resource
    private IThirdPartyMemberService iThirdPartyMemberService;

    @Resource
    private EventBusFacade eventBusFacade;

    /**
     * Get wechat signature.
     */
    @PostResource(path = "/signature", requiredLogin = false)
    @Operation(summary = "Get wechat signature")
    public ResponseData<WxJsapiSignature> signature(@RequestBody @Valid MpSignatureRo ro) {
        if (wxMpService == null) {
            throw new BusinessException("WeChat public account component is not enabled");
        }
        try {
            return ResponseData.success(wxMpService.createJsapiSignature(ro.getUrl()));
        } catch (WxErrorException e) {
            log.error("Wechat callback result is abnormal. Message:{}", e.getMessage());
            throw new BusinessException("Get failed");
        }
    }

    /**
     * Get qrcode.
     */
    @GetResource(path = "/qrcode", requiredLogin = false)
    @Operation(summary = "Get qrcode")
    public ResponseData<QrCodeVo> qrcode() {
        if (wxMpService == null) {
            throw new BusinessException("WeChat public account component is not enabled");
        }
        // Generate a random string as the unique identifier of the QR code
        int length = 12;
        String mark = RandomExtendUtil.randomString(length);
        try {
            // Generate QR code
            WxMpQrCodeTicket qrCodeTicket =
                wxMpService.getQrcodeService().qrCodeCreateTmpTicket(MARK_PRE + mark, TIMEOUT);
            QrCodeVo vo = QrCodeVo.builder().mark(mark).image(qrCodeTicket.getTicket())
                .url(qrCodeTicket.getUrl()).build();
            // Save the unique ID in the cache
            String key = StrUtil.format(WECHAT_MP_QRCODE_MARK, mark);
            BoundValueOperations<String, Object> opts = redisTemplate.boundValueOps(key);
            ClientOriginInfo origin =
                InformationUtil.getClientOriginInfoInCurrentHttpContext(true, false);
            opts.set(JSONUtil.toJsonStr(origin), TIMEOUT, TimeUnit.SECONDS);
            return ResponseData.success(vo);
        } catch (WxErrorException e) {
            log.error("The QR code of the WeChat official account is abnormal.", e);
            throw new BusinessException(QR_CODE_GET_ERROR);
        }
    }

    /**
     * Scan poll.
     */
    @GetResource(path = "/poll", requiredLogin = false)
    @Operation(summary = "Scan poll", description = "Scene: Scan code login, account binding "
        + "polling results")
    @Parameters({
        @Parameter(name = "type", description = "type (0: scan code to log in; 1: account "
            + "binding)", schema = @Schema(type = "integer"), required = true, in =
            ParameterIn.QUERY, example = "0"),
        @Parameter(name = "mark", description = "the unique identifier of the qrcode", schema =
        @Schema(type = "string"), required = true, in = ParameterIn.QUERY, example = "mark11")
    })
    public ResponseData<String> poll(@RequestParam(value = "type") Integer type,
                                     @RequestParam(value = "mark", required = false) String mark) {
        if (wxMpProperties == null) {
            throw new BusinessException("WeChat public account component is not enabled");
        }
        // Read qrcode unique ID cache
        String key = StrUtil.format(WECHAT_MP_QRCODE_MARK, mark);
        BoundValueOperations<String, Object> opts = redisTemplate.boundValueOps(key);
        if (opts.get() == null) {
            throw new BusinessException(QR_CODE_INVALID.getCode(), QR_CODE_INVALID.getMessage());
        }
        String unionId = Objects.requireNonNull(opts.get()).toString();
        if (JSONUtil.isTypeJSON(unionId)) {
            throw new BusinessException(NOT_SCANNED.getCode(), NOT_SCANNED.getMessage());
        }
        if (type == 0) {
            // Get the unionId from the cache and query the bound user ID
            Long linkUserId =
                userLinkMapper.selectUserIdByUnionIdAndType(unionId, LinkType.WECHAT.getType());
            // Scan code to log in
            if (linkUserId == null) {
                // When the associated Weige account cannot be found, save the unionId in the
                // user-authorized cache,
                // complete the association and log in after completing the user information on
                // the PC side
                SocialAuthInfo authInfo = new SocialAuthInfo();
                authInfo.setType(LinkType.WECHAT.getType());
                authInfo.setUnionId(unionId);
                return ResponseData.success(
                    socialAuthInfoCacheService.saveAuthInfoToCache(authInfo));
            }
            SessionContext.setUserId(linkUserId);
            ClientOriginInfo origin =
                InformationUtil.getClientOriginInfoInCurrentHttpContext(false, true);
            TaskManager.me().execute(() ->
                eventBusFacade.onEvent(new UserLoginEvent(linkUserId,
                    "WeChat official account scan code", false, origin)));
        } else {
            // account binding
            final Long userId = SessionContext.getUserId();
            ThirdPartyMemberInfo info =
                iThirdPartyMemberService.getMemberInfoByCondition(wxMpProperties.getAppId(),
                    unionId, ThirdPartyMemberType.WECHAT_PUBLIC_ACCOUNT.getType());
            ExceptionUtil.isNotNull(info, WECHAT_NO_EXIST);
            SocialAuthInfo authInfo = new SocialAuthInfo();
            authInfo.setUnionId(unionId);
            authInfo.setNickName(info.getNickName());
            iUserLinkService.createUserLink(userId, authInfo, LinkType.WECHAT.getType());
        }
        // Delete the unique ID cache
        redisTemplate.delete(key);
        return ResponseData.success(null);
    }

    /**
     * Web Page Authorization Callback.
     */
    @GetResource(path = "/web/callback", requiredLogin = false)
    @Operation(summary = "Web Page Authorization Callback")
    @Parameters({
        @Parameter(name = "code", description = "coding. JS gets the loginTmpCode, redirects and "
            + "returns after jumping to the specified connection", schema = @Schema(type =
            "string"), required = true, in = ParameterIn.QUERY, example = "ABC123"),
        @Parameter(name = "state", description = "declare value. Used to prevent replay attacks",
            schema = @Schema(type = "string"), required = true, in = ParameterIn.QUERY, example =
            "STATE")
    })
    public ResponseData<String> callback(@RequestParam(name = "code") String code,
                                         @RequestParam(name = "state") String state) {
        log.info("Web page authorization callback. code:{},state:{}", code, state);
        if (wxMpService == null || wxMpProperties == null) {
            throw new BusinessException("WeChat public account component is not enabled");
        }
        // prevent duplicate requests
        RedisLockHelper.me().preventDuplicateRequests(StrUtil.format(WECHAT_MP_CODE_MARK, code));
        try {
            // Exchange code for web page authorization access_token
            WxOAuth2AccessToken accessToken = wxMpService.getOAuth2Service().getAccessToken(code);
            if (accessToken.getUnionId() == null) {
                throw new BusinessException(
                    "Please set the scope parameter of user authorization to snsapi_userinfo");
            }
            // Check whether the member has been saved
            String unionId =
                iThirdPartyMemberService.getUnionIdByCondition(wxMpProperties.getAppId(),
                    accessToken.getOpenId(), ThirdPartyMemberType.WECHAT_PUBLIC_ACCOUNT.getType());
            if (unionId == null) {
                // Pull user information
                WxMpUser wxMpUser = wxMpService.getUserService().userInfo(accessToken.getOpenId());
                // Not following the user, unable to get union_id to complete the binding
                if (StrUtil.isBlank(wxMpUser.getUnionId())) {
                    return ResponseData.success(RandomExtendUtil.randomString(12));
                }
                unionId = wxMpUser.getUnionId();
                iThirdPartyMemberService.createMpMember(wxMpProperties.getAppId(), wxMpUser);
            }
            // Check if an account is linked
            Long linkUserId =
                userLinkMapper.selectUserIdByUnionIdAndType(unionId, LinkType.WECHAT.getType());
            if (linkUserId == null) {
                SocialAuthInfo authInfo = new SocialAuthInfo();
                authInfo.setType(LinkType.WECHAT.getType());
                authInfo.setUnionId(unionId);
                return ResponseData.success(
                    socialAuthInfoCacheService.saveAuthInfoToCache(authInfo));
            }
            SessionContext.setUserId(linkUserId);
            ClientOriginInfo origin =
                InformationUtil.getClientOriginInfoInCurrentHttpContext(false, true);
            TaskManager.me().execute(() ->
                eventBusFacade.onEvent(new UserLoginEvent(linkUserId,
                    "WeChat webpage authorization", false, origin)));
            return ResponseData.success(null);
        } catch (WxErrorException e) {
            log.error("Web page authorization callback failed. Message:{}", e.getMessage());
            throw new BusinessException("Web page authorization callback failed");
        }
    }
}
