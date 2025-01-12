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

package com.apitable.enterprise.apitablebilling.core;

import com.apitable.enterprise.apitablebilling.entity.BundleEntity;
import com.apitable.enterprise.apitablebilling.entity.SubscriptionEntity;
import com.apitable.enterprise.apitablebilling.enums.BundleState;
import com.apitable.enterprise.apitablebilling.enums.ProductCategory;
import com.apitable.enterprise.apitablebilling.enums.ProductEnum;
import com.apitable.enterprise.apitablebilling.enums.SubscriptionState;
import com.apitable.shared.clock.spring.ClockManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Space Subscription Package View
 * Usually a subscription product owned by a space will have one Base product and N addOn products
 * For example: Gold level 100 people (BASE) + 200,000 Api usage package (ADD_ON).
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Bundle {

    private String spaceId;

    /**
     * subscription bundle ID.
     */
    private String bundleId;

    /**
     * subscription bundle state.
     */
    private BundleState state;

    /**
     * subscription bundle begin time.
     */
    private LocalDateTime bundleStartDate;

    /**
     * subscription bundle end time.
     */
    private LocalDateTime bundleEndDate;

    /**
     * subscription entry.
     */
    private List<Subscription> subscriptions;

    /**
     * init construct.
     *
     * @param bundleEntity         bundle entity
     * @param subscriptionEntities subscription entities
     */
    public Bundle(BundleEntity bundleEntity, List<SubscriptionEntity> subscriptionEntities) {
        this.spaceId = bundleEntity.getSpaceId();
        this.bundleId = bundleEntity.getBundleId();
        this.state = BundleState.valueOf(bundleEntity.getState());
        this.bundleStartDate = bundleEntity.getStartDate();
        this.bundleEndDate = bundleEntity.getEndDate();
        this.subscriptions = new ArrayList<>();
        subscriptionEntities.forEach(
            subscriptionEntity -> this.subscriptions.add(new Subscription(subscriptionEntity)));
    }

    /**
     * Get subscriptions for base type products.
     *
     * @return Base Subscription.
     */
    public Subscription getBaseSubscription() {
        LocalDateTime todayTime = ClockManager.me().getLocalDateTimeNow();
        List<Subscription> subscriptionList = subscriptions.stream()
            .filter(subscription -> SubscriptionState.CANCELED != subscription.getState()
                && ProductCategory.BASE == subscription.getProductCategory()
            )
            .toList();
        return subscriptionList.stream()
            .filter(i -> !todayTime.isBefore(i.getStartDate())
                && !todayTime.isAfter(i.getExpireDate()))
            .findFirst().orElseGet(() -> subscriptionList.get(0));
    }

    /**
     * base product is free or not.
     *
     * @return true | false
     */
    public boolean isBaseForFree() {
        Subscription base = getBaseSubscription();
        ProductEnum productEnum = ProductEnum.of(base.getProductName());
        return productEnum.isFree();
    }
}
