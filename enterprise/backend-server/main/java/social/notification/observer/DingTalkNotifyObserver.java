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

import java.util.Map;
import java.util.Map.Entry;

import jakarta.annotation.Resource;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;

import com.apitable.enterprise.social.enums.SocialAppType;
import com.apitable.enterprise.social.enums.SocialPlatformType;
import com.apitable.enterprise.social.notification.subject.SocialNotifyContext;
import com.apitable.enterprise.social.service.IDingTalkService;
import com.apitable.player.ro.NotificationCreateRo;
import com.apitable.shared.config.properties.ConstProperties;
import com.apitable.shared.sysconfig.i18n.I18nStringsUtil;
import com.apitable.shared.sysconfig.notification.NotificationConfigLoader;
import com.apitable.shared.sysconfig.notification.SocialTemplate;
import com.vikadata.social.dingtalk.message.ActionCardMessage;
import com.vikadata.social.dingtalk.message.Message;
import com.vikadata.social.dingtalk.message.element.SingleActionCard;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import static com.apitable.enterprise.social.event.dingtalk.DingTalkCardFactory.DINGTALK_OA_OPEN;

@Component
@ConditionalOnProperty(value = "social.dingtalk.enabled", havingValue = "true")
public class DingTalkNotifyObserver
    extends SocialNotifyObserver<SocialTemplate, SocialNotifyContext> {
    public static String DINGTALK_PLATFORM = "dingtalk";

    @Resource
    private ConstProperties constProperties;

    @Resource
    private IDingTalkService iDingTalkService;

    @Override
    public boolean isNotify(SocialNotifyContext context) {
        return SocialAppType.INTERNAL.equals(context.getAppType()) &&
            SocialPlatformType.DINGTALK.equals(context.getPlatform());
    }

    @Override
    public SocialTemplate getTemplate(String templateId) {
        Map<String, SocialTemplate> socialTemplates =
            NotificationConfigLoader.getConfig().getSocialTemplates();
        for (Entry<String, SocialTemplate> template : socialTemplates.entrySet()) {
            if (templateId.equals(template.getValue().getNotificationTemplateId()) &&
                DINGTALK_PLATFORM.equals(template.getValue().getPlatform())) {
                return template.getValue();
            }
        }
        return null;
    }

    @Override
    public Message renderTemplate(SocialNotifyContext context, NotificationCreateRo ro) {
        SocialTemplate template = getTemplate(ro.getTemplateId());
        if (template == null) {
            return null;
        }
        Map<String, Object> renderMap = bindingMap(ro);
        String description =
            StrUtil.format(I18nStringsUtil.t(template.getTemplateString()), renderMap);
        String title = I18nStringsUtil.t(template.getTitle());
        // entry url：https://{domain}/user/dingtalk_callback?corpId={}&agentId={}
        String callbackUrl =
            StrUtil.format(context.getEntryUrl(), constProperties.getServerDomain(),
                    context.getTenantId(), context.getAgentId())
                .concat("&reference=").concat(constProperties.getServerDomain())
                .concat(CharSequenceUtil.prependIfMissingIgnoreCase(
                    StrUtil.format(StrUtil.blankToDefault(template.getUrl(), ""), renderMap), "/"));
        // build url
        String url = StrUtil.format(DINGTALK_OA_OPEN, context.getTenantId(), context.getAgentId(),
            URLUtil.encodeAll(callbackUrl));
        if (ActionCardMessage.ACTION_CARD_MSG_TYPE.equals(template.getMessageType())) {
            // Single link card message
            if (StrUtil.isBlank(template.getPicUrl())) {
                SingleActionCard singleActionCard = new SingleActionCard();
                singleActionCard.setTitle(title);
                singleActionCard.setSingleUrl(url);
                singleActionCard.setSingleTitle(I18nStringsUtil.t(template.getUrlTitle()));
                singleActionCard.setMarkdown(description);
                return new ActionCardMessage(singleActionCard);
            }
            // todo Multilink Type
            return null;
        }
        // todo Other types
        return null;

    }

    @Override
    public void notify(SocialNotifyContext context, NotificationCreateRo ro) {
        Message message = renderTemplate(context, ro);
        if (message == null) {
            return;
        }
        iDingTalkService.asyncSendCardMessageToUserPrivate(context.getAgentId(),
            renderTemplate(context, ro),
            toUser(ro));
    }
}
