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
import java.util.Collections;
import java.util.List;

import jakarta.annotation.Resource;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;

import com.apitable.enterprise.social.mapper.SocialTenantDepartmentBindMapper;
import com.apitable.enterprise.social.service.ISocialTenantDepartmentBindService;
import com.apitable.enterprise.social.entity.SocialTenantDepartmentBindEntity;

import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SocialTenantDepartmentBindServiceImpl
    extends ServiceImpl<SocialTenantDepartmentBindMapper, SocialTenantDepartmentBindEntity>
    implements ISocialTenantDepartmentBindService {

    @Resource
    private SocialTenantDepartmentBindMapper socialTenantDepartmentBindMapper;

    @Override
    public void createBatch(List<SocialTenantDepartmentBindEntity> entities) {
        if (CollUtil.isEmpty(entities)) {
            return;
        }
        socialTenantDepartmentBindMapper.insertBatch(entities);
    }

    @Override
    public List<SocialTenantDepartmentBindEntity> getBindDepartmentList(String tenantKey,
                                                                        String spaceId) {
        if (StrUtil.isBlank(spaceId)) {
            // The application is opened for the first time, and the department is empty, because after deactivation, the bound data will be deleted, which is empty
            return Collections.emptyList();
        }
        return getBindListByTenantId(tenantKey, spaceId);
    }

    @Override
    public List<SocialTenantDepartmentBindEntity> getBindListByTenantId(String tenantId,
                                                                        String spaceId) {
        return socialTenantDepartmentBindMapper.selectByTenantId(tenantId, spaceId);
    }

    @Override
    public Long getBindSpaceTeamId(String spaceId, String tenantId, String tenantDepartmentId) {
        return socialTenantDepartmentBindMapper.selectTeamIdByTenantDepartmentId(spaceId, tenantId,
            tenantDepartmentId);
    }

    @Override
    public Long getBindSpaceTeamIdBySpaceId(String spaceId, String tenantId,
                                            String tenantDepartmentId) {
        return socialTenantDepartmentBindMapper.selectSpaceTeamIdByTenantIdAndDepartmentId(spaceId,
            tenantId,
            tenantDepartmentId);
    }

    @Override
    public List<Long> getBindSpaceTeamIds(String spaceId, String tenantId,
                                          List<String> tenantDepartmentIds) {
        if (CollUtil.isEmpty(tenantDepartmentIds)) {
            return new ArrayList<>();
        }
        return socialTenantDepartmentBindMapper.selectTeamIdsByTenantDepartmentId(spaceId, tenantId,
            tenantDepartmentIds);
    }

    @Override
    public void deleteByTenantDepartmentId(String spaceId, String tenantId,
                                           String tenantDepartmentId) {
        socialTenantDepartmentBindMapper.deleteByTenantDepartmentId(spaceId, tenantId,
            tenantDepartmentId);
    }

    @Override
    public void deleteBatchByTenantDepartmentId(String spaceId, String tenantId,
                                                List<String> tenantDepartmentIds) {
        socialTenantDepartmentBindMapper.deleteBatchByTenantDepartmentId(spaceId, tenantId,
            tenantDepartmentIds);
    }

    @Override
    public void deleteByTenantId(String spaceId, String tenantId) {
        socialTenantDepartmentBindMapper.deleteByTenantId(spaceId, tenantId);
    }

    @Override
    public void deleteSpaceBindTenantDepartment(String spaceId, String tenantId,
                                                String departmentId) {
        socialTenantDepartmentBindMapper.deleteBySpaceIdAndTenantIdAndDepartmentId(spaceId,
            tenantId, departmentId);
    }

    @Override
    public List<Long> getBindSpaceTeamIdsByTenantId(String spaceId, String tenantId,
                                                    List<String> tenantDepartmentIds) {
        if (CollUtil.isEmpty(tenantDepartmentIds)) {
            return new ArrayList<>();
        }
        return socialTenantDepartmentBindMapper.selectSpaceTeamIdsByTenantIdAndDepartmentId(spaceId,
            tenantId,
            tenantDepartmentIds);
    }
}
