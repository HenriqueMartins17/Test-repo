package com.apitable.enterprise.ai.credit;

import com.apitable.enterprise.ai.model.AiModel;
import lombok.Data;

/**
 * Embedding Text.
 */
@Data
public class EmbeddingText {

    private AiModel model;

    private long chars;

    private long tokens;

    public EmbeddingText(AiModel model, long chars, long tokens) {
        this.model = model;
        this.chars = chars;
        this.tokens = tokens;
    }
}
