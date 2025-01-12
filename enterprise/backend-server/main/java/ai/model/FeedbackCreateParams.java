package com.apitable.enterprise.ai.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Feedback Creates Param.
 */
@Data
public class FeedbackCreateParams {

    @NotBlank(message = "aiId is required")
    private String aiId;
    private String trainingId;
    private String conversationId;
    private Integer like;
    private Integer messageIndex;
    private String comment;
}
