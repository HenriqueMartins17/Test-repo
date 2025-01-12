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

package com.apitable.enterprise.vikabilling.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;

import com.apitable.enterprise.vikabilling.entity.SocialWecomOrderEntity;
import com.vikadata.social.wecom.event.order.WeComOrderPaidEvent;

/**
 * <p>
 * Subscription Billing System - Wecom Order Service
 * </p>
 */
public interface ISocialWecomOrderService extends IService<SocialWecomOrderEntity> {

    /**
     * Create order
     *
     * @param paidEvent paid Event order info
     * @return SocialWecomOrderEntity
     */
    SocialWecomOrderEntity createOrder(WeComOrderPaidEvent paidEvent);

    /**
     * Get order
     *
     * @param orderId order id
     * @return SocialWecomOrderEntity
     */
    SocialWecomOrderEntity getByOrderId(String orderId);

    /**
     * Get the tenant's last successful payment order
     *
     * @param suiteId       suite id
     * @param paidCorpId    paid corp id
     * @return SocialWecomOrderEntity
     */
    SocialWecomOrderEntity getLastPaidOrder(String suiteId, String paidCorpId);

    /**
     * modify order status by order id
     *
     * @param orderId social order id
     * @param orderStatus order status
     */
    void updateOrderStatusByOrderId(String orderId, int orderStatus);

    /**
     * Get the latest non-refundable subscription for the current subscription
     *
     * @param spaceId space id
     * @param suiteId suite id
     * @param paidCorpId paid corp id
     * @return subscriptionId
     */
    List<String> getUnRefundedLastSubscriptionIds(String spaceId, String suiteId, String paidCorpId);
}
