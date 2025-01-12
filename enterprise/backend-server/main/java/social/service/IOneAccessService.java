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

import jakarta.servlet.http.HttpServletRequest;

import com.apitable.enterprise.social.model.OneAccessCopyInfoRo.MemberRo;
import com.apitable.enterprise.social.model.OneAccessOrgVo;
import com.apitable.enterprise.social.model.OneAccessUserQueryVo;


public interface IOneAccessService {

    /**
     * Get the authorized account space station ID
     *
     * @param bimRemoteUser Authentication and authorization account
     * @param bimRemotePwd  Authentication and authorization password
     * @return space id
     */
    String getSpaceIdByBimAccount(String bimRemoteUser, String bimRemotePwd);

    /**
     * create user
     *
     * @param spaceId   space id
     * @param mobile    mobile number
     * @param nickName  nickname
     * @param email     email
     * @param openOrgId external orgId
     * @param oneId     Oneaccess unit id
     * @return user id
     * @date 2021/11/24 13:50
     */
    Long createUser(String spaceId, String mobile, String loginName, String nickName, String email,
                    String openOrgId, String oneId);

    /**
     * Update user information
     *
     * @param spaceId   space id
     * @param mobile    mobile number
     * @param nickName  nickname
     * @param email     email
     * @param openOrgId external orgId
     * @return is success
     * @date 2021/11/24 13:52
     */
    boolean updateUser(String spaceId, Long userId, String mobile, String loginName,
                       String nickName, String email, String openOrgId);

    /**
     * Delete user.
     *
     * @param spaceId space id
     * @param userId  userId
     * @date 2021/11/24 13:52
     */
    void deleteUser(String spaceId, Long userId);


    /**
     * Get userInfo by id.
     *
     * @param spaceId space id
     * @param userId  internal userId
     * @param oneId   oneId
     * @return OneAccessUserVo
     */
    OneAccessUserQueryVo getUserById(String spaceId, Long userId, String oneId);

    /**
     * Login with authorization code
     *
     * @param code oauth2.0 authorization code
     * @return Whether the login is successful
     * @date 2021/11/24 18:52
     */
    Long getUserIdByCode(String code);


    /**
     * Get user ID by login name and external unitid
     *
     * @param loginName loginName
     * @param uid       external unitid
     * @return Whether the login is successful
     * @date 2021/11/24 18:52
     */
    Long getUserIdByLoginNameAndUnionId(String loginName, String uid);

    /**
     * Convert the obtained encrypted data to the official OneAccess source data
     */
    <T> T getRequestObject(HttpServletRequest request, Class<T> requestType);

    /**
     * Create a department
     *
     * @param spaceId         Space id
     * @param orgName         Department name
     * @param openOrgId       External Department ID
     * @param openParentOrgId External parent department ID
     * @return internal social_dep_id
     */
    Long createOrg(String spaceId, String orgName, String openOrgId, String openParentOrgId);

    /**
     * Update a department
     *
     * @param uid             internal social_dep_id
     * @param openOrgId       External department ID, unique value cannot be updated
     * @param orgName         Department name
     * @param openParentOrgId External Department ID
     * @param enable          whether to disable
     */
    void updateOrg(String uid, String spaceId, String openOrgId, String orgName,
                   String openParentOrgId, boolean enable);

    /**
     * Delete a department
     *
     * @param uid Department ID, unique value cannot be updated
     */
    void deleteOrg(String spaceId, String uid);

    /**
     * Get department by id.
     *
     * @param spaceId   space Id
     * @param uid       Department ID, unique value cannot be updated
     * @param openOrgId External department ID, unique value cannot be updated
     */
    OneAccessOrgVo getOrgById(String spaceId, String uid, String openOrgId);

    /**
     * Copy departments and personnel to the designated space station
     *
     * @param spaceId      Space Id
     * @param destSpaceId  Target Space Id
     * @param teamIds      List of team ids
     * @param memberRoList list of member ids
     */
    void copyTeamAndMembers(String spaceId, String destSpaceId, List<String> teamIds,
                            List<MemberRo> memberRoList);

}
