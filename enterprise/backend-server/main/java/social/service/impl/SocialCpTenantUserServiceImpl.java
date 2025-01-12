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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.annotation.Resource;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;

import com.apitable.enterprise.social.mapper.SocialCpTenantUserMapper;
import com.apitable.enterprise.social.model.CpTenantUserDTO;
import com.apitable.enterprise.social.service.ISocialCpTenantUserService;
import com.apitable.enterprise.social.service.ISocialCpUserBindService;
import com.apitable.user.service.IUserService;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.enterprise.social.entity.SocialCpTenantUserEntity;
import com.apitable.enterprise.social.entity.SocialCpUserBindEntity;
import com.apitable.user.entity.UserEntity;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.apitable.base.enums.DatabaseException.DELETE_ERROR;
import static com.apitable.base.enums.DatabaseException.INSERT_ERROR;

/**
 * <p>
 * Third party platform integration - WeCom tenant user
 * </p>
 */
@Slf4j
@Service
public class SocialCpTenantUserServiceImpl
    extends ServiceImpl<SocialCpTenantUserMapper, SocialCpTenantUserEntity>
    implements ISocialCpTenantUserService {

    @Resource
    private ISocialCpUserBindService iSocialCpUserBindService;

    @Resource
    private IUserService iUserService;

    @Override
    public Long create(String tenantId, String appId, String cpUserId, String cpOpenUserId) {
        SocialCpTenantUserEntity entity = new SocialCpTenantUserEntity()
            .setTenantId(tenantId)
            .setAppId(appId)
            .setCpUserId(cpUserId)
            .setCpOpenUserId(cpOpenUserId);
        boolean flag = save(entity);
        ExceptionUtil.isTrue(flag, INSERT_ERROR);
        return entity.getId();
    }

    @Override
    public void createBatch(List<SocialCpTenantUserEntity> entities) {
        if (CollUtil.isEmpty(entities)) {
            return;
        }
        baseMapper.insertBatch(entities);
    }

    @Override
    public SocialCpTenantUserEntity getCpTenantUser(String tenantId, String appId,
                                                    String cpUserId) {
        return baseMapper.selectByTenantIdAndAppIdAndCpUserId(tenantId, appId, cpUserId);
    }

    @Override
    public SocialCpTenantUserEntity getCpTenantUser(String tenantId, String appId, Long userId) {
        return baseMapper.selectByTenantIdAndAppIdAndUserId(tenantId, appId, userId);
    }

    @Override
    public Long getCpTenantUserId(String tenantId, String appId, String cpUserId) {
        return baseMapper.selectIdByTenantIdAndAppIdAndCpUserId(tenantId, appId, cpUserId);
    }

    @Override
    public Map<String, UserEntity> getUserByCpUserIds(String tenantId, String appId,
                                                      List<String> cpUserIds) {

        if (CollUtil.isEmpty(cpUserIds)) {
            return Collections.emptyMap();
        }

        // Find the user information in WeCom tenant through openId
        List<SocialCpTenantUserEntity> cpTenantUserEntities =
            baseMapper.selectByTenantIdAndAppIdAndCpUserIds(tenantId, appId, cpUserIds);
        if (CollUtil.isEmpty(cpTenantUserEntities)) {
            return Collections.emptyMap();
        }
        // Find the binding information with the Vigor user through the user in the WeCom tenant
        List<Long> cpTenantUserIds = cpTenantUserEntities.stream()
            .map(SocialCpTenantUserEntity::getId)
            .collect(Collectors.toList());
        List<SocialCpUserBindEntity> cpUserBindEntities =
            iSocialCpUserBindService.getByCpTenantUserIds(cpTenantUserIds);
        if (CollUtil.isEmpty(cpUserBindEntities)) {
            return Collections.emptyMap();
        }
        // Find vika user information through binding information
        List<Long> userIds = cpUserBindEntities.stream()
            .map(SocialCpUserBindEntity::getUserId)
            .collect(Collectors.toList());
        List<UserEntity> userEntities = iUserService.listByIds(userIds);
        if (CollUtil.isEmpty(userEntities)) {
            return Collections.emptyMap();
        }

        // Convert the corresponding information of openId ->user
        Map<Long, Long> cpUserBindAndUserIdMap = cpUserBindEntities.stream()
            .collect(Collectors.toMap(SocialCpUserBindEntity::getCpTenantUserId,
                SocialCpUserBindEntity::getUserId, (k1, k2) -> k2));
        Map<Long, UserEntity> userMap = userEntities.stream()
            .collect(Collectors.toMap(UserEntity::getId, v -> v, (k1, k2) -> k2));
        Map<String, UserEntity> resultMap = Maps.newHashMapWithExpectedSize(userMap.size());
        for (SocialCpTenantUserEntity cpTenantUserEntity : cpTenantUserEntities) {
            UserEntity userEntity =
                Optional.ofNullable(cpUserBindAndUserIdMap.get(cpTenantUserEntity.getId()))
                    .map(userMap::get)
                    .orElse(null);
            if (Objects.nonNull(userEntity)) {
                resultMap.put(cpTenantUserEntity.getCpUserId(), userEntity);
            }
        }

        return resultMap;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteByCorpAgentUsers(String tenantId, String appId,
                                            List<String> removeCpUserIds) {
        if (CollUtil.isEmpty(removeCpUserIds)) {
            return;
        }
        boolean flag =
            SqlHelper.retBool(baseMapper.batchDeleteByCorpAgent(tenantId, appId, removeCpUserIds));
        ExceptionUtil.isTrue(flag, DELETE_ERROR);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteByCorpAgent(String tenantId, String appId) {
        // Query all synchronized users
        Map<String, Long> openUsers = getOpenIdsByTenantId(tenantId, appId);
        if (MapUtil.isNotEmpty(openUsers)) {
            // Delete WeCom Tenant User
            this.batchDeleteByCorpAgentUsers(tenantId, appId, new ArrayList<>(openUsers.keySet()));
            // Delete WeCom tenant binding vika User relationship
            iSocialCpUserBindService.batchDeleteByCpTenantUserIds(
                new ArrayList<>(openUsers.values()));
        }
    }

    @Override
    public Map<String, Long> getOpenIdsByTenantId(String tenantId, String appId) {
        List<CpTenantUserDTO> dto = baseMapper.selectOpenIdsByTenantId(tenantId, appId);
        if (CollUtil.isEmpty(dto)) {
            return new LinkedHashMap<>();
        }
        return dto.stream().collect(
            Collectors.toMap(CpTenantUserDTO::getCpUserId, CpTenantUserDTO::getCpTenantUserId,
                (k1, k2) -> k1, LinkedHashMap::new));
    }

}
