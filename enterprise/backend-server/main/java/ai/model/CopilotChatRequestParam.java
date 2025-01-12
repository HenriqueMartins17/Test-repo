package com.apitable.enterprise.ai.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * copilot chat request message param.
 */
@Data
@Schema(description = "Chat Completions of Copilot request body")
public class CopilotChatRequestParam {

    @Schema(description = "The conversation of chat", requiredMode = Schema.RequiredMode.AUTO)
    @NotBlank(message = "conversationId is required")
    private String conversationId;

    @Schema(description = "The contents of the user message", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    @Schema(description = "The datasheet id of the space", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String datasheetId;

    @Schema(description = "The view id of the datasheet", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String viewId;
}
