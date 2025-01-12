package com.apitable.enterprise.ai.service;

import com.apitable.enterprise.ai.entity.AiCreditTransactionEntity;
import com.apitable.enterprise.ai.model.AiModel;
import com.apitable.enterprise.ai.model.CreditAmountSummary;
import com.apitable.enterprise.ai.model.CreditTransaction;
import com.apitable.interfaces.ai.model.CreditTransactionChartData;
import com.baomidou.mybatisplus.extension.service.IService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * AI - Credit Transaction Service.
 * This service is used to record the credit transaction of AI.
 * The credit transaction is used to record the credit consumption of AI.
 * The credit consumption of AI is based on the number of messages sent by the user.
 * The number of messages sent by the user is based on the number of messages sent by the user.
 * </p>
 *
 * @author Shawn Deng
 */
public interface IAiCreditTransactionService extends IService<AiCreditTransactionEntity> {

    /**
     * check chat credit.
     *
     * @param aiId           ai id
     * @param conversationId conversation id
     * @param byWho          user id
     */
    void saveTransaction(String spaceId, String aiId, AiModel aiModel, String conversationId,
                         Long byWho);

    /**
     * get credit transaction by ai id and training id.
     *
     * @param aiId       ai id
     * @param trainingId training id
     * @return credit transaction entity
     */
    AiCreditTransactionEntity getTransactionByAiIdAndTrainingId(String aiId, String trainingId);

    /**
     * get total credit transaction count by conversation id.
     *
     * @param conversationId conversation id
     * @return total credit transaction count
     */
    BigDecimal getTotalCreditTransactionAmountByConversationId(String conversationId);

    /**
     * get total credit transaction count.
     *
     * @param spaceId   space id
     * @param beginDate begin date
     * @param endDate   end date
     * @return total credit transaction count
     */
    BigDecimal countDateRangeAmount(String spaceId, LocalDate beginDate,
                                    LocalDate endDate);

    /**
     * get credit transaction summary by date.
     *
     * @param date date
     * @return credit amount summary
     */
    List<CreditAmountSummary> getCreditTransactionAmountSummary(LocalDate date);

    /**
     * get one credit transaction summary by date.
     *
     * @param date date
     * @return credit amount summary
     */
    CreditAmountSummary getOneCreditTransactionAmountSummary(LocalDate date);


    /**
     * get last un counted space credit transaction.
     *
     * @return list of credit transaction
     */
    List<CreditTransaction> getLastUnCountSpaceCreditTransaction();

    /**
     * summary today.
     *
     * @param spaceId space id
     * @param date    date
     * @return list of chart data
     */
    List<CreditTransactionChartData> summaryToday(@Param("spaceId") String spaceId,
                                                  @Param("date") LocalDate date);
}
