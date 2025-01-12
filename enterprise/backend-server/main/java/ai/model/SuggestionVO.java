package com.apitable.enterprise.ai.model;

import java.util.List;
import lombok.Data;

/**
 * Suggestion view.
 */
@Data
public class SuggestionVO {

    private String aiId;
    private String trainingId;
    private String conversationId;
    private List<String> suggestions;

    public SuggestionVO(String aiId, String trainingId, String conversationId,
                        List<String> suggestions) {
        this.aiId = aiId;
        this.trainingId = trainingId;
        this.conversationId = conversationId;
        this.suggestions = suggestions;
    }
}
