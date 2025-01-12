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

import com.apitable.enterprise.vikabilling.entity.SocialDingtalkOrderEntity;
import com.vikadata.social.dingtalk.event.order.SyncHttpMarketOrderEvent;

public interface ISocialDingTalkOrderService extends IService<SocialDingtalkOrderEntity> {
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
     * @param event Order purchase event data
     */
    void createOrder(SyncHttpMarketOrderEvent event);

    /**
     * Update order status based on DingTalk order number
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
     * @return List<String>
     */
    List<String> getOrdersByTenantIdAndAppId(String tenantId, String appId);

    /**
     * Get the order of the corresponding product of the tenant
     *
     * @param tenantId  tenant id
     * @param appId     app id
     * @param itemCode  item code
     * @return List<String>
     */
    List<String> getOrderIdsByTenantIdAndAppIdAndItemCode(String tenantId, String appId, String itemCode);
}
