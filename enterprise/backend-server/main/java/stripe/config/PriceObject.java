package com.apitable.enterprise.stripe.config;

import lombok.Data;

/**
 * price model.
 */
@Data
@Deprecated(since = "1.7.0", forRemoval = true)
public class PriceObject {

    private String id;

    private String type;

    private String productId;

    private String recurringInterval;

    private long includeUnit;

    private long amount;

    private long perUnitAmount;

    private boolean trial;
}
