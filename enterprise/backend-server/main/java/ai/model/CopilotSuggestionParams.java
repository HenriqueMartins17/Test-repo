package com.apitable.enterprise.ai.model;

import com.apitable.enterprise.ai.server.model.CopilotAssistantType;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

/**
 * Suggestion params.
 */
@Data
public class CopilotSuggestionParams {

    @JsonDeserialize(using = CopilotAssistantTypeDeserializer.class)
    private CopilotAssistantType type;
}
