package com.apitable.enterprise.apitablebilling.interfaces.facade;

import com.apitable.enterprise.apitablebilling.service.IEntitlementService;
import com.apitable.interfaces.billing.facade.EntitlementServiceFacade;
import com.apitable.interfaces.billing.model.SubscriptionFeature;
import com.apitable.interfaces.billing.model.SubscriptionInfo;
import java.util.List;
import java.util.Map;

/**
 * apitable entitlement service facade.
 */
public class EnterpriseEntitlementServiceFacadeImpl implements EntitlementServiceFacade {

    private final IEntitlementService iEntitlementService;

    public EnterpriseEntitlementServiceFacadeImpl(IEntitlementService entitlementService) {
        this.iEntitlementService = entitlementService;
    }

    @Override
    public SubscriptionInfo getSpaceSubscription(String spaceId) {
        return iEntitlementService.getEntitlementBySpaceId(spaceId);
    }

    @Override
    public Map<String, SubscriptionFeature> getSpaceSubscriptions(List<String> spaceIds) {
        return iEntitlementService.getSubscriptionFeatureBySpaceIds(spaceIds);
    }

    @Override
    public void cancelSubscription(String spaceId) {
        iEntitlementService.cancelSubscription(spaceId);
    }
}
