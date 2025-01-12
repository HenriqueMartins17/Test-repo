package com.apitable.enterprise.ai.server;

import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * copilot api.
 */
@Getter
public class CopilotAPI {

    private final String aiId;

    public CopilotAPI(String aiId) {
        this.aiId = aiId;
    }

    @NotNull
    public static CopilotAPI create(String aiId) {
        return new CopilotAPI(aiId);
    }


}
