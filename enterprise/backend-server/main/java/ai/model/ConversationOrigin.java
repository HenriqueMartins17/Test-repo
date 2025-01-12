package com.apitable.enterprise.ai.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * conversation origin.
 */
@Getter
@AllArgsConstructor
public enum ConversationOrigin {

    INTERNAL("internal"),
    ANONYMOUS("anonymous");

    private final String value;

    /**
     * transform string to enum.
     *
     * @param value string value
     * @return enum
     */
    public static ConversationOrigin of(String value) {
        for (ConversationOrigin origin : values()) {
            if (origin.getValue().equalsIgnoreCase(value)) {
                return origin;
            }
        }
        return null;
    }
}
