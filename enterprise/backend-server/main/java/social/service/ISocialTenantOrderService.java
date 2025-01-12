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

import com.apitable.enterprise.social.enums.SocialPlatformType;
import com.apitable.enterprise.social.entity.SocialTenantOrderEntity;

/**
 * <p>
 * Third party platform integration - order service interface
 * </p>
 */
@Deprecated
public interface ISocialTenantOrderService extends IService<SocialTenantOrderEntity> {
    /**
     * Whether the third-party order exists
     *
     * @param channelOrderId Channel order No
     * @param tenantId Enterprise ID
     * @param appId App ID
     * @param platformType Application Type
     * @return Boolean
     */
    Boolean tenantOrderExisted(String channelOrderId, String tenantId, String appId, SocialPlatformType platformType);

    /**
     * Create Third Party Orders
     *
     * @param orderId Third party order ID
     * @param tenantId Enterprise ID
     * @param appId App ID
     * @param platformType Application Type
     * @param orderData Order Data
     * @return Boolean
     */
    Boolean createTenantOrder(String orderId, String tenantId, String appId, SocialPlatformType platformType,
            String orderData);
    /**
     * Access to third party information
     *
     * @param tenantId Enterprise ID
     * @param appId App ID
     * @param platformType Application Type
     * @return  List<String> orderData
     */
    List<String> getOrderDataByTenantIdAndAppId(String tenantId, String appId, SocialPlatformType platformType);
}
