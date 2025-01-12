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

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.apitable.enterprise.vikabilling.enums.ProductChannel;
import com.apitable.enterprise.vikabilling.interfaces.model.BillingSubscriptionInfo;
import com.apitable.enterprise.vikabilling.setting.Plan;
import java.util.Collections;


/**
 * billing util.
 *
 * @author Shawn Deng
 */
public class BillingUtil {

    /**
     * get channel default subscription.
     *
     * @param channel product channel
     * @return subscription info
     */
    public static BillingSubscriptionInfo channelDefaultSubscription(ProductChannel channel) {
        Plan freePlan = BillingConfigManager.getFreePlan(channel);
        if (freePlan == null) {
            throw new RuntimeException("free plan is missing");
        }
        return new BillingSubscriptionInfo(freePlan.getProduct(), freePlan.getId(),
            Collections.emptyList());
    }

    /**
     * legacy plan id.
     *
     * @param planId plan id
     * @return legacy plan id name
     */
    public static String legacyPlanId(String planId) {
        String[] elements = planId.split("_");
        if (elements.length < 2) {
            throw new IllegalStateException("plan parse error");
        }
        String element = elements[1];
        if (NumberUtil.isNumber(element)) {
            return StrUtil.join("_", elements[0], elements[1]);
        }
        return planId;
    }
}
