package com.apitable.enterprise.ai.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * credit transaction.
 */
@Data
public class CreditTransaction {

    private Long id;

    private String spaceId;

    private BigDecimal amount;

    private LocalDateTime createdAt;

}
