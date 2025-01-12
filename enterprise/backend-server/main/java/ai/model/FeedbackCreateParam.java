package com.apitable.enterprise.ai.model;

import lombok.Data;

/**
 * Feedback Creates Param.
 */
@Data
@Deprecated(since = "1.8.0", forRemoval = true)
public class FeedbackCreateParam {

    private String trainingId;
    private String conversationId;
    private Integer like;
    private Integer messageIndex;
    private String comment;
}
