package com.apitable.enterprise.ai.server.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

/**
 * message type.
 */
@AllArgsConstructor
public enum MessageType {
    HUMAN("human"),
    AI("ai");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }
}
