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

import com.apitable.enterprise.vikabilling.entity.OrderItemEntity;

/**
 * <p>
 * Order Item Service
 * </p>
 */
public interface IOrderItemService extends IService<OrderItemEntity> {

    /**
     * Get order item by order id
     *
     * @param orderId order id
     * @return item entity list
     */
    List<OrderItemEntity> getByOrderId(String orderId);

    /**
     * Get the base product in the order item
     *
     * @param orderId order id
     * @return order item entity
     */
    OrderItemEntity getBaseProductInOrder(String orderId);

    /**
     * Get order item by subscription id
     *
     * @param subscriptionId subscription id
     * @return item entity list
     */
    List<OrderItemEntity> getBySubscriptionId(String subscriptionId);

    /**
     * Get order item by order id
     *
     * @param orderId order id
     * @return List<String>
     */
    List<String> getSubscriptionIdsByOrderId(String orderId);
}
