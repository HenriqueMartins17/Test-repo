package com.apitable.enterprise.ai.service.impl;

import com.apitable.core.exception.BusinessException;
import com.apitable.enterprise.ai.credit.CreditConverter;
import com.apitable.enterprise.ai.model.AiModel;
import com.apitable.enterprise.ai.model.MessageCreditLimit;
import com.apitable.enterprise.ai.service.IAiCreditService;
import com.apitable.interfaces.ai.model.CreditInfo;
import com.apitable.shared.exception.LimitException;
import com.apitable.space.service.ISpaceService;
import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

/**
 * AI credit service implementation.
 */
@Service
public class AiCreditServiceImpl implements IAiCreditService {

    @Resource
    private ISpaceService iSpaceService;

    @Override
    public MessageCreditLimit getMessageCreditLimit(String spaceId, AiModel aiModel) {
        CreditInfo creditInfo = iSpaceService.getCredit(spaceId);
        Long maxMessageCredits = creditInfo.getMaxMessageCredits();
        BigDecimal remainCredit = creditInfo.remainCredit();
        long remainChatTimes = 0L;
        if (aiModel != null) {
            BigDecimal creditPerQuery = CreditConverter.creditConsumedWithQuery(aiModel);
            remainChatTimes =
                remainCredit.divide(creditPerQuery, 0, RoundingMode.HALF_DOWN).longValue();
        }
        return new MessageCreditLimit(remainCredit, maxMessageCredits, remainChatTimes);
    }

    private boolean chatIntercept(String spaceId, AiModel aiModel) {
        CreditInfo creditInfo = iSpaceService.getCredit(spaceId);
        BigDecimal creditChatConsumed =
            CreditConverter.creditConsumedWithQuery(aiModel);
        return creditInfo.canConsume(creditChatConsumed);
    }

    @Override
    public void checkChatCredit(String spaceId, AiModel aiModel) {
        boolean canConsume = chatIntercept(spaceId, aiModel);
        if (!canConsume) {
            throw new BusinessException(LimitException.OVER_LIMIT);
        }
    }
}
