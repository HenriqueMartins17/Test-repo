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

import com.apitable.enterprise.social.entity.SocialUserBindEntity;

/**
 * Third party platform integration - user binding service interface
 */
public interface ISocialUserBindService extends IService<SocialUserBindEntity> {

    /**
     * Create user binding third-party account
     *
     * @param userId  User ID
     * @param unionId Third party user ID
     */
    void create(Long userId, String unionId);

    /**
     * Query the union ID bound by the user
     *
     * @param userId User ID
     * @return unionIds
     */
    List<String> getUnionIdsByUserId(Long userId);

    /**
     * Get the bound User ID
     *
     * @param unionId User ID of third-party platform
     * @return User ID
     */
    Long getUserIdByUnionId(String unionId);

    /**
     * Get the corresponding open ID of the tenant
     *
     * @param appId Application ID
     * @param tenantId Tenant ID
     * @param userId User ID
     * @return open id
     */
    String getOpenIdByTenantIdAndUserId(String appId, String tenantId, Long userId);

    /**
     * Get entity according to Union Id
     *
     * @param unionIds User ID of third-party platform
     * @return SocialUserBindEntity List
     */
    List<SocialUserBindEntity> getEntitiesByUnionId(List<String> unionIds);

    /**
     * Batch deletion
     *
     * @param unionIds User ID of third-party platform
     */
    void deleteBatchByUnionId(List<String> unionIds);

    /**
     * Physically delete the user's third-party information according to the User ID
     *
     * @param userId
     */
    void deleteByUserId(Long userId);

    /**
     * Check whether the union ID is bound
     *
     * @param unionId Third party platform user unique ID
     * @param userId User vika account ID
     * @return boolean
     */
    Boolean isUnionIdBind(Long userId, String unionId);
}
