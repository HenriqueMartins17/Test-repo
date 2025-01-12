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

import java.util.List;

import jakarta.annotation.Resource;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;

import com.apitable.enterprise.social.mapper.SocialCpUserBindMapper;
import com.apitable.base.enums.DatabaseException;
import com.apitable.enterprise.social.service.ISocialCpTenantUserService;
import com.apitable.enterprise.social.service.ISocialCpUserBindService;
import com.apitable.core.util.SqlTool;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.enterprise.social.entity.SocialCpUserBindEntity;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Third party platform integration WeCom user binding service interface
 * </p>
 */
@Slf4j
@Service
public class SocialCpUserBindServiceImpl
    extends ServiceImpl<SocialCpUserBindMapper, SocialCpUserBindEntity>
    implements ISocialCpUserBindService {

    @Resource
    private ISocialCpTenantUserService iSocialCpTenantUserService;

    @Override
    public void create(Long userId, Long cpTenantUserId) {
        SocialCpUserBindEntity cpUserBind = new SocialCpUserBindEntity()
            .setUserId(userId)
            .setCpTenantUserId(cpTenantUserId);
        boolean flag = save(cpUserBind);
        ExceptionUtil.isTrue(flag, DatabaseException.INSERT_ERROR);
    }

    @Override
    public Long getUserIdByTenantIdAndAppIdAndCpUserId(String tenantId, String appId,
                                                       String cpUserId) {
        Long cpTenantUserId =
            iSocialCpTenantUserService.getCpTenantUserId(tenantId, appId, cpUserId);
        return getUserIdByCpTenantUserId(cpTenantUserId);
    }

    @Override
    public Long getUserIdByTenantIdAndCpUserId(String tenantId, String cpUserId) {
        return baseMapper.selectUserIdByTenantIdAndCpUserId(tenantId, cpUserId);
    }

    @Override
    public Long getUserIdByCpTenantUserId(Long cpTenantUserId) {
        return baseMapper.selectUserIdByCpTenantUserId(cpTenantUserId);
    }

    @Override
    public List<SocialCpUserBindEntity> getByCpTenantUserIds(List<Long> cpTenantUserIds) {
        return baseMapper.selectByCpTenantUserIds(cpTenantUserIds);
    }

    @Override
    public String getOpenIdByTenantIdAndUserId(String tenantId, Long userId) {
        return baseMapper.selectOpenIdByTenantIdAndUserId(tenantId, userId);
    }

    @Override
    public boolean isCpTenantUserIdBind(Long userId, Long cpTenantUserId) {
        Long bindUserId = getUserIdByCpTenantUserId(cpTenantUserId);
        return ObjectUtil.equals(bindUserId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteByCpTenantUserIds(List<Long> removeCpTenantUserIds) {
        if (CollUtil.isEmpty(removeCpTenantUserIds)) {
            return;
        }
        baseMapper.batchDeleteByCpTenantUserIds(removeCpTenantUserIds);
    }

    @Override
    public long countTenantBindByUserId(String tenantId, Long userId) {
        return SqlTool.retCount(baseMapper.countTenantBindByUserId(tenantId, userId));
    }

    @Override
    public void deleteByUserId(Long userId) {
        baseMapper.deleteByUserId(userId);
    }

}
