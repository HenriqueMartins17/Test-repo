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
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.apitable.enterprise.vikabilling.entity.SocialWecomOrderEntity;

/**
 * <p>
 * Subscription Billing System - Wecom Order Mapper
 * </p>
 */
@Mapper
public interface SocialWecomOrderMapper extends BaseMapper<SocialWecomOrderEntity> {

    /**
     * Query all order
     *
     * @param suiteId       suite id
     * @param paidCorpId    paid corp id
     * @param orderStatuses order statuses
     * @return SocialWecomOrderEntity List
     */
    List<SocialWecomOrderEntity> selectAllOrders(@Param("suiteId") String suiteId, @Param("paidCorpId") String paidCorpId,
            @Param("orderStatuses") List<Integer> orderStatuses);

    /**
     * Query order
     *
     * @param orderId   order id
     * @return SocialWecomOrderEntity
     */
    SocialWecomOrderEntity selectByOrderId(@Param("orderId") String orderId);

    /**
     * Get first paid order
     *
     * @param suiteId Wecom isv suite ID
     * @param paidCorpId Paid corporation ID
     * @return Tenant's first paid order
     */
    SocialWecomOrderEntity selectFirstPaidOrder(@Param("suiteId") String suiteId, @Param("paidCorpId") String paidCorpId);

    /**
     * Get the tenant's last successful payment order
     *
     * @param suiteId       suite id
     * @param paidCorpId    paid corp id
     * @return SocialWecomOrderEntity
     */
    SocialWecomOrderEntity selectLastPaidOrder(@Param("suiteId") String suiteId, @Param("paidCorpId") String paidCorpId);

    /**
     * query id by order  id
     *
     * @param orderId order id
     * @return pre order status
     */
    Long selectIdByOrderId(@Param("orderId") String orderId);

    /**
     * get order status by id
     *
     * @param id primary key
     * @return id
     */
    Integer selectPreOrderStatusById(@Param("id") Long id);


    /**
     * modify order status by order id
     *
     * @param orderId social order id
     * @return number of rows affected
     */
    int updateOrderStatusByOrderId(@Param("orderId") String orderId, @Param("orderStatus") int orderStatus);
}
