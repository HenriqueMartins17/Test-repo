package com.apitable.enterprise.stripe.core;

/**
 * stripe subscription object status.
 *
 * @author Shawn Deng
 */
public enum StripeSubscriptionStatus {

    TRIALING("trialing"),
    INCOMPLETE("incomplete"),
    INCOMPLETE_EXPIRED("incomplete_expired"),
    CANCELED("canceled"),
    UNPAID("unpaid"),
    ACTIVE("active"),

    PAST_DUE("past_due");

    private final String value;

    StripeSubscriptionStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * transform from key.
     *
     * @param name string key
     * @return StripeSubscriptionStatus
     */
    public static StripeSubscriptionStatus of(String name) {
        for (StripeSubscriptionStatus value : StripeSubscriptionStatus.values()) {
            if (value.getValue().equals(name)) {
                return value;
            }
        }
        throw new IllegalArgumentException("unsupported status name");
    }

    /**
     * is valid status.
     *
     * @return true if status is valid
     */
    public boolean isValid() {
        return this == ACTIVE || this == TRIALING;
    }
}
