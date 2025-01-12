package com.apitable.enterprise.ai.server.model;

import lombok.Data;

/**
 * post train result.
 */
@Data
public class PostTrainResult {

    private String newTrainingId;

    private String aiBotType;

    private AiInfo aiInfo;
}
