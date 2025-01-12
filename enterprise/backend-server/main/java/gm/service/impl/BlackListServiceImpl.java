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

package com.apitable.enterprise.gm.service.impl;

import jakarta.annotation.Resource;

import com.apitable.shared.constants.InternalConstants;
import com.apitable.space.vo.SpaceGlobalFeature;
import com.apitable.enterprise.gm.service.IBlackListService;
import com.apitable.space.service.ISpaceService;
import com.apitable.user.service.IUserService;
import com.apitable.core.exception.BusinessException;
import com.apitable.user.entity.UserEntity;

import org.springframework.stereotype.Service;

/**
 * <p>
 * Black List Implement Class
 * </p>
 */
@Service
public class BlackListServiceImpl implements IBlackListService {

    @Resource
    private IUserService iUserService;

    @Resource
    private ISpaceService iSpaceService;

    @Override
    public void checkBlackUser(Long userId) {
        UserEntity entity = iUserService.getById(userId);
        if (entity != null && InternalConstants.BAN_ACCOUNT_REMARK.equals(entity.getRemark())) {
            throw new BusinessException(
                "The account has been banned, please contact customer service to unblock it.");
        }
    }

    @Override
    public void checkBlackSpace(String spaceId) {
        SpaceGlobalFeature spaceGlobalFeature = iSpaceService.getSpaceGlobalFeature(spaceId);
        if (Boolean.TRUE.equals(spaceGlobalFeature.getBan())) {
            throw new BusinessException(
                "The space has been banned, please contact customer service to unblock it.");
        }
    }
}
