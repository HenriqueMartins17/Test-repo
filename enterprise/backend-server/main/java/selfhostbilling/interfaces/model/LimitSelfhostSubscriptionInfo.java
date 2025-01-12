package com.apitable.enterprise.selfhostbilling.interfaces.model;

import com.apitable.interfaces.billing.model.SubscriptionFeature;
import com.apitable.interfaces.billing.model.SubscriptionInfo;

/**
 * limit selfhost subscription info.
 *
 * @author Shawn Deng
 */
public class LimitSelfhostSubscriptionInfo implements SubscriptionInfo {

    private final SubscriptionFeature feature = new LimitSelfHostSubscriptionFeature();

    @Override
    public String getProduct() {
        return "Private_Cloud";
    }

    @Override
    public boolean isFree() {
        return false;
    }

    @Override
    public boolean onTrial() {
        return false;
    }

    @Override
    public String getBasePlan() {
        return null;
    }

    @Override
    public SubscriptionFeature getFeature() {
        return this.feature;
    }
}
