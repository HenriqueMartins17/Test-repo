package com.apitable.enterprise.apitablebilling.util;

import static cn.hutool.core.date.DatePattern.PURE_DATETIME_MS_PATTERN;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import java.time.LocalDateTime;

/**
 * Billing Identify Util.
 */
public class BillingIdUtil {

    public static synchronized String createOrderId() {
        return DateUtil.format(LocalDateTime.now(), PURE_DATETIME_MS_PATTERN)
            + RandomUtil.randomNumbers(3);
    }
}
