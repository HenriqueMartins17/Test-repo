package com.apitable.enterprise.selfhostbilling.service;

import com.apitable.interfaces.billing.model.SubscriptionFeature;
import com.apitable.interfaces.billing.model.SubscriptionInfo;

import java.util.List;
import java.util.Map;

/**
 * entitlement service interface.
 *
 * @author Shawn Deng
 */
public interface ISelfhostEntitlementService {

    /**
     * get entitlement by space id.
     *
     * @param spaceId space id
     * @return SubscriptionInfo
     */
    SubscriptionInfo getEntitlementBySpaceId(String spaceId);

    /**
     * get subscription feature by spaces.
     *
     * @param spaceIds space id list
     * @return SubscriptionFeature map
     */
    Map<String, SubscriptionFeature> getSubscriptionFeatureBySpaceIds(
            List<String> spaceIds);
}
