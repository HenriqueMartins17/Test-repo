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

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.apitable.enterprise.social.enums.SocialPlatformType;
import com.apitable.shared.util.ibatis.ExpandBaseMapper;
import com.apitable.enterprise.social.model.SocialTenantUserDTO;
import com.apitable.enterprise.social.entity.SocialTenantUserEntity;

/**
 * Third party platform integration - enterprise tenant user mapper
 */
public interface SocialTenantUserMapper extends ExpandBaseMapper<SocialTenantUserEntity> {

    /**
     * Quick Bulk Insert
     *
     * @param entities List
     * @return Number of execution results
     */
    int insertBatch(@Param("entities") List<SocialTenantUserEntity> entities);

    /**
     * Query all union IDs under the tenant
     *
     * @param tenantId Tenant ID
     * @return unionId
     */
    List<String> selectUnionIdsByTenantId(@Param("appId") String appId, @Param("tenantId") String tenantId);

    /**
     * Query all open IDs under the tenant
     *
     * @param tenantId Tenant ID
     * @return openId
     */
    List<String> selectOpenIdsByTenantId(@Param("appId") String appId, @Param("tenantId") String tenantId);

    /**
     * Delete Tenant's Records
     *
     * @param tenantId Tenant ID
     * @return Number of execution results
     */
    int deleteByTenantId(@Param("appId") String appId, @Param("tenantId") String tenantId);

    /**
     * Query whether the user under the tenant exists
     *
     * @param tenantId Tenant ID
     * @param openId   User ID under the tenant
     * @return Total
     */
    Integer selectCountByTenantIdAndOpenId(@Param("appId") String appId, @Param("tenantId") String tenantId, @Param("openId") String openId);

    /**
     * Query the union ID according to the open ID
     *
     * @param tenantId Tenant ID
     * @param openId User ID under the tenant
     * @return unionId
     */
    String selectUnionIdByOpenId(@Param("appId") String appId, @Param("tenantId") String tenantId, @Param("openId") String openId);

    /**
     * Query the union ID according to the open ID
     *
     * @param tenantId Tenant ID
     * @param openIds List of user IDs under the tenant
     * @return unionId
     */
    List<String> selectUnionIdsByOpenIds(@Param("appId") String appId, @Param("tenantId") String tenantId, @Param("openIds") List<String> openIds);

    /**
     * Query the open ID according to the union ID
     *
     * @param tenantId Tenant ID
     * @param unionIds List of user IDs under the tenant
     * @return unionId
     */
    List<String> selectOpenIdByAppIdAndTenantIdAndUnionIds(@Param("appId") String appId, @Param("tenantId") String tenantId, @Param("unionIds") List<String> unionIds);

    /**
     * Delete the user under the tenant
     *
     * @param tenantId Tenant ID
     * @param openId   User ID under the tenant
     * @return Results-of-enforcement
     */
    int deleteByTenantIdAndOpenId(@Param("appId") String appId, @Param("tenantId") String tenantId, @Param("openId") String openId);

    /**
     * Batch deletion
     *
     * @param tenantId Tenant ID
     * @param openIds User ID under the tenant
     * @return Results of enforcement
     */
    int deleteBatchByTenantIdAndOpenId(@Param("appId") String appId, @Param("tenantId") String tenantId, @Param("openIds") List<String> openIds);

    /**
     * Batch deletion
     *
     * @param tenantId Tenant ID
     * @param openIds User ID under the tenant
     * @return Results of enforcement
     */
    int deleteBatchByOpenId(@Param("appId") String appId, @Param("tenantId") String tenantId, @Param("openIds") List<String> openIds);

    /**
     * Query openId according to unionId
     *
     * @param unionId User unified ID
     * @param platformType Platform ID
     * @return User ID under the tenant
     */
    String selectOpenIdByUnionIdAndPlatform(@Param("unionId") String unionId,
            @Param("platformType") SocialPlatformType platformType);

    /**
     * Delete according to application and tenant identity
     *
     * @param appId Application ID
     * @param tenantId Tenant ID
     * @return Number of execution results
     */
    int deleteByAppIdAndTenantId(@Param("appId") String appId, @Param("tenantId") String tenantId);

    /**
     * Get the union ID and open ID of the enterprise user
     *
     * @param tenantId Tenant ID
     * @return List<SocialTenantUserDTO>
     */
    List<SocialTenantUserDTO> selectOpenIdAndUnionIdByTenantId(@Param("tenantId") String tenantId, @Param("appId") String appId);
}
