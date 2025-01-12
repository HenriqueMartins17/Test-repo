package com.apitable.enterprise.ai.scheduler;

import com.apitable.enterprise.ai.entity.AiCreditTransactionEntity;
import com.apitable.enterprise.ai.entity.AiCreditTransactionOverallEntity;
import com.apitable.enterprise.ai.model.CreditAmountSummary;
import com.apitable.enterprise.ai.model.CreditSummaryPeriod;
import com.apitable.enterprise.ai.model.CreditTransaction;
import com.apitable.enterprise.ai.service.IAiCreditTransactionOverallService;
import com.apitable.enterprise.ai.service.IAiCreditTransactionService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * credit summary task.
 *
 * @author Shawn Deng
 */
@Component
@Slf4j
public class CreditSummaryTask {

    @Resource
    private IAiCreditTransactionService iAiCreditTransactionService;

    @Resource
    private IAiCreditTransactionOverallService iAiCreditTransactionOverallService;

    /**
     * count one by one.
     */
    @Transactional(rollbackFor = Exception.class)
    public void summary() {
        List<CreditTransaction> creditTransactions
            = iAiCreditTransactionService.getLastUnCountSpaceCreditTransaction();
        if (creditTransactions.isEmpty()) {
            log.info("no uncounted credit transaction");
            return;
        }
        // group by create at, create at transform to local date
        Map<LocalDate, List<CreditTransaction>> creditTransactionMap =
            creditTransactions.stream()
                .collect(Collectors.groupingBy(
                        creditTransaction -> creditTransaction.getCreatedAt().toLocalDate(),
                        TreeMap::new,
                        Collectors.toList()
                    )
                );

        creditTransactionMap.forEach(this::executeSummaryDayPeriodWithDate);

        // group by create at, create at transform to year month
        Map<YearMonth, List<CreditTransaction>> creditTransactionMonthMap =
            creditTransactions.stream()
                .collect(Collectors.groupingBy(
                        creditTransaction -> YearMonth.from(creditTransaction.getCreatedAt()),
                        TreeMap::new,
                        Collectors.toList()
                    )
                );

        creditTransactionMonthMap.forEach(this::executeSummaryMonthPeriodWithDate);

        // updated counted status
        List<Long> creditTransactionIds = creditTransactions.stream()
            .map(CreditTransaction::getId)
            .collect(Collectors.toList());
        setTransactionCounted(creditTransactionIds);
    }

    private void setTransactionCounted(List<Long> creditTransactionIds) {
        List<AiCreditTransactionEntity> updatedEntities = new ArrayList<>();
        creditTransactionIds.forEach(id -> {
            AiCreditTransactionEntity entity = new AiCreditTransactionEntity();
            entity.setId(id);
            entity.setIsCounted(true);
            updatedEntities.add(entity);
        });
        iAiCreditTransactionService.updateBatchById(updatedEntities);
    }

    private CreditAmountSummary createCreditAmountSummary(String spaceId,
                                                          List<CreditTransaction> transactions) {
        BigDecimal totalAmount = transactions.stream()
            .map(CreditTransaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal averageAmount = transactions.stream()
            .map(CreditTransaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(transactions.size()), 4, RoundingMode.HALF_UP);
        return new CreditAmountSummary(spaceId, totalAmount, averageAmount);
    }

    private void executeSummaryDayPeriodWithDate(LocalDate currentDate,
                                                 List<CreditTransaction> transactions) {
        String spaceId = transactions.iterator().next().getSpaceId();
        CreditAmountSummary amountSummary = createCreditAmountSummary(spaceId, transactions);
        // get all period transaction entity
        AiCreditTransactionOverallEntity overallEntity =
            iAiCreditTransactionOverallService.getBySpaceIdAndPeriod(spaceId,
                CreditSummaryPeriod.DAY, currentDate);
        if (overallEntity == null) {
            iAiCreditTransactionOverallService.saveDayPeriod(currentDate, amountSummary);
        } else {
            // update every period overall amount one by one
            AiCreditTransactionOverallEntity updatedEntity = new AiCreditTransactionOverallEntity();
            updatedEntity.setId(overallEntity.getId());
            updatedEntity.setTotalAmount(amountSummary.getTotalAmount());
            updatedEntity.setAverageAmount(amountSummary.getAverageAmount());
            iAiCreditTransactionOverallService.updateById(updatedEntity);
        }
    }

    private void executeSummaryMonthPeriodWithDate(YearMonth yearMonth,
                                                   List<CreditTransaction> transactions) {
        String spaceId = transactions.iterator().next().getSpaceId();
        CreditAmountSummary amountSummary = createCreditAmountSummary(spaceId, transactions);
        // get all period transaction entity
        LocalDate firstDayOfMonth = yearMonth.atDay(1);
        LocalDate lastDayOfMonth = yearMonth.atEndOfMonth();
        AiCreditTransactionOverallEntity overallEntity =
            iAiCreditTransactionOverallService.getBySpaceIdAndPeriod(spaceId,
                CreditSummaryPeriod.MONTH, firstDayOfMonth);
        if (overallEntity == null) {
            iAiCreditTransactionOverallService.saveMonthPeriod(firstDayOfMonth, lastDayOfMonth,
                amountSummary);
        } else {
            // update every period overall amount one by one
            AiCreditTransactionOverallEntity updatedEntity = new AiCreditTransactionOverallEntity();
            updatedEntity.setId(overallEntity.getId());
            updatedEntity.setTotalAmount(
                overallEntity.getTotalAmount().add(amountSummary.getTotalAmount()));
            updatedEntity.setAverageAmount(amountSummary.getAverageAmount());
            iAiCreditTransactionOverallService.updateById(updatedEntity);
        }
    }
}
