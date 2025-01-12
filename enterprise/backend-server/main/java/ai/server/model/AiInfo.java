package com.apitable.enterprise.ai.server.model;

import com.apitable.enterprise.ai.model.TrainingStatus;
import java.util.List;
import lombok.Data;

/**
 * ai info.
 */
@Data
public class AiInfo {

    private String aiId;
    private String currentTrainingId;
    private TrainingInfo currentTrainingInfo;
    private String lockingTrainingId;
    private TrainingInfo lockingTrainingInfo;
    private List<String> successTrainHistory;

    public boolean isNull() {
        return this.currentTrainingId == null && this.lockingTrainingId == null;
    }

    public boolean isTrainRunning() {
        return this.lockingTrainingId != null && this.lockingTrainingInfo != null;
    }

    public boolean hasTrained() {
        return this.successTrainHistory != null && !this.successTrainHistory.isEmpty();
    }

    public Training currentTrainingInfo() {
        return this.currentTrainingId != null && this.currentTrainingInfo != null
            ? new Training(this.currentTrainingInfo) : null;
    }

    /**
     * get latest success training info.
     *
     * @return latest success training info.
     */
    public Training latestSuccessTraining() {
        if (this.hasTrained()) {
            return this.currentTrainingInfo();
        }
        return null;
    }

    /**
     * get latest training info.
     *
     * @return latest training info.
     */
    public Training latestTraining() {
        if (this.isTrainRunning()) {
            return new Training(this.lockingTrainingInfo);
        }
        return this.currentTrainingInfo();
    }

    /**
     * get latest training status.
     *
     * @return latest training status
     */
    public TrainingStatus latestTrainingStatus() {
        if (this.isTrainRunning()) {
            return TrainingStatus.TRAINING;
        }
        Training training = this.currentTrainingInfo();
        if (training != null) {
            return training.getStatus();
        }
        throw new RuntimeException(
            String.format("no training info: %s", this.currentTrainingInfo));
    }
}
