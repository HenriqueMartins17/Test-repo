package com.apitable.enterprise.stripe.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.apitable.enterprise.apitablebilling.enums.ProductEnum;
import org.junit.jupiter.api.Test;

public class PlanFeatureLoaderTest {

    @Test
    void testLoadJsonFile() {
        PlanFeatures planFeatures = PlanFeatureLoader.getConfig();
        assertThat(planFeatures).isNotNull();
        assertThat(planFeatures).containsKeys(
            ProductEnum.FREE,
            ProductEnum.STARTER,
            ProductEnum.PLUS,
            ProductEnum.PRO,
            ProductEnum.BUSINESS,
            ProductEnum.ENTERPRISE,
            ProductEnum.COMMUNITY,
            ProductEnum.APITABLE_ENTERPRISE,
            ProductEnum.AITABLE_PREMIUM);
    }
}
