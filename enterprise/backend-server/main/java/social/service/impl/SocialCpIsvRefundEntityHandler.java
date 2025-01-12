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

import java.util.Objects;

import jakarta.annotation.Resource;

import cn.hutool.json.JSONUtil;
import me.chanjar.weixin.common.error.WxErrorException;

import com.apitable.shared.component.TaskManager;
import com.apitable.enterprise.social.enums.SocialPlatformType;
import com.apitable.enterprise.vikabilling.service.ISocialWecomOrderService;
import com.apitable.enterprise.vikabilling.strategy.SocialOrderStrategyFactory;
import com.apitable.enterprise.social.service.ISocialCpIsvEntityHandler;
import com.apitable.enterprise.social.service.ISocialCpIsvMessageService;
import com.apitable.enterprise.social.service.ISocialCpIsvPermitService;
import com.apitable.core.util.DateTimeUtil;
import com.apitable.enterprise.social.entity.SocialCpIsvEventLogEntity;
import com.apitable.enterprise.vikabilling.entity.SocialWecomOrderEntity;
import com.vikadata.social.wecom.WeComTemplate;
import com.vikadata.social.wecom.WxCpIsvServiceImpl;
import com.vikadata.social.wecom.constants.WeComIsvMessageType;
import com.vikadata.social.wecom.event.order.WeComOrderRefundEvent;
import com.vikadata.social.wecom.model.WxCpIsvGetOrder;
import com.vikadata.social.wecom.model.WxCpIsvXmlMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Third party platform integration - WeCom third-party service provider application refund processing
 * </p>
 */
@Service
public class SocialCpIsvRefundEntityHandler implements ISocialCpIsvEntityHandler {

    @Autowired(required = false)
    private WeComTemplate weComTemplate;

    @Resource
    private ISocialCpIsvMessageService socialCpIsvMessageService;

    @Resource
    private ISocialCpIsvPermitService socialCpIsvPermitService;

    @Resource
    private ISocialWecomOrderService socialWecomOrderService;

    @Override
    public WeComIsvMessageType type() {
        return WeComIsvMessageType.REFUND;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean process(SocialCpIsvEventLogEntity unprocessed) throws WxErrorException {
        // The order refund operation of the enterprise is performed in the reverse order of order payment
        // When refunding upgrade, renewal, and version switching orders, the previous version will be returned
        String suiteId = unprocessed.getSuiteId();
        String authCorpId = unprocessed.getAuthCorpId();
        WxCpIsvXmlMessage wxCpIsvXmlMessage =
            JSONUtil.toBean(unprocessed.getMessage(), WxCpIsvXmlMessage.class);
        String refundWeComOrderId = wxCpIsvXmlMessage.getOrderId();
        // 1 Change WeCom order to refunded status
        updateOrderStatus(suiteId, refundWeComOrderId);
        // 2 Process subscription changes
        WeComOrderRefundEvent refundEvent = new WeComOrderRefundEvent();
        refundEvent.setSuiteId(suiteId);
        refundEvent.setPaidCorpId(authCorpId);
        refundEvent.setOrderId(refundWeComOrderId);
        SocialOrderStrategyFactory.getService(SocialPlatformType.WECOM)
            .retrieveOrderRefundEvent(refundEvent);
        // 3 Determine whether there is no paid subscription
        SocialWecomOrderEntity lastPaidOrder =
            socialWecomOrderService.getLastPaidOrder(suiteId, authCorpId);
        if (Objects.isNull(lastPaidOrder) ||
            DateTimeUtil.localDateTimeNow(8).isAfter(lastPaidOrder.getEndTime())) {
            // 3.1 If the WeCom order successfully paid in the previous stage does not exist or has expired, that is,
            // there is no paid subscription version, the enterprise will be notified to refund the interface license
            TaskManager.me()
                .execute(() -> socialCpIsvPermitService.sendRefundWebhook(suiteId, authCorpId));
        }
        return true;
    }

    /**
     * Update the status of WeCom orders
     *
     * @param suiteId App Suite ID
     * @param orderId WeCom order ID
     */
    private void updateOrderStatus(String suiteId, String orderId) throws WxErrorException {
        // Get the latest information about WeCom orders
        WxCpIsvServiceImpl wxCpIsvService = (WxCpIsvServiceImpl) weComTemplate.isvService(suiteId);
        WxCpIsvGetOrder wxCpIsvGetOrder = wxCpIsvService.getOrder(orderId);
        socialWecomOrderService.updateOrderStatusByOrderId(orderId,
            wxCpIsvGetOrder.getOrderStatus());
    }

}
