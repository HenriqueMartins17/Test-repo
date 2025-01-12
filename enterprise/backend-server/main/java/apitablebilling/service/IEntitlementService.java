package com.apitable.enterprise.apitablebilling.service;

import com.apitable.enterprise.apitablebilling.model.dto.EntitlementCreationDTO;
import com.apitable.interfaces.billing.model.SubscriptionFeature;
import com.apitable.interfaces.billing.model.SubscriptionInfo;
import java.util.List;
import java.util.Map;

/**
 * entitlement service interface.
 */
public interface IEntitlementService {

    /**
     * create subscription with trial.
     *
     * @param spaceId          space id
     * @param createdBy        creator
     * @param externalProperty external property map
     */
    @Deprecated(since = "1.7.0", forRemoval = true)
    void createEntitlementWithTrial(String spaceId, Long createdBy,
                                    Map<String, String> externalProperty);

    /**
     * create entitlement.
     *
     * @param entitlementCreationDTO parameter
     */
    void createEntitlement(EntitlementCreationDTO entitlementCreationDTO);


    /**
     * get entitlement.
     *
     * @param spaceId space id
     * @return SubscriptionInfo model
     */
    SubscriptionInfo getEntitlementBySpaceId(String spaceId);

    /**
     * get subscription feature by spaces.
     *
     * @param spaceIds space id list
     * @return SubscriptionFeature map
     */
    Map<String, SubscriptionFeature> getSubscriptionFeatureBySpaceIds(List<String> spaceIds);

    /**
     * cancel subscription.
     *
     * @param spaceId space id
     */
    void cancelSubscription(String spaceId);
}
