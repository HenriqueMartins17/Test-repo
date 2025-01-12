package com.apitable.enterprise.apitablebilling.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.apitable.enterprise.AbstractApitableSaasIntegrationTest;
import com.apitable.enterprise.apitablebilling.enums.ProductEnum;
import com.apitable.enterprise.apitablebilling.enums.RecurringInterval;
import com.apitable.enterprise.apitablebilling.model.ro.CheckoutCreation;
import com.apitable.enterprise.apitablebilling.service.ICheckoutService;
import com.apitable.enterprise.stripe.config.Price;
import com.apitable.enterprise.vikabilling.core.Subscription;
import com.apitable.mock.bean.MockUserSpace;
import com.apitable.user.entity.UserEntity;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.checkout.Session;
import com.stripe.param.SubscriptionListParams;
import com.stripe.param.checkout.SessionCreateParams;
import org.assertj.core.util.IterableUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class CheckoutServiceImplTest extends AbstractApitableSaasIntegrationTest {

    @Autowired
    private ICheckoutService iCheckoutService;

    @Test
    void testCreateCheckoutSession() throws StripeException {
        MockUserSpace userSpace = createSingleUserAndSpace();
        Price testPrice = findPrice(ProductEnum.PLUS, RecurringInterval.YEAR);
        UserEntity userEntity = iUserService.getById(userSpace.getUserId());
        Customer customer = new Customer();
        customer.setId("cus_123");
        doReturn(customer).when(stripeTemplate)
            .createCustomer(userEntity.getNickName(), userEntity.getEmail(), null, null);
        com.stripe.model.Price price = new com.stripe.model.Price();
        price.setId(testPrice.getId());
        price.setProduct(testPrice.getProductId());
        doReturn(price).when(stripeTemplate).retrievePrice(anyString());
        Iterable<Subscription> subscriptions = IterableUtil.iterable();
        doReturn(subscriptions).when(stripeTemplate).listSubscriptions(
            any(SubscriptionListParams.class)
        );
        Session session = new Session();
        session.setUrl("https://checkout.stripe.com");
        doReturn(session).when(stripeTemplate).createSession(any(SessionCreateParams.class));
        String sessionUrl = iCheckoutService.createCheckoutSession(userSpace.getUserId(),
            CheckoutCreation.builder()
                .spaceId(userSpace.getSpaceId())
                .priceId(testPrice.getId())
                .build());
        assertThat(sessionUrl).isNotBlank();
    }


    @Test
    void testCreateExclusiveLimitedTire1CheckoutSession() throws StripeException {
        MockUserSpace userSpace = createSingleUserAndSpace();
        Price testPrice = findPrice(ProductEnum.PLUS, RecurringInterval.YEAR);
        UserEntity userEntity = iUserService.getById(userSpace.getUserId());
        Customer customer = new Customer();
        customer.setId("cus_123");
        doReturn(customer).when(stripeTemplate)
            .createCustomer(userEntity.getNickName(), userEntity.getEmail(), null, null);

        com.stripe.model.Price price = new com.stripe.model.Price();
        price.setId(testPrice.getId());
        price.setProduct(testPrice.getProductId());

        doReturn(price).when(stripeTemplate).retrievePrice(anyString());

        Iterable<Subscription> subscriptions = IterableUtil.iterable();
        doReturn(subscriptions).when(stripeTemplate).listSubscriptions(
            any(SubscriptionListParams.class)
        );

        Session session = new Session();
        session.setUrl("https://checkout.stripe.com");
        doReturn(session).when(stripeTemplate).createSession(any(SessionCreateParams.class));

        String sessionUrl = iCheckoutService.createCheckoutSession(userSpace.getUserId(),
            CheckoutCreation.builder()
                .spaceId(userSpace.getSpaceId())
                .priceId(testPrice.getId())
                .mode("PAYMENT")
                .build());

        verify(stripeTemplate, times(0)).createPortalUrl(anyString(), anyString());
        assertThat(sessionUrl).isNotBlank();
    }
}
