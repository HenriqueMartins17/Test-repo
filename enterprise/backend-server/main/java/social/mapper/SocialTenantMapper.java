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

package com.apitable.enterprise.social.mapper;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import com.apitable.enterprise.social.enums.SocialPlatformType;
import com.apitable.enterprise.social.enums.SocialAppType;
import com.apitable.enterprise.social.entity.SocialTenantEntity;

/**
 * <p>
 * Third party integration - enterprise tenant table mapper interface
 * </p>
 */
public interface SocialTenantMapper extends BaseMapper<SocialTenantEntity> {

    /**
     * Query the tenant ID of third-party platform applications
     *
     * @param appId App ID
     * @return Tenant ID List
     */
    List<String> selectTenantIdByAppId(@Param("appId") String appId);

    /**
     * Query tenants
     *
     * @param appId    App ID
     * @param tenantId Tenant ID
     * @return SocialTenantEntity
     */
    SocialTenantEntity selectByAppIdAndTenantId(@Param("appId") String appId, @Param("tenantId") String tenantId);

    /**
     * Total number of conditional query tenants
     *
     * @param appId    App ID
     * @param tenantId Tenant ID
     * @return Total
     */
    Integer selectCountByAppIdAndTenantId(@Param("appId") String appId, @Param("tenantId") String tenantId);

    /**
     * Restart tenant
     *
     * @param appId    App ID
     * @param tenantId Tenant ID
     * @return Number of executions
     */
    int setTenantOpen(@Param("appId") String appId, @Param("tenantId") String tenantId);

    /**
     * Stop Tenant
     *
     * @param appId    App ID
     * @param tenantId Tenant ID
     * @return Number of executions
     */
    int setTenantStop(@Param("appId") String appId, @Param("tenantId") String tenantId);

    /**
     * Delete all tenants of the app
     *
     * @param appId App ID
     * @return Number of executions
     */
    int deleteByAppId(@Param("appId") String appId);

    /**
     * Tenant address book authorization scope
     *
     * @param tenantId Tenant ID
     * @param scope    Address book authorization scope
     * @return Results of enforcement
     */
    int updateScopeByTenantId(@Param("tenantId") String tenantId, @Param("scope") String scope);

    /**
     * Get the number of applications bound under the tenant
     *
     * @param tenantId Tenant ID
     * @return Number of applications bound by tenants
     */
    Integer selectCountByTenantId(@Param("tenantId") String tenantId);

    /**
     * Get the agent ID of the Ding Talk application
     *
     * @param tenantId Tenant ID
     * @param appId App ID
     * @return agentId
     */
    String selectAgentIdByTenantIdAndAppId(@Param("tenantId") String tenantId, @Param("appId") String appId);

    /**
     * Query tenant authorization range
     *
     * @param tenantId Tenant ID
     * @return Address book authorization scope
     */
    String selectAuthScopeByTenantId(@Param("tenantId") String tenantId);

    /**
     * Update tenant disable status
     *
     * @param appId App ID
     * @param tenantId Tenant ID
     * @param enabled true or false
     * @return Number of influence pieces
     */
    int updateTenantStatus(@Param("appId") String appId, @Param("tenantId") String tenantId, @Param("enabled") Boolean enabled);

    /**
     * Query tenant status
     *
     * @param tenantId Tenant ID
     * @param appId App ID
     * @return Tenant Status
     */
    Integer selectTenantStatusByTenantIdAndAppId(@Param("tenantId") String tenantId, @Param("appId") String appId);

    /**
     * Query the agent ID of the Ding Talk third-party app
     *
     * @param tenantId Tenant ID
     * @param appId App ID
     * @return agentId
     */
    String selectIsvAgentIdByTenantIdAndAppId(@Param("tenantId") String tenantId, @Param("appId") String appId);

    /**
     * Batch query based on Tenant ID
     *
     * @param tenantIds Tenant ID List
     * @return SocialTenantEntity List
     */
    List<SocialTenantEntity> selectByTenantIds(@Param("tenantIds") List<String> tenantIds);

    /**
     * Delete according to Tenant ID
     *
     * @param tenantId Tenant ID
     * @return Number of successful execution lines
     */
    int deleteByTenantId(@Param("tenantId") String tenantId);

    /**
     * Query third-party platform information
     *
     * @param platformType Platform Type
     * @param appType Application Type
     * @return List<SocialTenantEntity>
     */
    List<SocialTenantEntity> selectByPlatformTypeAndAppType(@Param("platformType") SocialPlatformType platformType,
            @Param("appType") SocialAppType appType);

    /**
     * get a permanent authorization code
     * @param tenantId auth corp id
     * @param appId app id
     * @return permanentCode
     */
    String selectPermanentCodeByAppIdAndTenantId(@Param("appId") String appId, @Param("tenantId") String tenantId);

    /**
     * get created at time
     * @param tenantId auth corp id
     * @param appId app ido
     * @return LocalDateTime
     */
    LocalDateTime selectCreatedAtByAppIdAndTenantId(@Param("appId") String appId, @Param("tenantId") String tenantId);
}
