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

package com.apitable.enterprise.auth0.service;

import com.apitable.enterprise.auth0.entity.UserBindEntity;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * user bind service interface.
 */
public interface IUserBindService extends IService<UserBindEntity> {

    /**
     * get user id from external key.
     *
     * @param externalKey external key from outside system
     * @return user id
     */
    Long getUserIdByExternalKey(String externalKey);

    /**
     * create user bind.
     *
     * @param userId      user ID
     * @param externalKey external key from outside system
     */
    void create(Long userId, String externalKey);

    /**
     * get user id from external key.
     *
     * @param userId user id
     * @return external key
     */
    String getExternalKeyByUserId(Long userId);
}
