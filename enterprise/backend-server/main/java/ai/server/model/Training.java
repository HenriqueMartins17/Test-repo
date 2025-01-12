package com.apitable.enterprise.ai.server.model;

import com.apitable.enterprise.ai.credit.CreditConverter;
import com.apitable.enterprise.ai.credit.EmbeddingText;
import com.apitable.enterprise.ai.model.AiModel;
import com.apitable.enterprise.ai.model.TrainingStatus;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

/**
 * training object.
 */
@Data
public class Training {

    private String id;

    private TrainingStatus status;

    private Long startedAtMillis;

    private Long completedAtMillis;

    private List<DataSource> dataSources;

    /**
     * constructor.
     *
     * @param delegate TrainingInfo Object
     */
    public Training(TrainingInfo delegate) {
        this.id = delegate.getTrainingId();
        this.status = delegate.getStatus();
        if (delegate.getStartedAt() != null) {
            this.startedAtMillis = delegate.getStartedAt() * 1000;
        }
        if (delegate.getFinishedAt() != null) {
            this.completedAtMillis = delegate.getFinishedAt() * 1000;
        }
        this.dataSources = delegate.getDataSources();
    }

    /**
     * get latest training chars.
     *
     * @return chars
     */
    public long trainingChars() {
        if (this.dataSources == null || this.dataSources.isEmpty()) {
            return 0L;
        }
        DataSource dataSource = this.dataSources.iterator().next();
        if (dataSource != null) {
            return dataSource.getCharacters();
        }
        return 0;
    }

    /**
     * get latest training tokens.
     *
     * @return chars
     */
    public long trainingTokens() {
        if (this.dataSources == null || this.dataSources.isEmpty()) {
            return 0L;
        }
        DataSource dataSource = this.dataSources.iterator().next();
        if (dataSource != null) {
            return dataSource.getTokens();
        }
        return 0;
    }

    public BigDecimal creditCost(AiModel aiModel) {
        if (this.dataSources == null || this.dataSources.isEmpty()) {
            return BigDecimal.ZERO;
        }
        if (aiModel == null) {
            return BigDecimal.ZERO;
        }
        DataSource dataSource = this.dataSources.iterator().next();
        if (dataSource != null) {
            EmbeddingText embeddingText =
                new EmbeddingText(aiModel, dataSource.getCharacters(), dataSource.getTokens());
            return CreditConverter.creditConsumedWithTraining(embeddingText);
        }
        return BigDecimal.ZERO;
    }
}
