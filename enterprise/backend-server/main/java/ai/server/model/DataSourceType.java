package com.apitable.enterprise.ai.server.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * data source type.
 */
@AllArgsConstructor
public enum DataSourceType {

    DATASHEET("datasheet");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static DataSourceType of(String value) {
        for (DataSourceType type : DataSourceType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid DataSourceType value: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
}
