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

package com.apitable.enterprise.gm.service;

import com.apitable.enterprise.gm.enums.GmAction;
import com.apitable.enterprise.vika.core.model.UserContactInfo;
import com.apitable.space.enums.SpaceCertification;
import com.apitable.user.entity.UserEntity;
import java.util.List;

public interface IGmService {

    /**
     * Batch create vest numbers
     */
    void createUsersByCli();

    /**
     * Create User
     * Create operation initiated by CLI tool to create user vest
     *
     * @param username User name (email)
     * @param password Password
     * @param phone Phone number
     * @return user entity
     */
    UserEntity createUserByCli(String username, String password, String phone);

    /**
     * valid permission
     *
     * @param userId    userId
     * @param action    action
     */
    void validPermission(Long userId, GmAction action);

    /**
     * update the gm permission configuration
     *
     * @param userId    userId
     * @param dstId     config datasheet id
     */
    void updateGmPermissionConfig(Long userId, String dstId);

    /**
     * space enterprise certification
     *
     * @param spaceId spaceId
     * @param operatorUserUuid operatorUserUuid
     * @param certification certification level
     */
    void spaceCertification(String spaceId, String operatorUserUuid, SpaceCertification certification);

    /**
     * query and write back user's mobile phone and email by userId
     *
     * @param host        host
     * @param datasheetId datasheet's id
     * @param viewId      view's id
     * @param token       api token
     */
    void queryAndWriteBackUserContactInfo(String host, String datasheetId, String viewId, String token);

    /**
     * query user's mobile phone and email by userId
     *
     * @param userContactInfos user's contact info
     */
    void getUserPhoneAndEmailByUserId(List<UserContactInfo> userContactInfos);
}
