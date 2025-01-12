package com.apitable.enterprise.ai.model;

import lombok.Data;

/**
 * AI info view.
 */
@Data
public class AiInfoVO {

    private Ai ai;

    public AiInfoVO(Ai ai) {
        this.ai = ai;
    }
}
