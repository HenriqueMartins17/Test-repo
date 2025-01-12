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

package com.apitable.enterprise.vikabilling.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;

import com.apitable.enterprise.vikabilling.enums.SubscriptionState;
import com.apitable.enterprise.vikabilling.core.Subscription;
import com.apitable.enterprise.vikabilling.entity.SubscriptionEntity;

/**
 * <p>
 * Subscription Service
 * </p>
 */
public interface ISubscriptionService extends IService<SubscriptionEntity> {

    /**
     * Create subscription
     *
     * @param entity entity
     */
    void create(SubscriptionEntity entity);

    /**
     * Batch create subscription
     *
     * @param entities entities
     */
    void createBatch(List<SubscriptionEntity> entities);

    /**
     * Get subscription
     *
     * @param subscriptionId subscription id
     * @return SubscriptionEntity
     */
    SubscriptionEntity getBySubscriptionId(String subscriptionId);

    /**
     * Batch get subscription
     *
     * @param bundleIds bundle id list
     * @return subscription entities
     */
    List<SubscriptionEntity> getByBundleIds(List<String> bundleIds);

    /**
     * Bulk get subscription entries for different subscription bundle collections
     *
     * @param bundleIds bundle id list
     * @return Subscription List
     */
    List<Subscription> getSubscriptionsByBundleIds(List<String> bundleIds);

    /**
     * Update subscription
     *
     * @param subscriptionId        subscription id
     * @param updatedSubscription   updated subscription
     */
    void updateBySubscriptionId(String subscriptionId, SubscriptionEntity updatedSubscription);

    /**
     * Get subscription collections for subscription bundles in bulk
     *
     * @param bundleId  bundle id
     * @param state     subscription state
     * @return List<FinanceSubscriptionEntity>
     */
    List<SubscriptionEntity> getByBundleIdAndState(String bundleId, SubscriptionState state);

    /**
     * Batch remove
     *
     * @param subscriptionIds subscription id
     */
    void removeBatchBySubscriptionIds(List<String> subscriptionIds);

    /**
     * restore subscription
     *
     * @param subscriptionId Subscription id
     */
    void restoreBySubscriptionIds(List<String> subscriptionId);

    /**
     * Get last subscription id for subscription bundles in bulk
     *
     * @param spaceId space id
     * @return subscription id
     */
    String getActiveTrailSubscriptionIdBySpaceId(String spaceId);


    /**
     * does the space station have subscription entries
     *
     * @param bundleIds bundle id list
     * @return boolean
     */
    boolean bundlesHaveSubscriptions(List<String> bundleIds);

    /**
     * get subscriptions bundle id list
     *
     * @param subscriptionIds subscription id list
     * @return list of bundle id
     */
    List<String> getBundleIdsBySubscriptionIds(List<String> subscriptionIds);

}