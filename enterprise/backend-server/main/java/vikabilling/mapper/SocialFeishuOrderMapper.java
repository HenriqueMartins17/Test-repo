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

import com.apitable.enterprise.vikabilling.entity.SocialFeishuOrderEntity;

/**
 * Subscription Billing System - FeishuOrder Mapper
 */
public interface SocialFeishuOrderMapper extends BaseMapper<SocialFeishuOrderEntity> {

    /**
     * Query status by condition
     *
     * @param orderId   order id
     * @param tenantId  tenant id
     * @param appId     app id
     * @return count
     */
    Integer selectStatusByOrderId(@Param("orderId") String orderId, @Param("tenantId") String tenantId,
            @Param("appId") String appId);

    /**
     * Query order data
     *
     * @param tenantId  tenant id
     * @param appId     app id
     * @return order data
     */
    List<String> selectOrderDataByTenantIdAndAppId(@Param("tenantId") String tenantId, @Param("appId") String appId);

    /**
     * Update order process status
     *
     * @param tenantId  tenant id
     * @param appId     app id
     * @param orderId   order id
     * @return number of rows affected
     */
    Integer updateStatusByTenantIdAndAppIdAndOrderId(@Param("tenantId") String tenantId,
            @Param("appId") String appId, @Param("orderId") String orderId, @Param("status") Integer status);
}
