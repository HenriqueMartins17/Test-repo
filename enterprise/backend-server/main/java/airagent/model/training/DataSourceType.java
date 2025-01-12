package com.apitable.enterprise.airagent.model.training;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

/**
 * data source type.
 */
@AllArgsConstructor
public enum DataSourceType {

    AIRTABLE("airtable"),

    AITABLE("aitable"),

    FILE("file"),

    DATASHEET("datasheet");


    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    public boolean isAirtable() {
        return this == DataSourceType.AIRTABLE;
    }

    public boolean isAitable() {
        return this == DataSourceType.AITABLE;
    }

    public boolean isFile() {
        return this == DataSourceType.FILE;
    }

    public boolean isDatasheet() {
        return this == DataSourceType.DATASHEET;
    }

    /**
     * transform data source type by text.
     *
     * @param text text
     * @return data source type
     */
    public static DataSourceType of(String text) {
        for (DataSourceType b : DataSourceType.values()) {
            if (b.value.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }
}
