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

package com.apitable.enterprise.apitablebilling.service;

import com.apitable.enterprise.apitablebilling.core.Bundle;
import com.apitable.enterprise.apitablebilling.entity.BundleEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * <p>
 * Bundle Service.
 * </p>
 */
public interface IBundleInApitableService extends IService<BundleEntity> {

    /**
     * Create subscription bundle.
     *
     * @param entity entity
     */
    void create(BundleEntity entity);

    /**
     * Batch create subscription bundle.
     *
     * @param entities entities
     */
    void createBatch(List<BundleEntity> entities);

    /**
     * Get subscription bundle.
     *
     * @param bundleId bundle id
     * @return bundle entity
     */
    BundleEntity getByBundleId(String bundleId);

    /**
     * Get subscription bundle list.
     *
     * @param spaceId space id
     * @return bundle entity list
     */
    List<BundleEntity> getBySpaceId(String spaceId);

    /**
     * Batch get subscription bundle list.
     *
     * @param spaceIds space id list
     * @return bundle list
     */
    List<BundleEntity> getBySpaceIds(List<String> spaceIds);

    /**
     * get possible active bundle by space id.
     *
     * @param spaceId space id
     * @return active bundle
     */
    Bundle getValidBundleBySpaceId(String spaceId);

    /**
     * Get all subscription bundles for the space station
     * Subscription bundle with all states.
     *
     * @param spaceId space id
     * @return bundle List
     */
    List<Bundle> getBundlesBySpaceId(String spaceId);

    /**
     * Batch get subscriptions for the activation status of the space station
     * * Include only active state.
     *
     * @param spaceIds space id list
     * @return bundle List
     */
    List<Bundle> getValidBundlesBySpaceId(List<String> spaceIds);

    /**
     * Batch get all subscription bundles for the space station
     * * Subscription bundle with all states.
     *
     * @param spaceIds space id list
     * @return bundle list
     */
    List<Bundle> getBundlesBySpaceIds(List<String> spaceIds);

    /**
     * Update subscription bundle.
     *
     * @param bundleId      bundle id
     * @param updatedBundle updated bundle
     */
    void updateByBundleId(String bundleId, BundleEntity updatedBundle);

    /**
     * restore by bundle ids.
     *
     * @param bundleIds bundle id
     */
    void restoreByBundleIds(List<String> bundleIds);


    /**
     * Batch remove.
     *
     * @param bundleIds bundle id
     */
    void removeBatchByBundleIds(List<String> bundleIds);
}
