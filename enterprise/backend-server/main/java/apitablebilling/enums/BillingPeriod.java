package com.apitable.enterprise.apitablebilling.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BillingPeriod {

    MONTHLY("monthly"),
    YEARLY("yearly"),
    UNLIMITED("unlimited"),
    ;

    private final String name;

    public static BillingPeriod of(String name) {
        for (BillingPeriod value : BillingPeriod.values()) {
            if (value.getName().equalsIgnoreCase(name)) {
                return value;
            }
        }
        throw new IllegalArgumentException("unsupported period value");
    }

    public static BillingPeriod ofInterval(String interval) {
        for (BillingPeriod value : BillingPeriod.values()) {
            if (value.getName().contains(interval)) {
                return value;
            }
        }
        throw new IllegalArgumentException("unsupported period value");
    }
}
