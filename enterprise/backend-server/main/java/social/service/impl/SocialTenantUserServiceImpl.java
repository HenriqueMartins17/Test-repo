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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.annotation.Resource;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import com.apitable.enterprise.social.mapper.SocialTenantUserMapper;
import com.apitable.enterprise.social.model.SocialTenantUserDTO;
import com.apitable.enterprise.social.enums.SocialPlatformType;
import com.apitable.user.enums.LinkType;
import com.apitable.shared.util.ibatis.ExpandServiceImpl;
import com.apitable.enterprise.social.service.ISocialTenantUserService;
import com.apitable.enterprise.social.service.ISocialUserBindService;
import com.apitable.enterprise.social.service.ISocialUserService;
import com.apitable.enterprise.user.mapper.UserLinkMapper;
import com.apitable.enterprise.user.service.IUserLinkService;
import com.apitable.core.util.SqlTool;
import com.apitable.enterprise.social.entity.SocialTenantUserEntity;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Third party platform integration - enterprise tenant user service interface implementation
 */
@Service
@Slf4j
public class SocialTenantUserServiceImpl
    extends ExpandServiceImpl<SocialTenantUserMapper, SocialTenantUserEntity>
    implements ISocialTenantUserService {

    @Resource
    private SocialTenantUserMapper socialTenantUserMapper;

    @Resource
    private ISocialUserService iSocialUserService;

    @Resource
    private ISocialUserBindService iSocialUserBindService;

    @Resource
    private IUserLinkService iUserLinkService;

    @Resource
    private UserLinkMapper userLinkMapper;

    @Override
    public void create(String appId, String tenantId, String openId, String unionId) {
        SocialTenantUserEntity tenantUserEntity = new SocialTenantUserEntity();
        tenantUserEntity.setAppId(appId);
        tenantUserEntity.setTenantId(tenantId);
        tenantUserEntity.setOpenId(openId);
        tenantUserEntity.setUnionId(unionId);
        save(tenantUserEntity);
    }

    @Override
    public void createBatch(List<SocialTenantUserEntity> entities) {
        if (CollUtil.isEmpty(entities)) {
            return;
        }
        saveBatch(entities);
    }

    @Override
    public List<String> getOpenIdsByTenantId(String appId, String tenantId) {
        return socialTenantUserMapper.selectOpenIdsByTenantId(appId, tenantId);
    }

    @Override
    public List<String> getOpenIdsByAppIdAndTenantId(String appId, String tenantId) {
        return socialTenantUserMapper.selectOpenIdsByTenantId(appId, tenantId);
    }

    @Override
    public String getUnionIdByOpenId(String appId, String tenantId, String openId) {
        return socialTenantUserMapper.selectUnionIdByOpenId(appId, tenantId, openId);
    }

    @Override
    public boolean isTenantUserOpenIdExist(String appId, String tenantId, String openId) {
        return SqlTool.retCount(
            socialTenantUserMapper.selectCountByTenantIdAndOpenId(appId, tenantId, openId)) > 0;
    }

    @Override
    public boolean isTenantUserUnionIdExist(String appId, String tenantId, String openId,
                                            String unionId) {
        List<String> unionIds = socialTenantUserMapper.selectUnionIdsByOpenIds(appId, tenantId,
            Collections.singletonList(openId));
        return ObjectUtil.isNotEmpty(unionIds) && unionIds.contains(unionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByTenantId(String appId, String tenantId) {
        List<String> unionIds = socialTenantUserMapper.selectUnionIdsByTenantId(appId, tenantId);
        if (CollUtil.isNotEmpty(unionIds)) {
            iSocialUserService.deleteBatchByUnionId(unionIds);
            iUserLinkService.deleteBatchByUnionId(unionIds);
        }
        // Delete Record
        socialTenantUserMapper.deleteByTenantId(appId, tenantId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByFeishuOpenIds(String appId, String tenantId, List<String> openIds) {
        if (CollUtil.isEmpty(openIds)) {
            return;
        }
        socialTenantUserMapper.deleteBatchByTenantIdAndOpenId(appId, tenantId, openIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByTenantIdAndOpenId(String appId, String tenantId, String openId) {
        // Lark tenant employee deletion is conditional on employee resignation
        String unionId = socialTenantUserMapper.selectUnionIdByOpenId(appId, tenantId, openId);
        if (StrUtil.isNotBlank(unionId)) {
            iSocialUserService.deleteBatchByUnionId(Collections.singletonList(unionId));
            iUserLinkService.deleteBatchByUnionId(Collections.singletonList(unionId));
        }
        socialTenantUserMapper.deleteByTenantIdAndOpenId(appId, tenantId, openId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByTenantIdAndOpenIds(String appId, String tenantId, List<String> openIds) {
        if (CollUtil.isNotEmpty(openIds)) {
            // Delete Record
            socialTenantUserMapper.deleteBatchByOpenId(appId, tenantId, openIds);
        }
    }

    @Override
    public void deleteByAppIdAndTenantId(String appId, String tenantId) {
        socialTenantUserMapper.deleteByAppIdAndTenantId(appId, tenantId);
    }

    @Override
    public Long getUserIdByDingTalkUnionId(String unionId) {
        String openId =
            baseMapper.selectOpenIdByUnionIdAndPlatform(unionId, SocialPlatformType.DINGTALK);
        if (StrUtil.isNotBlank(openId)) {
            return userLinkMapper.selectUserIdByUnionIdAndOpenIdAndType(unionId, openId,
                LinkType.DINGTALK);
        }
        return userLinkMapper.selectUserIdByUnionIdAndType(unionId, LinkType.DINGTALK.getType());
    }

    @Override
    public Map<String, List<String>> getOpenIdMapByTenantId(String appId, String tenantId) {
        List<SocialTenantUserDTO> tenantUsers =
            socialTenantUserMapper.selectOpenIdAndUnionIdByTenantId(tenantId, appId);
        if (!ObjectUtil.isEmpty(tenantUsers)) {
            return tenantUsers.stream()
                .collect(Collectors.groupingBy(SocialTenantUserDTO::getOpenId,
                    Collectors.mapping(SocialTenantUserDTO::getUnionId, Collectors.toList())));
        }
        return new HashMap<>();
    }

    @Override
    public String getOpenIdByTenantIdAndUserId(String appId, String tenantId, Long userId) {
        // Query the union ID bound by the user
        List<String> unionIds = iSocialUserBindService.getUnionIdsByUserId(userId);
        if (unionIds.isEmpty()) {
            return null;
        }
        List<String> openIds =
            socialTenantUserMapper.selectOpenIdByAppIdAndTenantIdAndUnionIds(appId, tenantId,
                unionIds);
        return openIds.stream().findFirst().orElse(null);
    }
}
