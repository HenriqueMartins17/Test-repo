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
import java.util.stream.Collectors;

import jakarta.annotation.Resource;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import com.apitable.enterprise.social.entity.SocialTenantBindEntity;
import com.apitable.enterprise.social.entity.SocialTenantEntity;
import com.apitable.enterprise.social.enums.SocialPlatformType;
import com.apitable.enterprise.social.mapper.SocialTenantBindMapper;
import com.apitable.enterprise.social.model.SpaceBindTenantInfoDTO;
import com.apitable.enterprise.social.model.TenantBindDTO;
import com.apitable.enterprise.social.service.IDingTalkService;
import com.apitable.enterprise.social.service.ISocialTenantBindService;
import com.apitable.enterprise.social.service.ISocialTenantService;
import com.apitable.enterprise.social.service.ISocialTenantUserService;
import com.apitable.space.service.ISpaceService;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.core.util.SqlTool;

import org.springframework.stereotype.Service;

import static com.apitable.enterprise.social.enums.SocialException.TENANT_BIND_FAIL;

/**
 * Third party platform integration - enterprise tenant binding space service interface implementation
 */
@Service
@Slf4j
public class SocialTenantBindServiceImpl
    extends ServiceImpl<SocialTenantBindMapper, SocialTenantBindEntity>
    implements ISocialTenantBindService {

    @Resource
    private SocialTenantBindMapper socialTenantBindMapper;

    @Resource
    private ISocialTenantService socialTenantService;

    @Resource
    private IDingTalkService dingTalkService;

    @Resource
    private ISpaceService spaceService;

    @Resource
    private ISocialTenantUserService tenantUserService;

    @Resource
    private ISocialTenantService iSocialTenantService;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public boolean getTenantBindStatus(String tenantId) {
        int count = SqlTool.retCount(socialTenantBindMapper.selectCountByTenantId(tenantId));
        return count > 0;
    }

    @Override
    public boolean getSpaceBindStatus(String spaceId) {
        // Whether the space is bound to third-party integration
        SocialTenantBindEntity tenantBind = getBySpaceId(spaceId);
        if (tenantBind == null) {
            return false;
        }
        // Query tenants
        SocialTenantEntity tenantEntity =
            iSocialTenantService.getByAppIdAndTenantId(tenantBind.getAppId(),
                tenantBind.getTenantId());
        // Whether the tenant is enabled
        return tenantEntity != null && tenantEntity.getStatus();
    }

    @Override
    public List<String> getTenantIdBySpaceId(String spaceId) {
        return socialTenantBindMapper.selectTenantIdBySpaceId(spaceId);
    }

    @Override
    public SocialTenantBindEntity getBySpaceId(String spaceId) {
        return socialTenantBindMapper.selectBySpaceId(spaceId);
    }

    @Override
    public List<String> getSpaceIdsByTenantId(String tenantId) {
        return socialTenantBindMapper.selectSpaceIdByTenantId(tenantId);
    }

    @Override
    public List<String> getSpaceIdsByTenantIdAndAppId(String tenantId, String appId) {
        return baseMapper.selectSpaceIdsByTenantIdAndAppId(tenantId, appId);
    }

    @Override
    public boolean checkExistBySpaceIdAndTenantId(String appId, String spaceId, String tenantId) {
        List<SocialTenantBindEntity> entities =
            baseMapper.selectBySpaceIdAndTenantId(spaceId, tenantId);
        if (entities.isEmpty()) {
            return false;
        }
        return entities.stream().anyMatch(
            entityClass -> StrUtil.isNotBlank(entityClass.getAppId()) &&
                entityClass.getAppId().equals(appId));
    }

    @Override
    public void addTenantBind(String appId, String tenantId, String spaceId) {
        SocialTenantBindEntity tenantBind = new SocialTenantBindEntity();
        tenantBind.setAppId(appId);
        tenantBind.setSpaceId(spaceId);
        tenantBind.setTenantId(tenantId);
        boolean saveFlag = save(tenantBind);
        ExceptionUtil.isTrue(saveFlag, TENANT_BIND_FAIL);
    }

    @Override
    public SocialTenantBindEntity getByTenantIdAndAppId(String tenantId, String appId) {
        return getBaseMapper().selectByTenantIdAndAppId(tenantId, appId);
    }

    @Override
    public void removeBySpaceIdAndTenantId(String spaceId, String tenantId) {
        socialTenantBindMapper.deleteBySpaceIdAndTenantId(spaceId, tenantId);
    }

    @Override
    public TenantBindDTO getTenantBindInfoBySpaceId(String spaceId) {
        return baseMapper.selectBaseInfoBySpaceId(spaceId);
    }

    @Override
    public boolean getDingTalkTenantBindStatus(String tenantId, String appId) {
        int count =
            SqlTool.retCount(socialTenantBindMapper.selectCountByTenantIdAndAppId(tenantId, appId));
        return count > 0;
    }

    @Override
    public boolean getWeComTenantBindStatus(String tenantId, String appId) {
        int count =
            SqlTool.retCount(socialTenantBindMapper.selectCountByTenantIdAndAppId(tenantId, appId));
        return count > 0;
    }

    @Override
    public String getTenantBindSpaceId(String tenantId, String appId) {
        return baseMapper.selectSpaceIdByTenantIdAndAppId(tenantId, appId);
    }

    @Override
    public void removeBySpaceId(String spaceId) {
        baseMapper.deleteBySpaceId(spaceId);
    }

    @Override
    public boolean getSpaceBindStatusByPlatformType(String spaceId,
                                                    SocialPlatformType platformType) {
        int count = SqlTool.retCount(socialTenantBindMapper.selectCountBySpaceIdAndPlatform(spaceId,
            platformType.getValue()));
        return count > 0;
    }

    @Override
    public String getTenantDepartmentBindSpaceId(String appId, String tenantKey) {
        // Query the list of space stations bound by tenants
        if (StrUtil.isBlank(appId) || StrUtil.isBlank(tenantKey)) {
            log.error(
                "Error querying tenant bound space parameters, application ID:{},tenant ID:{}",
                appId, tenantKey);
            return null;
        }
        List<String> bindSpaceIds =
            socialTenantBindMapper.selectSpaceIdsByTenantIdAndAppId(tenantKey, appId);
        if (bindSpaceIds.isEmpty()) {
            return null;
        }
        if (bindSpaceIds.size() > 1) {
            log.error("Tenant[" + tenantKey + "]Multiple space are tied, error");
            return null;
        }
        return bindSpaceIds.get(0);
    }

    @SneakyThrows
    @Override
    public SpaceBindTenantInfoDTO getSpaceBindTenantInfoByPlatform(String spaceId,
                                                                   SocialPlatformType socialPlatformType,
                                                                   Class<?> authInfoType) {
        Integer platform = null;
        if (null != socialPlatformType) {
            platform = socialPlatformType.getValue();
        }
        SpaceBindTenantInfoDTO spaceBindTenantInfoDTO =
            socialTenantBindMapper.selectSpaceBindTenantInfoByPlatform(spaceId, platform);
        if (null != spaceBindTenantInfoDTO && null != authInfoType &&
            StrUtil.isNotBlank(spaceBindTenantInfoDTO.getAuthInfoStr())) {
            spaceBindTenantInfoDTO.setAuthInfo(
                objectMapper.readValue(spaceBindTenantInfoDTO.getAuthInfoStr(), authInfoType));
        }
        return spaceBindTenantInfoDTO;
    }

    @Override
    public List<SocialTenantEntity> getFeishuTenantsBySpaceId(String spaceId) {
        // Query the list of tenants bound to the space
        List<String> tenantIds = getTenantIdBySpaceId(spaceId);
        if (CollUtil.isEmpty(tenantIds)) {
            return null;
        }
        // Query tenant information
        List<SocialTenantEntity> tenantEntities = iSocialTenantService.getByTenantIds(tenantIds);
        if (CollUtil.isEmpty(tenantEntities)) {
            return null;
        }
        // Filter out the application type of Lark
        return tenantEntities.stream()
            .filter(tenant -> SocialPlatformType.toEnum(tenant.getPlatform()) ==
                SocialPlatformType.FEISHU)
            .collect(Collectors.toList());
    }

    @Override
    public List<String> getSpaceIdsByTenantIdsAndAppIds(List<String> tenantIds,
                                                        List<String> appIds) {
        if (CollUtil.isNotEmpty(tenantIds) && CollUtil.isNotEmpty(appIds)) {
            return baseMapper.selectSpaceIdsByTenantIdsAndAppIds(tenantIds, appIds);
        }
        return ListUtil.empty();
    }

    @Override
    public List<String> getAllSpaceIdsByAppId(String appId) {
        return getBaseMapper().selectAllSpaceIdsByAppId(appId);
    }
}
