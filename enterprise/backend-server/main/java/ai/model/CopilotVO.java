package com.apitable.enterprise.ai.model;

import lombok.Data;

/**
 * AI info view.
 */
@Data
public class CopilotVO {

    private Copilot copilot;

    public CopilotVO(Copilot copilot) {
        this.copilot = copilot;
    }
}
