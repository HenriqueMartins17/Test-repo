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

import com.apitable.enterprise.social.entity.SocialTenantBindEntity;
import com.apitable.enterprise.social.entity.SocialTenantEntity;
import com.apitable.enterprise.social.enums.SocialPlatformType;
import com.apitable.enterprise.social.model.SpaceBindTenantInfoDTO;
import com.apitable.enterprise.social.model.TenantBindDTO;

/**
 * Third party platform integration - enterprise tenant binding space service interface
 */
public interface ISocialTenantBindService extends IService<SocialTenantBindEntity> {

    /**
     * Get tenant binding status
     *
     * @param tenantId Third party enterprise logo
     * @return True | False
     */
    boolean getTenantBindStatus(String tenantId);

    /**
     * Get space binding status
     *
     * @param spaceId Space ID
     * @return True | False
     */
    boolean getSpaceBindStatus(String spaceId);

    /**
     * Get the tenant of space binding
     *
     * @param spaceId Space ID
     * @return Tenant ID
     */
    List<String> getTenantIdBySpaceId(String spaceId);

    /**
     * Get the tenant of space binding
     *
     * @param spaceId Space ID
     * @return SocialTenantBindEntity
     */
    SocialTenantBindEntity getBySpaceId(String spaceId);

    /**
     * Get List of bound spaces
     * Abandonment: tenants in different applications are the same
     *
     * @param tenantId Third party enterprise ID
     * @return List of bound spaces
     */
    @Deprecated
    List<String> getSpaceIdsByTenantId(String tenantId);

    /**
     * Get List of bound spaces
     *
     * @param tenantId Third party enterprise ID
     * @param appId Application ID
     * @return List of bound spaces
     */
    List<String> getSpaceIdsByTenantIdAndAppId(String tenantId, String appId);

    /**
     * Check whether the space binding tenant exists
     *
     * @param appId Application ID
     * @param spaceId Space ID
     * @param tenantId Tenant ID
     * @return true | false
     */
    boolean checkExistBySpaceIdAndTenantId(String appId, String spaceId, String tenantId);

    /**
     * Enterprise bound space
     *
     * @param appId Third party Application ID
     * @param tenantId Third party enterprise ID
     * @param spaceId Space ID
     */
    void addTenantBind(String appId, String tenantId, String spaceId);

    /**
     * Query the binding information of the enterprise
     *
     * @param tenantId Tenant ID
     * @param appId App ID
     * @return Binding information
     */
    SocialTenantBindEntity getByTenantIdAndAppId(String tenantId, String appId);

    /**
     * Remove the tenant specified by the space
     *
     * @param spaceId Space ID
     * @param tenantId Third party enterprise ID
     */
    void removeBySpaceIdAndTenantId(String spaceId, String tenantId);

    /**
     * Get the list of space bound tenants
     *
     * @param spaceId Space ID
     * @return Bind Tenant List
     */
    TenantBindDTO getTenantBindInfoBySpaceId(String spaceId);

    /**
     * DingTalk Third Party Integration Get Tenant Binding Status
     *
     * @param tenantId Third party enterprise ID
     * @param appId Third party application ID
     * @return boolean
     */
    boolean getDingTalkTenantBindStatus(String tenantId, String appId);

    /**
     * Whether weCom third-party applications have been bound to the space station
     *
     * @param tenantId Third party enterprise ID
     * @param appId Third party application ID
     * @return true | false
     */
    boolean getWeComTenantBindStatus(String tenantId, String appId);

    /**
     * Ding Talk third-party integration to obtain tenant bound space station ID
     *
     * @param tenantId Third party enterprise logo
     * @param appId Third party application ID
     * @return boolean
     */
    String getTenantBindSpaceId(String tenantId, String appId);

    /**
     * Application of Unbinding Space Station
     *
     * @param spaceId Space station ID
     */
    void removeBySpaceId(String spaceId);

    /**
     * Check whether the space is bound to a specific third-party platform
     *
     * @param spaceId Space ID
     * @param socialPlatformType Third party platform type
     * @return boolean
     */
    boolean getSpaceBindStatusByPlatformType(String spaceId, SocialPlatformType socialPlatformType);

    /**
     * Get the space ID bound by the tenant
     *
     * @param tenantKey Tenant ID
     * @return Bound space station ID
     */
    String getTenantDepartmentBindSpaceId(String appId, String tenantKey);

    /**
     * Obtain the space station binding tenant authorization information according to the binding platform
     *
     * @param spaceId               Space ID
     * @param socialPlatformType    Third party platform type
     * @param authInfoType          The authorization information Class type can be NULL
     * @return Space station binding information
     */
    SpaceBindTenantInfoDTO getSpaceBindTenantInfoByPlatform(String spaceId, SocialPlatformType socialPlatformType, Class<?> authInfoType);

    /**
     * Get the list of tenants corresponding to the space
     *
     * @param spaceId Space ID
     * @return SocialTenantEntity List
     */
    List<SocialTenantEntity> getFeishuTenantsBySpaceId(String spaceId);

    /**
     * Get the list of third-party bound space stations
     *
     * @param tenantIds Platform ID
     * @param appIds Application ID
     * @return List<String>
     */
    List<String> getSpaceIdsByTenantIdsAndAppIds(List<String> tenantIds, List<String> appIds);

    /**
     * Get all space ID by app ID
     *
     * @param appId App ID
     * @return Space ID
     */
    List<String> getAllSpaceIdsByAppId(String appId);

}
