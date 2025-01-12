package com.apitable.enterprise.apitablebilling.service;

import com.apitable.enterprise.apitablebilling.enums.UpdateSubscriptionAction;
import com.apitable.enterprise.apitablebilling.model.dto.PaginationRequest;
import com.apitable.enterprise.apitablebilling.model.vo.BillingInfo;
import com.apitable.enterprise.apitablebilling.model.vo.CustomerInvoices;
import java.util.Map;

/**
 * billing service interface.
 *
 * @author Shawn Deng
 */
public interface IBillingService {

    /**
     * get space billing info.
     *
     * @param spaceId space id
     * @return BillingInfo
     */
    BillingInfo getBillingInfo(String spaceId);

    /**
     * get stripe customer portal url.
     *
     * @param spaceId          space id
     * @param externalProperty external property
     * @return portal url
     */
    String getCustomerPortalUrl(String spaceId, Map<String, String> externalProperty);

    /**
     * get invoice in page.
     *
     * @param spaceId           space id
     * @param paginationRequest pagination request
     * @return CustomerInvoices
     */
    CustomerInvoices getInvoices(String spaceId, PaginationRequest paginationRequest);

    /**
     * get change payment method url of stripe.
     *
     * @param spaceId          space id
     * @param externalProperty external property
     * @return customer portal url
     */
    String getChangePaymentMethodUrl(String spaceId, Map<String, String> externalProperty);

    /**
     * get special subscription link by action.
     *
     * @param spaceId          space id
     * @param subscriptionId   subscription id
     * @param action           update subscription action
     * @param externalProperty external property
     * @return url
     */
    String getCustomerUpdateSubscriptionLink(String spaceId, String subscriptionId,
                                             UpdateSubscriptionAction action,
                                             Map<String, String> externalProperty);

    /**
     * get update subscription url of stripe.
     *
     * @param spaceId        space id
     * @param subscriptionId subscription id in subscription table
     * @param confirmPriceId change price id
     * @return customer portal url
     */
    @Deprecated
    String getCustomerUpdateSubscriptionConfirmLink(String spaceId, String subscriptionId,
                                                    String confirmPriceId);

    /**
     * get cancel subscription url of stripe.
     *
     * @param spaceId        space id
     * @param subscriptionId subscription id
     * @return customer portal url
     */
    String getCancelSubscriptionLink(String spaceId, String subscriptionId);
}
