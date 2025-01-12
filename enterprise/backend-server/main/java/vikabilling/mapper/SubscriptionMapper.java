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

import com.apitable.enterprise.vikabilling.entity.SubscriptionEntity;
import com.apitable.enterprise.vikabilling.enums.ProductCategory;
import com.apitable.enterprise.vikabilling.enums.SubscriptionState;
import com.apitable.space.dto.SpaceSubscriptionDto;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * Subscription Billing System - Subscription Mapper
 */
public interface SubscriptionMapper extends BaseMapper<SubscriptionEntity> {

    /**
     * Query by subscription id
     *
     * @param subscriptionId subscription id
     * @return subscription entity
     */
    SubscriptionEntity selectBySubscriptionId(@Param("subscriptionId") String subscriptionId);

    /**
     * Batch query by subscription id
     *
     * @param subscriptionIds subscription id list
     * @return bundle list
     */
    List<SubscriptionEntity> selectBySubscriptionIds(@Param("subscriptionIds") List<String> subscriptionIds);

    /**
     * Query by bundle id
     *
     * @param bundleId  bundle id
     * @return subscription entities
     */
    List<SubscriptionEntity> selectByBundleId(@Param("bundleId") String bundleId);

    /**
     * Batch query by bundle id
     *
     * @param bundleIds bundle id list
     * @return subscription entities
     */
    List<SubscriptionEntity> selectByBundleIds(@Param("bundleIds") Collection<String> bundleIds);

    /**
     * Query by bundle id and state
     *
     * @param bundleId  bundle id
     * @param state     subscription state
     * @return List<FinanceSubscriptionEntity>
     */
    List<SubscriptionEntity> selectByBundleIdAndState(@Param("bundleId") String bundleId,
            @Param("state") SubscriptionState state);

    /**
     * Batch update isDeleted status
     *
     * @param subscriptionIds   subscription id list
     * @param isDeleted         isDeleted status
     * @return number of rows affected
     */
    Integer updateIsDeletedBySubscriptionIds(@Param("subscriptionIds") List<String> subscriptionIds, @Param(
            "isDeleted") boolean isDeleted);

    /**
     * Query the attachment capacity information in effect
     * @param spaceId space id
     * @param page page param
     * @param state subscription state
     * @return Page
     */
    @InterceptorIgnore(illegalSql = "true")
    IPage<SpaceSubscriptionDto> selectUnExpireCapacityBySpaceId(@Param("spaceId") String spaceId, Page page, @Param("state") SubscriptionState state);

    /**
     * Query invalid attachment capacity information
     * @param spaceId space id
     * @param page page param
     * @return Page
     */
    IPage<SpaceSubscriptionDto> selectExpireCapacityBySpaceId(@Param("spaceId") String spaceId, Page page);

    /**
     * Query the number of gifted unexpired attachment capacity
     * @param spaceId space id
     * @param planId plan id
     * @param state subscription state
     * @return number
     */
    @InterceptorIgnore(illegalSql = "true")
    Integer selectUnExpireGiftCapacityBySpaceId(@Param("spaceId") String spaceId, @Param("planId") String planId, @Param("state") String state);

    /**
     * Query space has not expired BASE type subscription
     * @param spaceId space id
     * @param state subscription state
     * @param category product category
     */
    Integer selectUnExpireBaseProductBySpaceId(@Param("spaceId") String spaceId, @Param("state") SubscriptionState state, @Param("category") ProductCategory category);

    /**
     * get space subscription_id list
     *
     * @param spaceId space id
     * @param phase trial, fixedterm
     * @return subscription id
     */
    String selectSubscriptionIdBySpaceIdAndPhaseIgnoreDeleted(@Param("spaceId") String spaceId, @Param("phase") String phase);

    /**
     * get subscription count by bundle id
     *
     * @param bundleIds bundle id list
     * @return count
     */
    Integer selectCountByBundleIds(@Param("bundleIds") List<String> bundleIds);

    /**
     * select subscription's bundle id list
     *
     * @param subscriptionIds subscription id list
     * @return list of bundle id
     */
    List<String> selectBundleIdsBySubscriptionIds(@Param("subscriptionIds") List<String> subscriptionIds);

    /**
     * select unExpired add-on subscription except current bundle
     *
     * @param bundleId bundle id
     * @param spaceId space id
     * @return SubscriptionEntity list
     */
    List<SubscriptionEntity> selectUnExpiredSubscriptionByBundleIdAndSpaceId(@Param("bundleId") String bundleId, @Param("spaceId") String spaceId);

    /**
     * get gift add-on subscription count
     *
     * @param bundleId bundle id
     * @param planId plan id
     * @return count
     */
    Integer selectGiftCountByBundleIdAndPlanId(@Param("bundleId") String bundleId, @Param("planId") String planId);
}
