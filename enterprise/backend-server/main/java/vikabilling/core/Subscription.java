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

import lombok.Builder;
import lombok.Data;

import com.apitable.enterprise.vikabilling.enums.SubscriptionPhase;
import com.apitable.enterprise.vikabilling.enums.SubscriptionState;
import com.apitable.enterprise.vikabilling.enums.ProductCategory;

/**
 * Subscription Items in Subscription Bundles
 */
@Data
@Builder
public class Subscription {

    private String spaceId;

    /**
     * subscription bundle ID
     */
    private String bundleId;

    private String subscriptionId;

    private String productName;

    private ProductCategory productCategory;

    /**
     * product plan
     */
    private String planId;

    /**
     * subscription state
     */
    private SubscriptionState state;

    private LocalDateTime startDate;

    private LocalDateTime expireDate;

    /**
     * subscription phase
     */
    private SubscriptionPhase phase;
}
