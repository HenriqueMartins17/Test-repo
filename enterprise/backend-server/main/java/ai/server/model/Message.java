package com.apitable.enterprise.ai.server.model;

import lombok.Data;

/**
 * message.
 */
@Data
public class Message {

    private MessageType type;

    private MessageItem data;
}
