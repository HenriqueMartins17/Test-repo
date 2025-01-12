package com.apitable.enterprise.ai.service;

import com.apitable.enterprise.ai.entity.AiCreditTransactionOverallEntity;
import com.apitable.enterprise.ai.model.CreditAmountSummary;
import com.apitable.enterprise.ai.model.CreditSummaryPeriod;
import com.apitable.interfaces.ai.model.ChartTimeDimension;
import com.apitable.interfaces.ai.model.CreditTransactionChartData;
import com.baomidou.mybatisplus.extension.service.IService;
import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * AI - Credit Transaction Overall Service.
 * </p>
 *
 * @author Shawn Deng
 */
public interface IAiCreditTransactionOverallService
    extends IService<AiCreditTransactionOverallEntity> {

    /**
     * get by space id and period.
     *
     * @param spaceId   space id
     * @param period    period
     * @param startDate start date
     * @return entity
     */
    AiCreditTransactionOverallEntity getBySpaceIdAndPeriod(String spaceId,
                                                           CreditSummaryPeriod period,
                                                           LocalDate startDate);

    /**
     * get credit amount summary.
     *
     * @param startDate start date
     * @param endDate   end date
     * @return credit amount summary
     */
    List<CreditAmountSummary> getCreditTransactionAmountSummary(LocalDate startDate,
                                                                LocalDate endDate);

    /**
     * get credit amount summary.
     *
     * @param spaceId   space id
     * @param startDate start date
     * @param endDate   end date
     * @return credit amount summary
     */
    CreditAmountSummary getCreditTransactionAmountSummary(String spaceId,
                                                          LocalDate startDate,
                                                          LocalDate endDate);

    /**
     * save day period.
     *
     * @param currentDate   current date
     * @param amountSummary amount summary
     */
    void saveDayPeriod(LocalDate currentDate, CreditAmountSummary amountSummary);

    /**
     * save month period.
     *
     * @param startDate     start date
     * @param endDate       end date
     * @param amountSummary amount summary
     */

    void saveMonthPeriod(LocalDate startDate, LocalDate endDate, CreditAmountSummary amountSummary);

    /**
     * batch save.
     *
     * @param entities entity list
     */
    void batchSave(List<AiCreditTransactionOverallEntity> entities);

    /**
     * summary with time dimension.
     *
     * @param spaceId       space id
     * @param timeDimension time dimension
     * @return list of chart data
     */
    List<CreditTransactionChartData> summary(
        String spaceId, ChartTimeDimension timeDimension);
}
