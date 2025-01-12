package com.apitable.enterprise.aliyun.interfaces.facade;

import com.apitable.enterprise.aliyun.interfaces.model.AliyunSubscriptionInfo;
import com.apitable.interfaces.billing.facade.EntitlementServiceFacade;
import com.apitable.interfaces.billing.model.SubscriptionFeature;
import com.apitable.interfaces.billing.model.SubscriptionInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * aliyun entitlement service facade implements.
 *
 * @author Shawn Deng
 */
public class AliyunEntitleServiceFacadeImpl implements EntitlementServiceFacade {

    @Override
    public SubscriptionInfo getSpaceSubscription(String spaceId) {
        return new AliyunSubscriptionInfo();
    }

    @Override
    public Map<String, SubscriptionFeature> getSpaceSubscriptions(List<String> spaceIds) {
        Map<String, SubscriptionFeature> planFeatureMap = new HashMap<>(spaceIds.size());
        spaceIds.forEach(spaceId -> {
            SubscriptionInfo subscriptionInfo = getSpaceSubscription(spaceId);
            planFeatureMap.put(spaceId, subscriptionInfo.getFeature());
        });
        return planFeatureMap;
    }
}
