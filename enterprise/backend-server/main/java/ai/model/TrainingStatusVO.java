package com.apitable.enterprise.ai.model;

import lombok.Data;

/**
 * Training status view.
 */
@Data
public class TrainingStatusVO {

    private TrainingStatus status;

    public TrainingStatusVO(TrainingStatus status) {
        this.status = status;
    }
}
