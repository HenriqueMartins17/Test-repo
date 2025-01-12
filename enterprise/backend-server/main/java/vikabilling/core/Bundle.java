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

package com.apitable.enterprise.vikabilling.core;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Builder;
import lombok.Data;

import com.apitable.enterprise.vikabilling.enums.BundleState;
import com.apitable.enterprise.vikabilling.enums.ProductCategory;
import com.apitable.enterprise.vikabilling.enums.ProductEnum;
import com.apitable.enterprise.vikabilling.enums.SubscriptionState;
import com.apitable.shared.clock.spring.ClockManager;

/**
 * Space Subscription Package View
 * Usually a subscription product owned by a space will have one Base product and N addOn products
 * For example: Gold level 100 people (BASE) + 50G accessory capacity package (ADD_ON) + 200,000 Api usage package (ADD_ON)
 */
@Data
@Builder
public class Bundle {

    private String spaceId;

    /**
     * subscription bundle ID
     */
    private String bundleId;

    /**
     * subscription bundle state
     */
    private BundleState state;

    /**
     * subscription bundle begin time
     */
    private LocalDateTime bundleStartDate;

    /**
     * subscription bundle end time
     */
    private LocalDateTime bundleEndDate;

    /**
     * subscription entry
     */
    private List<Subscription> subscriptions;

    /**
     * Get subscriptions for base type products
     * @return Base Subscription
     */
    public Subscription getBaseSubscription() {
        LocalDateTime todayTime = ClockManager.me().getLocalDateTimeNow();
        List<Subscription> subscriptionList = subscriptions.stream()
                .filter(subscription -> SubscriptionState.ACTIVATED == subscription.getState()
                        && ProductCategory.BASE == subscription.getProductCategory()
                )
                .collect(Collectors.toList());
        return subscriptionList.stream()
                .filter(i -> todayTime.compareTo(i.getStartDate()) >= 0
                        && todayTime.compareTo(i.getExpireDate()) <= 0)
                .findFirst().orElseGet(() -> subscriptionList.get(0));
    }

    public boolean isBaseForFree() {
        Subscription base = getBaseSubscription();
        ProductEnum productEnum = ProductEnum.of(base.getProductName());
        if (productEnum == null) {
            return false;
        }
        return productEnum.isFree();
    }

    public List<Subscription> getAddOnSubscription() {
        return subscriptions.stream()
                .filter(subscription -> SubscriptionState.ACTIVATED == subscription.getState()
                        && ProductCategory.ADD_ON == subscription.getProductCategory())
                .collect(Collectors.toList());
    }
}
