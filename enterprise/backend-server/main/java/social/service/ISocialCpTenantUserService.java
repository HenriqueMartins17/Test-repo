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
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;

import com.apitable.enterprise.social.entity.SocialCpTenantUserEntity;
import com.apitable.user.entity.UserEntity;

/**
 * <p>
 * Third party platform integration WeCom tenant user service interface
 * </p>
 */
public interface ISocialCpTenantUserService extends IService<SocialCpTenantUserEntity> {

    /**
     * Create
     *
     * @param tenantId  Tenant ID
     * @param appId     Tenant Application ID
     * @param cpUserId  Tenant User ID
     * @param cpOpenUserId Tenant platform OpenUserId. Probably {@code null}
     * @return New Data Id
     */
    Long create(String tenantId, String appId, String cpUserId, String cpOpenUserId);

    /**
     * Bulk Insert
     *
     * @param entities Entity List
     */
    void createBatch(List<SocialCpTenantUserEntity> entities);

    /**
     * Get WeCom members
     *
     * @param tenantId  Tenant ID
     * @param appId     Tenant Application ID
     * @param cpUserId  Tenant User ID
     * @return WeCom User Information
     */
    SocialCpTenantUserEntity getCpTenantUser(String tenantId, String appId, String cpUserId);

    /**
     * Query enterprise WeCom member information
     *
     * @param tenantId Tenant ID
     * @param appId Application ID
     * @param userId vika User ID
     * @return WeCom User Information
     */
    SocialCpTenantUserEntity getCpTenantUser(String tenantId, String appId, Long userId);

    /**
     * CP Tenant User ID
     *
     * @param tenantId  Tenant ID
     * @param appId     Tenant Application ID
     * @param cpUserId  Tenant User ID
     * @return WeCom Tenant user ID(Primary key)
     */
    Long getCpTenantUserId(String tenantId, String appId, String cpUserId);

    /**
     * Get vika user information corresponding to openId in batch
     *
     * @param tenantId Tenant ID
     * @param appId App ID
     * @param cpUserIds openId
     * @return vika user information. CpUserId (openId) in the parameter corresponding to the key
     */
    Map<String, UserEntity> getUserByCpUserIds(String tenantId, String appId, List<String> cpUserIds);

    /**
     * Mass deletion of WeCom users
     *
     * @param tenantId          Enterprise ID
     * @param appId             Enterprise Application ID
     * @param removeCpUserIds   WeCom user ID
     */
    void batchDeleteByCorpAgentUsers(String tenantId, String appId, List<String> removeCpUserIds);

    /**
     * Batch delete all users of enterprise WeChat customized application
     *
     * @param tenantId  Enterprise ID
     * @param appId     Enterprise Application ID
     */
    void batchDeleteByCorpAgent(String tenantId, String appId);

    /**
     * Get the open IDs of all users under the tenant
     *
     * @param tenantId Tenant ID
     * @param appId    Tenant application ID
     * @return openIds
     */
    Map<String, Long> getOpenIdsByTenantId(String tenantId, String appId);

}
