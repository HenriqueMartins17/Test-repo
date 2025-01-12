package com.apitable.enterprise.ai.model;

import com.apitable.enterprise.ai.server.model.DataSource;
import com.apitable.enterprise.ai.server.model.Training;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

/**
 * training info.
 */
@Data
public class TrainingInfoVO {

    private String aiId;
    private String trainingId;
    private TrainingStatus status;
    private List<DataSource> dataSources;
    private Long startedAt;
    private Long finishedAt;
    private Long characterSum;
    private Long tokensSum;
    private BigDecimal creditCost;

    public TrainingInfoVO(String aiId, AiModel aiModel, Training training) {
        this.aiId = aiId;
        this.trainingId = training.getId();
        this.status = training.getStatus();
        this.dataSources = training.getDataSources();
        this.startedAt = training.getStartedAtMillis();
        this.finishedAt = training.getCompletedAtMillis();
        this.characterSum = training.trainingChars();
        this.tokensSum = training.trainingTokens();
        this.creditCost = training.creditCost(aiModel);
    }
}
