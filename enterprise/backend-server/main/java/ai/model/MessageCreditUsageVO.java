package com.apitable.enterprise.ai.model;

import lombok.Data;

/**
 * message credit usage view.
 */
@Data
public class MessageCreditUsageVO {

    private MessageCreditLimit messageCreditLimit;

    public MessageCreditUsageVO(MessageCreditLimit messageCreditLimit) {
        this.messageCreditLimit = messageCreditLimit;
    }
}
