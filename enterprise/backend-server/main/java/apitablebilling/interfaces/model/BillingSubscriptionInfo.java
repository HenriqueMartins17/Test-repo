/*
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its subdirectories does not constitute permission to use this code or APITable Enterprise Edition features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

package com.apitable.enterprise.apitablebilling.interfaces.model;

import static com.apitable.enterprise.apitablebilling.enums.ProductEnum.FREE;
import static com.apitable.enterprise.stripe.config.ProductCatalogFactory.getProductFeature;

import com.apitable.enterprise.apitablebilling.core.Bundle;
import com.apitable.enterprise.apitablebilling.enums.ProductEnum;
import com.apitable.interfaces.billing.model.SubscriptionFeature;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.ConsumeFeatures.CapacitySize;
import com.apitable.interfaces.billing.model.SubscriptionInfo;
import java.time.LocalDate;
import java.util.List;

/**
 * implement subscription info.
 */
public class BillingSubscriptionInfo implements SubscriptionInfo {

    private final Bundle bundle;

    private final String product;

    private final boolean isFree;

    private final boolean isTrialing;

    private String billingMode;

    private String recurringInterval;

    private final LocalDate startDate;

    private LocalDate endDate;

    private final SubscriptionFeature feature;

    private final CapacitySize giftCapacity;

    public BillingSubscriptionInfo() {
        this(null, FREE.getName(), FREE.isFree(), false, null, null,
            getProductFeature(FREE).safeConvert());
    }

    public BillingSubscriptionInfo(Bundle bundle, ProductEnum product,
                                   boolean isTrialing,
                                   LocalDate startDate,
                                   LocalDate endDate, SubscriptionFeature feature) {
        this(bundle, product.getName(), product.isFree(), isTrialing, startDate, endDate, feature);
    }


    /**
     * construct.
     *
     * @param product    product name from ProductEnum
     * @param isFree     whether is free
     * @param isTrialing whether is trialing
     * @param startDate  period start
     * @param endDate    period end
     * @param feature    plan feature map
     */
    public BillingSubscriptionInfo(Bundle bundle, String product, boolean isFree,
                                   boolean isTrialing,
                                   LocalDate startDate,
                                   LocalDate endDate, SubscriptionFeature feature) {
        this.bundle = bundle;
        this.product = product;
        this.isFree = isFree;
        this.isTrialing = isTrialing;
        this.startDate = startDate;
        this.endDate = endDate;
        this.feature = feature;
        this.giftCapacity = new CapacitySize(0L);
    }

    public Bundle getBundle() {
        return bundle;
    }

    @Override
    public String getProduct() {
        return product;
    }

    @Override
    public boolean isFree() {
        return isFree;
    }

    @Override
    public String getBillingMode() {
        return billingMode;
    }

    public void setBillingMode(String billingMode) {
        this.billingMode = billingMode;
    }

    @Override
    public String getRecurringInterval() {
        return recurringInterval;
    }

    public void setRecurringInterval(String recurringInterval) {
        this.recurringInterval = recurringInterval;
    }

    @Override
    public boolean onTrial() {
        return this.isTrialing;
    }

    @Override
    public String getBasePlan() {
        return null;
    }

    @Override
    public List<String> getAddOnPlans() {
        return null;
    }

    @Override
    public LocalDate getStartDate() {
        return startDate;
    }

    @Override
    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    @Override
    public SubscriptionFeature getFeature() {
        return feature;
    }

    @Override
    public CapacitySize getGiftCapacity() {
        return giftCapacity;
    }

}
