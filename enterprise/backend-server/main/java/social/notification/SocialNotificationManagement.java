/*
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its subdirectories does not constitute permission to use this code or APITable Enterprise Edition features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

package com.apitable.enterprise.social.notification;

import static com.apitable.enterprise.social.notification.SocialTemplateId.SPACE_PAID_NOTIFY;
import static com.apitable.enterprise.social.notification.SocialTemplateId.SPACE_VIKA_PAID_NOTIFY;
import static com.apitable.shared.constants.NotificationConstants.EXPIRE_AT;
import static com.apitable.shared.constants.NotificationConstants.PAY_FEE;
import static com.apitable.shared.constants.NotificationConstants.PLAN_NAME;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.apitable.core.util.SpringContextHolder;
import com.apitable.enterprise.appstore.entity.AppInstanceEntity;
import com.apitable.enterprise.appstore.enums.AppType;
import com.apitable.enterprise.appstore.model.LarkInstanceConfig;
import com.apitable.enterprise.appstore.model.LarkInstanceConfigProfile;
import com.apitable.enterprise.appstore.service.IAppInstanceService;
import com.apitable.enterprise.social.autoconfigure.dingtalk.DingTalkProperties.IsvAppProperty;
import com.apitable.enterprise.social.constants.LarkConstants;
import com.apitable.enterprise.social.entity.SocialTenantEntity;
import com.apitable.enterprise.social.enums.SocialAppType;
import com.apitable.enterprise.social.enums.SocialPlatformType;
import com.apitable.enterprise.social.event.wecom.WeComCardFactory;
import com.apitable.enterprise.social.event.wecom.WeComIsvCardFactory;
import com.apitable.enterprise.social.model.TenantBindDTO;
import com.apitable.enterprise.social.notification.observer.DingTalkIsvNotifyObserver;
import com.apitable.enterprise.social.notification.observer.DingTalkNotifyObserver;
import com.apitable.enterprise.social.notification.observer.LarkIsvNotifyObserver;
import com.apitable.enterprise.social.notification.observer.LarkNotifyObserver;
import com.apitable.enterprise.social.notification.observer.WecomIsvNotifyObserver;
import com.apitable.enterprise.social.notification.observer.WecomNotifyObserver;
import com.apitable.enterprise.social.notification.subject.SocialNotifyContext;
import com.apitable.enterprise.social.notification.subject.SocialNotifySubject;
import com.apitable.enterprise.social.service.IDingTalkInternalService;
import com.apitable.enterprise.social.service.IDingTalkService;
import com.apitable.enterprise.social.service.ISocialTenantBindService;
import com.apitable.enterprise.social.service.ISocialTenantService;
import com.apitable.player.ro.NotificationCreateRo;
import com.apitable.shared.component.notification.NotificationHelper;
import com.apitable.shared.component.notification.NotificationManager;
import com.apitable.shared.component.notification.NotificationTemplateId;
import java.time.LocalDate;
import java.util.Collections;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.cp.bean.WxCpTpAuthInfo.Agent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SocialNotificationManagement {

    @Autowired(required = false)
    private WecomNotifyObserver wecomNotifyObserver;

    @Autowired(required = false)
    private WecomIsvNotifyObserver wecomIsvNotifyObserver;

    @Autowired(required = false)
    private DingTalkNotifyObserver dingTalkNotifyObserver;

    @Autowired(required = false)
    private DingTalkIsvNotifyObserver dingTalkIsvNotifyObserver;

    @Autowired(required = false)
    private LarkNotifyObserver larkNotifyObserver;

    @Autowired(required = false)
    private LarkIsvNotifyObserver larkIsvNotifyObserver;

    @Resource
    private ISocialTenantBindService iSocialTenantBindService;

    @Resource
    private ISocialTenantService iSocialTenantService;

    @Resource
    private IDingTalkInternalService iDingTalkInternalService;

    @Resource
    private IDingTalkService iDingTalkService;

    public static String DINGTALK_ISV_ENTRY_URL =
        "{}/user/dingtalk/social_bind_space?corpId={}&suiteId={}";

    public static String DINGTALK_ENTRY_URL = "{}/user/dingtalk_callback?corpId={}&agentId={}";

    @Resource
    private IAppInstanceService iAppInstanceService;

    public static SocialNotificationManagement me() {
        return SpringContextHolder.getBean(SocialNotificationManagement.class);
    }

    public void socialNotify(NotificationCreateRo ro) {
        SocialNotifyContext context = buildSocialNotifyContext(ro.getSpaceId());
        if (context == null) {
            return;
        }
        SocialNotifySubject imSub = new SocialNotifySubject();
        if (wecomNotifyObserver != null || wecomIsvNotifyObserver != null) {
            imSub.addObserver(wecomNotifyObserver);
            imSub.addObserver(wecomIsvNotifyObserver);
        }
        if (dingTalkNotifyObserver != null || dingTalkIsvNotifyObserver != null) {
            imSub.addObserver(dingTalkNotifyObserver);
            imSub.addObserver(dingTalkIsvNotifyObserver);
        }
        if (larkIsvNotifyObserver != null || larkNotifyObserver != null) {
            imSub.addObserver(larkIsvNotifyObserver);
            imSub.addObserver(larkNotifyObserver);
        }
        imSub.setContext(context);
        imSub.send(ro);
    }

    private SocialNotifyContext buildSocialNotifyContext(String spaceId) {
        if (StrUtil.isBlank(spaceId)) {
            log.warn("Lost space id");
            return null;
        }
        TenantBindDTO bindInfo = iSocialTenantBindService.getTenantBindInfoBySpaceId(spaceId);
        if (bindInfo == null || StrUtil.isBlank(bindInfo.getAppId())) {
            log.warn("space no bind social:{}", spaceId);
            return null;
        }
        SocialTenantEntity tenantEntity =
            iSocialTenantService.getByAppIdAndTenantId(bindInfo.getAppId(), bindInfo.getTenantId());
        if (tenantEntity == null || !tenantEntity.getStatus()) {
            return null;
        }
        SocialAppType appType = SocialAppType.of(tenantEntity.getAppType());
        if (appType == null) {
            return null;
        }
        SocialPlatformType platform = SocialPlatformType.toEnum(tenantEntity.getPlatform());
        SocialNotifyContext context = new SocialNotifyContext();
        context.setAppId(tenantEntity.getAppId());
        context.setAppType(appType);
        context.setPlatform(platform);
        context.setTenantId(tenantEntity.getTenantId());
        String entryUrl = getSocialAppEntryUrl(bindInfo, platform, appType);
        if (StrUtil.isBlank(entryUrl)) {
            return null;
        }
        context.setEntryUrl(entryUrl);
        String agentId = getSocialAppAgentId(tenantEntity);
        if (StrUtil.isBlank(agentId)) {
            return null;
        }
        context.setAgentId(agentId);
        return context;
    }

    /**
     * get entry address
     *
     * @param bindInfo tenant bind info
     * @param platform social platform
     * @param appType  app type
     * @return entry address
     */
    private String getSocialAppEntryUrl(TenantBindDTO bindInfo, SocialPlatformType platform,
                                        SocialAppType appType) {
        if (platform.equals(SocialPlatformType.WECOM)) {
            if (appType.equals(SocialAppType.INTERNAL)) {
                return WeComCardFactory.WECOM_CALLBACK_PATH;
            }
            if (appType.equals(SocialAppType.ISV)) {
                return WeComIsvCardFactory.WECOM_ISV_LOGIN_PATH;
            }
        }
        if (platform.equals(SocialPlatformType.DINGTALK)) {
            if (appType.equals(SocialAppType.INTERNAL)) {
                return DINGTALK_ENTRY_URL;
            }
            if (appType.equals(SocialAppType.ISV)) {
                return DINGTALK_ISV_ENTRY_URL;
            }
        }
        if (platform.equals(SocialPlatformType.FEISHU)) {
            if (appType.equals(SocialAppType.ISV)) {
                return LarkConstants.ISV_ENTRY_URL;
            }
            if (appType.equals(SocialAppType.INTERNAL)) {
                AppInstanceEntity instance =
                    iAppInstanceService.getInstanceBySpaceIdAndAppType(bindInfo.getSpaceId(),
                        AppType.LARK);
                if (instance == null) {
                    return null;
                }
                LarkInstanceConfig instanceConfig =
                    LarkInstanceConfig.fromJsonString(instance.getConfig());
                LarkInstanceConfigProfile profile =
                    (LarkInstanceConfigProfile) instanceConfig.getProfile();
                if (StrUtil.isBlank(profile.getAppKey())) {
                    return null;
                }
                if (!profile.getAppKey().equals(bindInfo.getAppId())) {
                    return null;
                }
                return LarkConstants.formatInternalEntryUrl(instance.getAppInstanceId());
            }
        }
        return null;
    }

    private String getSocialAppAgentId(SocialTenantEntity entity) {
        SocialPlatformType platform = SocialPlatformType.toEnum(entity.getPlatform());
        SocialAppType appType = SocialAppType.of(entity.getAppType());
        if (platform.equals(SocialPlatformType.WECOM)) {
            if (SocialAppType.ISV.equals(appType)) {
                Agent agent = JSONUtil.toBean(entity.getContactAuthScope(), Agent.class);
                return agent.getAgentId().toString();
            }
            return entity.getAppId();
        }
        if (platform.equals(SocialPlatformType.DINGTALK)) {
            if (SocialAppType.ISV.equals(appType)) {
                IsvAppProperty bizApp = iDingTalkInternalService.getIsvAppConfig(entity.getAppId());
                return bizApp.getAppId();
            }
            if (SocialAppType.INTERNAL.equals(appType)) {
                return iDingTalkService.getAgentIdByAppIdAndTenantId(entity.getAppId(),
                    entity.getTenantId());
            }
        }
        if (platform.equals(SocialPlatformType.FEISHU)) {
            return entity.getAppId();
        }
        return null;
    }

    /**
     * send billing notification
     *
     * @param spaceId    space id
     * @param fromUserId from
     * @param expireAt   billing expired at
     * @param planTitle  billing plan name
     * @param amount     billing paid price
     */
    public void sendSubscribeNotify(String spaceId, Long fromUserId, Long expireAt,
                                    String planTitle, Integer amount, String orderType) {
        Dict paidExtra = Dict.create().set(PLAN_NAME, planTitle)
            .set(EXPIRE_AT, expireAt.toString())
            .set(PAY_FEE, String.format("¥%.2f", amount.doubleValue() / 100))
            .set("orderType", orderType);
        NotificationManager.me().playerNotify(SPACE_VIKA_PAID_NOTIFY,
            Collections.singletonList(fromUserId), 0L, spaceId, paidExtra);
    }

    /**
     * send billing notification in social platform
     *
     * @param planTitle billing plan name
     * @param amount    billing paid price
     */
    public void sendSocialSubscribeNotify(String spaceId, Long toUserId, LocalDate expireAt,
                                          String planTitle,
                                          Long amount) {
        if (toUserId != null && amount > 0) {
            Dict paidExtra = Dict.create().set(PLAN_NAME, planTitle)
                .set(EXPIRE_AT, String.valueOf(LocalDateTimeUtil.toEpochMilli(expireAt)))
                .set(PAY_FEE, String.format("¥%.2f", amount.doubleValue() / 100));
            NotificationManager.me().playerNotify(SPACE_PAID_NOTIFY,
                Collections.singletonList(toUserId), 0L, spaceId, paidExtra);
        }
        Dict subscriptionExtra = Dict.create().set(PLAN_NAME, planTitle)
            .set(EXPIRE_AT, String.valueOf(LocalDateTimeUtil.toEpochMilli(expireAt)));
        NotificationManager.me().playerNotify(NotificationTemplateId.SPACE_SUBSCRIPTION_NOTIFY,
            null, 0L, spaceId, subscriptionExtra);
    }
}
