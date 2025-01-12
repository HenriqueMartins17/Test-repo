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

import com.baomidou.mybatisplus.extension.service.IService;

import com.apitable.enterprise.vikabilling.entity.SocialDingtalkRefundEntity;
import com.vikadata.social.dingtalk.event.order.SyncHttpMarketServiceCloseEvent;

public interface ISocialDingTalkRefundService extends IService<SocialDingtalkRefundEntity> {
    /**
     * check whether refund existed
     *
     * @param orderId  order id
     * @param tenantId tenant key
     * @param appId app id
     * @return boolean
     */
    Integer getStatusByOrderId(String tenantId, String appId, String orderId);

    /**
     * Create refund
     *
     * @param event order refund event data
     */
    void createRefund(SyncHttpMarketServiceCloseEvent event);

    /**
     * Update order status based on DingTalk order number
     *
     * @param orderId   order id
     * @param tenantId  tenant id
     * @param appId     app id
     * @param status    Order processing status 1: processed, 0 not processed
     */
    void updateTenantRefundStatusByOrderId(String tenantId, String appId, String orderId, Integer status);
}
