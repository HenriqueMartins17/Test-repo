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

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.apitable.enterprise.social.autoconfigure.dingtalk.DingTalkProperties.IsvAppProperty;
import com.apitable.enterprise.social.enums.SocialAppType;
import com.apitable.enterprise.social.enums.SocialPlatformType;
import com.apitable.enterprise.social.notification.subject.SocialNotifyContext;
import com.apitable.enterprise.social.service.IDingTalkInternalService;
import com.apitable.player.ro.NotificationCreateRo;
import com.apitable.shared.config.properties.ConstProperties;
import com.apitable.shared.sysconfig.i18n.I18nStringsUtil;
import com.apitable.shared.sysconfig.notification.NotificationConfigLoader;
import com.apitable.shared.sysconfig.notification.SocialTemplate;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(value = "social.dingtalk.enabled", havingValue = "true")
public class DingTalkIsvNotifyObserver
    extends SocialNotifyObserver<SocialTemplate, SocialNotifyContext> {

    @Resource
    private ConstProperties constProperties;

    @Resource
    private IDingTalkInternalService iDingTalkInternalService;

    @Override
    public boolean isNotify(SocialNotifyContext context) {
        return SocialAppType.ISV.equals(context.getAppType()) &&
            SocialPlatformType.DINGTALK.equals(context.getPlatform());
    }

    @Override
    public SocialTemplate getTemplate(String templateId) {
        Map<String, SocialTemplate> socialTemplates =
            NotificationConfigLoader.getConfig().getSocialTemplates();
        for (Entry<String, SocialTemplate> templateEntry : socialTemplates.entrySet()) {
            SocialTemplate template = templateEntry.getValue();
            if (StrUtil.isNotBlank(template.getAppId())) {
                IsvAppProperty isvApp =
                    iDingTalkInternalService.getIsvAppConfig(template.getAppId());
                if (isvApp != null && template.getAppId().equals(isvApp.getSuiteId()) &&
                    templateId.equals(template.getNotificationTemplateId())) {
                    return template;
                }
            }
        }
        return null;
    }

    @Override
    public HashMap<String, String> renderTemplate(SocialNotifyContext context,
                                                  NotificationCreateRo ro) {
        SocialTemplate template = getTemplate(ro.getTemplateId());
        if (template == null) {
            return null;
        }
        Map<String, Object> renderMap = bindingMap(ro);
        HashMap<String, String> dataMap = new HashMap<>();
        renderMap.forEach((k, v) -> dataMap.put(k, v.toString()));
        dataMap.put("corpId", context.getTenantId());
        dataMap.put("suiteId", context.getAppId());
        dataMap.put("appId", context.getAgentId());
        // Remove the beginning of https and be compatible with the previous card
        // todo It can be removed after migration
        dataMap.put("domain", URLUtil.url(constProperties.getServerDomain()).getHost());
        // Build Links
        String reference = constProperties.getServerDomain().concat(
            CharSequenceUtil.prependIfMissingIgnoreCase(
                StrUtil.format(StrUtil.blankToDefault(template.getUrl(), ""), renderMap), "/"));
        String redirectUrl =
            StrUtil.format(context.getEntryUrl(), constProperties.getServerDomain(),
                    context.getTenantId(), context.getAppId()).concat("&reference" + "=")
                .concat(reference);
        dataMap.put("reference", URLUtil.encodeAll(reference));
        dataMap.put("redirectUrl", URLUtil.encodeAll(redirectUrl));
        return dataMap;
    }

    @Override
    public void notify(SocialNotifyContext context, NotificationCreateRo ro) {
        SocialTemplate template = getTemplate(ro.getTemplateId());
        if (template == null) {
            log.error("DingTalk isv template error:{}", ro.getTemplateId());
            return;
        }
        HashMap<String, String> renderMap = renderTemplate(context, ro);
        iDingTalkInternalService.sendMessageToUserByTemplateId(context.getAppId(),
            context.getTenantId(),
            I18nStringsUtil.t(template.getTemplateString()), renderMap, toUser(ro));

    }
}
