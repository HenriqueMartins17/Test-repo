package com.apitable.enterprise.ai.queue;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * training message object in queue.
 */
@Data
@NoArgsConstructor
public class TrainingMessage {

    private String aiId;

    private String trainingId;

    private Long userId;

    /**
     * constructor.
     *
     * @param aiId       ai id
     * @param trainingId training id
     * @param userId     user id
     */
    public TrainingMessage(String aiId, String trainingId, Long userId) {
        this.aiId = aiId;
        this.trainingId = trainingId;
        this.userId = userId;
    }
}
