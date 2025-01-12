package com.apitable.enterprise.ai.scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import com.apitable.enterprise.AbstractApitableSaasIntegrationTest;
import com.apitable.enterprise.ai.entity.AiCreditTransactionEntity;
import com.apitable.enterprise.ai.model.CreditTransactionType;
import com.apitable.interfaces.ai.model.ChartTimeDimension;
import com.apitable.interfaces.ai.model.CreditTransactionChartData;
import com.apitable.shared.clock.spring.ClockManager;
import com.apitable.shared.util.IdUtil;
import com.apitable.workspace.enums.NodeType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

public class CreditSummaryTaskTest extends AbstractApitableSaasIntegrationTest {

    @Test
    void testSummary() {
        final OffsetDateTime
            initialCreateDate = OffsetDateTime.of(2023, 8, 19, 10, 15, 47, 0, ZoneOffset.UTC);
        getClock().setTime(initialCreateDate);
        Random random = new Random();
        // random space id
        String spaceId = String.format("spc%s", random.nextInt(11));
        // create difference date mock data
        createMockData(spaceId, LocalDateTime.of(LocalDate.of(2023, 6, 15), LocalTime.of(random.nextInt(11), random.nextInt(59), random.nextInt(60))), 2);
        createMockData(spaceId, LocalDateTime.of(LocalDate.of(2023, 6, 16), LocalTime.of(random.nextInt(11), random.nextInt(59), random.nextInt(60))), 2);
        createMockData(spaceId, LocalDateTime.of(LocalDate.of(2023, 7, 15), LocalTime.of(random.nextInt(11), random.nextInt(59), random.nextInt(60))), 2);
        createMockData(spaceId, LocalDateTime.of(LocalDate.of(2023, 7, 16), LocalTime.of(random.nextInt(11), random.nextInt(59), random.nextInt(60))), 2);
        createMockData(spaceId, LocalDateTime.of(LocalDate.of(2023, 8, 15), LocalTime.of(random.nextInt(11), random.nextInt(59), random.nextInt(60))), 2);
        createMockData(spaceId, LocalDateTime.of(LocalDate.of(2023, 8, 16), LocalTime.of(random.nextInt(11), random.nextInt(59), random.nextInt(60))), 2);
        // create mock data on now
        LocalDateTime currentDateTime = ClockManager.me().getLocalDateTimeNow();
        createMockData(spaceId, currentDateTime, 1);

        // execute summary
        creditSummaryTask.summary();
        // check today dimension
        List<CreditTransactionChartData> todayChartData =
            iAiCreditTransactionOverallService.summary(spaceId, ChartTimeDimension.TODAY);
        int hour = currentDateTime.getHour();
        assertThat(todayChartData).isNotEmpty().hasSize(hour + 1);
        // check week dimension
        List<CreditTransactionChartData> weekChartData =
            iAiCreditTransactionOverallService.summary(spaceId, ChartTimeDimension.WEEKDAY);
        assertThat(weekChartData).isNotEmpty().hasSize(7);
        // check month dimension
        List<CreditTransactionChartData> monthChartData =
            iAiCreditTransactionOverallService.summary(spaceId, ChartTimeDimension.MONTH);
        int dayOfMonth = currentDateTime.getDayOfMonth();
        assertThat(monthChartData).isNotEmpty().hasSize(dayOfMonth);
        // check year dimension
        List<CreditTransactionChartData> yearChartData =
            iAiCreditTransactionOverallService.summary(spaceId, ChartTimeDimension.YEAR);
        int months = currentDateTime.getMonthValue();
        assertThat(yearChartData).isNotEmpty().hasSize(months);
    }

    private void createMockData(String spaceId, LocalDateTime createdDateTime, int randomCounts) {
        Random random = new Random();
        List<AiCreditTransactionEntity> entities = new ArrayList<>();
        for (int i = 0; i < randomCounts; i++) {
            AiCreditTransactionEntity entity = new AiCreditTransactionEntity();
            entity.setSpaceId(spaceId);
            entity.setAiId(IdUtil.createNodeId(NodeType.AI_CHAT_BOT));
            entity.setUserId(random.nextLong());
            entity.setAmount(
                BigDecimal.valueOf(random.nextDouble() * 100)
                    .setScale(4, RoundingMode.HALF_UP));
            entity.setTransactionType(CreditTransactionType.QUERY.getValue());
            entity.setCreatedAt(createdDateTime);
            entities.add(entity);
        }
        iAiCreditTransactionService.saveBatch(entities);
    }
}
