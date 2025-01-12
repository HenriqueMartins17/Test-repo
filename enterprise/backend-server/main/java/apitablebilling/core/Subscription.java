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

import com.apitable.enterprise.apitablebilling.entity.SubscriptionEntity;
import com.apitable.enterprise.apitablebilling.enums.BillingMode;
import com.apitable.enterprise.apitablebilling.enums.BillingPeriod;
import com.apitable.enterprise.apitablebilling.enums.ProductCategory;
import com.apitable.enterprise.apitablebilling.enums.SubscriptionState;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Subscription Items in Subscription Bundles.
 */
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Subscription {

    private String spaceId;

    private String bundleId;

    private String subscriptionId;

    private String stripeId;

    private String stripeSubId;

    private String productName;

    private ProductCategory productCategory;

    private String priceId;

    private Integer quantity;

    private SubscriptionState state;

    private LocalDateTime startDate;

    private LocalDateTime expireDate;

    private BillingMode billingMode;

    private BillingPeriod period;

    private String metadata;

    /**
     * class construct.
     *
     * @param subscriptionEntity Subscription Entity
     */
    public Subscription(SubscriptionEntity subscriptionEntity) {
        this.spaceId = subscriptionEntity.getSpaceId();
        this.bundleId = subscriptionEntity.getBundleId();
        this.subscriptionId = subscriptionEntity.getSubscriptionId();
        this.stripeId = subscriptionEntity.getStripeId();
        this.stripeSubId = subscriptionEntity.getStripeSubId();
        this.productName = subscriptionEntity.getProductName();
        this.productCategory = ProductCategory.valueOf(subscriptionEntity.getProductCategory());
        this.priceId = subscriptionEntity.getPriceId();
        this.quantity = subscriptionEntity.getQuantity();
        this.state = SubscriptionState.valueOf(subscriptionEntity.getState());
        this.startDate = subscriptionEntity.getStartDate();
        this.expireDate = subscriptionEntity.getExpireDate();
    }
}
