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

import com.apitable.enterprise.social.model.FeishuTenantDetailVO;
import com.apitable.enterprise.social.model.SocialUser;
import com.apitable.enterprise.social.model.TenantDetailVO;
import com.apitable.enterprise.social.model.TenantDetailVO.Space;
import com.apitable.enterprise.social.model.User;
import com.apitable.space.enums.SpaceUpdateOperate;

/**
 * Third party integration service interface
 */
public interface ISocialService {

    /**
     * Create Lark User
     *
     * @param user User
     * @return user id
     */
    Long createUser(SocialUser user);

    /**
     * Create User
     *
     * @param user User
     * @return user id
     */
    Long createSocialUser(User user);

    /**
     * Create enterprise WeChat third-party associated users
     *
     * @param user User information
     * @return vika user ID
     */
    Long createWeComUser(SocialUser user);

    /**
     * Activate the specified space
     *
     * @param userId User ID
     * @param spaceId Space ID
     * @param openId Open Unique ID
     */
    void activeTenantSpace(Long userId, String spaceId, String openId);

    /**
     * Users activate space members
     * 
     * @param userId User ID
     * @param spaceId Space ID
     * @param openId openId
     * @param mobile mobile
     */
    Long activeSpaceByMobile(Long userId, String spaceId, String openId, String mobile);

    /**
     * Verify whether the user is the tenant's administrator
     * 
     * @param userId User ID
     * @param tenantKey Tenant ID
     */
    void checkUserIfInTenant(Long userId, String appId, String tenantKey);

    /**
     * Obtain the spatial information of the enterprise
     *
     * @param appId Application ID
     * @param tenantKey Lark Enterprise ID
     * @return FeishuTenantInfoVO
     */
    FeishuTenantDetailVO getFeishuTenantInfo(String appId, String tenantKey);

    /**
     * Obtain the spatial information of the enterprise
     *
     * @param tenantKey Enterprise ID
     * @param appId Application ID
     * @return TenantDetailVO
     */
    TenantDetailVO getTenantInfo(String tenantKey, String appId);

    /**
     * Obtain the spatial information of the enterprise
     *
     * @param tenantKey Enterprise ID
     * @param appId Application ID
     * @return List<Space>
     */
    List<Space> getTenantBindSpaceInfo(String tenantKey, String appId);

    /**
     * Replace the master administrator
     *
     * @param spaceId Space ID
     * @param memberId Member ID
     */
    void changeMainAdmin(String spaceId, Long memberId);

    /**
     * Get forbidden resources for space binding integration
     *
     * @param spaceId Space ID
     * @return Space permission resource prohibition list
     */
    List<String> getSocialDisableRoleGroupCode(String spaceId);

    /**
     * Check whether spatial data is allowed to be manipulated，member,team
     *
     * @param spaceId space id
     * @param spaceUpdateOperate spaceUpdateOperates
     */
    void checkCanOperateSpaceUpdate(String spaceId, SpaceUpdateOperate spaceUpdateOperate);

    /**
     * Check whether space resources are allowed to be manipulated
     *
     * @param spaceId                space id
     * @param opMemberId             opMemberId
     * @param acceptMemberId         the accept action member id
     * @param spaceUpdateOperates    spaceUpdateOperates
     */
    void checkCanOperateSpaceUpdate(String spaceId, Long opMemberId, Long acceptMemberId, List<SpaceUpdateOperate> spaceUpdateOperates);

    /**
     * delete user bind
     * @param userId userId
     */
    void deleteSocialUserBind(Long userId);

    /**
     * Check whether the contact is being synchronized
     *
     * @param spaceId space id
     * @return Boolean
     */
    Boolean isContactSyncing(String spaceId);

    /**
     * the label space is synchronizing the contact
     *
     * @param spaceId space id
     */
    void setContactSyncing(String spaceId, String value);

    /**
     * the space synchronization contact is marked
     *
     * @param spaceId space id
     */
    void contactFinished(String spaceId);
}
