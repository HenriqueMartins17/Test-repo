package com.apitable.enterprise.apitablebilling.component;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import com.apitable.enterprise.AbstractApitableSaasIntegrationTest;
import com.apitable.enterprise.apitablebilling.rewardful.model.RewardfulData;
import com.stripe.model.Coupon;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class RewardfulServiceTest extends AbstractApitableSaasIntegrationTest {

    @Test
    void testExtractData() {
        Map<String, String> externalProperty = new HashMap<>();
        externalProperty.put("referral", UUID.randomUUID().toString());
        String coupon = UUID.randomUUID().toString();
        externalProperty.put("coupon", coupon);
        Coupon couponObject = new Coupon();
        couponObject.setId(coupon);
        doReturn(couponObject).when(stripeTemplate).retrieveCoupon(coupon);
        RewardfulData rewardfulData = rewardfulService.extractData(externalProperty);
        assertThat(rewardfulData).isNotNull();
        assertThat(rewardfulData.getCoupon()).isNotBlank();
        assertThat(rewardfulData.getCustomerMetadata()).isNotNull().isNotEmpty();
    }

    @Test
    void testExtractDataWithEmptyMap() {
        Map<String, String> externalProperty = new HashMap<>();
        RewardfulData rewardfulData = rewardfulService.extractData(externalProperty);
        assertThat(rewardfulData).isNotNull();
        assertThat(rewardfulData.getCoupon()).isNull();
        assertThat(rewardfulData.getCustomerMetadata()).isNull();
    }
}
