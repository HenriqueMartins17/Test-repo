package com.apitable.enterprise.airagent.model.billing;

import com.apitable.shared.support.serializer.CreditUnitSerializer;
import com.apitable.shared.support.serializer.NullNumberSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.math.BigDecimal;
import lombok.Data;

/**
 * subscription info.
 */
@Data
public class Subscription {

    private String plan;

    @JsonSerialize(nullsUsing = NullNumberSerializer.class)
    private Long numOfAgents;

    @JsonSerialize(using = CreditUnitSerializer.class)
    private BigDecimal numOfCredits;
}
