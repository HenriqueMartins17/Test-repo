package com.apitable.enterprise.apitablebilling.appsumo.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

import cn.hutool.core.io.IoUtil;
import cn.hutool.json.JSONUtil;
import com.apitable.FileHelper;
import com.apitable.enterprise.AbstractApitableSaasIntegrationTest;
import com.apitable.enterprise.apitablebilling.enums.ProductEnum;
import com.apitable.enterprise.stripe.config.Product;
import com.apitable.enterprise.stripe.config.ProductCatalogFactory;
import com.apitable.interfaces.billing.model.SubscriptionInfo;
import com.apitable.mock.bean.MockUserSpace;
import com.stripe.model.checkout.Session;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class StripeEventHandlerImplTest extends AbstractApitableSaasIntegrationTest {

    @Test
    void testHandFillOrderWithPaymentMode() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        Session session = getTestSession("enterprise/stripe/payment_mode_session.json");
        Map<String, String> metadata = session.getMetadata();
        metadata.put("spaceId", userSpace.getSpaceId());
        session.setMetadata(metadata);
        String spaceId = session.getMetadata().get("spaceId");
        String priceId = session.getMetadata().get("priceId");
        Optional<Product> productObject = ProductCatalogFactory.INSTANCE.findByPriceId(priceId);
        assertThat(productObject.isPresent()).isTrue();
        doReturn(session).when(stripeTemplate).expandSession(anyString());
        iStripeEventHandler.fulfillOrder(session);
        // move clock
        getClock().addDays(1);
        SubscriptionInfo subscription = iEntitlementService.getEntitlementBySpaceId(spaceId);
        getClock().resetDeltaFromReality();
        assertThat(subscription.getProduct()).isEqualTo(productObject.get().getName());
    }

    @Test
    void testHandFillOrderWithPaymentModeWithExclusiveTier1(){
        MockUserSpace userSpace = createSingleUserAndSpace();
        Session session = getTestSession("enterprise/stripe/payment_mode_exclusive_tier1_session.json");
        Map<String, String> metadata = session.getMetadata();
        metadata.put("spaceId", userSpace.getSpaceId());
        session.setMetadata(metadata);
        String spaceId = session.getMetadata().get("spaceId");
        String priceId = session.getMetadata().get("priceId");
        Optional<Product> productObject = ProductCatalogFactory.INSTANCE.findByPriceId(priceId);
        assertThat(productObject.isPresent()).isTrue();

        doReturn(session).when(stripeTemplate).expandSession(anyString());
        iStripeEventHandler.fulfillOrder(session);
        // move clock
        getClock().addDays(1);
        SubscriptionInfo subscription = iEntitlementService.getEntitlementBySpaceId(spaceId);
        getClock().resetDeltaFromReality();
        assertThat(subscription.getProduct()).isEqualTo(ProductEnum.EXCLUSIVE_LIMITED_TIER1.getName());
    }


    private Session getTestSession(String filePath) {
        InputStream resourceAsStream = FileHelper.getInputStreamFromResource(filePath);
        String jsonString = IoUtil.read(resourceAsStream, StandardCharsets.UTF_8);
        return JSONUtil.toBean(jsonString, Session.class);
    }

}
