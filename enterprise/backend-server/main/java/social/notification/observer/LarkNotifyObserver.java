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

package com.apitable.enterprise.social.notification.observer;

import jakarta.annotation.Resource;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import com.apitable.enterprise.appstore.entity.AppInstanceEntity;
import com.apitable.enterprise.appstore.enums.AppType;
import com.apitable.enterprise.appstore.model.LarkInstanceConfig;
import com.apitable.enterprise.appstore.model.LarkInstanceConfigProfile;
import com.apitable.enterprise.appstore.service.IAppInstanceService;
import com.apitable.enterprise.social.enums.SocialAppType;
import com.apitable.enterprise.social.enums.SocialPlatformType;
import com.apitable.enterprise.social.notification.subject.SocialNotifyContext;
import com.apitable.enterprise.social.service.IFeishuService;
import com.apitable.player.ro.NotificationCreateRo;
import com.vikadata.social.feishu.card.Message;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@ConditionalOnProperty(value = "social.feishu.enabled", havingValue = "true")
public class LarkNotifyObserver extends AbstractLarkNotifyObserver {

    @Resource
    private IFeishuService iFeishuService;

    @Resource
    private IAppInstanceService iAppInstanceService;


    @Override
    public boolean isNotify(SocialNotifyContext context) {
        return SocialAppType.INTERNAL.equals(context.getAppType()) &&
            SocialPlatformType.FEISHU.equals(context.getPlatform());
    }

    @Override
    public void notify(SocialNotifyContext context, NotificationCreateRo ro) {
        Message message = renderTemplate(context, ro);
        if (message == null) {
            return;
        }
        AppInstanceEntity instance =
            iAppInstanceService.getInstanceBySpaceIdAndAppType(ro.getSpaceId(), AppType.LARK);
        if (instance == null) {
            return;
        }
        LarkInstanceConfig instanceConfig = LarkInstanceConfig.fromJsonString(instance.getConfig());
        LarkInstanceConfigProfile profile = (LarkInstanceConfigProfile) instanceConfig.getProfile();
        if (StrUtil.isBlank(profile.getAppKey())) {
            return;
        }
        if (!profile.getAppKey().equals(context.getAppId())) {
            return;
        }
        // Switch Context
        iFeishuService.switchContextIfAbsent(profile.buildConfigStorage());
        try {
            iFeishuService.batchSendCardMessage(context.getTenantId(), toUser(ro),
                message);
        } catch (Exception e) {
            log.error("fail to send car message", e);
        }
    }
}
