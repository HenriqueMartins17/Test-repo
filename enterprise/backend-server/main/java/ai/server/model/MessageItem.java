package com.apitable.enterprise.ai.server.model;

import java.util.Map;
import lombok.Data;

/**
 * message item.
 */
@Data
public class MessageItem {

    private String content;
    private Map<String, Object> additionalKwargs;
    private boolean example;
}
