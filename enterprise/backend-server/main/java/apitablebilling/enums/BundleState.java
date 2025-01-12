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

package com.apitable.enterprise.apitablebilling.enums;

import com.apitable.enterprise.stripe.core.StripeSubscriptionStatus;
import lombok.Getter;

/**
 * Bundle state.
 *
 * @author Shawn Deng
 */
@Getter
public enum BundleState {

    TRIALING, ACTIVATED, CANCELED;

    /**
     * transform from stripe subscription status.
     *
     * @param status stripe subscription status
     * @return BundleState
     */
    public static BundleState of(StripeSubscriptionStatus status) {
        if (status == null) {
            return null;
        }
        switch (status) {
            case TRIALING:
                return TRIALING;
            case ACTIVE:
                return ACTIVATED;
            case CANCELED:
                return CANCELED;
            default:
                return null;
        }
    }
}
