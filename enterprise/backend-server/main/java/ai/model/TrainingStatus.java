package com.apitable.enterprise.ai.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

/**
 * training status.
 *
 * @author Shawn Deng
 */
@AllArgsConstructor
public enum TrainingStatus {

    FAILED("failed"),
    NEW("new"),
    TRAINING("training"),
    SUCCESS("success");

    private final String status;

    @JsonValue
    public String getStatus() {
        return status;
    }

    /**
     * transform training status by text.
     *
     * @param text text
     * @return training status
     */
    public static TrainingStatus of(String text) {
        for (TrainingStatus b : TrainingStatus.values()) {
            if (b.status.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return status;
    }
}
