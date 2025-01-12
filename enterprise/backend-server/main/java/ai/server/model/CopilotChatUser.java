package com.apitable.enterprise.ai.server.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * copilot chat user.
 */

public enum CopilotChatUser {

    USER("user"),
    ASSISTANT("assistant");

    private final String value;

    CopilotChatUser(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }


    @Override
    public String toString() {
        return value;
    }

    /**
     * transform by text.
     *
     * @param text text
     * @return training status
     */
    public static CopilotChatUser of(String text) {
        for (CopilotChatUser b : CopilotChatUser.values()) {
            if (b.value.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }
}
