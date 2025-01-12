package com.apitable.enterprise.apitablebilling.service.impl;

import static com.apitable.enterprise.stripe.config.ProductCatalogFactory.reverseInterval;

import com.apitable.core.exception.BusinessException;
import com.apitable.core.util.DateTimeUtil;
import com.apitable.enterprise.apitablebilling.appsumo.util.AppsumoLicenseConfigUtil;
import com.apitable.enterprise.apitablebilling.entity.SubscriptionEntity;
import com.apitable.enterprise.apitablebilling.enums.ProductEnum;
import com.apitable.enterprise.apitablebilling.enums.RecurringInterval;
import com.apitable.enterprise.apitablebilling.enums.UpdateSubscriptionAction;
import com.apitable.enterprise.apitablebilling.interfaces.model.BillingSubscriptionInfo;
import com.apitable.enterprise.apitablebilling.model.dto.PaginationRequest;
import com.apitable.enterprise.apitablebilling.model.vo.BillingDetail;
import com.apitable.enterprise.apitablebilling.model.vo.BillingInfo;
import com.apitable.enterprise.apitablebilling.model.vo.CustomerInvoices;
import com.apitable.enterprise.apitablebilling.model.vo.PaymentMethodDetail;
import com.apitable.enterprise.apitablebilling.rewardful.RewardfulService;
import com.apitable.enterprise.apitablebilling.rewardful.model.RewardfulData;
import com.apitable.enterprise.apitablebilling.service.IBillingService;
import com.apitable.enterprise.apitablebilling.service.IEntitlementService;
import com.apitable.enterprise.apitablebilling.service.IStripeCustomerService;
import com.apitable.enterprise.apitablebilling.service.ISubscriptionInApitableService;
import com.apitable.enterprise.stripe.core.StripeTemplate;
import com.apitable.interfaces.billing.model.SubscriptionInfo;
import com.apitable.shared.config.properties.ConstProperties;
import com.stripe.exception.StripeException;
import com.stripe.model.Card;
import com.stripe.model.Customer;
import com.stripe.model.InvoiceCollection;
import com.stripe.model.PaymentMethod;
import com.stripe.model.Price;
import com.stripe.model.Subscription;
import com.stripe.model.billingportal.Session;
import com.stripe.param.billingportal.SessionCreateParams;
import jakarta.annotation.Resource;
import java.time.ZoneOffset;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * billing service implements.
 *
 * @author Shawn Deng
 */
@Service
public class BillingServiceImpl implements IBillingService {

    @Resource
    private ConstProperties constProperties;

    @Resource
    private IEntitlementService iEntitlementService;

    @Autowired(required = false)
    private StripeTemplate stripeTemplate;

    @Resource
    private IStripeCustomerService iStripeCustomerService;

    @Resource
    private ISubscriptionInApitableService iSubscriptionInApitableService;

    @Resource
    private RewardfulService rewardfulService;

    @Override
    public BillingInfo getBillingInfo(String spaceId) {
        BillingSubscriptionInfo subscriptionInfo =
            (BillingSubscriptionInfo) iEntitlementService.getEntitlementBySpaceId(spaceId);

        BillingInfo info = new BillingInfo();
        info.setPlanName(subscriptionInfo.getProduct());
        info.setTrial(subscriptionInfo.onTrial());
        if (subscriptionInfo.getBundle() != null) {
            info.setInterval(subscriptionInfo.getRecurringInterval());
            info.setChargedThroughDate(
                DateTimeUtil.localDateToSecond(subscriptionInfo.getEndDate(), ZoneOffset.UTC));
            info.setSubscriptionId(
                subscriptionInfo.getBundle().getBaseSubscription().getSubscriptionId()
            );
            if (ProductEnum.isAppsumoProduct(subscriptionInfo.getProduct())) {
                info.setPlanName(AppsumoLicenseConfigUtil.findProductNameByProductId(
                    subscriptionInfo.getProduct()));
                return info;
            }
            if (ProductEnum.isExclusiveLimitProduct(subscriptionInfo.getProduct())) {
                info.setPlanName(subscriptionInfo.getProduct());
                return info;
            }
            Price price = stripeTemplate.retrievePrice(
                subscriptionInfo.getBundle().getBaseSubscription().getPriceId(), "tiers");
            info.setPrice(com.apitable.enterprise.stripe.config.Price.of(price));

            // query stripe subscription model
            Subscription subscription = stripeTemplate.retrieveSubscription(
                subscriptionInfo.getBundle().getBaseSubscription().getStripeId(),
                "default_source",
                "default_payment_method",
                "customer.invoice_settings.default_payment_method"
            );

            if (subscription == null) {
                throw new RuntimeException("can not find subscription");
            }

            Customer customer = subscription.getCustomerObject();

            // set credit balance
            info.setCredit(customer.getBalance());

            BillingDetail billingDetail = new BillingDetail();
            billingDetail.setName(customer.getName());
            billingDetail.setEmail(customer.getEmail());
            info.setBillingDetail(billingDetail);

            PaymentMethodDetail paymentMethodDetail = new PaymentMethodDetail();

            if (subscription.getDefaultPaymentMethodObject() != null) {
                PaymentMethod paymentMethod = subscription.getDefaultPaymentMethodObject();
                if ("card".equals(paymentMethod.getType())) {
                    PaymentMethod.Card card = paymentMethod.getCard();
                    paymentMethodDetail.setBrand(card.getBrand());
                    paymentMethodDetail.setType("card");
                    paymentMethodDetail.setLast4(card.getLast4());
                }
            } else if (subscription.getDefaultSourceObject() != null) {
                Card card = (Card) subscription.getDefaultSourceObject();
                paymentMethodDetail.setBrand(card.getBrand());
                paymentMethodDetail.setType("card");
                paymentMethodDetail.setLast4(card.getLast4());
            } else {
                Customer.InvoiceSettings invoiceSettings = customer.getInvoiceSettings();
                if (invoiceSettings.getDefaultPaymentMethodObject() != null) {
                    PaymentMethod paymentMethod = invoiceSettings.getDefaultPaymentMethodObject();
                    if ("card".equals(paymentMethod.getType())) {
                        PaymentMethod.Card card = paymentMethod.getCard();
                        paymentMethodDetail.setBrand(card.getBrand());
                        paymentMethodDetail.setType("card");
                        paymentMethodDetail.setLast4(card.getLast4());
                    }
                }
            }

            info.setPaymentMethodDetail(paymentMethodDetail);
        } else {
            // find whether current customer has balance credit value
            String stripeCustomerId = iStripeCustomerService.getFirstCustomerIdBySpaceId(spaceId);
            if (stripeCustomerId != null) {
                Customer customer = stripeTemplate.retrieveCustomer(stripeCustomerId);
                if (customer != null) {
                    info.setCredit(customer.getBalance());
                    BillingDetail billingDetail = new BillingDetail();
                    billingDetail.setName(customer.getName());
                    billingDetail.setEmail(customer.getEmail());
                    info.setBillingDetail(billingDetail);
                }
            }
        }
        return info;
    }

    @Override
    public String getCustomerPortalUrl(String spaceId, Map<String, String> externalProperty) {
        String customerId = iStripeCustomerService.getFirstCustomerIdOrThrow(spaceId,
            () -> new RuntimeException("error get customer of space"));
        // get space entitlement
        SubscriptionInfo subscriptionInfo = iEntitlementService.getEntitlementBySpaceId(spaceId);
        if (subscriptionInfo.isFree()) {
            throw new BusinessException("not subscription exist");
        }
        // extract rewardful data
        RewardfulData rewardfulData = rewardfulService.extractData(externalProperty);
        // find customer
        Customer customer = stripeTemplate.updateCustomerIfSatisfy(customerId,
            rewardfulData.getCustomerMetadata(), rewardfulData.getCoupon());
        // not free subscription, may be upgrade and downgrade operation
        // create customer portal using the Stripe API
        String returnUrl = constProperties.getServerDomain() + "/management/billing";
        return stripeTemplate.createPortalUrl(customer.getId(), returnUrl);
    }

    @Override
    public CustomerInvoices getInvoices(String spaceId, PaginationRequest paginationRequest) {
        // get customer of space
        String customerId = iStripeCustomerService.getFirstCustomerIdBySpaceId(spaceId);
        if (customerId == null) {
            return new CustomerInvoices();
        }
        InvoiceCollection invoiceCollection =
            stripeTemplate.listCustomerInvoices(customerId, paginationRequest.getStartingAfter(),
                paginationRequest.getEndingBefore(), paginationRequest.getLimit());
        CustomerInvoices customerInvoices = new CustomerInvoices();
        customerInvoices.setHasMore(invoiceCollection.getHasMore());
        customerInvoices.addInvoices(invoiceCollection.getData());
        return customerInvoices;
    }

    @Override
    public String getChangePaymentMethodUrl(String spaceId, Map<String, String> externalProperty) {
        String customerId = iStripeCustomerService.getFirstCustomerIdOrThrow(spaceId,
            () -> new RuntimeException("error get customer of space"));
        // extract rewardful data
        RewardfulData rewardfulData = rewardfulService.extractData(externalProperty);
        // find customer
        Customer customer = stripeTemplate.updateCustomerIfSatisfy(customerId,
            rewardfulData.getCustomerMetadata(), rewardfulData.getCoupon());
        SessionCreateParams params =
            SessionCreateParams.builder()
                .setCustomer(customer.getId())
                .setReturnUrl(constProperties.getServerDomain() + "/management/billing")
                .setFlowData(
                    SessionCreateParams.FlowData.builder()
                        .setType(SessionCreateParams.FlowData.Type.PAYMENT_METHOD_UPDATE)
                        .build()
                )
                .build();

        try {
            Session session = Session.create(params);
            return session.getUrl();
        } catch (StripeException e) {
            throw new RuntimeException("get change payment method link error", e);
        }
    }

    @Override
    public String getCustomerUpdateSubscriptionLink(String spaceId, String subscriptionId,
                                                    UpdateSubscriptionAction action,
                                                    Map<String, String> externalProperty) {
        // find customer id in space
        String customerId = iStripeCustomerService.getFirstCustomerIdOrThrow(spaceId,
            () -> new RuntimeException("error get customer of space"));
        // found subscription in system
        SubscriptionEntity subscription =
            iSubscriptionInApitableService.getBySubscriptionId(subscriptionId);
        if (subscription == null) {
            throw new RuntimeException(
                "subscription not found, wrong subscription id: " + subscriptionId);
        }

        // extract rewardful data
        RewardfulData rewardfulData = rewardfulService.extractData(externalProperty);
        // find customer
        Customer customer = stripeTemplate.updateCustomerIfSatisfy(customerId,
            rewardfulData.getCustomerMetadata(), rewardfulData.getCoupon());

        if (action == UpdateSubscriptionAction.INTERVAL) {
            // find another interval of this price
            // found price
            Price stripePrice =
                stripeTemplate.retrievePrice(subscription.getPriceId());
            if (stripePrice == null) {
                throw new RuntimeException(
                    "price not found, subscription without price: " + subscription.getPriceId());
            }
            RecurringInterval interval =
                RecurringInterval.of(stripePrice.getRecurring().getInterval());
            String reversePriceId =
                reverseInterval(stripePrice.getProduct(), interval);
            if (reversePriceId == null) {
                throw new RuntimeException(
                    "another price interval not found, without this price: "
                        + subscription.getPriceId());
            }
            SessionCreateParams params =
                SessionCreateParams.builder()
                    .setCustomer(customer.getId())
                    .setReturnUrl(constProperties.getServerDomain() + "/management/billing")
                    .setFlowData(
                        SessionCreateParams.FlowData.builder()
                            .setType(SessionCreateParams.FlowData.Type.SUBSCRIPTION_UPDATE_CONFIRM)
                            .setSubscriptionUpdateConfirm(
                                SessionCreateParams.FlowData.SubscriptionUpdateConfirm.builder()
                                    .setSubscription(subscription.getStripeId())
                                    .addItem(
                                        SessionCreateParams.FlowData.SubscriptionUpdateConfirm.Item.builder()
                                            .setId(subscription.getStripeSubId())
                                            .setPrice(reversePriceId)
                                            .build()
                                    )
                                    .build()
                            )
                            .build()
                    )
                    .build();

            try {
                Session session = Session.create(params);
                return session.getUrl();
            } catch (StripeException e) {
                throw new RuntimeException("fail to get update subscription interval portal url",
                    e);
            }
        } else {
            SessionCreateParams params =
                SessionCreateParams.builder()
                    .setCustomer(customer.getId())
                    .setReturnUrl(constProperties.getServerDomain() + "/management/billing")
                    .setFlowData(
                        SessionCreateParams.FlowData.builder()
                            .setType(SessionCreateParams.FlowData.Type.SUBSCRIPTION_UPDATE)
                            .setSubscriptionUpdate(
                                SessionCreateParams.FlowData.SubscriptionUpdate.builder()
                                    .setSubscription(subscription.getStripeId())
                                    .build()
                            )
                            .build()
                    )
                    .build();
            try {
                Session session = Session.create(params);
                return session.getUrl();
            } catch (StripeException e) {
                throw new RuntimeException("fail to get update subscription portal url", e);
            }
        }
    }

    @Override
    public String getCustomerUpdateSubscriptionConfirmLink(String spaceId, String subscriptionId,
                                                           String confirmPriceId) {
        // find customer id in space
        String customerId = iStripeCustomerService.getFirstCustomerIdOrThrow(spaceId,
            () -> new RuntimeException("error get customer of space"));

        // found subscription in system
        SubscriptionEntity subscription =
            iSubscriptionInApitableService.getBySubscriptionId(subscriptionId);
        if (subscription == null) {
            throw new RuntimeException(
                "subscription not found, wrong subscription id: " + subscriptionId);
        }
        // found price
        Price confirmPrice = stripeTemplate.retrievePrice(confirmPriceId);
        if (confirmPrice == null) {
            throw new RuntimeException("price not found, wrong price id: " + confirmPriceId);
        }
        SessionCreateParams params =
            SessionCreateParams.builder()
                .setCustomer(customerId)
                .setReturnUrl(constProperties.getServerDomain() + "/management/billing")
                .setFlowData(
                    SessionCreateParams.FlowData.builder()
                        .setType(SessionCreateParams.FlowData.Type.SUBSCRIPTION_UPDATE_CONFIRM)
                        .setSubscriptionUpdateConfirm(
                            SessionCreateParams.FlowData.SubscriptionUpdateConfirm.builder()
                                .setSubscription(subscription.getStripeId())
                                .addItem(
                                    SessionCreateParams.FlowData.SubscriptionUpdateConfirm.Item.builder()
                                        .setId(subscription.getStripeSubId())
                                        .setPrice(confirmPrice.getId())
                                        .build()
                                )
                                .build()
                        )
                        .build()
                )
                .build();

        try {
            Session session = Session.create(params);
            return session.getUrl();
        } catch (StripeException e) {
            throw new RuntimeException("fail to get update subscription confirm portal url", e);
        }
    }

    @Override
    public String getCancelSubscriptionLink(String spaceId, String subscriptionId) {
        String customerId = iStripeCustomerService.getFirstCustomerIdOrThrow(spaceId,
            () -> new RuntimeException("error get customer of space"));
        // found subscription in system
        SubscriptionEntity subscription =
            iSubscriptionInApitableService.getBySubscriptionId(subscriptionId);
        if (subscription == null) {
            throw new RuntimeException(
                "subscription not found, wrong subscription id: " + subscriptionId);
        }
        SessionCreateParams params =
            SessionCreateParams.builder()
                .setCustomer(customerId)
                .setReturnUrl(constProperties.getServerDomain() + "/management/billing")
                .setFlowData(
                    SessionCreateParams.FlowData.builder()
                        .setType(SessionCreateParams.FlowData.Type.SUBSCRIPTION_CANCEL)
                        .setSubscriptionCancel(
                            SessionCreateParams.FlowData.SubscriptionCancel.builder()
                                .setSubscription(subscription.getStripeId())
                                .build()
                        )
                        .build()
                )
                .build();
        try {
            Session session = Session.create(params);
            return session.getUrl();
        } catch (StripeException e) {
            throw new RuntimeException("fail to get cancel subscription url", e);
        }
    }
}
