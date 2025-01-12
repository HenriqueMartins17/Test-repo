package com.apitable.enterprise.ai.service.impl;

import static com.apitable.enterprise.vikabilling.util.BillingUtil.channelDefaultSubscription;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

import com.apitable.enterprise.AbstractVikaSaasIntegrationTest;
import com.apitable.enterprise.ai.model.AiModel;
import com.apitable.enterprise.ai.model.MessageCreditLimit;
import com.apitable.enterprise.vikabilling.enums.ProductChannel;
import com.apitable.mock.bean.MockUserSpace;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

public class AiCreditServiceImplInVikaTest extends AbstractVikaSaasIntegrationTest {

    @Test
    void testGetMessageCreditLimitWithGPT4() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        String aiNodeId = createAiNode(userSpace.getSpaceId());
        doReturn(channelDefaultSubscription(ProductChannel.VIKA))
            .when(entitlementServiceFacade).getSpaceSubscription(anyString());
        AiModel aiModel = AiModel.GPT_4;
        MessageCreditLimit limit = iAiCreditService.getMessageCreditLimit(aiNodeId, aiModel);
        assertThat(limit).isNotNull();
        assertThat(limit.getMaxCreditNums()).isEqualTo(0);
        assertThat(limit.getRemainCreditNums()).isEqualTo(new BigDecimal("0.0000"));
        assertThat(limit.getRemainChatTimes()).isEqualTo(0);
    }

    @Test
    void testGetMessageCreditLimitWithGPT3() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        String aiNodeId = createAiNode(userSpace.getSpaceId());
        doReturn(channelDefaultSubscription(ProductChannel.VIKA))
            .when(entitlementServiceFacade).getSpaceSubscription(anyString());
        AiModel aiModel = AiModel.GPT_3_5_TURBO;
        MessageCreditLimit limit = iAiCreditService.getMessageCreditLimit(aiNodeId, aiModel);
        assertThat(limit).isNotNull();
        assertThat(limit.getMaxCreditNums()).isEqualTo(0);
        assertThat(limit.getRemainCreditNums()).isEqualTo(new BigDecimal("0.0000"));
        assertThat(limit.getRemainChatTimes()).isEqualTo(0);
    }
}
