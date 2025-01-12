package com.apitable.enterprise.vikabilling.util;

import static com.apitable.enterprise.vikabilling.enums.BillingFunctionEnum.ORG_API;
import static com.apitable.enterprise.vikabilling.enums.ProductEnum.PRIVATE_CLOUD;
import static com.apitable.enterprise.vikabilling.util.BillingUtil.channelDefaultSubscription;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.apitable.enterprise.vikabilling.enums.ProductChannel;
import com.apitable.enterprise.vikabilling.enums.ProductEnum;
import com.apitable.enterprise.vikabilling.interfaces.model.BillingSubscriptionInfo;
import com.apitable.enterprise.vikabilling.setting.Plan;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
public class BillingFeatureConfigTest {

    @Test
    public void testEnterpriseAllowOrgApi() {
        Map<String, Plan> plans = BillingConfigManager.getBillingConfig().getPlans();
        plans.forEach((key, plan) -> {
            if (plan.getProduct().contains(ProductEnum.ENTERPRISE.getName())) {
                assertTrue(plan.getFeatures().contains(ORG_API.getCode()));
            }
        });
    }


    @Test
    public void testSelfHostedAllowOrgApi() {
        BillingSubscriptionInfo planInfo  = Optional.of(channelDefaultSubscription(ProductChannel.PRIVATE))
            .orElse(channelDefaultSubscription(ProductChannel.VIKA));
        assertTrue(planInfo.getFeature().getAllowOrgApi().getValue());
    }

    @Test
    public void testNotEnterpriseNotAllowedOrgApi() {
        Map<String, Plan> plans = BillingConfigManager.getBillingConfig().getPlans();
        plans.forEach((key, plan) -> {
            if (!plan.getProduct().contains(ProductEnum.ENTERPRISE.getName()) && !plan.getProduct().equals(PRIVATE_CLOUD.getName())) {
                assertFalse(plan.getFeatures().contains(ORG_API.getCode()));
            }
        });
    }

}
