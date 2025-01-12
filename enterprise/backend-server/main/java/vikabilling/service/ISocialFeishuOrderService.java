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

import com.apitable.enterprise.vikabilling.entity.SocialFeishuOrderEntity;
import com.vikadata.social.feishu.event.app.OrderPaidEvent;

public interface ISocialFeishuOrderService extends IService<SocialFeishuOrderEntity> {

    /**
     * check whether order existed
     *
     * @param orderId order id
     * @param tenantId tenant key
     * @param appId app id
     * @return boolean
     */
    Integer getStatusByOrderId(String tenantId, String appId, String orderId);

    /**
     * Create order
     *
     * @param event order purchase event data
     */
    void createOrder(OrderPaidEvent event);

    /**
     * Update order status based on Feishu order number
     *
     * @param orderId   order id
     * @param tenantId  tenant id
     * @param appId     app id
     * @param status    order processing status 1: processed, 0 not processed
     */
    void updateTenantOrderStatusByOrderId(String tenantId, String appId, String orderId, Integer status);

    /**
     * Get all orders under tenant app
     *
     * @param tenantId  tenant id
     * @param appId     app id
     * @return List<OrderPaidEvent>
     */
    List<String> getOrdersByTenantIdAndAppId(String tenantId, String appId);
}
