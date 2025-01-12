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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;

import static cn.hutool.core.date.DatePattern.PURE_DATETIME_MS_PATTERN;

/**
 * order util
 * @author Shawn Deng
 */
public class OrderUtil {
    
    public static String createOrderId() {
        return DateUtil.format(LocalDateTime.now(), PURE_DATETIME_MS_PATTERN) + RandomUtil.randomNumbers(3);
    }

    public static String createPayTransactionId() {
        return DateUtil.format(LocalDateTime.now(), PURE_DATETIME_MS_PATTERN) + RandomUtil.randomNumbers(2);
    }

    public static BigDecimal calculateProrationBetweenDates(final LocalDate startDate, final LocalDate endDate, long daysBetween) {
        if (daysBetween <= 0) {
            return BigDecimal.ZERO;
        }

        final BigDecimal daysInPeriod = new BigDecimal(daysBetween);
        final BigDecimal days = new BigDecimal(ChronoUnit.DAYS.between(startDate, endDate));

        return days.divide(daysInPeriod, BillingMoney.MAX_SCALE, BillingMoney.ROUNDING_MODE);
    }

    public static BigDecimal calculateUnusedAmount(BigDecimal totalAmount, BigDecimal usedDaysProrated) {
        return totalAmount.subtract(usedDaysProrated.multiply(totalAmount));
    }

    /**
     * yuan to cent
     * @param bigDecimal yuan
     * @return int
     */
    public static int yuanToCents(BigDecimal bigDecimal) {
        if (bigDecimal == null) {
            return BigDecimal.ZERO.intValue();
        }
        return bigDecimal.compareTo(BigDecimal.ZERO) <= 0 ? BigDecimal.ZERO.intValue()
                : bigDecimal.setScale(2, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).intValue();
    }

    /**
     * cents to yuan, round to two decimal places
     * @param amount cents
     * @return BigDecimal
     */
    public static BigDecimal centsToYuan(int amount) {
        return new BigDecimal(amount).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
    }

    public static BigDecimal toCurrencyUnit(BigDecimal bigDecimal) {
        if (bigDecimal == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return bigDecimal.setScale(2, RoundingMode.HALF_UP);
    }
}
