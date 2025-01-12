/*
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up
 * license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its
 * subdirectories does not constitute permission to use this code or APITable Enterprise Edition
 * features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

package com.apitable.enterprise.social.service;


import com.apitable.enterprise.social.model.WoaUserLoginVo;

/**
 * Woa service interface
 */
public interface IWoaService {

    WoaUserLoginVo userLoginByOAuth2Code(String appId, String code);

    /**
     * Refresh Woa Address Book
     *
     * @param operatingUserId   Refresh operation user ID
     * @param spaceId           Space ID
     */
    void refreshContact(Long operatingUserId, String spaceId);

    /**
     * Bind space.
     *
     * @param bindUserId
     * @param spaceId
     * @param appId
     * @param secretKey
     */
    void bindSpace(Long bindUserId, String spaceId, String appId, String secretKey);
}
