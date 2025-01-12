package com.apitable.enterprise.ai.model;

import com.apitable.enterprise.ai.server.model.CopilotAssistantType;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;

/**
 * copilot assistant type deserializer.
 */
public class CopilotAssistantTypeDeserializer extends JsonDeserializer<CopilotAssistantType> {

    @Override
    public CopilotAssistantType deserialize(JsonParser p, DeserializationContext context)
        throws IOException {
        String value = p.getValueAsString();

        try {
            return CopilotAssistantType.of(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
