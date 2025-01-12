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

package com.apitable.enterprise.social.handler;

import java.util.Map;

import jakarta.annotation.Resource;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.cp.bean.WxCpTpPermanentCodeInfo.AuthCorpInfo;
import me.chanjar.weixin.cp.bean.message.WxCpTpXmlMessage;
import me.chanjar.weixin.cp.bean.message.WxCpXmlOutMessage;
import me.chanjar.weixin.cp.bean.message.WxCpXmlOutTextMessage;
import me.chanjar.weixin.cp.tp.service.WxCpTpService;

import com.apitable.enterprise.social.enums.SocialCpIsvMessageProcessStatus;
import com.apitable.enterprise.social.service.ISocialCpIsvMessageService;
import com.apitable.enterprise.social.entity.SocialCpIsvEventLogEntity;
import com.vikadata.social.wecom.WeComTemplate;
import com.vikadata.social.wecom.WxCpIsvServiceImpl;
import com.vikadata.social.wecom.constants.WeComIsvMessageType;
import com.vikadata.social.wecom.handler.WeComIsvMessageHandler;
import com.vikadata.social.wecom.model.WxCpIsvPermanentCodeInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <p>
 * The third-party service provider sends a successful authorization notification from the We Com application market
 * </p>
 */
@Component
@Slf4j
public class WeComIsvAuthCreateMessageHandler implements WeComIsvMessageHandler {

    @Autowired(required = false)
    private WeComTemplate weComTemplate;

    @Resource
    private ISocialCpIsvMessageService socialCpIsvMessageService;

    @Override
    public WeComIsvMessageType messageType() {

        return WeComIsvMessageType.AUTH_CREATE;

    }

    @Override
    public WxCpXmlOutMessage handle(WxCpTpXmlMessage wxMessage, Map<String, Object> context,
                                    WxCpTpService wxCpService, WxSessionManager sessionManager) {

        SocialCpIsvEventLogEntity entity = SocialCpIsvEventLogEntity.builder()
            .type(WeComIsvMessageType.AUTH_CREATE.getType())
            .suiteId(wxMessage.getSuiteId())
            .infoType(WeComIsvMessageType.AUTH_CREATE.getInfoType())
            .timestamp(Long.parseLong(wxMessage.getTimeStamp()))
            .build();
        try {
            WxCpIsvServiceImpl wxCpIsvService =
                (WxCpIsvServiceImpl) weComTemplate.isvService(wxMessage.getSuiteId());
            WxCpIsvPermanentCodeInfo permanentCodeInfo =
                wxCpIsvService.getPermanentCodeInfo(wxMessage.getAuthCode());
            AuthCorpInfo authCorpInfo = permanentCodeInfo.getAuthCorpInfo();

            entity.setAuthCorpId(authCorpInfo.getCorpId());
            // The callback notification data is not saved here, but the information returned by the interface for obtaining the enterprise permanent authorization code is saved
            entity.setMessage(JSONUtil.toJsonStr(permanentCodeInfo));
            entity.setProcessStatus(SocialCpIsvMessageProcessStatus.PENDING.getValue());
            // Save notification information
            socialCpIsvMessageService.save(entity);

            // The response must be completed within 1000ms, so only the relevant information is recorded in the current event, and then the business is processed later
            socialCpIsvMessageService.sendToMq(entity.getId(), entity.getInfoType(),
                entity.getAuthCorpId(),
                entity.getSuiteId());
        } catch (WxErrorException ex) {
            log.warn("Exception occurred while getting permanent code.", ex);

            entity.setMessage(JSONUtil.toJsonStr(wxMessage));
            entity.setProcessStatus(SocialCpIsvMessageProcessStatus.REJECT_PERMANENTLY.getValue());
            // Save notification information
            socialCpIsvMessageService.save(entity);

            return new WxCpXmlOutTextMessage();
        } catch (Exception ex) {
            entity.setMessage(JSONUtil.toJsonStr(wxMessage));
            entity.setProcessStatus(SocialCpIsvMessageProcessStatus.REJECT_PERMANENTLY.getValue());
            // Save notification information
            socialCpIsvMessageService.save(entity);

            throw ex;
        }

        return null;

    }

}
