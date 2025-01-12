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

package com.apitable.enterprise.social.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;

import com.apitable.enterprise.social.entity.SocialWecomPermitOrderEntity;

/**
 * <p>
 * WeCom Service Provider Interface License Ordering Information
 * </p>
 */
public interface ISocialWecomPermitOrderService extends IService<SocialWecomPermitOrderEntity> {

    /**
     * Get details according to the order number
     *
     * @param orderId License Order Number
     * @return Order Details
     */
    SocialWecomPermitOrderEntity getByOrderId(String orderId);

    /**
     * Query according to order status
     *
     * @param suiteId App Suite ID
     * @param authCorpId Authorized enterprise ID
     * @param orderStatuses Order status queried
     * @return List of qualified orders
     */
    List<SocialWecomPermitOrderEntity> getByOrderStatuses(String suiteId, String authCorpId, List<Integer> orderStatuses);

}
