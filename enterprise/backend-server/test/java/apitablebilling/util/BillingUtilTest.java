package com.apitable.enterprise.apitablebilling.util;

import static com.apitable.enterprise.apitablebilling.util.BillingUtil.buildFeature;
import static org.assertj.core.api.Assertions.assertThat;

import com.apitable.enterprise.apitablebilling.enums.ProductEnum;
import com.apitable.interfaces.billing.model.SubscriptionFeature;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

public class BillingUtilTest {

    @Test
    void testTimestampToLocalDateTime() {
        LocalDateTime localDateTime = BillingUtil.timestampToLocalDateTime(1632931200L);
        assertThat(localDateTime).isNotNull().isEqualTo(LocalDateTime.of(2021, 9, 29, 16, 0, 0));
    }


    @Test
    void testFreeFeatureByBuild() {
        SubscriptionFeature feature =  buildFeature(ProductEnum.FREE, 1, false);
        assertThat(feature.getControlFormBrandLogo().getValue()).isEqualTo(false);
    }

    @Test
    void testEnterpriseFeatureByBuild() {
        SubscriptionFeature feature =  buildFeature(ProductEnum.ENTERPRISE, 1, false);
        assertThat(feature.getControlFormBrandLogo().getValue()).isEqualTo(true);
    }
}
