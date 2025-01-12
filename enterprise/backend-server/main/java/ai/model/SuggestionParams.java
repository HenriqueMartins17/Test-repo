package com.apitable.enterprise.ai.model;

import lombok.Data;

/**
 * Suggestion params.
 */
@Data
@SuppressWarnings("checkstyle:MemberName")
public class SuggestionParams {

    private String trainingId;
    private String conversationId;
    private String question;
    private int n = 3;
}
