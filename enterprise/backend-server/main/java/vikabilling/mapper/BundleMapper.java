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

package com.apitable.enterprise.vikabilling.mapper;

import java.util.Collection;
import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import com.apitable.enterprise.vikabilling.enums.BundleState;
import com.apitable.enterprise.vikabilling.entity.BundleEntity;

/**
 * Subscription Billing System - Bundle Mapper
 */
public interface BundleMapper extends BaseMapper<BundleEntity> {

    /**
     * Query subscription bundle by bundle id
     *
     * @param bundleId subscription bundle id
     * @return bundle list
     */
    BundleEntity selectByBundleId(@Param("bundleId") String bundleId);

    /**
     * Batch query subscription bundle by bundle id list
     *
     * @param bundleIds subscription bundle id list
     * @return bundle list
     */
    List<BundleEntity> selectByBundleIds(@Param("bundleIds") List<String> bundleIds);

    /**
     * Query subscription bundle by space id
     *
     * @param spaceId space id
     * @return bundle list
     */
    List<BundleEntity> selectBySpaceId(@Param("spaceId") String spaceId);

    /**
     * Query subscription bundle by space id list
     *
     * @param spaceIds space id list
     * @return bundle list
     */
    List<BundleEntity> selectBySpaceIds(@Param("spaceIds") Collection<String> spaceIds);

    /**
     * Query subscription bundle by space id and state
     *
     * @param spaceId space id
     * @return bundle list
     */
    List<BundleEntity> selectBySpaceIdAndByState(@Param("spaceId") String spaceId, @Param("state") BundleState state);

    /**
     * Update isDeleted status
     *
     * @param bundleIds subscription bundle id list
     * @param isDeleted isDeleted status
     * @return number of rows affected
     */
    Integer updateIsDeletedByBundleIds(@Param("bundleIds") List<String> bundleIds, @Param("isDeleted") boolean isDeleted);

    /**
     * select space activated state bundle
     *
     * @param spaceId space id
     * @param state bundle state
     * @return BundleEntity list
     */
    List<BundleEntity> selectBySpaceIdAndState(@Param("spaceId") String spaceId, @Param("state") BundleState state);
}
