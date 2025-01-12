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

package com.apitable.enterprise.auth0.service.impl;

import com.apitable.enterprise.auth0.entity.UserBindEntity;
import com.apitable.enterprise.auth0.mapper.UserBindMapper;
import com.apitable.enterprise.auth0.service.IUserBindService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * user bind service implementation.
 */
@Service
public class UserBindServiceImpl extends
    ServiceImpl<UserBindMapper, UserBindEntity> implements IUserBindService {

    @Override
    public Long getUserIdByExternalKey(String externalKey) {
        return baseMapper.selectByExternalKey(externalKey);
    }

    @Override
    public void create(Long userId, String externalKey) {
        UserBindEntity userBindEntity = new UserBindEntity();
        userBindEntity.setUserId(userId);
        userBindEntity.setExternalKey(externalKey);
        save(userBindEntity);
    }

    @Override
    public String getExternalKeyByUserId(Long userId) {
        return baseMapper.selectExternalKeyByUserId(userId);
    }
}
