package com.apitable.enterprise.ai.mapper;

import com.apitable.enterprise.ai.entity.AiCreditTransactionOverallEntity;
import com.apitable.enterprise.ai.model.CreditAmountSummary;
import com.apitable.enterprise.ai.model.CreditSummaryPeriod;
import com.apitable.interfaces.ai.model.CreditTransactionChartData;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Param;

/**
 * ai credit transaction overall mapper.
 *
 * @author Shawn Deng
 */
public interface AiCreditTransactionOverallMapper
    extends BaseMapper<AiCreditTransactionOverallEntity> {

    /**
     * insert batch.
     *
     * @param entities entity list
     * @return affected rows
     */
    int insertBatch(@Param("entities") List<AiCreditTransactionOverallEntity> entities);

    /**
     * select by space id and period.
     *
     * @param spaceId   space id
     * @param period    period
     * @param startDate start date
     * @return entity
     */
    AiCreditTransactionOverallEntity selectBySpaceIdAndPeriod(@Param("spaceId") String spaceId,
                                                              @Param("period") String period,
                                                              @Param("startDate")
                                                              LocalDate startDate);

    /**
     * select by space id and period.
     *
     * @param spaceId   space id
     * @param period    period
     * @param startDate start date
     * @return entity
     */
    List<AiCreditTransactionOverallEntity> selectBySpaceIdAndStartDate(
        @Param("spaceId") String spaceId,
        @Param("period") String period,
        @Param("startDate") LocalDate startDate);

    /**
     * summary this weekday.
     *
     * @param spaceId     space id
     * @param currentDate current date
     * @return list of chart data
     */
    List<CreditTransactionChartData> summaryThisWeekday(@Param("spaceId") String spaceId,
                                                        @Param("currentDate")
                                                        LocalDate currentDate);

    /**
     * summary this month.
     *
     * @param spaceId     space id
     * @param currentDate current date
     * @return list of chart data
     */
    List<CreditTransactionChartData> summaryThisMonth(@Param("spaceId") String spaceId,
                                                      @Param("currentDate") LocalDate currentDate);

    /**
     * summary this year.
     *
     * @param spaceId     space id
     * @param currentDate current date
     * @return list of chart data
     */
    List<CreditTransactionChartData> summaryThisYear(@Param("spaceId") String spaceId,
                                                     @Param("currentDate") LocalDate currentDate);

    /**
     * summary by space id and date.
     *
     * @param startDate start date
     * @param endDate   end date
     * @return credit amount summary
     */
    List<CreditAmountSummary> summaryByDate(@Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

    /**
     * summary by space id and date.
     *
     * @param spaceId   space id
     * @param startDate start date
     * @param endDate   end date
     * @return credit amount summary
     */
    CreditAmountSummary summaryBySpaceIdAndDate(@Param("spaceId") String spaceId,
                                                @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);
}
