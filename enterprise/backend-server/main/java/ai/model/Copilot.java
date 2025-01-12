package com.apitable.enterprise.ai.model;

import lombok.Data;

/**
 * copilot object.
 */
@Data
public class Copilot {

    private boolean firstTimeUsed;

    private CopilotConversation latestConversation;
}
