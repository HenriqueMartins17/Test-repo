package com.apitable.enterprise.stripe.util;

import com.apitable.enterprise.stripe.core.StripeSubscriptionStatus;

/**
 * stripe util.
 */
public class StripeUtil {

    /**
     * is subscription cancel.
     *
     * @param subscriptionStatus subscription status value
     * @return true if subscription is canceled
     */
    public static boolean isSubscriptionCancel(String subscriptionStatus) {
        return subscriptionStatus.equals(StripeSubscriptionStatus.CANCELED.getValue());
    }
}
