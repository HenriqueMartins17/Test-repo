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

package com.apitable.enterprise.social.service.impl;

import java.util.List;
import java.util.Objects;

import jakarta.annotation.Resource;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;

import com.apitable.enterprise.vikabilling.service.ISocialWecomOrderService;
import com.apitable.enterprise.social.service.ISocialCpIsvEntityHandler;
import com.apitable.enterprise.social.service.ISocialCpIsvMessageService;
import com.apitable.enterprise.social.service.ISocialCpIsvService;
import com.apitable.enterprise.social.service.ISocialTenantBindService;
import com.apitable.enterprise.social.entity.SocialCpIsvEventLogEntity;
import com.apitable.enterprise.vikabilling.entity.SocialWecomOrderEntity;
import com.vikadata.social.wecom.constants.WeComIsvMessageType;
import com.vikadata.social.wecom.event.order.WeComOrderPaidEvent;
import com.vikadata.social.wecom.model.WxCpIsvXmlMessage;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Third party platform integration - WeCom third-party service provider order payment successfully processed
 * </p>
 */
@Service
@Slf4j
public class SocialCpIsvPayForAppSuccessEntityHandler implements ISocialCpIsvEntityHandler {

    @Resource
    private ISocialCpIsvService socialCpIsvService;

    @Resource
    private ISocialCpIsvMessageService socialCpIsvMessageService;

    @Resource
    private ISocialTenantBindService socialTenantBindService;

    @Resource
    private ISocialWecomOrderService socialWecomOrderService;

    @Override
    public WeComIsvMessageType type() {
        return WeComIsvMessageType.PAY_FOR_APP_SUCCESS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean process(SocialCpIsvEventLogEntity unprocessed) throws WxErrorException {
        String suiteId = unprocessed.getSuiteId();
        String authCorpId = unprocessed.getAuthCorpId();
        WxCpIsvXmlMessage wxCpIsvXmlMessage =
            JSONUtil.toBean(unprocessed.getMessage(), WxCpIsvXmlMessage.class);
        // 1 wecom order saved or not
        String orderId = wxCpIsvXmlMessage.getOrderId();
        SocialWecomOrderEntity existedOrder = socialWecomOrderService.getByOrderId(orderId);
        if (Objects.isNull(existedOrder)) {
            // 1.1 save order and handle paid subscription if not
            List<String> spaceIds =
                socialTenantBindService.getSpaceIdsByTenantIdAndAppId(authCorpId, suiteId);
            for (String spaceId : spaceIds) {
                WeComOrderPaidEvent paidEvent = socialCpIsvService.fetchPaidEvent(suiteId, orderId);
                socialCpIsvService.handleTenantPaidSubscribe(suiteId, authCorpId, spaceId,
                    paidEvent);
            }
        } else {
            log.warn("Wecom order has handled：{}", orderId);
        }
        return true;
    }

}
