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
import me.chanjar.weixin.cp.bean.message.WxCpTpXmlMessage;
import me.chanjar.weixin.cp.bean.message.WxCpXmlOutMessage;
import me.chanjar.weixin.cp.tp.service.WxCpTpService;

import com.apitable.enterprise.social.enums.SocialCpIsvMessageProcessStatus;
import com.apitable.enterprise.social.service.ISocialCpIsvMessageService;
import com.apitable.enterprise.social.entity.SocialCpIsvEventLogEntity;
import com.vikadata.social.wecom.constants.WeComIsvMessageType;
import com.vikadata.social.wecom.handler.WeComIsvMessageHandler;
import com.vikadata.social.wecom.model.WxCpIsvXmlMessage;

import org.springframework.stereotype.Component;

/**
 * <p>
 * Notice of successful payment of the order of the enterprise authorized by the third-party service provider
 * </p>
 */
@Component
@Slf4j
public class WeComIsvPayForAppSuccessMessageHandler implements WeComIsvMessageHandler {

    @Resource
    private ISocialCpIsvMessageService socialCpIsvMessageService;

    @Override
    public WeComIsvMessageType messageType() {

        return WeComIsvMessageType.PAY_FOR_APP_SUCCESS;

    }

    @Override
    public WxCpXmlOutMessage handle(WxCpTpXmlMessage wxMessage, Map<String, Object> context,
                                    WxCpTpService wxCpService, WxSessionManager sessionManager)
        throws WxErrorException {
        WxCpIsvXmlMessage wxCpIsvXmlMessage = (WxCpIsvXmlMessage) wxMessage;
        SocialCpIsvEventLogEntity entity = SocialCpIsvEventLogEntity.builder()
            .type(WeComIsvMessageType.PAY_FOR_APP_SUCCESS.getType())
            .suiteId(wxCpIsvXmlMessage.getSuiteId())
            .infoType(WeComIsvMessageType.PAY_FOR_APP_SUCCESS.getInfoType())
            .authCorpId(wxCpIsvXmlMessage.getPaidCorpId())
            .timestamp(Long.parseLong(wxCpIsvXmlMessage.getTimeStamp()))
            .message(JSONUtil.toJsonStr(wxCpIsvXmlMessage))
            .processStatus(SocialCpIsvMessageProcessStatus.PENDING.getValue())
            .build();
        socialCpIsvMessageService.save(entity);

        socialCpIsvMessageService.sendToMq(entity.getId(), entity.getInfoType(),
            entity.getAuthCorpId(), entity.getSuiteId());

        return null;
    }

}
