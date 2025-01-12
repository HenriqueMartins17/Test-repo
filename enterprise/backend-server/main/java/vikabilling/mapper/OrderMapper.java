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
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;

import com.apitable.enterprise.vikabilling.entity.OrderEntity;

/**
 * Subscription Billing System - Order Mapper
 */
public interface OrderMapper extends BaseMapper<OrderEntity> {

    /**
     * Query by order id
     *
     * @param orderId order id
     * @return FinanceOrderEntity
     */
    OrderEntity selectByOrderId(@Param("orderId") String orderId);

    /**
     * Query order id by space id and channel order id
     *
     * @param spaceId           space id
     * @param channelOrderId    channel order id
     */
    String selectOrderBySpaceIdChannelOrderId(@Param("spaceId") String spaceId,
            @Param("channelOrderId") String channelOrderId);

    /**
     * Batch query by oder id list
     *
     * @param orderIds order id list
     * @return FinanceOrderEntity
     */
    List<OrderEntity> selectByOrderIds(@Param("orderIds") List<String> orderIds);

    /**
     * Query space order page
     *
     * @param spaceId   space id
     * @param orderType order type
     * @return page list
     */
    IPage<OrderEntity> selectBySpaceIdAndOrderType(IPage<OrderEntity> page, @Param("spaceId") String spaceId, @Param("orderType") String orderType);
}
