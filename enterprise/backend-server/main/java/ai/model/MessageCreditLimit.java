package com.apitable.enterprise.ai.model;

import com.apitable.shared.support.serializer.CreditUnitSerializer;
import com.apitable.shared.support.serializer.NullNumberSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.math.BigDecimal;
import lombok.Data;

/**
 * AI Message Credit Limit View.
 */
@Data
public class MessageCreditLimit {

    @JsonSerialize(using = CreditUnitSerializer.class)
    private BigDecimal remainCreditNums;

    @JsonSerialize(nullsUsing = NullNumberSerializer.class)
    private Long maxCreditNums;

    @JsonSerialize(nullsUsing = NullNumberSerializer.class)
    private Long remainChatTimes;

    /**
     * Constructor.
     *
     * @param remainCreditNums remainCreditNums
     * @param maxCreditNums    maxCreditNums
     * @param remainChatTimes  remainChatTimes
     */
    public MessageCreditLimit(BigDecimal remainCreditNums, Long maxCreditNums,
                              Long remainChatTimes) {
        this.remainCreditNums = remainCreditNums;
        this.maxCreditNums = maxCreditNums;
        this.remainChatTimes = remainChatTimes;
    }
}
