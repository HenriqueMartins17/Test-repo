package com.apitable.enterprise.ai.server.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * copilot type.
 */
public enum CopilotAssistantType {

    HELP("help"),
    DATA("data");
    private final String value;

    CopilotAssistantType(String value) {
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
    public static CopilotAssistantType of(String text) {
        for (CopilotAssistantType b : CopilotAssistantType.values()) {
            if (b.value.equalsIgnoreCase(text)) {
                return b;
            }
        }
        throw new IllegalArgumentException("unknown copilot agent type: " + text);
    }
}
