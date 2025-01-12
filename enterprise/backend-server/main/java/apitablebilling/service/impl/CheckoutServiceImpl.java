package com.apitable.enterprise.apitablebilling.service.impl;

import static com.apitable.enterprise.stripe.config.ProductCatalogFactory.findByProductId;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.apitable.enterprise.apitablebilling.entity.StripeCustomerEntity;
import com.apitable.enterprise.apitablebilling.model.ro.CheckoutCreation;
import com.apitable.enterprise.apitablebilling.service.ICheckoutService;
import com.apitable.enterprise.apitablebilling.service.IEntitlementService;
import com.apitable.enterprise.apitablebilling.service.IStripeCheckoutSessionService;
import com.apitable.enterprise.apitablebilling.service.IStripeCustomerService;
import com.apitable.enterprise.stripe.config.Product;
import com.apitable.enterprise.stripe.core.StripeTemplate;
import com.apitable.interfaces.billing.model.SubscriptionInfo;
import com.apitable.shared.config.properties.ConstProperties;
import com.apitable.user.entity.UserEntity;
import com.apitable.user.service.IUserService;
import com.stripe.exception.StripeException;
import com.stripe.model.Coupon;
import com.stripe.model.Customer;
import com.stripe.model.Price;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerUpdateParams;
import com.stripe.param.SubscriptionListParams;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionCreateParams.Mode;
import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * stripe checkout session service implements.
 */
@Service
@Slf4j
public class CheckoutServiceImpl implements ICheckoutService {

    @Resource
    private IEntitlementService iEntitlementService;

    @Resource
    private IStripeCheckoutSessionService iStripeCheckoutSessionService;

    @Resource
    private IStripeCustomerService iStripeCustomerService;

    @Resource
    private IUserService iUserService;

    @Autowired(required = false)
    private StripeTemplate stripeTemplate;

    @Resource
    private ConstProperties constProperties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createCheckoutSession(Long userId, CheckoutCreation checkoutCreation) {
        // get user
        UserEntity userEntity = iUserService.getById(userId);
        String spaceId = checkoutCreation.getSpaceId();

        Map<String, String> customerMetadata = null;
        if (StrUtil.isNotBlank(checkoutCreation.getClientReferenceId())) {
            customerMetadata = new HashMap<>(1);
            customerMetadata.put("referral", checkoutCreation.getClientReferenceId());
        }
        String coupon = null;
        if (StrUtil.isNotBlank(checkoutCreation.getCouponId())) {
            String couponId = checkoutCreation.getCouponId();
            Coupon couponObj = stripeTemplate.retrieveCoupon(couponId);
            if (couponObj != null) {
                coupon = couponId;
            }
        }
        Customer customer;
        List<StripeCustomerEntity> stripeCustomers = iStripeCustomerService.getBySpaceId(spaceId);
        if (stripeCustomers.isEmpty()) {
            // create stripe customer
            customer =
                stripeTemplate.createCustomer(userEntity.getNickName(), userEntity.getEmail(),
                    customerMetadata, coupon);
            // save db
            iStripeCustomerService.createCustomer(customer.getId(), userEntity.getEmail(), spaceId);
        } else {
            StripeCustomerEntity stripeCustomerEntity = stripeCustomers.get(0);
            customer =
                stripeTemplate.updateCustomerIfSatisfy(stripeCustomerEntity.getStripeId(),
                    customerMetadata,
                    coupon);
            if (customer.getDiscount() == null && customerMetadata != null) {
                try {
                    customer.update(
                        CustomerUpdateParams.builder()
                            .setCoupon(coupon)
                            .setMetadata(customerMetadata)
                            .build()
                    );
                } catch (StripeException e) {
                    log.error("fail to update stripe customer {}", customer.getId());
                }
            }
        }
        // get space entitlement
        SubscriptionInfo subscriptionInfo = iEntitlementService.getEntitlementBySpaceId(spaceId);
        if (!subscriptionInfo.isFree()) {
            // not free subscription, may be upgrade, and downgrade operation
            // creates customer portal using the Stripe API
            String returnUrl = constProperties.getServerDomain() + "/management/upgrade";
            return stripeTemplate.createPortalUrl(customer.getId(), returnUrl);
        }
        Session session;
        try {
            // find price
            String priceId = checkoutCreation.getPriceId();
            Price price = stripeTemplate.retrievePrice(priceId);

            Mode mode = StrUtil.isNotBlank(checkoutCreation.getMode()) ?
                Mode.valueOf(checkoutCreation.getMode()) : Mode.SUBSCRIPTION;
            SessionCreateParams.Builder sessionCreateParams =
                SessionCreateParams.builder()
                    .setMode(mode)
                    .setSuccessUrl(
                        constProperties.getServerDomain() + "/workbench?stripePaySuccess=true")
                    .setCancelUrl(constProperties.getServerDomain() + "/management/upgrade")
                    .putMetadata("spaceId", spaceId)
                    .putMetadata("priceId", priceId)
                    .setCustomer(customer.getId())
                    // necessary for
                    .setCustomerUpdate(SessionCreateParams.CustomerUpdate.builder()
                        .setAddress(SessionCreateParams.CustomerUpdate.Address.AUTO).build())
                    .addLineItem(
                        SessionCreateParams.LineItem.builder()
                            .setPrice(price.getId())
                            .setQuantity(1L)
                            .build()
                    )
                    .setAutomaticTax(
                        SessionCreateParams.AutomaticTax.builder()
                            .setEnabled(true)
                            .build())
                    .setAllowPromotionCodes(true);
            if (ObjectUtil.equals(mode, Mode.SUBSCRIPTION)) {
                sessionCreateParams.setPaymentMethodCollection(
                    SessionCreateParams.PaymentMethodCollection.ALWAYS);
            }
            if (BooleanUtil.isTrue(checkoutCreation.getTrial())) {
                // check if customer has trial end subscription before
                Iterable<Subscription> subscriptions = stripeTemplate.listSubscriptions(
                    SubscriptionListParams.builder()
                        .setCustomer(customer.getId())
                        .setStatus(SubscriptionListParams.Status.CANCELED)
                        .build()
                );
                boolean hasTrialEndSubscription =
                    StreamSupport.stream(subscriptions.spliterator(), false)
                        .anyMatch(subscription -> subscription.getTrialEnd() != null);
                // set trial period days if exists
                Product product = findByProductId(price.getProduct())
                    .orElseThrow(() -> new RuntimeException("product not found"));
                if (product.hasTrialPeriodDays() && !hasTrialEndSubscription) {
                    sessionCreateParams.setSubscriptionData(
                        SessionCreateParams.SubscriptionData.builder()
                            .setTrialPeriodDays(product.safeGetTrialPeriodDays())
                            .build()
                    );
                }
            }

            session = stripeTemplate.createSession(sessionCreateParams.build());
            // save session to db
            iStripeCheckoutSessionService.createSession(spaceId, session);
            // return checkout session url
            return session.getUrl();
        } catch (StripeException e) {
            // rollback customer create in stripe
            stripeTemplate.deleteCustomer(customer.getId());
            throw new RuntimeException("create checkout error", e);
        }
    }
}
