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

import com.apitable.enterprise.vikabilling.model.OfflineOrderInfo;
import com.apitable.enterprise.vikabilling.model.SpaceSubscriptionVo;
import com.apitable.enterprise.gm.ro.CreateBusinessOrderRo;
import com.apitable.enterprise.gm.ro.CreateEntitlementWithAddOn;

/**
 * <p>
 * Financial Service
 * </p>
 */
public interface IBillingOfflineService {

    /**
     * Get a subscription to a space
     *
     * @param spaceId space id
     * @return SpaceSubscriptionVo
     */
    SpaceSubscriptionVo getSpaceSubscription(String spaceId);

    /**
     * Create a business order
     * @param userId user id
     * @param data request data
     * @return Message
     */
    OfflineOrderInfo createBusinessOrder(Long userId, CreateBusinessOrderRo data);

    /**
     * Create a subscription with add-on plans
     * @param data request data
     * @param createdBy userId
     */
    void createSubscriptionWithAddOn(CreateEntitlementWithAddOn data, Long createdBy);
}
