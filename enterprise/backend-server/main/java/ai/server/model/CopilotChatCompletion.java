package com.apitable.enterprise.ai.server.model;

import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * copilot chat completion model.
 */
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class CopilotChatCompletion {

    private CopilotAssistantType assistantType;

    private String conversationId;

    private List<Message> messages;

    private final boolean stream = true;

    private CopilotChatMetadata meta;

    public CopilotChatCompletion(CopilotAssistantType assistantType, String conversationId,
                                 String content,
                                 CopilotChatMetadata meta) {
        this.assistantType = assistantType;
        this.conversationId = conversationId;
        this.messages = Collections.singletonList(new Message(CopilotChatUser.USER, content));
        this.meta = meta;
    }

    @Getter
    @Setter
    static class Message {

        private CopilotChatUser role;

        private String content;

        public Message(CopilotChatUser role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}
