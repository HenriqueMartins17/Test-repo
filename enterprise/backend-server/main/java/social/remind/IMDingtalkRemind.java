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

import java.util.HashMap;

import jakarta.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import com.apitable.enterprise.social.autoconfigure.dingtalk.DingTalkProperties.IsvAppProperty;
import com.apitable.enterprise.social.event.dingtalk.DingTalkCardFactory;
import com.apitable.enterprise.social.service.IDingTalkInternalService;
import com.apitable.enterprise.social.service.IDingTalkService;
import com.apitable.workspace.observer.remind.NotifyDataSheetMeta;
import com.apitable.workspace.observer.remind.RemindChannel;
import com.vikadata.social.dingtalk.message.Message;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * <p>
 * im- dingtalk，automatically register subscription topics according to the open status
 * </p>
 */
@Slf4j
@Component
@ConditionalOnProperty(value = "social.dingtalk.enabled", havingValue = "true")
public class IMDingtalkRemind extends AbstractIMRemind {

    @Resource
    private IDingTalkService iDingTalkService;

    @Resource
    private IDingTalkInternalService iDingTalkInternalService;

    @Override
    public RemindChannel getRemindType() {
        return DingtalkRemindChannel.DINGTALK;
    }

    @Override
    public void notifyMemberAction(NotifyDataSheetMeta meta) {
        log.info("[remind notification]-user subscribe third party im dingtalk remind=>@member");
        String notifyUrl = meta.getRemindParameter().getNotifyUrl();
        String fromMemberName = meta.getRemindParameter().getFromMemberName();
        String nodeName = meta.getRemindParameter().getNodeName();
        String agentId = iDingTalkService.getAgentIdByAppIdAndTenantId(socialAppId, socialTenantId);

        if (null != agentId) {
            Message recordRemindMemberCardMsg =
                DingTalkCardFactory.createRecordRemindMemberCardMsg(agentId, meta.getRecordTitle(),
                    fromMemberName, nodeName, notifyUrl);
            iDingTalkService.asyncSendCardMessageToUserPrivate(agentId, recordRemindMemberCardMsg,
                toOpenIds);
        } else {
            // isv
            IsvAppProperty bizApp = iDingTalkInternalService.getIsvAppConfig(socialAppId);
            HashMap<String, String> isvRecordRemindMemberData =
                DingTalkCardFactory.createIsvRecordRemindMemberData(socialTenantId,
                    bizApp.getAppId(), meta, fromMemberName, nodeName, notifyUrl);

            iDingTalkInternalService.sendMessageToUserByTemplateId(socialAppId, socialTenantId,
                bizApp.getMsgTplId().getMember(), isvRecordRemindMemberData, toOpenIds);
        }
    }

    @Override
    public void notifyCommentAction(NotifyDataSheetMeta meta) {
        log.info("[remind notification]-user subscribe third party im dingtalk remind=>comments");
        String notifyUrl = meta.getRemindParameter().getNotifyUrl();
        String fromMemberName = meta.getRemindParameter().getFromMemberName();
        String nodeName = meta.getRemindParameter().getNodeName();
        String agentId = iDingTalkService.getAgentIdByAppIdAndTenantId(socialAppId, socialTenantId);
        String commentContentHtml = super.unescapeHtml(meta.getExtra().getContent());

        if (null != agentId) {
            Message commentRemindCardMsg =
                DingTalkCardFactory.createCommentRemindCardMsg(agentId, meta.getRecordTitle(),
                    commentContentHtml, fromMemberName, nodeName, notifyUrl);
            iDingTalkService.asyncSendCardMessageToUserPrivate(agentId, commentRemindCardMsg,
                toOpenIds);
        } else {
            // isv
            IsvAppProperty bizApp = iDingTalkInternalService.getIsvAppConfig(socialAppId);

            HashMap<String, String> isvCommentRemindData =
                DingTalkCardFactory.createIsvCommentRemindData(socialTenantId, bizApp.getAppId(),
                    meta, fromMemberName, nodeName, commentContentHtml, notifyUrl);
            iDingTalkInternalService.sendMessageToUserByTemplateId(socialAppId, socialTenantId,
                bizApp.getMsgTplId().getComment(), isvCommentRemindData, toOpenIds);
        }
    }

}
