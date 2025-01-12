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

package com.apitable.enterprise.social.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;

import com.apitable.enterprise.social.entity.SocialTenantDepartmentBindEntity;

public interface ISocialTenantDepartmentBindService extends IService<SocialTenantDepartmentBindEntity> {

    /**
     * Batch create
     *
     * @param entities Entity List
     */
    void createBatch(List<SocialTenantDepartmentBindEntity> entities);

    /**
     * Get the list of bound departments
     *
     * @param tenantKey Tenant
     * @param spaceId Space
     * @return SocialTenantDepartmentBindEntity List
     */
    List<SocialTenantDepartmentBindEntity> getBindDepartmentList(String tenantKey, String spaceId);

    /**
     * Query the tenant binding all departments
     *
     * @param tenantId Tenant ID
     * @param spaceId Space ID
     * @return SocialTenantDepartmentBindEntity List
     */
    List<SocialTenantDepartmentBindEntity> getBindListByTenantId(String tenantId, String spaceId);

    /**
     * Obtain the space station group ID bound by the tenant department ID
     *
     * @param tenantId Tenant ID
     * @param tenantDepartmentId Department ID of the tenant enterprise
     * @return Space team ID
     */
    Long getBindSpaceTeamId(String spaceId, String tenantId, String tenantDepartmentId);

    /**
     * Obtain the space station group ID bound by the tenant department ID
     *
     * @param tenantId Enterprise ID
     * @param tenantDepartmentId Department ID of the tenant enterprise
     * @return teamId
     */
    Long getBindSpaceTeamIdBySpaceId(String spaceId, String tenantId, String tenantDepartmentId);

    /**
     * Obtain the space station group ID bound by the tenant department ID
     *
     * @param tenantId Enterprise ID
     * @param tenantDepartmentIds Department ID of the tenant enterprise
     * @return Space team ID
     */
    List<Long> getBindSpaceTeamIds(String spaceId, String tenantId, List<String> tenantDepartmentIds);

    /**
     * Delete the binding of tenant department
     *
     * @param tenantId Tenant ID
     * @param tenantDepartmentId Department ID of the tenant enterprise
     */
    void deleteByTenantDepartmentId(String spaceId, String tenantId, String tenantDepartmentId);

    /**
     * Batch delete the binding of tenant departments
     *
     * @param tenantId Tenant ID
     * @param tenantDepartmentIds Department ID of the tenant enterprise
     */
    void deleteBatchByTenantDepartmentId(String spaceId, String tenantId, List<String> tenantDepartmentIds);

    /**
     * Delete the tenant's department binding information
     *
     * @param tenantId Tenant ID
     */
    void deleteByTenantId(String spaceId, String tenantId);

    /**
     * Delete records according to enterprise ID and enterprise department ID
     *
     * @param spaceId Space station ID
     * @param tenantId Tenant Enterprise ID
     * @param departmentId Tenant enterprise department ID
     */
    void deleteSpaceBindTenantDepartment(String spaceId, String tenantId, String departmentId);

    /**
     * Obtain the space station group ID bound by the tenant department ID
     *
     * @param spaceId Space ID
     * @param tenantId Tenant Enterprise ID
     * @param tenantDepartmentIds Department ID of the tenant enterprise
     * @return Space station team ID
     */
    List<Long> getBindSpaceTeamIdsByTenantId(String spaceId, String tenantId, List<String> tenantDepartmentIds);
}
