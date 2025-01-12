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

package com.apitable.enterprise.social.service.impl;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.Resource;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;

import com.apitable.base.enums.DatabaseException;
import com.apitable.enterprise.social.entity.SocialUserBindEntity;
import com.apitable.enterprise.social.mapper.SocialUserBindMapper;
import com.apitable.enterprise.social.service.ISocialUserBindService;
import com.apitable.core.util.ExceptionUtil;

import org.springframework.stereotype.Service;

/**
 * Third party platform integration - user binding service interface implementation
 */
@Service
@Slf4j
public class SocialUserBindServiceImpl
    extends ServiceImpl<SocialUserBindMapper, SocialUserBindEntity>
    implements ISocialUserBindService {

    @Resource
    private SocialUserBindMapper socialUserBindMapper;

    @Override
    public void create(Long userId, String unionId) {
        SocialUserBindEntity userBind = new SocialUserBindEntity();
        userBind.setUserId(userId);
        userBind.setUnionId(unionId);
        boolean saveFlag = save(userBind);
        ExceptionUtil.isTrue(saveFlag, DatabaseException.INSERT_ERROR);
    }

    @Override
    public List<String> getUnionIdsByUserId(Long userId) {
        return socialUserBindMapper.selectUnionIdByUserId(userId);
    }

    @Override
    public Long getUserIdByUnionId(String unionId) {
        return socialUserBindMapper.selectUserIdByUnionId(unionId);
    }

    @Override
    public String getOpenIdByTenantIdAndUserId(String appId, String tenantId, Long userId) {
        return socialUserBindMapper.selectOpenIdByTenantIdAndUserId(appId, tenantId, userId);
    }

    @Override
    public List<SocialUserBindEntity> getEntitiesByUnionId(List<String> unionIds) {
        if (CollUtil.isEmpty(unionIds)) {
            return new ArrayList<>();
        }
        return socialUserBindMapper.selectByUnionIds(unionIds);
    }

    @Override
    public void deleteBatchByUnionId(List<String> unionIds) {
        if (CollUtil.isEmpty(unionIds)) {
            return;
        }
        socialUserBindMapper.deleteByUnionIds(unionIds);
    }

    @Override
    public void deleteByUserId(Long userId) {
        socialUserBindMapper.deleteByUserId(userId);
    }

    @Override
    public Boolean isUnionIdBind(Long userId, String unionId) {
        List<String> unionIds = socialUserBindMapper.selectUnionIdByUserId(userId);
        for (String id : unionIds) {
            if (StrUtil.equals(id, unionId)) {
                return true;
            }
        }
        return false;
    }
}
