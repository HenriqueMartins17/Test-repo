package com.apitable.enterprise.stripe.core;

import cn.hutool.core.util.StrUtil;
import com.apitable.enterprise.stripe.autoconfigure.StripeProperties;
import com.apitable.enterprise.stripe.core.model.SubscriptionCreation;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Coupon;
import com.stripe.model.Customer;
import com.stripe.model.Event;
import com.stripe.model.Invoice;
import com.stripe.model.InvoiceCollection;
import com.stripe.model.Price;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerRetrieveParams;
import com.stripe.param.CustomerUpdateParams;
import com.stripe.param.InvoiceListParams;
import com.stripe.param.PriceRetrieveParams;
import com.stripe.param.SubscriptionCancelParams;
import com.stripe.param.SubscriptionCreateParams;
import com.stripe.param.SubscriptionListParams;
import com.stripe.param.SubscriptionRetrieveParams;
import com.stripe.param.SubscriptionUpdateParams;
import com.stripe.param.billingportal.SessionCreateParams;
import com.stripe.param.checkout.SessionRetrieveParams;
import java.util.Arrays;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * Stripe api template.
 *
 * @author Shawn Deng
 */
@Slf4j
public class StripeTemplate {

    private final StripeProperties properties;

    public StripeTemplate(StripeProperties properties) {
        this.properties = properties;
    }

    /**
     * retrieve event.
     *
     * @param payload   payload
     * @param signature signature
     * @return Event
     * @throws SignatureVerificationException SignatureVerificationException
     */
    public Event retrieveEvent(String payload, String signature)
        throws SignatureVerificationException {
        return Webhook.constructEvent(
            payload, signature, properties.getSignatureSecret()
        );
    }

    /**
     * retrieve price on stripe.
     *
     * @param stripePriceId stripe price id
     * @param expandKeys    price object expand keys
     * @return com.stripe.model.Price
     */
    public Price retrievePrice(String stripePriceId, String... expandKeys) {
        try {
            if (expandKeys.length > 0) {
                PriceRetrieveParams params = PriceRetrieveParams.builder()
                    .addAllExpand(Arrays.asList(expandKeys))
                    .build();
                return Price.retrieve(stripePriceId, params, null);
            }
            return Price.retrieve(stripePriceId);
        } catch (StripeException e) {
            throw new RuntimeException(String.format("fail to retrieve price: %s", stripePriceId),
                e);
        }
    }

    /**
     * expand checkout session.
     *
     * @param sessionId checkout session id
     * @return Session object
     */
    public Session expandSession(String sessionId) {
        try {
            SessionRetrieveParams params =
                SessionRetrieveParams.builder()
                    .addExpand("customer")
                    .addExpand("subscription")
                    .build();

            return Session.retrieve(sessionId, params, null);
        } catch (StripeException e) {
            throw new RuntimeException("can't expand checkout session, please check request params",
                e);
        }
    }

    /**
     * create customer portal.
     *
     * @param customerId stripe customer id
     * @param returnUrl  back url
     * @return portal access url
     */
    public String createPortalUrl(String customerId, String returnUrl) {
        SessionCreateParams params =
            SessionCreateParams
                .builder()
                .setCustomer(customerId)
                .setReturnUrl(returnUrl)
                .build();

        try {
            com.stripe.model.billingportal.Session session =
                com.stripe.model.billingportal.Session.create(params);
            return session.getUrl();
        } catch (StripeException e) {
            throw new RuntimeException("create customer portal error", e);
        }
    }

    /**
     * expand subscription object.
     *
     * @param subscriptionId subscription id
     * @return Subscription object
     */
    public Subscription expandSubscription(String subscriptionId) {
        try {
            SubscriptionRetrieveParams params =
                SubscriptionRetrieveParams.builder()
                    .addExpand("customer")
                    .build();

            return Subscription.retrieve(subscriptionId, params, null);
        } catch (StripeException e) {
            throw new RuntimeException("can't expand subscription, please check request params.",
                e);
        }
    }

    /**
     * retrieve a subscription.
     *
     * @param stripeSubscriptionId stripe subscription id
     * @return Subscription Object
     */
    public Subscription retrieveSubscription(String stripeSubscriptionId, String... expandKeys) {
        try {
            if (expandKeys.length > 0) {
                SubscriptionRetrieveParams retrieveParams = SubscriptionRetrieveParams.builder()
                    .addAllExpand(Arrays.asList(expandKeys))
                    .build();
                return Subscription.retrieve(stripeSubscriptionId, retrieveParams, null);
            }
            return Subscription.retrieve(stripeSubscriptionId);
        } catch (StripeException e) {
            throw new RuntimeException(
                String.format("fail to retrieve subscription %s", stripeSubscriptionId), e);
        }
    }

    /**
     * list subscriptions.
     *
     * @param params list params
     * @return SubscriptionCollection
     */
    public Iterable<Subscription> listSubscriptions(SubscriptionListParams params) {
        try {
            return Subscription.list(params).autoPagingIterable();
        } catch (StripeException e) {
            throw new RuntimeException("list subscriptions error", e);
        }
    }

    public long getFirstSubscriptionItemQuantity(String stripeSubscriptionId) {
        Subscription subscription = retrieveSubscription(stripeSubscriptionId);
        return subscription.getItems().getData().get(0).getQuantity();
    }

    /**
     * update subscription item quantity.
     *
     * @param stripeSubscriptionId stripe subscription id
     * @param newQuantity          new quantity in subscription item
     */
    public void updateSubscriptionQuantity(String stripeSubscriptionId, long newQuantity) {
        Subscription subscription = retrieveSubscription(stripeSubscriptionId);

        SubscriptionUpdateParams params =
            SubscriptionUpdateParams.builder()
                .setProrationBehavior(SubscriptionUpdateParams.ProrationBehavior.ALWAYS_INVOICE)
                .addItem(
                    SubscriptionUpdateParams.Item.builder()
                        .setId(subscription.getItems().getData().get(0).getId())
                        .setQuantity(newQuantity)
                        .build())
                .build();

        try {
            subscription.update(params);
        } catch (StripeException e) {
            throw new RuntimeException(
                String.format("fail to update subscription %s", stripeSubscriptionId), e);
        }
    }

    /**
     * cancel subscription.
     *
     * @param stripeSubscriptionId stripe subscription id
     */
    public void cancelSubscription(String stripeSubscriptionId) {
        Subscription subscription = retrieveSubscription(stripeSubscriptionId);
        SubscriptionCancelParams cancelParams = SubscriptionCancelParams.builder()
            .setCancellationDetails(
                SubscriptionCancelParams.CancellationDetails.builder()
                    .setComment("user count deleted")
                    .build()
            )
            .build();
        try {
            subscription.cancel(cancelParams);
        } catch (StripeException e) {
            log.error("fail to cancel stripe subscription : {}", stripeSubscriptionId, e);
        }
    }

    /**
     * retrieve customer.
     *
     * @param customerId stripe customer id
     * @return Customer Object
     */
    public Customer retrieveCustomer(String customerId, String... expandKeys) {
        try {
            if (expandKeys.length > 0) {
                CustomerRetrieveParams params = CustomerRetrieveParams.builder()
                    .addAllExpand(Arrays.asList(expandKeys))
                    .build();
                return Customer.retrieve(customerId, params, null);
            }
            return Customer.retrieve(customerId);
        } catch (StripeException e) {
            log.error(String.format("fail to retrieve customer %s", customerId),
                e);
        }
        return null;
    }

    /**
     * create stripe customer.
     *
     * @param name     customer name
     * @param email    customer email
     * @param metadata metadata
     * @param coupon   coupon
     * @return stripe customer id
     */
    public Customer createCustomer(String name, String email, Map<String, String> metadata,
                                   String coupon) {
        CustomerCreateParams params =
            CustomerCreateParams
                .builder()
                .setName(name)
                .setEmail(email)
                .setMetadata(metadata)
                .setCoupon(coupon)
                .build();

        try {
            return Customer.create(params);
        } catch (StripeException e) {
            throw new RuntimeException(String.format("fail to create stripe customer by %s", email),
                e);
        }
    }

    /**
     * update customer.
     *
     * @param customerId stripe customer id
     * @param metadata   metadata
     * @param coupon     coupon
     * @return Customer Object
     */
    public Customer updateCustomerIfSatisfy(String customerId, Map<String, String> metadata,
                                            String coupon) {
        try {
            Customer customer = Customer.retrieve(customerId);
            if (metadata != null && coupon != null) {
                return customer.update(
                    CustomerUpdateParams.builder()
                        .setMetadata(metadata)
                        .setCoupon(coupon)
                        .build()
                );
            }
            return customer;
        } catch (StripeException e) {
            log.warn("fail to update customer {}", customerId);
            return null;
        }
    }

    /**
     * delete customer.
     *
     * @param customerId stripe customer id
     */
    public void deleteCustomer(String customerId) {
        try {
            Customer customer = Customer.retrieve(customerId);
            customer.delete();
        } catch (StripeException e) {
            throw new RuntimeException(
                String.format("can not delete stripe customer by %s", customerId), e);
        }
    }

    /**
     * create checkout session.
     *
     * @param params session create params
     * @return Session Object
     * @throws StripeException throw StripeException if create session failed
     */
    public Session createSession(com.stripe.param.checkout.SessionCreateParams params)
        throws StripeException {
        return Session.create(params);
    }

    /**
     * create subscription with trial period.
     *
     * @param subscriptionCreation subscription create parameter
     */
    @Deprecated(since = "1.7.0", forRemoval = true)
    public void createSubscriptionWithTrialPeriod(SubscriptionCreation subscriptionCreation) {
        String trialPriceId = subscriptionCreation.getStripePriceId();
        long defaultQuantity = 1L;
        if (StrUtil.isBlank(subscriptionCreation.getStripePriceId())) {
            throw new RuntimeException("price id can not be null");
        }
        SubscriptionCreateParams params =
            SubscriptionCreateParams
                .builder()
                .setCustomer(subscriptionCreation.getStripeCustomerId())
                .addItem(
                    SubscriptionCreateParams.Item.builder()
                        .setPrice(trialPriceId)
                        .setQuantity(defaultQuantity)
                        .build()
                )
                .setTrialPeriodDays(subscriptionCreation.getTrialPeriodDays())
                .setPaymentSettings(
                    SubscriptionCreateParams.PaymentSettings
                        .builder()
                        .setSaveDefaultPaymentMethod(
                            SubscriptionCreateParams.PaymentSettings.SaveDefaultPaymentMethod.ON_SUBSCRIPTION
                        )
                        .build()
                )
                .setTrialSettings(
                    SubscriptionCreateParams.TrialSettings
                        .builder()
                        .setEndBehavior(
                            SubscriptionCreateParams.TrialSettings.EndBehavior
                                .builder()
                                .setMissingPaymentMethod(
                                    SubscriptionCreateParams.TrialSettings.EndBehavior.MissingPaymentMethod.CANCEL
                                )
                                .build()
                        )
                        .build()
                )
                .setMetadata(subscriptionCreation.getMetadata())
                .build();

        try {
            Subscription.create(params);
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * retrieve a coupon.
     *
     * @param couponId stripe coupon id
     * @return Coupon Object
     */
    public Coupon retrieveCoupon(String couponId) {
        try {
            return Coupon.retrieve(couponId);
        } catch (StripeException e) {
            log.error("fail to retrieve coupon {}", couponId);
            return null;
        }
    }

    /**
     * list customer's invoices in paging.
     *
     * @param customerId    customer id
     * @param startingAfter starting after
     * @param endingBefore  ending before
     * @param limit         limit
     * @return InvoiceCollection
     */
    public InvoiceCollection listCustomerInvoices(String customerId,
                                                  String startingAfter,
                                                  String endingBefore,
                                                  long limit) {
        InvoiceListParams params = InvoiceListParams.builder()
            .setCustomer(customerId)
            .setStartingAfter(startingAfter)
            .setEndingBefore(endingBefore)
            .setLimit(limit)
            .setStatus(InvoiceListParams.Status.PAID)
            .build();
        try {
            return Invoice.list(params);
        } catch (StripeException e) {
            throw new RuntimeException("List customer's invoices error", e);
        }
    }
}
