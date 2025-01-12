package com.apitable.enterprise.ai.model;

import java.math.BigDecimal;
import lombok.Data;

/**
 * credit amount summary.
 *
 * @author Shawn Deng
 */
@Data
public class CreditAmountSummary {

    private String spaceId;

    private BigDecimal totalAmount;

    private BigDecimal averageAmount;

    public CreditAmountSummary() {
    }

    public CreditAmountSummary(String spaceId, BigDecimal totalAmount, BigDecimal averageAmount) {
        this.spaceId = spaceId;
        this.totalAmount = totalAmount;
        this.averageAmount = averageAmount;
    }
}
