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

import com.apitable.enterprise.vikabilling.enums.OrderStatus;
import com.apitable.enterprise.vikabilling.enums.OrderType;
import com.apitable.enterprise.vikabilling.enums.PayChannel;
import com.apitable.enterprise.vikabilling.core.Bundle;
import com.apitable.enterprise.vikabilling.core.DryRunArguments;
import com.apitable.enterprise.vikabilling.core.OrderArguments;
import com.apitable.enterprise.vikabilling.core.OrderPrice;
import com.apitable.enterprise.vikabilling.model.OrderDetailVo;
import com.apitable.enterprise.vikabilling.model.OrderPaymentVo;
import com.apitable.enterprise.vikabilling.model.OrderPreview;
import com.apitable.enterprise.vikabilling.entity.OrderEntity;
import com.apitable.enterprise.vikabilling.setting.Price;

/**
 * <p>
 * Order Service
 * </p>
 */
public interface IOrderV2Service extends IService<OrderEntity> {

    /**
     * Get order
     *
     * @param orderId order id
     * @return entity
     */
    OrderEntity getByOrderId(String orderId);

    /**
     * Batch get order
     *
     * @param orderIds order id list
     * @return order list
     */
    List<OrderEntity> getByOrderIds(List<String> orderIds);

    /**
     * Get order detail
     *
     * @param orderId order id
     * @return OrderDetailVo
     */
    OrderDetailVo getOrderDetailByOrderId(String orderId);

    /**
     * Trial run order generation
     *
     * @param dryRunArguments arguments
     * @return OrderPreview
     */
    OrderPreview triggerDryRunOrderGeneration(DryRunArguments dryRunArguments);

    /**
     * Fix order price
     *
     * @param actionBundle space subscription bundle
     * @param newPricePlan new price plan
     * @return OrderPrice
     */
    OrderPrice repairOrderPrice(Bundle actionBundle, Price newPricePlan);

    /**
     * Parse order type based on current space station
     *
     * @param bundle            space subscription bundle
     * @param requestPricePlan  Requested payment plan
     * @return OrderType
     */
    OrderType parseOrderType(Bundle bundle, Price requestPricePlan);

    /**
     * Create order
     *
     * @param orderArguments order arguments
     * @return order id
     */
    String createOrder(OrderArguments orderArguments);

    /**
     * Create order payment
     *
     * @param userId    user id
     * @param orderId   order id
     * @param channel   pay channel
     * @return OrderPaymentVo
     */
    OrderPaymentVo createOrderPayment(Long userId, String orderId, PayChannel channel);

    /**
     * Get order status
     *
     * @param orderId order id
     * @return OrderStatus
     */
    OrderStatus getOrderStatusByOrderId(String orderId);

    /**
     * Get order id
     *
     * @param spaceId           space id
     * @param channelOrderId    channel order id
     * @return String
     */
    String getOrderIdByChannelOrderId(String spaceId, String channelOrderId);

    /**
     * Check the payment status of the refresh order
     *
     * @param orderId order id
     * @return OrderStatus
     */
    OrderStatus checkOrderStatus(String orderId);
}
