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

package com.apitable.enterprise.vikabilling.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import com.apitable.enterprise.vikabilling.entity.OrderItemEntity;

/**
 * Subscription Billing System - Order Item Mapper
 */
public interface OrderItemMapper extends BaseMapper<OrderItemEntity> {

    /**
     * Query order item by order id
     *
     * @param orderId order id
     * @return order item list
     */
    List<OrderItemEntity> selectByOrderId(@Param("orderId") String orderId);

    /**
     * Query order item by subscription id
     *
     * @param subscriptionId subscription id
     * @return order item list
     */
    List<OrderItemEntity> selectBySubscriptionId(@Param("subscriptionId") String subscriptionId);

    /**
     * Query subscription id by order id
     *
     * @param orderId order id
     * @return List<String>
     */
    List<String> selectSubscriptionIdsByOrderId(@Param("orderId") String orderId);
}
