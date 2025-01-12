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

import java.util.List;
import java.util.Map;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;

import com.apitable.enterprise.vikabilling.setting.BillingConfigLoader;
import com.apitable.enterprise.vikabilling.setting.Price;
import com.apitable.enterprise.vikabilling.setting.dingtalk.DingTalkPlan;

/**
 * <p>
 * dingtalk plan config manager
 * </p>
 * @author zoe zheng
 */
@Slf4j
public class DingTalkPlanConfigManager {

    private static final Map<String, DingTalkPlan> DING_TALK_PLAN =
            BillingConfigLoader.getConfig().getDingtalk().getPlans();

    public static Map<String, DingTalkPlan> getBillingConfig() {
        return DING_TALK_PLAN;
    }

    /**
     * get dingtalk price plan
     *
     * @param itemCode dingtalk item code
     */
    public static Price getPriceByItemCodeAndMonth(String itemCode) {
        DingTalkPlan dingTalkPlan = DING_TALK_PLAN.get(itemCode);
        if (dingTalkPlan == null) {
            return null;
        }
        // get price
        List<String> billingPriceId = dingTalkPlan.getBillingPriceId();
        if (CollUtil.isEmpty(billingPriceId)) {
            return null;
        }
        return BillingConfigManager.getBillingConfig().getPrices().get(billingPriceId.get(0));
    }
}
