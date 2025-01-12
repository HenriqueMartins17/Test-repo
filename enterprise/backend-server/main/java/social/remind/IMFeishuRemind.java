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

package com.apitable.enterprise.social.remind;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import jakarta.annotation.Resource;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import com.apitable.enterprise.appstore.entity.AppInstanceEntity;
import com.apitable.enterprise.appstore.enums.AppType;
import com.apitable.enterprise.appstore.model.LarkInstanceConfig;
import com.apitable.enterprise.appstore.model.LarkInstanceConfigProfile;
import com.apitable.enterprise.appstore.service.IAppInstanceService;
import com.apitable.enterprise.social.constants.LarkConstants;
import com.apitable.enterprise.social.entity.SocialTenantEntity;
import com.apitable.enterprise.social.enums.SocialAppType;
import com.apitable.enterprise.social.service.IFeishuService;
import com.apitable.enterprise.social.service.ISocialTenantBindService;
import com.apitable.shared.config.properties.ConstProperties;
import com.apitable.workspace.observer.remind.NotifyDataSheetMeta;
import com.apitable.workspace.observer.remind.RemindChannel;
import com.apitable.core.util.SpringContextHolder;
import com.vikadata.social.core.URIUtil;
import com.vikadata.social.feishu.card.Card;
import com.vikadata.social.feishu.card.CardMessage;
import com.vikadata.social.feishu.card.Config;
import com.vikadata.social.feishu.card.Header;
import com.vikadata.social.feishu.card.Message;
import com.vikadata.social.feishu.card.element.Button;
import com.vikadata.social.feishu.card.module.Action;
import com.vikadata.social.feishu.card.module.Div;
import com.vikadata.social.feishu.card.module.Module;
import com.vikadata.social.feishu.card.objects.Text;
import com.vikadata.social.feishu.model.BatchSendChatMessageResult;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Im-Feishu reminder, automatically register subscription topics according to the open status
 * </p>
 */
@Slf4j
@Component
@ConditionalOnProperty(value = "social.feishu.enabled", havingValue = "true")
public class IMFeishuRemind extends AbstractIMRemind {

    @Resource
    private IFeishuService iFeishuService;

    @Resource
    private ISocialTenantBindService iSocialTenantBindService;

    @Resource
    private IAppInstanceService iAppInstanceService;

    @Override
    public RemindChannel getRemindType() {
        return FeishuRemindChannel.FEISHU;
    }

    @Override
    public void notifyMemberAction(NotifyDataSheetMeta meta) {
        log.info("[remind notification]-user subscribe third party im feishu remind=>@member");
        sendImCardMessage(false, meta);
    }

    @Override
    public void notifyCommentAction(NotifyDataSheetMeta meta) {
        log.info("[remind notification]-user subscribe third party im feishu remind=>comments");
        sendImCardMessage(true, meta);
    }

    private void sendImCardMessage(boolean isCommentAction, NotifyDataSheetMeta meta) {
        String spaceId = meta.getSpaceId();
        List<SocialTenantEntity> feishuTenants =
            iSocialTenantBindService.getFeishuTenantsBySpaceId(spaceId);
        if (CollUtil.isEmpty(feishuTenants)) {
            log.warn("space is not bound to any feishu");
            return;
        }
        // Query the specified tenant according to app id and tenant id
        SocialTenantEntity feishuTenant = feishuTenants.stream()
            .filter(tenant -> tenant.getAppId().equals(socialAppId)
                && tenant.getTenantId().equals(socialTenantId))
            .findFirst().orElse(null);
        if (feishuTenant == null) {
            log.warn("space is not bound to the tenant of feishu: {}", spaceId);
            return;
        }
        String entryUrl = null;
        SocialAppType appType = SocialAppType.of(feishuTenant.getAppType());
        if (appType == SocialAppType.ISV) {
            entryUrl = LarkConstants.ISV_ENTRY_URL;
            iFeishuService.switchDefaultContext();
        } else if (appType == SocialAppType.INTERNAL) {
            // feishu self-built application, query the instance id of the application.
            AppInstanceEntity instance =
                iAppInstanceService.getInstanceBySpaceIdAndAppType(spaceId, AppType.LARK);
            if (instance == null) {
                log.warn("space feishu app no exist.");
                return;
            }
            LarkInstanceConfig instanceConfig =
                LarkInstanceConfig.fromJsonString(instance.getConfig());
            LarkInstanceConfigProfile profile =
                (LarkInstanceConfigProfile) instanceConfig.getProfile();
            if (StrUtil.isBlank(profile.getAppKey())) {
                log.warn("config is null，don't send");
                return;
            }
            if (!profile.getAppKey().equals(feishuTenant.getAppId())) {
                log.warn("config  mismatch app key，don't send");
                return;
            }
            entryUrl = LarkConstants.formatInternalEntryUrl(instance.getAppInstanceId());
            // toggle context
            iFeishuService.switchContextIfAbsent(profile.buildConfigStorage());
        }

        String notifyUrl = meta.getRemindParameter().getNotifyUrl();
        String nodeName = meta.getRemindParameter().getNodeName();
        Message cardMessage;
        if (isCommentAction) {
            // comment notification
            String commentContentHtml = super.unescapeHtml(meta.getExtra().getContent());
            cardMessage =
                createRemindFromCommentCardMsg(socialAppId, entryUrl, meta.getRecordTitle(),
                    commentContentHtml, fromOpenId, nodeName, notifyUrl);
        } else {
            // remind notification
            cardMessage =
                createRemindMemberCardMsg(socialAppId, entryUrl, meta.getRecordTitle(), fromOpenId,
                    nodeName, notifyUrl);
        }
        try {
            BatchSendChatMessageResult result =
                iFeishuService.batchSendCardMessage(feishuTenant.getTenantId(), toOpenIds,
                    cardMessage);
            log.info("[remind notification]-feishu message id: {}", result.getMessageId());
            log.warn("[remind notification]-users whose feishu cannot be delivered: {}",
                result.getInvalidOpenIds());
        } catch (Exception e) {
            log.error("[remind notification]-failed to send message card", e);
        }
    }

    private Message createRemindMemberCardMsg(String appId, String entryUrl, String recordTitle,
                                              String openId, String nodeName, String url) {
        Header header = new Header(new Text(Text.Mode.LARK_MD, "**有人在记录中提及你**"));
        String contentMdTemplate = "**记录：**%s\n**提及人：**<at id=%s></at>\n**维格表：**%s";
        String contentMd = String.format(contentMdTemplate, recordTitle, openId, nodeName);
        return createMessage(appId, entryUrl, header, url, contentMd);
    }

    private Message createRemindFromCommentCardMsg(String appId, String entryUrl,
                                                   String recordTitle, String commentContent,
                                                   String openId, String nodeName,
                                                   String recordUrl) {
        Header header = new Header(new Text(Text.Mode.LARK_MD, "**有人在评论中@你**"));
        String contentMdTemplate =
            "**记录：**%s\n**内容：**%s\n**评论人：**<at id=%s></at>\n**维格表：**%s";
        String contentMd =
            String.format(contentMdTemplate, recordTitle, commentContent, openId, nodeName);
        return createMessage(appId, entryUrl, header, recordUrl, contentMd);
    }

    private Message createMessage(String appId, String entryUrl, Header header, String recordUrl,
                                  String contentMd) {
        String FEISHU_WEB_OPEN = "https://applink.feishu.cn/client/web_app/open?appId=%s&path=%s";
        ConstProperties constProperties = SpringContextHolder.getBean(ConstProperties.class);
        Div div = new Div(new Text(Text.Mode.LARK_MD, contentMd));
        if (!entryUrl.contains("lark")) {
            String callbackUrl = entryUrl + "&url=%s";
            String path = String.format(callbackUrl,
                URIUtil.encodeURIComponent(constProperties.getServerDomain() + recordUrl));
            Button entryBtn = new Button(new Text(Text.Mode.LARK_MD, "进入查看"))
                .setUrl(String.format(FEISHU_WEB_OPEN, appId, path))
                .setType(Button.StyleType.PRIMARY);
            Action action = new Action(Collections.singletonList(entryBtn));
            return create(header, new Module[] {div, action});
        } else {
            return create(header, new Module[] {div});
        }
    }

    private Message create(Header header, Module[] modules) {
        // create a card
        Card card = new Card(new Config(false), header);
        // set content elements
        card.setModules(Arrays.asList(modules));
        return new CardMessage(card.toObj());
    }
}
