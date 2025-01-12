package com.apitable.enterprise.apitablebilling.service.impl;

import static com.apitable.enterprise.apitablebilling.enums.BillingException.PRICE_NOT_FOUND;
import static com.apitable.enterprise.apitablebilling.util.BillingUtil.timestampToLocalDateTime;

import cn.hutool.core.util.StrUtil;
import com.apitable.core.exception.BusinessException;
import com.apitable.enterprise.apitablebilling.entity.BundleEntity;
import com.apitable.enterprise.apitablebilling.entity.SubscriptionEntity;
import com.apitable.enterprise.apitablebilling.enums.BillingPeriod;
import com.apitable.enterprise.apitablebilling.enums.BundleState;
import com.apitable.enterprise.apitablebilling.enums.SubscriptionState;
import com.apitable.enterprise.apitablebilling.model.dto.EntitlementCreationDTO;
import com.apitable.enterprise.apitablebilling.service.IBundleInApitableService;
import com.apitable.enterprise.apitablebilling.service.IEntitlementService;
import com.apitable.enterprise.apitablebilling.service.IStripeEventHandler;
import com.apitable.enterprise.apitablebilling.service.ISubscriptionInApitableService;
import com.apitable.enterprise.stripe.config.Product;
import com.apitable.enterprise.stripe.config.ProductCatalogFactory;
import com.apitable.enterprise.stripe.core.StripeSubscriptionStatus;
import com.apitable.enterprise.stripe.core.StripeTemplate;
import com.apitable.shared.clock.spring.ClockManager;
import com.apitable.space.entity.SpaceEntity;
import com.apitable.space.service.ISpaceService;
import com.stripe.model.Price;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionItem;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * stripe webhook event handler implements.
 */
@Service
@Slf4j
public class StripeEventHandlerImpl implements IStripeEventHandler {

    @Resource
    private IEntitlementService iEntitlementService;

    @Resource
    private IBundleInApitableService iBundleInApitableService;

    @Resource
    private ISubscriptionInApitableService iSubscriptionInApitableService;

    @Resource
    private ISpaceService iSpaceService;

    @Autowired(required = false)
    private StripeTemplate stripeTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void fulfillOrder(Session session) {
        Session retrieveSession = stripeTemplate.expandSession(session.getId());

        if (retrieveSession.getPaymentStatus().equals("paid")) {
            // get spaceId
            String spaceId = retrieveSession.getMetadata().get("spaceId");
            if (StrUtil.isNotBlank(spaceId)) {
                // check space is exist
                SpaceEntity spaceEntity = iSpaceService.getEntityBySpaceId(spaceId);
                if (spaceEntity == null) {
                    log.error("retrieve subscription of space is not on this environment");
                    return;
                }
                if (retrieveSession.getMode().equals(SessionCreateParams.Mode.SUBSCRIPTION.getValue())) {
                    Subscription subscriptionObject = retrieveSession.getSubscriptionObject();
                    saveEntitlementFromStripeSubscription(spaceId, subscriptionObject);
                }
                if (retrieveSession.getMode().equals(SessionCreateParams.Mode.PAYMENT.getValue())) {
                    String priceId = retrieveSession.getMetadata().get("priceId");
                    if (null == priceId) {
                        log.error("retrieve subscription of price is not on this environment");
                        return;
                    }
                    saveEntitlementFromStripePayment(spaceId, priceId, retrieveSession.getPaymentIntent());
                }
            }
        }
    }

    private void saveEntitlementFromStripeSubscription(String spaceId,
                                                       Subscription stripeSubscription) {
        // create entitlement
        SubscriptionItem subscriptionItem =
            stripeSubscription.getItems().getData().iterator().next();
        Price price = subscriptionItem.getPrice();
        Product productObject = ProductCatalogFactory.INSTANCE.findByProductId(price.getProduct())
            .orElseThrow(() -> new BusinessException(PRICE_NOT_FOUND));
        boolean trialStatus = stripeSubscription.getTrialStart() != null
            || stripeSubscription.getTrialEnd() != null;
        iEntitlementService.createEntitlement(
            EntitlementCreationDTO.builder()
                .spaceId(spaceId)
                .stripeId(stripeSubscription.getId())
                .stripeSubId(subscriptionItem.getId())
                .productName(productObject.getName())
                .priceId(price.getId())
                .quantity(subscriptionItem.getQuantity().intValue())
                .period(BillingPeriod.ofInterval(price.getRecurring().getInterval()))
                .trial(trialStatus)
                .startDate(timestampToLocalDateTime(stripeSubscription.getCurrentPeriodStart()))
                .endDate(timestampToLocalDateTime(stripeSubscription.getCurrentPeriodEnd()))
                .build()
        );
    }

    private void saveEntitlementFromStripePayment(String spaceId, String priceId, String paymentIntent) {
        // create entitlement
        Product productObject = ProductCatalogFactory.INSTANCE.findByPriceId(priceId)
            .orElseThrow(() -> new BusinessException(PRICE_NOT_FOUND));
        LocalDateTime startDate = ClockManager.me().getLocalDateTimeNow();
        iEntitlementService.createEntitlement(
            EntitlementCreationDTO.builder()
                .spaceId(spaceId)
                .stripeId(paymentIntent)
                .productName(productObject.getName())
                .priceId(priceId)
                .quantity(1)
                .period(BillingPeriod.UNLIMITED)
                .trial(false)
                .startDate(startDate)
                .endDate(startDate.plusYears(100))
                .build());
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void subscriptionCreated(Subscription subscription) {
        Subscription expandSubscription = stripeTemplate.expandSubscription(subscription.getId());
        String spaceId = expandSubscription.getMetadata().get("spaceId");
        if (StrUtil.isBlank(spaceId)) {
            log.error("not from apitable saas services, subscription: {}, spaceId: {}",
                subscription.getId(), spaceId);
            return;
        }
        // create entitlement
        saveEntitlementFromStripeSubscription(spaceId, expandSubscription);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void subscriptionUpdated(Subscription subscription) {
        SubscriptionEntity subscriptionEntity =
            iSubscriptionInApitableService.getByStripeId(subscription.getId());
        if (subscriptionEntity == null) {
            log.error("retrieve subscription id [{}] is not found", subscription.getId());
            return;
        }
        StripeSubscriptionStatus status = StripeSubscriptionStatus.of(subscription.getStatus());
        if (!status.isValid()) {
            // any other status will end subscription unless active
            expireEntitlement(subscriptionEntity);
            return;
        }
        BundleState bundleState = BundleState.of(status);
        SubscriptionState subscriptionState = SubscriptionState.of(status);
        // update subscription
        SubscriptionItem subscriptionItem = subscription.getItems().getData().iterator().next();
        Price price = subscriptionItem.getPrice();
        Product productObject = ProductCatalogFactory.INSTANCE
            .findByProductId(price.getProduct())
            .orElseThrow(() -> new BusinessException(PRICE_NOT_FOUND));
        // Update subscription bundle
        LocalDateTime startDate =
            timestampToLocalDateTime(subscription.getCurrentPeriodStart());
        LocalDateTime endDate = timestampToLocalDateTime(subscription.getCurrentPeriodEnd());
        BundleEntity updateBundle = BundleEntity.builder()
            .startDate(startDate)
            .endDate(endDate)
            .state(bundleState.name())
            .build();
        iBundleInApitableService.updateByBundleId(subscriptionEntity.getBundleId(), updateBundle);
        // Update subscription
        SubscriptionEntity updateSubscription = SubscriptionEntity.builder()
            .priceId(price.getId())
            .productName(productObject.getName())
            .quantity(subscriptionItem.getQuantity().intValue())
            .period(BillingPeriod.ofInterval(price.getRecurring().getInterval()).getName())
            .state(subscriptionState.name())
            .bundleStartDate(startDate)
            .startDate(startDate)
            .expireDate(endDate)
            .build();
        iSubscriptionInApitableService.updateBySubscriptionId(
            subscriptionEntity.getSubscriptionId(),
            updateSubscription);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void subscriptionDeleted(Subscription subscription) {
        SubscriptionEntity subscriptionEntity =
            iSubscriptionInApitableService.getByStripeId(subscription.getId());
        if (subscriptionEntity == null) {
            log.error("retrieve subscription id [{}] is not found", subscription.getId());
            return;
        }
        expireEntitlement(subscriptionEntity);
    }

    private void expireEntitlement(SubscriptionEntity subscriptionEntity) {
        // update bundle
        BundleEntity updateBundle = BundleEntity.builder()
            .state(BundleState.CANCELED.name())
            .build();
        iBundleInApitableService.updateByBundleId(subscriptionEntity.getBundleId(), updateBundle);
        // Update subscription
        SubscriptionEntity updateSubscription = SubscriptionEntity.builder()
            .state(SubscriptionState.CANCELED.name())
            .build();
        iSubscriptionInApitableService.updateBySubscriptionId(
            subscriptionEntity.getSubscriptionId(),
            updateSubscription);
    }
}
