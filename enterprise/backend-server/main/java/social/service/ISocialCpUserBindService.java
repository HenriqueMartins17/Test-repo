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

import com.apitable.enterprise.social.entity.SocialCpUserBindEntity;

/**
 * <p>
 * Third party platform integration WeCom user binding service interface
 * </p>
 */
public interface ISocialCpUserBindService extends IService<SocialCpUserBindEntity> {

    /**
     * Create user binding third-party account
     *
     * @param userId            User ID
     * @param cpTenantUserId    Third party user ID (Social Cp Tenant User ID)
     */
    void create(Long userId, Long cpTenantUserId);

    /**
     * Get User Id
     *
     * @param tenantId  Enterprise Id
     * @param appId     Enterprise Application Id
     * @param cpUserId  Enterprise WeCom user ID
     * @return vika User Id
     */
    Long getUserIdByTenantIdAndAppIdAndCpUserId(String tenantId, String appId, String cpUserId);

    /**
     * Get User Id
     * The user ID is returned for different applications in the same enterprise as long as the binding relationship exists
     *
     * @param tenantId  Enterprise Id
     * @param cpUserId  Enterprise WeCom user ID
     * @return vika User Id
     */
    Long getUserIdByTenantIdAndCpUserId(String tenantId, String cpUserId);

    /**
     * Get User Id
     *
     * @param cpTenantUserId   Third party platform user ID (Social Cp Tenant User ID)
     * @return vika User ID
     */
    Long getUserIdByCpTenantUserId(Long cpTenantUserId);

    /**
     * Get information in batches
     *
     * @param cpTenantUserIds Third party platform user ID (Social Cp Tenant User ID)
     * @return Information List
     */
    List<SocialCpUserBindEntity> getByCpTenantUserIds(List<Long> cpTenantUserIds);

    /**
     * Get Open Id
     *
     * @param tenantId  Enterprise Id
     * @param userId    vika User ID
     * @return Enterprise WeCom Open Id
     */
    String getOpenIdByTenantIdAndUserId(String tenantId, Long userId);

    /**
     * Check whether the union ID is bound
     *
     * @param userId            User vika account ID
     * @param cpTenantUserId    Third party platform user unique ID (Social Cp Tenant User ID)
     * @return Whether to bind
     */
    boolean isCpTenantUserIdBind(Long userId, Long cpTenantUserId);

    /**
     * Batch Delete WeCom Binding Relationship
     *
     * @param removeCpTenantUserIds    Third party platform user unique ID (Social Cp Tenant User ID)
     */
    void batchDeleteByCpTenantUserIds(List<Long> removeCpTenantUserIds);

    /**
     * Count the number of specified users under the specified tenant
     *
     * @param tenantId    Tenant Id
     * @param userId      User Id
     * @return Number of users
     */
    long countTenantBindByUserId(String tenantId, Long userId);

    /**
     * The third party information of the user is physically deleted according to the user ID
     *
     * @param userId
     */
    void deleteByUserId(Long userId);

}
