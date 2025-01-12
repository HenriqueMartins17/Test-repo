package com.apitable.enterprise.ai.service;

import com.apitable.enterprise.ai.model.AiModel;
import com.apitable.enterprise.ai.model.MessageCreditLimit;

/**
 * AI credit service.
 */
public interface IAiCreditService {

    /**
     * get message credit limit.
     *
     * @param spaceId space id
     * @param aiModel ai model
     * @return MessageCreditLimit
     */
    MessageCreditLimit getMessageCreditLimit(String spaceId, AiModel aiModel);

    /**
     * check chat credit.
     *
     * @param spaceId space id
     * @param aiModel ai model
     */
    void checkChatCredit(String spaceId, AiModel aiModel);
}
