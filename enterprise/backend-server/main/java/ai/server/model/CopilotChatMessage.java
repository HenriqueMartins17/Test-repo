package com.apitable.enterprise.ai.server.model;

import lombok.Data;

/**
 * message.
 */
@Data
public class CopilotChatMessage {

    private CopilotChatUser type;

    private CopilotMessageItem data;
}
