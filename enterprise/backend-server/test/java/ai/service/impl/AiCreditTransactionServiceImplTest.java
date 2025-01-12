package com.apitable.enterprise.ai.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.apitable.enterprise.AbstractApitableSaasIntegrationTest;
import com.apitable.enterprise.ai.entity.AiCreditTransactionEntity;
import com.apitable.enterprise.ai.model.CreditTransactionType;
import com.apitable.interfaces.ai.model.CreditTransactionChartData;
import com.apitable.mock.bean.MockUserSpace;
import com.apitable.shared.clock.spring.ClockManager;
import com.apitable.shared.util.IdUtil;
import com.apitable.workspace.enums.NodeType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

public class AiCreditTransactionServiceImplTest extends AbstractApitableSaasIntegrationTest {

    private void insertTransactionData(MockUserSpace userSpace, LocalDate beginDate,
                                       LocalDate endDate, int dataLength) {
        List<AiCreditTransactionEntity> entities = new ArrayList<>();
        long daysBetween = ChronoUnit.DAYS.between(beginDate, endDate);
        Random random = new Random();
        for (int i = 0; i < dataLength; i++) {
            AiCreditTransactionEntity entity = new AiCreditTransactionEntity();
            entity.setSpaceId(userSpace.getSpaceId());
            entity.setAiId(IdUtil.createNodeId(NodeType.AI_CHAT_BOT));
            entity.setUserId(userSpace.getUserId());
            entity.setAmount(
                BigDecimal.valueOf(1.0001).setScale(4, RoundingMode.HALF_UP));
            entity.setTransactionType(CreditTransactionType.QUERY.getValue());
            long randomDaysOffset = random.nextInt((int) daysBetween + 1);
            LocalDate date = beginDate.plusDays(randomDaysOffset);
            LocalTime time = LocalTime.of(i, random.nextInt(59), random.nextInt(60));
            entity.setCreatedAt(LocalDateTime.of(date, time));
            entities.add(entity);
        }
        iAiCreditTransactionService.saveBatch(entities);
    }

    @Test
    void testGetTotalCreditTransactionCount() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        int year = 2023;
        int monthOfYear = 5;
        // insert previous month transactions
        LocalDate previousDate = LocalDate.of(year, monthOfYear, 31);
        insertTransactionData(userSpace, previousDate, LocalDate.of(year, monthOfYear + 1, 28), 24);

        // get total count
        LocalDate currentDate = LocalDate.of(year, monthOfYear + 1, 29);
        BigDecimal total = iAiCreditTransactionService
            .countDateRangeAmount(userSpace.getSpaceId(), previousDate, currentDate);
        assertThat(total).isNotNull().isEqualTo(BigDecimal.valueOf(24.0024));
    }

    @Test
    void testGetTotalCreditTransactionCountShouldZero() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        int year = 2023;
        int monthOfYear = 5;
        // insert previous month transactions
        LocalDate previousDate = LocalDate.of(year, monthOfYear, 31);
        insertTransactionData(userSpace, previousDate, LocalDate.of(year, monthOfYear + 1, 28), 24);

        // get total count
        LocalDate startDate = LocalDate.of(year, monthOfYear + 2, 1);
        LocalDate currentDate = LocalDate.of(year, monthOfYear + 2, 10);
        BigDecimal total = iAiCreditTransactionService
            .countDateRangeAmount(userSpace.getSpaceId(), startDate, currentDate);
        assertThat(total).isNotNull().isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void testGetTotalCreditTransactionCountOnlyThisMonth() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        int year = 2023;
        int monthOfYear = 5;
        // insert previous month transactions
        LocalDate previousDate = LocalDate.of(year, monthOfYear, 31);
        insertTransactionData(userSpace, previousDate, LocalDate.of(year, monthOfYear + 1, 28), 24);

        // get total count
        LocalDate currentDate = LocalDate.of(year, monthOfYear + 2, 1);
        LocalDate endDate = LocalDate.of(year, monthOfYear + 2, 20);
        insertTransactionData(userSpace, currentDate, endDate, 11);

        BigDecimal total = iAiCreditTransactionService
            .countDateRangeAmount(userSpace.getSpaceId(), currentDate, endDate);
        assertThat(total).isNotNull().isEqualTo(BigDecimal.valueOf(11.0011));
    }

    @Test
    void testSummaryToday() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        // mock data
        List<AiCreditTransactionEntity> entities = new ArrayList<>();
        LocalDate date = ClockManager.me().getLocalDateNow();
        Random random = new Random();
        for (int i = 0; i < 24; i++) {
            AiCreditTransactionEntity entity = new AiCreditTransactionEntity();
            entity.setSpaceId(userSpace.getSpaceId());
            entity.setAiId(IdUtil.createNodeId(NodeType.AI_CHAT_BOT));
            entity.setUserId(userSpace.getUserId());
            entity.setAmount(
                BigDecimal.valueOf(random.nextDouble() * 100).setScale(4, RoundingMode.HALF_UP));
            entity.setTransactionType(CreditTransactionType.QUERY.getValue());
            LocalTime time = LocalTime.of(i, random.nextInt(59), random.nextInt(60));
            entity.setCreatedAt(LocalDateTime.of(date, time));
            entities.add(entity);
        }
        iAiCreditTransactionService.saveBatch(entities);
        // summary
        List<CreditTransactionChartData> chartData = iAiCreditTransactionService.summaryToday(
            userSpace.getSpaceId(), date);
        assertThat(chartData).isNotEmpty().hasSize(24);
    }
}
