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

import java.util.List;

import jakarta.annotation.Resource;

import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.cp.bean.message.WxCpMessage;

import com.apitable.enterprise.social.notification.subject.SocialNotifyContext;
import com.apitable.enterprise.social.enums.SocialPlatformType;
import com.apitable.player.ro.NotificationCreateRo;
import com.apitable.enterprise.social.enums.SocialAppType;
import com.apitable.enterprise.social.service.ISocialCpIsvService;
import com.apitable.enterprise.social.service.ISocialTenantService;
import com.apitable.enterprise.social.entity.SocialTenantEntity;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@ConditionalOnProperty(value = "social.wecom.enabled", havingValue = "true")
public class WecomIsvNotifyObserver extends AbstractWecomNotifyObserver {
    @Resource
    private ISocialCpIsvService iSocialCpIsvService;

    @Resource
    private ISocialTenantService iSocialTenantService;

    @Override
    public boolean isNotify(SocialNotifyContext context) {
        return SocialAppType.ISV.equals(context.getAppType()) &&
            SocialPlatformType.WECOM.equals(context.getPlatform());
    }

    @Override
    public void notify(SocialNotifyContext context, NotificationCreateRo ro) {
        WxCpMessage wxCpMessage = renderTemplate(context, ro);
        if (wxCpMessage == null) {
            return;
        }
        List<String> toUsers = toUser(ro);
        if (toUsers.isEmpty()) {
            return;
        }
        SocialTenantEntity tenantEntity =
            iSocialTenantService.getByAppIdAndTenantId(context.getAppId(), context.getTenantId());
        try {
            iSocialCpIsvService.sendMessageToUser(tenantEntity, ro.getSpaceId(), wxCpMessage,
                toUsers);
        } catch (WxErrorException ex) {
            log.error("fail to send wecom isv card message", ex);
        }
    }
}
