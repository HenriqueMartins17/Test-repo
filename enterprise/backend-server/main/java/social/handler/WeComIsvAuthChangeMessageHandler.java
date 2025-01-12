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
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.cp.bean.message.WxCpTpXmlMessage;
import me.chanjar.weixin.cp.bean.message.WxCpXmlOutMessage;
import me.chanjar.weixin.cp.tp.service.WxCpTpService;

import com.apitable.enterprise.social.enums.SocialCpIsvMessageProcessStatus;
import com.apitable.enterprise.social.service.ISocialCpIsvMessageService;
import com.apitable.enterprise.social.entity.SocialCpIsvEventLogEntity;
import com.vikadata.social.wecom.constants.WeComIsvMessageType;
import com.vikadata.social.wecom.handler.WeComIsvMessageHandler;

import org.springframework.stereotype.Component;

/**
 * <p>
 * Third party service provider initiates change authorization notice from WeCom application market
 * </p>
 */
@Component
public class WeComIsvAuthChangeMessageHandler implements WeComIsvMessageHandler {

    @Resource
    private ISocialCpIsvMessageService socialCpIsvMessageService;

    @Override
    public WeComIsvMessageType messageType() {

        return WeComIsvMessageType.AUTH_CHANGE;

    }

    @Override
    public WxCpXmlOutMessage handle(WxCpTpXmlMessage wxMessage, Map<String, Object> context,
                                    WxCpTpService wxCpService, WxSessionManager sessionManager) {

        // The response must be completed within 1000ms, so only the relevant information is recorded in the current event, and then the business is processed later
        SocialCpIsvEventLogEntity entity = SocialCpIsvEventLogEntity.builder()
            .type(WeComIsvMessageType.AUTH_CHANGE.getType())
            .suiteId(wxMessage.getSuiteId())
            .infoType(WeComIsvMessageType.AUTH_CHANGE.getInfoType())
            .authCorpId(wxMessage.getAuthCorpId())
            .timestamp(Long.parseLong(wxMessage.getTimeStamp()))
            .message(JSONUtil.toJsonStr(wxMessage))
            .processStatus(SocialCpIsvMessageProcessStatus.PENDING.getValue())
            .build();
        socialCpIsvMessageService.save(entity);

        socialCpIsvMessageService.sendToMq(entity.getId(), entity.getInfoType(),
            entity.getAuthCorpId(), entity.getSuiteId());

        return null;

    }

}
