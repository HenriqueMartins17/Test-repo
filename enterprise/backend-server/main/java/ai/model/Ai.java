package com.apitable.enterprise.ai.model;

import com.apitable.shared.support.serializer.NullBooleanSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI Object class.
 */
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Ai {

    private String id;
    private String name;
    private String picture;
    private String description;

    private boolean livemode;
    private MessageCreditLimit messageCreditLimit;

    private String type;

    @JsonSerialize(nullsUsing = NullBooleanSerializer.class)
    private Boolean isTrained;
    private String latestTrainingId;
    private TrainingStatus latestTrainingStatus;
    private Long latestTrainingCompletedAt;
    private String currentConversationId;
    private Long currentConversationCreatedAt;
    private Long created;

    private Map<String, Object> setting;

    private boolean dataSourcesUpdated;
    private List<AiTrainingDataSource> dataSources;
}
