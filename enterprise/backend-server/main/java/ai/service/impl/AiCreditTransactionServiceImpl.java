package com.apitable.enterprise.ai.service.impl;

import com.apitable.enterprise.ai.credit.CreditConverter;
import com.apitable.enterprise.ai.entity.AiCreditTransactionEntity;
import com.apitable.enterprise.ai.mapper.AiCreditTransactionMapper;
import com.apitable.enterprise.ai.model.AiModel;
import com.apitable.enterprise.ai.model.CreditAmountSummary;
import com.apitable.enterprise.ai.model.CreditTransaction;
import com.apitable.enterprise.ai.model.TransactionType;
import com.apitable.enterprise.ai.service.IAiCreditTransactionService;
import com.apitable.interfaces.ai.model.CreditTransactionChartData;
import com.apitable.shared.clock.spring.ClockManager;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * <p>
 * AI - Credit Transaction Service.
 * </p>
 *
 * @author Shawn Deng
 */
@Service
public class AiCreditTransactionServiceImpl
    extends ServiceImpl<AiCreditTransactionMapper, AiCreditTransactionEntity>
    implements IAiCreditTransactionService {

    @Override
    public void saveTransaction(String spaceId, String aiId, AiModel aiModel, String conversationId,
                                Long byWho) {
        BigDecimal creditConsumed =
            CreditConverter.creditConsumedWithQuery(aiModel);
        AiCreditTransactionEntity transactionEntity = new AiCreditTransactionEntity();
        transactionEntity.setSpaceId(spaceId);
        transactionEntity.setAiId(aiId);
        transactionEntity.setConversationId(conversationId);
        transactionEntity.setTransactionType(TransactionType.QUERY.getValue());
        transactionEntity.setAmount(creditConsumed);
        transactionEntity.setUserId(byWho);
        transactionEntity.setCreatedAt(ClockManager.me().getLocalDateTimeNow());
        save(transactionEntity);
    }

    @Override
    public AiCreditTransactionEntity getTransactionByAiIdAndTrainingId(String aiId,
                                                                       String trainingId) {
        QueryWrapper<AiCreditTransactionEntity> queryWrapper =
            new QueryWrapper<AiCreditTransactionEntity>()
                .eq("ai_id", aiId)
                .eq("training_id", trainingId);
        return getOne(queryWrapper, false);
    }

    @Override
    public BigDecimal getTotalCreditTransactionAmountByConversationId(String conversationId) {
        BigDecimal count =
            baseMapper.selectTotalCreditTransactionCountByConversationId(conversationId);
        return (null == count) ? BigDecimal.ZERO : count;
    }

    @Override
    public BigDecimal countDateRangeAmount(String spaceId, LocalDate beginDate,
                                           LocalDate endDate) {
        // calculate with cycle date
        BigDecimal count =
            baseMapper.selectTotalCreditTransactionCount(spaceId, beginDate, endDate);
        return (null == count) ? BigDecimal.ZERO : count;
    }

    @Override
    public List<CreditAmountSummary> getCreditTransactionAmountSummary(LocalDate date) {
        return baseMapper.selectCreditSummaryByDate(date);
    }

    @Override
    public CreditAmountSummary getOneCreditTransactionAmountSummary(LocalDate date) {
        return baseMapper.selectCreditSummaryByDateLimitOne(date);
    }

    @Override
    public List<CreditTransaction> getLastUnCountSpaceCreditTransaction() {
        String spaceId = baseMapper.selectLastUnCountedSpaceId();
        if (spaceId == null) {
            return Collections.emptyList();
        }
        return baseMapper.selectBySpaceIdWhereUnCounted(spaceId);
    }

    @Override
    public List<CreditTransactionChartData> summaryToday(String spaceId, LocalDate date) {
        return baseMapper.summaryToday(spaceId, date);
    }
}
