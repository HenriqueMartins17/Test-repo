package com.apitable.enterprise.ai.server.model;

import lombok.Data;

/**
 * copilot chat metadata
 */
@Data
public class CopilotChatMetadata {

    private String datasheetId;

    private String viewId;

    public CopilotChatMetadata(String datasheetId, String viewId) {
        this.datasheetId = datasheetId;
        this.viewId = viewId;
    }
}
