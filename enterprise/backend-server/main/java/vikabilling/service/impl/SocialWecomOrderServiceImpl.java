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

package com.apitable.enterprise.vikabilling.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import jakarta.annotation.Resource;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.apitable.enterprise.vikabilling.mapper.SocialWecomOrderMapper;
import com.apitable.enterprise.vikabilling.service.IOrderItemService;
import com.apitable.enterprise.vikabilling.service.IOrderV2Service;
import com.apitable.enterprise.vikabilling.service.ISocialWecomOrderService;
import com.apitable.enterprise.vikabilling.service.ISubscriptionService;
import com.apitable.core.util.DateTimeUtil;
import com.apitable.enterprise.vikabilling.entity.SocialWecomOrderEntity;
import com.vikadata.social.wecom.event.order.WeComOrderPaidEvent;

import org.springframework.stereotype.Service;

/**
 * <p>
 * Subscription Billing System - Wecom Order Service Implement Class
 * </p>
 */
@Service
public class SocialWecomOrderServiceImpl
    extends ServiceImpl<SocialWecomOrderMapper, SocialWecomOrderEntity>
    implements ISocialWecomOrderService {

    @Resource
    private IOrderItemService iOrderItemService;

    @Resource
    private IOrderV2Service iOrderV2Service;

    @Resource
    private ISubscriptionService iSubscriptionService;


    @Override
    public SocialWecomOrderEntity createOrder(WeComOrderPaidEvent paidEvent) {
        SocialWecomOrderEntity orderEntity = new SocialWecomOrderEntity();
        orderEntity.setOrderId(paidEvent.getOrderId());
        orderEntity.setOrderStatus(paidEvent.getOrderStatus());
        orderEntity.setOrderType(paidEvent.getOrderType());
        orderEntity.setPaidCorpId(paidEvent.getPaidCorpId());
        orderEntity.setOperatorId(paidEvent.getOperatorId());
        orderEntity.setSuiteId(paidEvent.getSuiteId());
        orderEntity.setEditionId(paidEvent.getEditionId());
        orderEntity.setPrice(paidEvent.getPrice());
        orderEntity.setUserCount(paidEvent.getUserCount());
        orderEntity.setOrderPeriod(paidEvent.getOrderPeriod());
        orderEntity.setOrderTime(
            DateTimeUtil.localDateTimeFromSeconds(paidEvent.getOrderTime(), 8));
        orderEntity.setPaidTime(DateTimeUtil.localDateTimeFromSeconds(paidEvent.getPaidTime(), 8));
        orderEntity.setBeginTime(
            DateTimeUtil.localDateTimeFromSeconds(paidEvent.getBeginTime(), 8));
        orderEntity.setEndTime(DateTimeUtil.localDateTimeFromSeconds(paidEvent.getEndTime(), 8));
        orderEntity.setOrderFrom(paidEvent.getOrderFrom());
        orderEntity.setOperatorCorpId(paidEvent.getOperatorCorpId());
        orderEntity.setServiceShareAmount(paidEvent.getServiceShareAmount());
        orderEntity.setPlatformShareAmount(paidEvent.getPlatformShareAmount());
        orderEntity.setDealerShareAmount(paidEvent.getDealerShareAmount());
        orderEntity.setDealerCorpId(Optional.ofNullable(paidEvent.getDealerCorpInfo())
            .map(WeComOrderPaidEvent.DealerCorpInfo::getCorpId)
            .orElse(null));
        orderEntity.setOrderInfo(JSONUtil.toJsonStr(paidEvent));
        orderEntity.setCreatedBy(-1L);
        orderEntity.setUpdatedBy(-1L);
        save(orderEntity);
        return orderEntity;
    }


    @Override
    public SocialWecomOrderEntity getByOrderId(String orderId) {
        return getBaseMapper().selectByOrderId(orderId);
    }

    @Override
    public SocialWecomOrderEntity getLastPaidOrder(String suiteId, String paidCorpId) {
        return getBaseMapper().selectLastPaidOrder(suiteId, paidCorpId);
    }

    @Override
    public void updateOrderStatusByOrderId(String orderId, int orderStatus) {
        baseMapper.updateOrderStatusByOrderId(orderId, orderStatus);
    }

    @Override
    public List<String> getUnRefundedLastSubscriptionIds(String spaceId, String suiteId,
                                                         String paidCorpId) {
        SocialWecomOrderEntity order = baseMapper.selectLastPaidOrder(suiteId, paidCorpId);
        if (null == order) {
            // determine whether it is in the trial period
            String subscriptionId =
                iSubscriptionService.getActiveTrailSubscriptionIdBySpaceId(spaceId);
            if (null != subscriptionId) {
                return Collections.singletonList(subscriptionId);
            }
            return new ArrayList<>();
        }
        String billingOrderId =
            iOrderV2Service.getOrderIdByChannelOrderId(spaceId, order.getOrderId());
        if (null == billingOrderId) {
            return new ArrayList<>();
        }
        return iOrderItemService.getSubscriptionIdsByOrderId(billingOrderId);
    }
}
