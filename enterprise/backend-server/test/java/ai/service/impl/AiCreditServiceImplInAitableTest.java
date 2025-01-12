package com.apitable.enterprise.ai.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

import com.apitable.enterprise.AbstractApitableSaasIntegrationTest;
import com.apitable.enterprise.ai.model.AiModel;
import com.apitable.enterprise.ai.model.MessageCreditLimit;
import com.apitable.enterprise.apitablebilling.interfaces.model.BillingSubscriptionInfo;
import com.apitable.mock.bean.MockUserSpace;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

public class AiCreditServiceImplInAitableTest extends AbstractApitableSaasIntegrationTest {

    @Test
    void testGetMessageCreditLimitWithGPT4() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        String aiNodeId = createAiNode(userSpace.getSpaceId());
        doReturn(new BillingSubscriptionInfo()).when(
            entitlementServiceFacade).getSpaceSubscription(anyString());
        AiModel aiModel = AiModel.GPT_4;
        MessageCreditLimit limit = iAiCreditService.getMessageCreditLimit(aiNodeId, aiModel);
        assertThat(limit).isNotNull();
        assertThat(limit.getMaxCreditNums()).isEqualTo(20);
        assertThat(limit.getRemainCreditNums()).isEqualTo(new BigDecimal("20.0000"));
        assertThat(limit.getRemainChatTimes()).isEqualTo(1);
    }

    @Test
    void testGetMessageCreditLimitWithGPT3() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        String aiNodeId = createAiNode(userSpace.getSpaceId());
        doReturn(new BillingSubscriptionInfo()).when(
            entitlementServiceFacade).getSpaceSubscription(anyString());
        AiModel aiModel = AiModel.GPT_3_5_TURBO;
        MessageCreditLimit limit = iAiCreditService.getMessageCreditLimit(aiNodeId, aiModel);
        assertThat(limit).isNotNull();
        assertThat(limit.getMaxCreditNums()).isEqualTo(20);
        assertThat(limit.getRemainCreditNums()).isEqualTo(new BigDecimal("20.0000"));
        assertThat(limit.getRemainChatTimes()).isEqualTo(20);
    }
}
