package com.apitable.enterprise.ai.server.model;

import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * message item.
 */
@Data
public class CopilotMessageItem {

    private String id;
    private String conversationId;
    private String openaiMessageId;
    private String openaiThreadId;
    private String openaiRunId;
    private String openaiAssistantId;
    private List<String> fileIds;
    private String content;
    private Map<String, Object> additionalKwargs;
    private Long createdAt;
}
