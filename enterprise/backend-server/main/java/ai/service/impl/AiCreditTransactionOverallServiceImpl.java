package com.apitable.enterprise.ai.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.apitable.enterprise.ai.entity.AiCreditTransactionOverallEntity;
import com.apitable.enterprise.ai.mapper.AiCreditTransactionOverallMapper;
import com.apitable.enterprise.ai.model.CreditAmountSummary;
import com.apitable.enterprise.ai.model.CreditSummaryPeriod;
import com.apitable.enterprise.ai.service.IAiCreditTransactionOverallService;
import com.apitable.enterprise.ai.service.IAiCreditTransactionService;
import com.apitable.interfaces.ai.model.ChartTimeDimension;
import com.apitable.interfaces.ai.model.CreditTransactionChartData;
import com.apitable.shared.clock.spring.ClockManager;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * AI - Credit Transaction Overall Service.
 * </p>
 *
 * @author Shawn Deng
 */
@Service
public class AiCreditTransactionOverallServiceImpl
    extends ServiceImpl<AiCreditTransactionOverallMapper, AiCreditTransactionOverallEntity>
    implements IAiCreditTransactionOverallService {

    @Resource
    private IAiCreditTransactionService iAiCreditTransactionService;

    @Override
    public AiCreditTransactionOverallEntity getBySpaceIdAndPeriod(String spaceId,
                                                                  CreditSummaryPeriod period,
                                                                  LocalDate startDate) {
        return baseMapper.selectBySpaceIdAndPeriod(spaceId, period.getValue(), startDate);
    }


    @Override
    public List<CreditAmountSummary> getCreditTransactionAmountSummary(LocalDate startDate,
                                                                       LocalDate endDate) {
        return baseMapper.summaryByDate(startDate, endDate);
    }

    @Override
    public CreditAmountSummary getCreditTransactionAmountSummary(String spaceId,
                                                                 LocalDate startDate,
                                                                 LocalDate endDate) {
        return baseMapper.summaryBySpaceIdAndDate(spaceId, startDate, endDate);
    }

    @Override
    public void saveDayPeriod(LocalDate currentDate, CreditAmountSummary amountSummary) {
        AiCreditTransactionOverallEntity entity = create(
            amountSummary,
            CreditSummaryPeriod.DAY,
            LocalDateTime.of(currentDate, LocalTime.MIN),
            LocalDateTime.of(currentDate, LocalTime.MIN)
        );
        save(entity);
    }

    @Override
    public void saveMonthPeriod(LocalDate startDate, LocalDate endDate,
                                CreditAmountSummary amountSummary) {

        AiCreditTransactionOverallEntity entity = create(amountSummary, CreditSummaryPeriod.MONTH,
            LocalDateTime.of(startDate, LocalTime.MIN),
            LocalDateTime.of(endDate, LocalTime.MIN));
        save(entity);
    }

    private AiCreditTransactionOverallEntity create(CreditAmountSummary amountSummary,
                                                    CreditSummaryPeriod period,
                                                    LocalDateTime startDate,
                                                    LocalDateTime endDate) {
        AiCreditTransactionOverallEntity entity = new AiCreditTransactionOverallEntity();
        entity.setId(IdWorker.getId());
        entity.setSpaceId(amountSummary.getSpaceId());
        entity.setPeriod(period.getValue());
        entity.setStartDate(startDate);
        entity.setEndDate(endDate);
        entity.setTotalAmount(amountSummary.getTotalAmount());
        entity.setAverageAmount(amountSummary.getAverageAmount());
        return entity;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSave(List<AiCreditTransactionOverallEntity> entities) {
        if (CollUtil.isEmpty(entities)) {
            return;
        }
        int affectRows = baseMapper.insertBatch(entities);
        if (affectRows != entities.size()) {
            throw new RuntimeException("batch save credit transaction error");
        }
    }

    @Override
    public List<CreditTransactionChartData> summary(
        String spaceId, ChartTimeDimension timeDimension) {
        switch (timeDimension) {
            case TODAY:
                return wrapperHours(iAiCreditTransactionService.summaryToday(spaceId,
                    ClockManager.me().getLocalDateNow()));
            case WEEKDAY:
                return baseMapper.summaryThisWeekday(spaceId, ClockManager.me().getLocalDateNow());
            case MONTH:
                return wrapperMonth(
                    baseMapper.summaryThisMonth(spaceId, ClockManager.me().getLocalDateNow()));
            case YEAR:
                return wrapperYear(
                    baseMapper.summaryThisYear(spaceId, ClockManager.me().getLocalDateNow()));
            default:
                throw new RuntimeException("don't support time dimension");
        }
    }

    private List<CreditTransactionChartData> wrapperHours(
        List<CreditTransactionChartData> sources) {
        LocalDateTime now = ClockManager.me().getLocalDateTimeNow();
        int hours = now.getHour();
        if (sources.size() == hours) {
            return sources;
        }

        // dateline -> index
        Map<Integer, Integer> datelineIndexMap = sources.stream()
            .collect(
                Collectors.toMap(data -> {
                    Instant instant = Instant.ofEpochSecond(Integer.parseInt(data.getDateline()));
                    LocalDateTime dateTime =
                        LocalDateTime.ofInstant(instant, ClockManager.me().getDefaultTimeZone());
                    return dateTime.getHour();
                }, sources::indexOf));

        List<CreditTransactionChartData> wrapperDataList = new ArrayList<>(hours);
        for (int i = 0; i <= hours; i++) {
            if (datelineIndexMap.containsKey(i)) {
                CreditTransactionChartData data = sources.get(datelineIndexMap.get(i));
                wrapperDataList.add(data);
            } else {
                LocalDateTime dateTime = LocalDateTime.of(now.toLocalDate(), LocalTime.of(i, 0, 0));
                Instant instant =
                    dateTime.atZone(ClockManager.me().getDefaultTimeZone()).toInstant();
                CreditTransactionChartData chartData = new CreditTransactionChartData();
                chartData.setDateline(String.valueOf(instant.getEpochSecond()));
                chartData.setTotalCount(BigDecimal.ZERO);
                wrapperDataList.add(chartData);
            }
        }
        return wrapperDataList;
    }

    private List<CreditTransactionChartData> wrapperMonth(
        List<CreditTransactionChartData> sources) {
        LocalDate now = ClockManager.me().getLocalDateNow();
        int dayOfMonth = now.getDayOfMonth();
        if (sources.size() == dayOfMonth) {
            return sources;
        }

        // dateline -> index
        Map<Integer, Integer> datelineIndexMap = sources.stream()
            .collect(
                Collectors.toMap(data -> Integer.parseInt(data.getDateline()), sources::indexOf));


        List<CreditTransactionChartData> wrapperDataList = new ArrayList<>(dayOfMonth);
        for (int i = 1; i <= dayOfMonth; i++) {
            if (datelineIndexMap.containsKey(i)) {
                wrapperDataList.add(sources.get(datelineIndexMap.get(i)));
            } else {
                CreditTransactionChartData chartData = new CreditTransactionChartData();
                chartData.setDateline(String.valueOf(i));
                chartData.setTotalCount(BigDecimal.ZERO);
                wrapperDataList.add(chartData);
            }
        }

        return wrapperDataList;
    }

    private List<CreditTransactionChartData> wrapperYear(List<CreditTransactionChartData> sources) {
        LocalDate now = ClockManager.me().getLocalDateNow();
        int month = now.getMonthValue();
        if (sources.size() == month) {
            return sources;
        }

        // dateline -> index
        Map<Integer, Integer> datelineIndexMap = sources.stream()
            .collect(
                Collectors.toMap(data -> Integer.parseInt(data.getDateline()), sources::indexOf));

        List<CreditTransactionChartData> wrapperDataList = new ArrayList<>(month);
        for (int i = 1; i <= month; i++) {
            if (datelineIndexMap.containsKey(i)) {
                wrapperDataList.add(sources.get(datelineIndexMap.get(i)));
            } else {
                CreditTransactionChartData chartData = new CreditTransactionChartData();
                chartData.setDateline(String.valueOf(i));
                chartData.setTotalCount(BigDecimal.ZERO);
                wrapperDataList.add(chartData);
            }
        }

        return wrapperDataList;
    }
}
