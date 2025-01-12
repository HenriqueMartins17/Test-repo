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
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;

import com.apitable.enterprise.social.service.ISocialCpIsvEntityHandler;
import com.apitable.enterprise.social.service.ISocialCpIsvMessageService;
import com.apitable.enterprise.social.service.ISocialCpIsvPermitService;
import com.apitable.enterprise.social.service.ISocialWecomPermitOrderService;
import com.apitable.enterprise.social.entity.SocialCpIsvEventLogEntity;
import com.apitable.enterprise.social.entity.SocialWecomPermitOrderEntity;
import com.vikadata.social.wecom.constants.WeComIsvMessageType;
import com.vikadata.social.wecom.model.WxCpIsvXmlMessage;

import org.springframework.stereotype.Service;

/**
 * <p>
 * Third party platform integration - WeCom third-party service provider interface license refund result processing
 * </p>
 */
@Slf4j
@Service
public class SocialCpIsvLicenseRefundEntityHandler implements ISocialCpIsvEntityHandler {

    @Resource
    private ISocialCpIsvMessageService socialCpIsvMessageService;

    @Resource
    private ISocialCpIsvPermitService socialCpIsvPermitService;

    @Resource
    private ISocialWecomPermitOrderService socialWecomPermitOrderService;

    @Override
    public WeComIsvMessageType type() {
        return WeComIsvMessageType.LICENSE_REFUND;
    }

    @Override
    public boolean process(SocialCpIsvEventLogEntity unprocessed) throws WxErrorException {
        WxCpIsvXmlMessage wxCpIsvXmlMessage =
            JSONUtil.toBean(unprocessed.getMessage(), WxCpIsvXmlMessage.class);
        String orderId = wxCpIsvXmlMessage.getOrderId();
        // Obtain interface license order information
        SocialWecomPermitOrderEntity orderEntity =
            socialWecomPermitOrderService.getByOrderId(orderId);
        if (Objects.isNull(orderEntity)) {
            log.warn(
                "No refunded interface license order was found. Please check whether it is other environmental data. SuiteId: {}. AuthCorpId:{}. Order: {}",
                unprocessed.getSuiteId(), unprocessed.getAuthCorpId(), orderId);
        } else {
            // Order exists, confirm the order status
            orderEntity = socialCpIsvPermitService.ensureOrder(orderId);
            if (orderEntity.getOrderStatus() == 5) {
                // If the refund is successful, confirm the activation status of all accounts
                socialCpIsvPermitService.ensureAllActiveCodes(unprocessed.getSuiteId(),
                    unprocessed.getAuthCorpId());
            }
        }
        return true;
    }

}
