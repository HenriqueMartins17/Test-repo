package com.apitable.enterprise.ai.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.apitable.enterprise.AbstractApitableSaasIntegrationTest;
import com.apitable.enterprise.ai.entity.AiCreditTransactionOverallEntity;
import com.apitable.enterprise.ai.model.CreditSummaryPeriod;
import com.apitable.interfaces.ai.model.ChartTimeDimension;
import com.apitable.interfaces.ai.model.CreditTransactionChartData;
import com.apitable.mock.bean.MockUserSpace;
import com.apitable.shared.clock.spring.ClockManager;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

public class AiCreditTransactionOverallServiceImplTest extends AbstractApitableSaasIntegrationTest {

    private MockData setUp() {
        MockData mockData = new MockData();
        mockData.mockUserSpace = createSingleUserAndSpace();
        List<AiCreditTransactionOverallEntity> entities = new ArrayList<>();
        // insert day period overall data
        mockData.now = ClockManager.me().getLocalDateTimeNow();
        // insert month period overall data
        Random random = new Random();
        for (int day = 1; day <= mockData.now.getDayOfMonth(); day++) {
            AiCreditTransactionOverallEntity entity = new AiCreditTransactionOverallEntity();
            entity.setSpaceId(mockData.mockUserSpace.getSpaceId());
            entity.setPeriod(CreditSummaryPeriod.DAY.getValue());
            LocalDate date = LocalDate.of(mockData.now.getYear(), mockData.now.getMonth(), day);
            entity.setStartDate(LocalDateTime.of(date, LocalTime.MIN));
            entity.setEndDate(LocalDateTime.of(date, LocalTime.MIN));
            entity.setTotalAmount(
                BigDecimal.valueOf(random.nextDouble() * 100).setScale(4, RoundingMode.HALF_UP));
            entity.setAverageAmount(
                BigDecimal.valueOf(random.nextDouble() * 100).setScale(4, RoundingMode.HALF_UP));
            entities.add(entity);
        }

        // insert year period overall data
        for (int month = 1; month <= mockData.now.getMonthValue(); month++) {
            AiCreditTransactionOverallEntity entity = new AiCreditTransactionOverallEntity();
            entity.setSpaceId(mockData.mockUserSpace.getSpaceId());
            entity.setPeriod(CreditSummaryPeriod.MONTH.getValue());
            LocalDate startDate = LocalDate.of(mockData.now.getYear(), month, 1);
            entity.setStartDate(LocalDateTime.of(startDate, LocalTime.MIN));
            LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
            entity.setEndDate(LocalDateTime.of(endDate, LocalTime.MAX));
            entity.setTotalAmount(
                BigDecimal.valueOf(random.nextDouble() * 100).setScale(4, RoundingMode.HALF_UP));
            entity.setAverageAmount(
                BigDecimal.valueOf(random.nextDouble() * 100).setScale(4, RoundingMode.HALF_UP));
            entities.add(entity);
        }

        iAiCreditTransactionOverallService.saveBatch(entities);

        return mockData;
    }

    @Test
    void testSummaryWithWeekday() {
        MockData mockData = setUp();
        List<CreditTransactionChartData> chartData =
            iAiCreditTransactionOverallService.summary(mockData.mockUserSpace.getSpaceId(),
                ChartTimeDimension.WEEKDAY);
        assertThat(chartData).isNotEmpty().hasSize(7);
    }

    @Test
    void testSummaryWithThisMonth() {
        MockData mockData = setUp();
        List<CreditTransactionChartData> chartData =
            iAiCreditTransactionOverallService.summary(mockData.mockUserSpace.getSpaceId(),
                ChartTimeDimension.MONTH);
        assertThat(chartData).isNotEmpty().hasSize(mockData.now.getDayOfMonth());
    }

    @Test
    void testSummaryWithThisYear() {
        MockData mockData = setUp();
        List<CreditTransactionChartData> chartData =
            iAiCreditTransactionOverallService.summary(mockData.mockUserSpace.getSpaceId(),
                ChartTimeDimension.YEAR);
        assertThat(chartData).isNotEmpty().hasSize(mockData.now.getMonthValue());
    }

    static class MockData {
        MockUserSpace mockUserSpace;
        LocalDateTime now;
    }
}
