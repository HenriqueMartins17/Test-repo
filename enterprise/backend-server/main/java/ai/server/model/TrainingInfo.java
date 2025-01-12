package com.apitable.enterprise.ai.server.model;

import com.apitable.enterprise.ai.model.TrainingStatus;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.IOException;
import java.util.List;
import lombok.Data;

/**
 * training info.
 */
@Data
public class TrainingInfo {

    private String aiId;
    private String trainingId;

    @JsonDeserialize(using = TrainingStatusDeserializer.class)
    private TrainingStatus status;
    private String info;
    private List<DataSource> dataSources;
    private Long startedAt;
    private Long finishedAt;

    static class TrainingStatusDeserializer extends JsonDeserializer<TrainingStatus> {

        @Override
        public TrainingStatus deserialize(JsonParser p, DeserializationContext context)
            throws IOException {
            String value = p.getValueAsString();

            try {
                return TrainingStatus.of(value);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }
}
