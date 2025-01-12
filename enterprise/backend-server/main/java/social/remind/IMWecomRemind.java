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

import java.util.Objects;

import jakarta.annotation.Resource;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.cp.bean.WxCpTpAuthInfo.Agent;
import me.chanjar.weixin.cp.bean.message.WxCpMessage;

import com.apitable.enterprise.social.entity.SocialTenantEntity;
import com.apitable.enterprise.social.event.wecom.WeComCardFactory;
import com.apitable.enterprise.social.event.wecom.WeComIsvCardFactory;
import com.apitable.enterprise.social.service.ISocialCpIsvService;
import com.apitable.enterprise.social.service.ISocialTenantService;
import com.apitable.enterprise.social.service.IWeComService;
import com.apitable.enterprise.social.enums.SocialAppType;
import com.apitable.workspace.observer.remind.NotifyDataSheetMeta;
import com.apitable.workspace.observer.remind.RemindChannel;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Im-wecom reminder，automatically register subscription topics according to the open status.
 * </p>
 */
@Slf4j
@Component
@ConditionalOnProperty(value = "social.wecom.enabled", havingValue = "true")
public class IMWecomRemind extends AbstractIMRemind {

    @Resource
    private ISocialCpIsvService socialCpIsvService;
    @Resource
    private ISocialTenantService socialTenantService;
    @Resource
    private IWeComService iWeComService;

    @Override
    public RemindChannel getRemindType() {
        return WecomRemindChannel.WECOM;
    }

    @Override
    public void notifyMemberAction(NotifyDataSheetMeta meta) {
        log.info("[remind notification]-user subscribe third party im wecom remind=>@member");
        String notifyUrl = meta.getRemindParameter().getNotifyUrl();
        String fromMemberName = meta.getRemindParameter().getFromMemberName();
        Boolean fromMemberNameModified = meta.getRemindParameter().getFromMemberNameModified();
        String nodeName = meta.getRemindParameter().getNodeName();

        if (Objects.nonNull(appType) && appType == SocialAppType.ISV) {
            SocialTenantEntity tenantEntity =
                socialTenantService.getByAppIdAndTenantId(socialAppId, socialTenantId);
            Agent agent = JSONUtil.toBean(tenantEntity.getContactAuthScope(), Agent.class);
            // If the member name is not modified, you need to open openId -> name translation
            WxCpMessage recordRemindMemberMsg =
                WeComIsvCardFactory.createRecordRemindMemberCardMsg(agent.getAgentId(),
                    meta.getRecordTitle(), fromMemberName, fromMemberNameModified, nodeName,
                    notifyUrl);
            try {
                socialCpIsvService.sendMessageToUser(tenantEntity, meta.getSpaceId(),
                    recordRemindMemberMsg, toOpenIds);
            } catch (WxErrorException ex) {
                log.error("wecom third-party service provider failed to send messages.", ex);
            }
        } else {
            WxCpMessage recordRemindMemberMsg =
                WeComCardFactory.createRecordRemindMemberCardMsg(Integer.valueOf(socialAppId),
                    meta.getRecordTitle(), fromMemberName, nodeName, notifyUrl);
            iWeComService.sendMessageToUserPrivate(socialTenantId, Integer.valueOf(socialAppId),
                meta.getSpaceId(), toOpenIds, recordRemindMemberMsg);
        }
    }

    @Override
    public void notifyCommentAction(NotifyDataSheetMeta meta) {
        log.info("[remind notification]-user subscribe third party im wecom remind=>comments");
        String notifyUrl = meta.getRemindParameter().getNotifyUrl();
        String fromMemberName = meta.getRemindParameter().getFromMemberName();
        Boolean fromMemberNameModified = meta.getRemindParameter().getFromMemberNameModified();
        String nodeName = meta.getRemindParameter().getNodeName();
        String commentContentHtml = super.unescapeHtml(meta.getExtra().getContent());

        if (Objects.nonNull(appType) && appType == SocialAppType.ISV) {
            SocialTenantEntity tenantEntity =
                socialTenantService.getByAppIdAndTenantId(socialAppId, socialTenantId);
            Agent agent = JSONUtil.toBean(tenantEntity.getContactAuthScope(), Agent.class);
            // If the member name is not modified, you need to open openId -> name translation
            WxCpMessage recordRemindMemberMsg =
                WeComIsvCardFactory.createCommentRemindCardMsg(agent.getAgentId(),
                    meta.getRecordTitle(), commentContentHtml, fromMemberName,
                    fromMemberNameModified, nodeName, notifyUrl);
            try {
                socialCpIsvService.sendMessageToUser(tenantEntity, meta.getSpaceId(),
                    recordRemindMemberMsg, toOpenIds);
            } catch (WxErrorException ex) {
                log.error("wecom third-party service provider failed to send messages.", ex);
            }
        } else {
            WxCpMessage commentRemindMsg =
                WeComCardFactory.createCommentRemindCardMsg(Integer.valueOf(socialAppId),
                    meta.getRecordTitle(), commentContentHtml, fromMemberName, nodeName, notifyUrl);
            iWeComService.sendMessageToUserPrivate(socialTenantId, Integer.valueOf(socialAppId),
                meta.getSpaceId(), toOpenIds, commentRemindMsg);
        }
    }

}
