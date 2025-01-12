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

import org.junit.jupiter.api.Test;

import static com.apitable.enterprise.vikabilling.util.BillingUtil.legacyPlanId;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Shawn Deng
 */
public class BillingUtilTest {

    @Test
    public void testSilverOnLegacyPlanIdWithoutChange() {
        String legacyPlan = "silver_10";
        assertThat(legacyPlanId(legacyPlan)).isEqualTo(legacyPlan);
    }

    @Test
    public void testSilverOnLegacyPlanId() {
        String legacyPlan = "silver_10_monthly";
        assertThat(legacyPlanId(legacyPlan)).isEqualTo("silver_10");
    }

    @Test
    public void testGoldOnLegacyPlanIdWithoutChange() {
        String legacyPlan = "gold_100";
        assertThat(legacyPlanId(legacyPlan)).isEqualTo(legacyPlan);
    }

    @Test
    public void testEnterpriseOnLegacyPlanId() {
        String legacyPlan = "enterprise_200_monthly";
        assertThat(legacyPlanId(legacyPlan)).isEqualTo("enterprise_200");
    }

    @Test
    public void testEnterpriseOnLegacyPlanIdWithoutChange() {
        String legacyPlan = "enterprise_200";
        assertThat(legacyPlanId(legacyPlan)).isEqualTo(legacyPlan);
    }

    @Test
    public void testGoldOnLegacyPlanId() {
        String legacyPlan = "gold_100_monthly_v1";
        assertThat(legacyPlanId(legacyPlan)).isEqualTo("gold_100");
    }

    @Test
    public void testDingtalkFreeOnLegacyPlanId() {
        String legacyPlan = "dingtalk_base_no_billing_period";
        assertThat(legacyPlanId(legacyPlan)).isEqualTo(legacyPlan);
    }
}
