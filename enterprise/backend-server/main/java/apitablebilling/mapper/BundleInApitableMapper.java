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

package com.apitable.enterprise.apitablebilling.mapper;

import com.apitable.enterprise.apitablebilling.entity.BundleEntity;
import com.apitable.enterprise.apitablebilling.enums.BundleState;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * Subscription Billing System - Bundle Mapper
 */
public interface BundleInApitableMapper extends BaseMapper<BundleEntity> {

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
    List<BundleEntity> selectBySpaceIdAndByState(@Param("spaceId") String spaceId,
                                                 @Param("state") BundleState state);

    /**
     * Update isDeleted status
     *
     * @param bundleIds subscription bundle id list
     * @param isDeleted isDeleted status
     * @return number of rows affected
     */
    Integer updateIsDeletedByBundleIds(@Param("bundleIds") List<String> bundleIds,
                                       @Param("isDeleted") boolean isDeleted);
}
