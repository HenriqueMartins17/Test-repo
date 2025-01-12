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

package com.apitable.enterprise.appstore.controller;

import static com.apitable.enterprise.social.constants.LarkConstants.CONFIG_ERROR_URL;
import static com.vikadata.social.feishu.FeishuServiceProvider.EVENT_CALLBACK_EVENT;
import static com.vikadata.social.feishu.FeishuServiceProvider.URL_VERIFICATION_EVENT;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.apitable.core.support.ResponseData;
import com.apitable.enterprise.appstore.entity.AppInstanceEntity;
import com.apitable.enterprise.appstore.enums.AppType;
import com.apitable.enterprise.appstore.model.AppInstance;
import com.apitable.enterprise.appstore.model.LarkInstanceConfig;
import com.apitable.enterprise.appstore.model.LarkInstanceConfigProfile;
import com.apitable.enterprise.appstore.service.IAppInstanceService;
import com.apitable.enterprise.appstore.service.ILarkAppInstanceConfigService;
import com.apitable.enterprise.social.constants.LarkConstants;
import com.apitable.enterprise.social.enums.SocialPlatformType;
import com.apitable.enterprise.social.model.FeishuAppConfigRo;
import com.apitable.enterprise.social.model.FeishuAppEventConfigRo;
import com.apitable.enterprise.social.model.SocialUser;
import com.apitable.enterprise.social.properties.FeishuAppProperties;
import com.apitable.enterprise.social.service.IFeishuInternalEventService;
import com.apitable.enterprise.social.service.IFeishuService;
import com.apitable.enterprise.social.service.ISocialService;
import com.apitable.enterprise.social.service.ISocialUserBindService;
import com.apitable.enterprise.social.util.HttpServletUtil;
import com.apitable.interfaces.eventbus.facade.EventBusFacade;
import com.apitable.interfaces.eventbus.model.UserLoginEvent;
import com.apitable.shared.component.TaskManager;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.GetResource;
import com.apitable.shared.component.scanner.annotation.PostResource;
import com.apitable.shared.config.properties.ConstProperties;
import com.apitable.shared.context.SessionContext;
import com.apitable.shared.util.information.ClientOriginInfo;
import com.apitable.shared.util.information.InformationUtil;
import com.vikadata.social.feishu.FeishuConfigStorageHolder;
import com.vikadata.social.feishu.config.FeishuConfigStorage;
import com.vikadata.social.feishu.event.BaseEvent;
import com.vikadata.social.feishu.event.contact.v3.BaseV3ContactEvent;
import com.vikadata.social.feishu.model.FeishuAccessToken;
import com.vikadata.social.feishu.model.FeishuPassportAccessToken;
import com.vikadata.social.feishu.model.FeishuPassportUserInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * LarkSelf built application configuration interface.
 */
@RestController
@ApiResource(path = "/")
@Tag(name = "App Store - Lark Instance")
@Slf4j
public class LarkAppInstanceController {

    private static final String REDIRECT_STATE = "adminScan";

    @Resource
    private ConstProperties constProperties;

    @Resource
    private FeishuAppProperties feishuAppProperties;

    @Resource
    private IAppInstanceService iAppInstanceService;

    @Resource
    private ILarkAppInstanceConfigService iLarkAppInstanceConfigService;

    @Resource
    private IFeishuInternalEventService iFeishuInternalEventService;

    @Resource
    private IFeishuService iFeishuService;

    @Resource
    private EventBusFacade eventBusFacade;

    @Resource
    private ISocialUserBindService iSocialUserBindService;

    @Resource
    private ISocialService iSocialService;

    private String getErrorPath() {
        return StrUtil.format(constProperties.getServerDomain() + feishuAppProperties.getErrorUri(),
            "auth_fail");
    }

    /**
     * Lark Self built Application Event Callback.
     */
    @PostResource(path = "/lark/event/{appInstanceId}", requiredLogin = false)
    @Operation(summary = "Lark Self built Application Event Callback", description = "Receive "
        + "self built callback of Lark application identity", hidden = true)
    public Object larkEvent(@PathVariable("appInstanceId") String appInstanceId,
                            HttpServletRequest request) {
        FeishuConfigStorage configStorage =
            iAppInstanceService.buildConfigStorageByInstanceId(appInstanceId);
        // Switch application context
        iFeishuService.switchContextIfAbsent(configStorage);
        String requestBody = HttpServletUtil.getRequestBody(request);
        // Decrypt
        Map<String, Object> jsonData = iFeishuService.decryptData(requestBody);
        // Get event type
        if (jsonData.containsKey("schema")) {
            // 2.0Version event push, verify TOKEN
            TypeReference<Map<String, Object>> typeReference = new TypeReference<>() {
            };
            Map<String, Object> header =
                MapUtil.get(jsonData, "header", typeReference);
            iFeishuService.checkVerificationToken(header);
            String contactType = (String) header.get("event_type");
            BaseV3ContactEvent event =
                iFeishuService.getV3EventParser().parseEvent(contactType, jsonData);
            event.setAppInstanceId(appInstanceId);
            iFeishuService.getEventListenerManager().fireV3ContactEventCallback(event);
        } else {
            // Version 1.0 event, verify TOKEN
            iFeishuService.checkVerificationToken(jsonData);
            String eventType = MapUtil.getStr(jsonData, "type");
            if (URL_VERIFICATION_EVENT.equals(eventType)) {
                // Verify the callback address and set the callback successfully
                iFeishuInternalEventService.urlCheck(appInstanceId);
                // Return content as required
                return JSONUtil.createObj().set("challenge", jsonData.get("challenge")).toString();
            } else if (EVENT_CALLBACK_EVENT.equals(eventType)) {
                // Event Push
                log.info("Lark event push: {}", eventType);
                TypeReference<Map<String, Object>> typeReference = new TypeReference<>() {
                };
                Map<String, Object> eventData =
                    MapUtil.get(jsonData, "event", typeReference);
                if (log.isDebugEnabled()) {
                    log.debug("Event content:{}", JSONUtil.toJsonPrettyStr(eventData));
                }
                String eventSubType = eventData.get("type").toString();
                BaseEvent event =
                    iFeishuService.getEventParser().parseEvent(eventSubType, eventData);
                if (event == null) {
                    log.error("Event{}, Content:{}, does not exist and cannot be processed",
                        eventSubType, eventData);
                    return "SUCCESS";
                }
                event.setAppInstanceId(appInstanceId);
                BaseEvent.Meta meta = new BaseEvent.Meta();
                meta.setUuid(MapUtil.getStr(jsonData, "uuid"));
                meta.setTs(MapUtil.getStr(jsonData, "ts"));
                event.setMeta(meta);
                iFeishuService.getEventListenerManager().fireEventCallback(event);
            } else {
                log.error("Illegal event type of Lark self built application: {}", eventType);
            }
        }
        return "SUCCESS";
    }

    /**
     * Lark Self built Application Identity Application Portal.
     */
    @GetResource(path = "/lark/idp/entry/{appInstanceId}", requiredLogin = false)
    @Operation(summary = "Lark Self built Application Identity Application Portal", description =
        "Lark Idp identity login free entry route", hidden = true)
    public RedirectView larkIdpEntry(@PathVariable("appInstanceId") String appInstanceId,
                                     @RequestParam(name = "url", required = false) String url) {
        // Construct Lark authorization login
        String redirectUri =
            constProperties.getServerDomain() + LarkConstants.formatInternalLoginUrl(appInstanceId);
        if (StrUtil.isNotBlank(url)) {
            redirectUri = StrUtil.format(redirectUri + "?url={}", url);
        }
        try {
            LarkInstanceConfig instanceConfig =
                iLarkAppInstanceConfigService.getLarkConfig(appInstanceId);
            LarkInstanceConfigProfile profile =
                (LarkInstanceConfigProfile) instanceConfig.getProfile();
            iFeishuService.switchContextIfAbsent(profile.buildConfigStorage());
            String authUrl =
                iFeishuService.buildAuthUrl(redirectUri, String.valueOf(DateUtil.date().getTime()));
            return new RedirectView(authUrl);
        } catch (Exception e) {
            log.error(
                "Lark self built application message card reported an error when routing in, "
                    + "instance ID:{}, path: {}",
                appInstanceId, url, e);
            return new RedirectView(getErrorPath());
        } finally {
            FeishuConfigStorageHolder.remove();
        }
    }

    /**
     * Lark application authentication callback service.
     */
    @GetResource(path = "/lark/idp/login/{appInstanceId}", requiredLogin = false)
    @Operation(summary = "Lark application authentication callback service", description =
        "Receive Lark Idp identity provider callback temporary authorization code", hidden = true)
    public RedirectView larkIdpLogin(@PathVariable("appInstanceId") String appInstanceId,
                                     @RequestParam("code") String code,
                                     @RequestParam("state") String state,
                                     @RequestParam(name = "url", required = false) String url) {
        log.info("Self built application callback parameters[code:{}, state:{}]", code, state);
        // Query Lark Configuration of an Application Instance
        AppInstanceEntity appInstanceEntity = iAppInstanceService.getByAppInstanceId(appInstanceId);
        if (appInstanceEntity == null) {
            // Instance does not exist, routing to error page
            return new RedirectView(getErrorPath());
        }
        if (!appInstanceEntity.getIsEnabled()) {
            // When disabled, route to the error page
            return new RedirectView(getErrorPath());
        }
        AppInstance appInstance = iAppInstanceService.buildInstance(appInstanceEntity);
        if (appInstance.getConfig().getType() != AppType.LARK) {
            // Not an instance of Lark type
            return new RedirectView(getErrorPath());
        }
        LarkInstanceConfigProfile profile =
            (LarkInstanceConfigProfile) appInstance.getConfig().getProfile();
        if (StrUtil.isBlank(profile.getAppKey())
            || StrUtil.isBlank(profile.getAppSecret())
            || StrUtil.isBlank(profile.getEventVerificationToken())
            || !profile.isEventCheck()) {
            // Redirect page with incomplete basic configuration
            return new RedirectView(constProperties.getServerDomain() + CONFIG_ERROR_URL);
        }
        // Switch application context
        iFeishuService.switchContextIfAbsent(profile.buildConfigStorage());
        // All configurations completed
        if (!profile.isContactSyncDone()) {
            // Address book not synchronized
            if (REDIRECT_STATE.equals(state)) {
                FeishuPassportUserInfo userInfo;
                try {
                    // It can only be used within the scope of application available authorization
                    FeishuPassportAccessToken accessToken =
                        iFeishuService.getPassportAccessToken(code, profile.getRedirectUrl());
                    userInfo = iFeishuService.getPassportUserInfo(accessToken.getAccessToken());
                } catch (Exception exception) {
                    log.error("Lark application instance tenant entry authorization failed",
                        exception);
                    return new RedirectView(getErrorPath());
                }
                // The administrator scans the code to create a new asynchronous task and execute
                // the synchronous address book
                iFeishuInternalEventService.syncContactFirst(userInfo, appInstance);
            }
            // The synchronization is not completed or failed. Redirect to the waiting page
            return new RedirectView(
                constProperties.getServerDomain() + LarkConstants.formatContactSyncingUrl(
                    appInstanceId));
        } else {
            // Synchronized address book, pure login operation
            FeishuAccessToken accessToken;
            try {
                // It can only be used within the scope of application available authorization
                accessToken = iFeishuService.getUserAccessToken(code);
            } catch (Exception exception) {
                log.error("Lark application instance tenant entry authorization failed", exception);
                return new RedirectView(getErrorPath());
            }
            // Self built application, query whether the user exists according to the email or
            // mobile phone, if not, create a new one
            Long userId = iSocialUserBindService.getUserIdByUnionId(accessToken.getUnionId());
            if (userId == null) {
                // None exists, create user
                userId = iSocialService.createUser(
                    new SocialUser(accessToken.getName(), accessToken.getAvatarUrl(),
                        StrUtil.subPre(accessToken.getMobile(), 3),
                        StrUtil.subSuf(accessToken.getMobile(), 3), accessToken.getEmail(),
                        profile.getAppKey(), accessToken.getTenantKey(), accessToken.getOpenId(),
                        accessToken.getUnionId(), SocialPlatformType.FEISHU));
                ClientOriginInfo origin =
                    InformationUtil.getClientOriginInfoInCurrentHttpContext(false, true);
                // Shence burial site - registration
                Long finalUserId = userId;
                TaskManager.me().execute(() ->
                    eventBusFacade.onEvent(new UserLoginEvent(finalUserId,
                        "Lark self built application", true, origin)));
            }
            iSocialService.activeTenantSpace(userId, appInstance.getSpaceId(),
                accessToken.getOpenId());
            SessionContext.setUserId(userId);
            if (StrUtil.isNotBlank(url)) {
                // Redirect to mention notification address
                return new RedirectView(url);
            } else {
                // Redirect the specified page
                return new RedirectView(
                    constProperties.getServerDomain() + LarkConstants.formatSpaceWorkbenchUrl(
                        appInstance.getSpaceId()));
            }
        }
    }

    /**
     * Update basic configuration.
     */
    @PostResource(path = "/lark/appInstance/{appInstanceId}/updateBaseConfig", method =
        RequestMethod.PUT, requiredPermission = false)
    @Operation(summary = "Update basic configuration", description = "Update the basic "
        + "configuration of the application instance")
    @Parameters({
        @Parameter(name = "appInstanceId", description = "Application instance ID", required =
            true, schema = @Schema(type = "string"), in = ParameterIn.PATH, example = "ai-1jsjakd1")
    })
    public ResponseData<AppInstance> initConfig(@PathVariable("appInstanceId") String appInstanceId,
                                                @RequestBody @Valid FeishuAppConfigRo data) {
        String appKey = data.getAppKey();
        String appSecret = data.getAppSecret();
        return ResponseData.success(
            iLarkAppInstanceConfigService.updateLarkBaseConfig(appInstanceId, appKey, appSecret));
    }

    /**
     * Update Event Configuration.
     */
    @PostResource(path = "/lark/appInstance/{appInstanceId}/updateEventConfig", method =
        RequestMethod.PUT, requiredPermission = false)
    @Operation(summary = "Update Event Configuration", description = "Change the event "
        + "configuration of an application instance")
    @Parameters({
        @Parameter(name = "appInstanceId", description = "Application instance ID", required =
            true, schema = @Schema(type = "string"), in = ParameterIn.PATH, example = "ai-1jsjakd1")
    })
    public ResponseData<AppInstance> eventConfig(
        @PathVariable("appInstanceId") String appInstanceId,
        @RequestBody @Valid FeishuAppEventConfigRo data) {
        return ResponseData.success(
            iLarkAppInstanceConfigService.updateLarkEventConfig(appInstanceId,
                data.getEventEncryptKey(), data.getEventVerificationToken()));
    }
}
