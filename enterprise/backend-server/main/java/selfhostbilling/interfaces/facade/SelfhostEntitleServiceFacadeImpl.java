package com.apitable.enterprise.selfhostbilling.interfaces.facade;

import com.apitable.enterprise.selfhostbilling.service.ISelfhostEntitlementService;
import com.apitable.interfaces.billing.facade.EntitlementServiceFacade;
import com.apitable.interfaces.billing.model.SubscriptionFeature;
import com.apitable.interfaces.billing.model.SubscriptionInfo;
import java.util.List;
import java.util.Map;

/**
 * selfhost entitlement service facade implements.
 *
 * @author Shawn Deng
 */
public class SelfhostEntitleServiceFacadeImpl implements EntitlementServiceFacade {

    private final ISelfhostEntitlementService entitlementService;

    public SelfhostEntitleServiceFacadeImpl(ISelfhostEntitlementService entitlementService) {
        this.entitlementService = entitlementService;
    }

    @Override
    public SubscriptionInfo getSpaceSubscription(String spaceId) {
        return entitlementService.getEntitlementBySpaceId(spaceId);
    }

    @Override
    public Map<String, SubscriptionFeature> getSpaceSubscriptions(List<String> spaceIds) {
        return entitlementService.getSubscriptionFeatureBySpaceIds(spaceIds);
    }
}
