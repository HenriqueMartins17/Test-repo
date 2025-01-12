package com.apitable.enterprise.apitablebilling.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RecurringInterval {

    MONTH("month"), YEAR("year");

    private final String name;

    public static RecurringInterval of(String name) {
        for (RecurringInterval value : RecurringInterval.values()) {
            if (value.getName().equalsIgnoreCase(name)) {
                return value;
            }
        }
        throw new IllegalArgumentException("unsupported interval value");
    }
}
