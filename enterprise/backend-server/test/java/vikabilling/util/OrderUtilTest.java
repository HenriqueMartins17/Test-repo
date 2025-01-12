/*
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its subdirectories does not constitute permission to use this code or APITable Enterprise Edition features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

package com.apitable.enterprise.vikabilling.util;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import static com.apitable.enterprise.vikabilling.util.OrderUtil.centsToYuan;
import static com.apitable.enterprise.vikabilling.util.OrderUtil.toCurrencyUnit;
import static com.apitable.enterprise.vikabilling.util.OrderUtil.yuanToCents;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withinPercentage;

/**
 *
 * @author Shawn Deng
 * @date 2022-05-19 12:35:42
 */
public class OrderUtilTest {

    @Test
    public void testCalculateProrationBetweenDates() {
        BigDecimal orderAmount = new BigDecimal(288800);
        LocalDate startDate = LocalDate.of(2021, 5, 1);
        LocalDate endDate = startDate.plusMonths(1);

        long nbTotalDays = DAYS.between(startDate, endDate);
        System.out.printf("number of total days: %d \n", nbTotalDays);
        assertThat(nbTotalDays).isEqualTo(31);
        LocalDate changeDate = LocalDate.of(2021, 5, 15);

        BigDecimal prorated = OrderUtil.calculateProrationBetweenDates(startDate, changeDate, nbTotalDays);
        System.out.printf("used pro-rated is %f \n", prorated);
        assertThat(prorated).isCloseTo(new BigDecimal("0.5"), withinPercentage(10));

        BigDecimal usedAmount = prorated.multiply(orderAmount);
        System.out.printf("used amount is %f \n", usedAmount);
        System.out.printf("remain amount is %f \n", orderAmount.subtract(usedAmount));
    }

    @Test
    public void testYuanToCentsOnHalfUp() {
        BigDecimal b = new BigDecimal("12852.69041131200");
        int value = yuanToCents(b);
        assertThat(value).isEqualTo(1285269);

        BigDecimal b1 = new BigDecimal("12852.69541131200");
        int value1 = yuanToCents(b1);
        assertThat(value1).isEqualTo(1285270);
    }

    @Test
    public void testCentsToYuan() {
        int amount = 10201;
        BigDecimal value = centsToYuan(amount);
        assertThat(value.doubleValue()).isEqualTo(102.01);
    }

    @Test
    public void testToCurrencyUnit() {
        BigDecimal amount = new BigDecimal("12852.69041131200");
        BigDecimal value = toCurrencyUnit(amount);
        assertThat(value.doubleValue()).isEqualTo(12852.69);

        BigDecimal amount1 = new BigDecimal("12852.69541131200");
        BigDecimal value1 = toCurrencyUnit(amount1);
        assertThat(value1.doubleValue()).isEqualTo(12852.70);
    }
}
