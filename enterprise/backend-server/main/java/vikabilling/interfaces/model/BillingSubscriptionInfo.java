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

package com.apitable.enterprise.vikabilling.interfaces.model;

import static com.apitable.enterprise.vikabilling.util.BillingConfigManager.buildPlanFeature;
import static com.apitable.enterprise.vikabilling.util.BillingConfigManager.getBillingConfig;

import com.apitable.interfaces.billing.model.SubscriptionConfig;
import com.apitable.interfaces.billing.model.SubscriptionFeature;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.ConsumeFeatures.CapacitySize;
import com.apitable.interfaces.billing.model.SubscriptionInfo;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * subscription info implementation by vika.
 */
public class BillingSubscriptionInfo implements SubscriptionInfo {

    private final String product;

    private final boolean isFree;

    private final boolean onTrial;

    private final String basePlan;

    private final List<String> addOnPlans;

    private final LocalDate startDate;

    private LocalDate endDate;

    private final SubscriptionFeature feature;

    private final CapacitySize giftCapacity;

    private final SubscriptionConfig config;

    /**
     * constructor.
     *
     * @param product    product
     * @param basePlan   base plan
     * @param addOnPlans add-on plans
     */
    public BillingSubscriptionInfo(String product, String basePlan, List<String> addOnPlans) {
        this(product, true, false, basePlan, addOnPlans, null, null,
            buildPlanFeature(getBillingConfig().getPlans().get(basePlan),
                addOnPlans.stream().map(plan -> getBillingConfig().getPlans().get(plan))
                    .collect(Collectors.toList())), SubscriptionConfig.create().setAllowCreditOverLimit(true));
    }

    /**
     * constructor.
     *
     * @param product    product
     * @param isFree     is free
     * @param onTrial    on trial
     * @param basePlan   base plan
     * @param addOnPlans add-on plans
     * @param startDate  start date
     * @param endDate    end date
     */
    public BillingSubscriptionInfo(String product, boolean isFree, boolean onTrial, String basePlan,
                                   List<String> addOnPlans, LocalDate startDate,
                                   LocalDate endDate) {
        this(product, isFree, onTrial, basePlan, addOnPlans, startDate, endDate,
            buildPlanFeature(getBillingConfig().getPlans().get(basePlan),
                addOnPlans.stream().map(plan -> getBillingConfig().getPlans().get(plan))
                    .collect(Collectors.toList())), SubscriptionConfig.create().setAllowCreditOverLimit(true));
    }

    /**
     * constructor.
     *
     * @param product    product
     * @param isFree     is free
     * @param onTrial    trial
     * @param basePlan   base plan
     * @param addOnPlans add-on plans
     * @param startDate  start date
     * @param endDate    end date
     * @param feature    feature
     */
    public BillingSubscriptionInfo(String product, boolean isFree, boolean onTrial, String basePlan,
                                   List<String> addOnPlans, LocalDate startDate, LocalDate endDate,
                                   SubscriptionFeature feature, SubscriptionConfig config) {
        this.product = product;
        this.isFree = isFree;
        this.onTrial = onTrial;
        this.basePlan = basePlan;
        this.addOnPlans = addOnPlans;
        this.startDate = startDate;
        this.endDate = endDate;
        this.feature = feature;
        this.giftCapacity = new CapacitySize(0L);
        this.config = config;
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
    public boolean onTrial() {
        return onTrial;
    }

    @Override
    public String getBasePlan() {
        return basePlan;
    }

    @Override
    public List<String> getAddOnPlans() {
        return addOnPlans;
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

    @Override
    public SubscriptionConfig getConfig() {
        return this.config;
    }
}
