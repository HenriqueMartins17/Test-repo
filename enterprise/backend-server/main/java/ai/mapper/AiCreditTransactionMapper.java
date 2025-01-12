package com.apitable.enterprise.ai.mapper;

import com.apitable.enterprise.ai.entity.AiCreditTransactionEntity;
import com.apitable.enterprise.ai.model.CreditAmountSummary;
import com.apitable.enterprise.ai.model.CreditTransaction;
import com.apitable.interfaces.ai.model.CreditTransactionChartData;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * ai credit transaction mapper.
 *
 * @author Shawn Deng
 */
public interface AiCreditTransactionMapper extends BaseMapper<AiCreditTransactionEntity> {

    /**
     * count today total credit transaction.
     *
     * @param spaceId space id
     * @return total count
     */
    BigDecimal selectTotalCreditTransactionCount(@Param("spaceId") String spaceId,
                                                 @Param("beginDate") LocalDate beginDate,
                                                 @Param("endDate") LocalDate endDate);

    /**
     * count conversation total credit transaction.
     *
     * @param conversationId space id
     * @return total count
     */
    BigDecimal selectTotalCreditTransactionCountByConversationId(
        @Param("conversationId") String conversationId);

    /**
     * count the total credit transaction with date.
     *
     * @param date date
     * @return total count
     */
    List<CreditAmountSummary> selectCreditSummaryByDate(@Param("date") LocalDate date);

    /**
     * count the total credit transaction with date limit one row.
     *
     * @param date date
     * @return credit amount summary
     */
    CreditAmountSummary selectCreditSummaryByDateLimitOne(@Param("date") LocalDate date);

    /**
     * summary today.
     *
     * @param spaceId space id
     * @param today   today
     * @return list of chart data
     */
    List<CreditTransactionChartData> summaryToday(@Param("spaceId") String spaceId,
                                                  @Param("today") LocalDate today);

    /**
     * select last un counted space id.
     *
     * @return space id
     */
    String selectLastUnCountedSpaceId();

    /**
     * select by space id where un counted.
     *
     * @param spaceId space id
     * @return list of credit transaction
     */
    List<CreditTransaction> selectBySpaceIdWhereUnCounted(@Param("spaceId") String spaceId);
}
