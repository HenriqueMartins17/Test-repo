package com.apitable.enterprise.ai.server.model;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Chat completion request object.
 */
@Getter
@Setter
public class ChatCompletion {

    private final String conversationId;

    private final List<Message> messages;

    private final boolean stream = true;

    public ChatCompletion(String conversationId, String content) {
        this.conversationId = conversationId;
        this.messages = Collections.singletonList(new Message(Role.USER, content));
    }

    @Getter
    @Setter
    static class Message {

        private Role role;

        private String content;

        public Message(Role role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    @AllArgsConstructor
    enum Role {

        USER("user");

        private final String value;

        @JsonValue
        public String getValue() {
            return value;
        }
    }
}
