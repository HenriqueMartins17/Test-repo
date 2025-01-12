package com.apitable.enterprise.selfhostbilling.interfaces.model;

import com.apitable.interfaces.billing.model.SubscriptionFeature;
import com.apitable.interfaces.billing.model.SubscriptionInfo;
import java.time.LocalDate;

/**
 * selfhost subscription info.
 *
 * @author Shawn Deng
 */
public class SelfhostSubscriptionInfo implements SubscriptionInfo {

    private final LocalDate endDate;

    private final SubscriptionFeature feature = new SelfHostSubscriptionFeature();

    public SelfhostSubscriptionInfo(LocalDate endDate) {
        this.endDate = endDate;
    }

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
    public LocalDate getEndDate() {
        return this.endDate;
    }

    @Override
    public SubscriptionFeature getFeature() {
        return this.feature;
    }
}
