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

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import com.apitable.enterprise.social.entity.SocialTenantDepartmentBindEntity;

/**
 * Third party platform integration - enterprise tenant department association table Mapper
 */
public interface SocialTenantDepartmentBindMapper extends BaseMapper<SocialTenantDepartmentBindEntity> {

    /**
     * Quick Bulk Insert
     *
     * @param entities Member List
     * @return Number of execution results
     */
    int insertBatch(@Param("entities") List<SocialTenantDepartmentBindEntity> entities);

    /**
     * Query the space group ID bound by the tenant's department ID
     *
     * @param tenantId Tenant ID
     * @param tenantDepartmentId Tenant's department ID
     * @return Space department ID
     */
    Long selectTeamIdByTenantDepartmentId(@Param("spaceId") String spaceId, @Param("tenantId") String tenantId, @Param("tenantDepartmentId") String tenantDepartmentId);

    /**
     * Query the space station group ID bound by the tenant's department ID
     *
     * @param tenantId Tenant ID
     * @param tenantDepartmentIds Tenant's department ID
     * @return Space department ID
     */
    List<Long> selectTeamIdsByTenantDepartmentId(@Param("spaceId") String spaceId, @Param("tenantId") String tenantId, @Param("tenantDepartmentIds") List<String> tenantDepartmentIds);

    /**
     * Query the list of departments bound by the tenant
     *
     * @param tenantId Tenant ID
     * @return SocialTenantDepartmentBindEntity List
     */
    List<SocialTenantDepartmentBindEntity> selectByTenantId(@Param("tenantId") String tenantId, @Param("spaceId") String spaceId);

    /**
     * Delete binding according to tenant department ID
     *
     * @param tenantId Tenant ID
     * @param tenantDepartmentId Tenant's department ID
     * @return Results of enforcement
     */
    int deleteByTenantDepartmentId(@Param("spaceId") String spaceId, @Param("tenantId") String tenantId, @Param("tenantDepartmentId") String tenantDepartmentId);

    /**
     * Batch delete binding according to tenant department ID
     *
     * @param tenantId Tenant ID
     * @param tenantDepartmentIds Tenant s department id
     * @return Results of enforcement
     */
    int deleteBatchByTenantDepartmentId(@Param("spaceId") String spaceId, @Param("tenantId") String tenantId, @Param("tenantDepartmentIds") List<String> tenantDepartmentIds);

    /**
     * Batch deletion based on tenant identity
     *
     * @param tenantId Tenant ID
     * @return Results of enforcement
     */
    int deleteByTenantId(@Param("spaceId") String spaceId, @Param("tenantId") String tenantId);

    /**
     * Query the space station group ID bound by the tenant's department ID
     *
     * @param spaceId Space station ID
     * @param tenantId Enterprise ID
     * @param tenantDepartmentId Tenant's department ID
     * @return Space department ID
     */
    Long selectSpaceTeamIdByTenantIdAndDepartmentId(@Param("spaceId") String spaceId, @Param("tenantId") String tenantId,
            @Param("tenantDepartmentId") String tenantDepartmentId);


    /**
     * Delete binding according to tenant ID and department ID
     *
     * @param spaceId Space ID
     * @param tenantId Tenant Enterprise ID
     * @param tenantDepartmentId Tenant's department ID
     * @return Results of enforcement
     */
    int deleteBySpaceIdAndTenantIdAndDepartmentId(@Param("spaceId") String spaceId, @Param("tenantId") String tenantId,
            @Param("tenantDepartmentId") String tenantDepartmentId);

    /**
     * Query the space group ID bound by the tenant's department ID
     *
     * @param spaceId Space station ID
     * @param tenantDepartmentIds Tenant's department ID
     * @param tenantId Tenant Enterprise ID
     * @return Space station department ID
     */
    List<Long> selectSpaceTeamIdsByTenantIdAndDepartmentId(@Param("spaceId") String spaceId,
            @Param("tenantId") String tenantId, @Param("tenantDepartmentIds") List<String> tenantDepartmentIds);

    /**
     * Batch deletion based on tenant logo and space ID
     *
     * @param tenantId Tenant ID
     * @param spaceId Space ID
     * @return Space station department ID
     */
    int deleteByTenantIdAndSpaceId(@Param("tenantId") String tenantId, @Param("spaceId") String spaceId);
}
