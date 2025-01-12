package com.apitable.enterprise.ai.model;

import lombok.Data;

/**
 * latest conversation.
 */
@Data
public class LatestConversation {

    private boolean firstTimeUsed;

    private String latestConversationId;

    public LatestConversation(boolean firstTimeUsed, String latestConversationId) {
        this.firstTimeUsed = firstTimeUsed;
        this.latestConversationId = latestConversationId;
    }
}
