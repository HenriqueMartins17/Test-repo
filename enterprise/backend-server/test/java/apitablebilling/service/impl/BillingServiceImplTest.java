package com.apitable.enterprise.apitablebilling.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.apitable.enterprise.AbstractApitableSaasIntegrationTest;
import com.apitable.enterprise.apitablebilling.enums.ProductEnum;
import com.apitable.enterprise.apitablebilling.model.vo.BillingInfo;
import com.apitable.mock.bean.MockUserSpace;
import org.junit.jupiter.api.Test;

public class BillingServiceImplTest extends AbstractApitableSaasIntegrationTest {

    @Test
    void testGetBillingInfoOnFree() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        BillingInfo billingInfo = iBillingService.getBillingInfo(userSpace.getSpaceId());
        assertThat(billingInfo).isNotNull();
        assertThat(billingInfo.getPlanName()).isEqualTo(ProductEnum.FREE.getName());
        assertThat(billingInfo.isTrial()).isFalse();
        assertThat(billingInfo.getCredit()).isZero();
    }
}
