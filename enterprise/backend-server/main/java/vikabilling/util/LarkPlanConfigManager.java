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

import java.util.Map;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import com.apitable.enterprise.vikabilling.setting.BillingConfigLoader;
import com.apitable.enterprise.vikabilling.setting.Price;
import com.apitable.enterprise.vikabilling.setting.lark.LarkPlan;

/**
 * <p>
 * lark plan config manager
 * </p>
 * @author zoe zheng
 */
@Slf4j
public class LarkPlanConfigManager {

    private static final Map<String, LarkPlan> LARK_PLAN = BillingConfigLoader.getConfig().getLark().getPlans();

    public static Map<String, LarkPlan> getBillingConfig() {
        return LARK_PLAN;
    }

    /**
     * get price by lark plan id
     *
     * @param larkPlanId lark plan id
     * @return Price
     */
    public static Price getPriceByLarkPlanId(String larkPlanId) {
        LarkPlan larkPlan = LARK_PLAN.get(larkPlanId);
        if (larkPlan == null) {
            return null;
        }
        String billingPriceId = larkPlan.getBillingPriceId();
        if (StrUtil.isBlank(billingPriceId)) {
            return null;
        }
        return BillingConfigManager.getBillingConfig().getPrices().get(billingPriceId);
    }
}
