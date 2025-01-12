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
import java.util.Map;

import com.apitable.interfaces.billing.model.SubscriptionFeature;
import com.apitable.interfaces.billing.model.SubscriptionInfo;

/**
 * <p>
 * Space Subscription Service
 * </p>
 */
public interface ISpaceSubscriptionService {

    /**
     * Get the specifications of the space station subscription plan in batches
     *
     * @param spaceIds space id list
     * @return spaceId -> BillingPlanFeature
     */
    Map<String, SubscriptionFeature> getSubscriptionFeatureBySpaceIds(List<String> spaceIds);

    /**
     * Get the subscription plan for the specified space station
     *
     * @param spaceId space id
     * @return SubscribePlanInfo
     */
    SubscriptionInfo getPlanInfoBySpaceId(String spaceId);

    /**
     * Get the capacity of unexpired attachments given by space
     * * Complimentary attachment capacity is an attachment subscription plan
     *
     * @return InternalSpaceSubscriptionVo
     */
    Long getSpaceUnExpireGiftCapacity(String spaceId);


    /**
     * Create a subscription that gives away attachment capacity with an add-on subscription plan
     *
     * @param userId   user id
     * @param userName user id
     * @param spaceId  space id
     */
    void createAddOnWithGiftCapacity(Long userId, String userName, String spaceId);
}
