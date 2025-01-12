package com.apitable.enterprise.aliyun.interfaces.model;

import com.apitable.interfaces.billing.model.SubscriptionFeature;
import com.apitable.interfaces.billing.model.SubscriptionInfo;

/**
 * aliyun subscription info.
 *
 * @author Shawn Deng
 */
public class AliyunSubscriptionInfo implements SubscriptionInfo {

    private final SubscriptionFeature feature = new AliyunSubscriptionFeature();

    @Override
    public String getProduct() {
        return "Atlas";
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
        return "atlas_unlimited";
    }

    @Override
    public SubscriptionFeature getFeature() {
        return this.feature;
    }
}
