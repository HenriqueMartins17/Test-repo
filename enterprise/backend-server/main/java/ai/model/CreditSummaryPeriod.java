package com.apitable.enterprise.ai.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Credit summary period.
 *
 * @author Shawn Deng
 */
@AllArgsConstructor
@Getter
public enum CreditSummaryPeriod {

    DAY("day"),
    WEEKDAY("weekday"),
    MONTH("month"),
    YEAR("year");

    private final String value;

    /**
     * transform by name.
     *
     * @param name name
     * @return credit summary period
     */
    public static CreditSummaryPeriod of(String name) {
        for (CreditSummaryPeriod period : CreditSummaryPeriod.values()) {
            if (period.getValue().equalsIgnoreCase(name)) {
                return period;
            }
        }
        return null;
    }

    public boolean isDay() {
        return this == DAY;
    }

    public boolean isMonth() {
        return this == MONTH;
    }
}
