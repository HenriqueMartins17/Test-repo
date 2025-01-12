package com.apitable.enterprise.apitablebilling.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * stripe billing schema.
 *
 * @author Shawn Deng
 */
@Getter
@AllArgsConstructor
public enum BillingSchema {

    PER_UNIT("per_unit"),
    TIERED("tiered");

    private final String value;

    /**
     * transform.
     *
     * @param schema string value
     * @return BillingSchema
     */
    public static BillingSchema of(String schema) {
        for (BillingSchema value : BillingSchema.values()) {
            if (value.getValue().equalsIgnoreCase(schema)) {
                return value;
            }
        }
        return null;
    }

    public boolean isPerUnitSchema() {
        return this == BillingSchema.PER_UNIT;
    }

    public boolean isTieredSchema() {
        return this == BillingSchema.TIERED;
    }
}
