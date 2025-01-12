package com.apitable.enterprise.ai.model;

import lombok.Data;

/**
 * Training Predict result view.
 */
@Data
public class TrainingPredictResult {

    private Number characters;
    private Number words;
    private Number creditCost;

    public TrainingPredictResult(Number characters, Number words, Number creditCost) {
        this.characters = characters;
        this.words = words;
        this.creditCost = creditCost;
    }
}
