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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import jakarta.annotation.Resource;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import lombok.extern.slf4j.Slf4j;

import com.apitable.enterprise.social.factory.SocialFactory;
import com.apitable.enterprise.social.mapper.SocialTenantMapper;
import com.apitable.enterprise.social.model.TenantBaseInfoDto;
import com.apitable.enterprise.social.model.TenantBindDTO;
import com.apitable.enterprise.social.enums.SocialPlatformType;
import com.apitable.space.vo.SpaceGlobalFeature;
import com.apitable.enterprise.appstore.enums.AppType;
import com.apitable.enterprise.appstore.service.IAppInstanceService;
import com.apitable.organization.mapper.MemberMapper;
import com.apitable.organization.service.IMemberService;
import com.apitable.enterprise.social.enums.SocialAppType;
import com.apitable.enterprise.social.enums.SocialTenantAuthMode;
import com.apitable.enterprise.social.service.IDingTalkService;
import com.apitable.enterprise.social.service.ISocialTenantBindService;
import com.apitable.enterprise.social.service.ISocialTenantDepartmentService;
import com.apitable.enterprise.social.service.ISocialTenantService;
import com.apitable.enterprise.social.service.ISocialTenantUserService;
import com.apitable.space.service.ISpaceService;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.core.util.SqlTool;
import com.apitable.organization.entity.MemberEntity;
import com.apitable.enterprise.social.entity.SocialTenantEntity;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.apitable.enterprise.social.enums.SocialException.TENANT_APP_BIND_INFO_NOT_EXISTS;

/**
 * <p>
 * Third party integration - enterprise tenant service interface implementation
 * </p>
 */
@Service
@Slf4j
public class SocialTenantServiceImpl extends ServiceImpl<SocialTenantMapper, SocialTenantEntity>
    implements ISocialTenantService {

    @Resource
    private SocialTenantMapper socialTenantMapper;

    @Resource
    private ISocialTenantUserService iSocialTenantUserService;

    @Resource
    private ISocialTenantBindService iSocialTenantBindService;

    @Resource
    private ISocialTenantDepartmentService iSocialTenantDepartmentService;

    @Resource
    private ISpaceService iSpaceService;

    @Resource
    private IAppInstanceService iAppInstanceService;

    @Resource
    private MemberMapper memberMapper;

    @Resource
    private IMemberService iMemberService;

    @Resource
    private IDingTalkService dingTalkService;

    @Override
    public boolean isTenantExist(String tenantId, String appId) {
        return SqlTool.retCount(socialTenantMapper.selectCountByAppIdAndTenantId(appId, tenantId)) >
            0;
    }

    @Override
    public void createTenant(SocialPlatformType socialType, SocialAppType appType, String appId,
                             String tenantId, String contactScope) {
        log.info("Third party platform type:{}, enterprise tenants opening applications: {}",
            socialType.getValue(), tenantId);
        SocialTenantEntity tenant = new SocialTenantEntity();
        tenant.setAppId(appId);
        tenant.setAppType(appType.getType());
        tenant.setPlatform(socialType.getValue());
        tenant.setTenantId(tenantId);
        tenant.setContactAuthScope(contactScope);
        boolean flag = save(tenant);
        if (!flag) {
            throw new RuntimeException("[Lark] Failed to add tenant");
        }
    }

    @Override
    public void updateTenantStatus(String appId, String tenantId, boolean enabled) {
        // Update tenant's status
        socialTenantMapper.updateTenantStatus(appId, tenantId, enabled);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void stopByTenant(String appId, String tenantId) {
        List<String> spaceIds =
            iSocialTenantBindService.getSpaceIdsByTenantIdAndAppId(tenantId, appId);
        if (CollUtil.isNotEmpty(spaceIds)) {
            for (String spaceId : spaceIds) {
                iAppInstanceService.deleteBySpaceIdAndAppType(spaceId, AppType.LARK_STORE.name());
            }
        }
        // Change the tenant stop enabling status
        updateTenantStatus(appId, tenantId, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeTenant(String appId, String tenantId, String spaceId) {
        // Invitable status of all members of the recovery space (synchronize the default configuration of the startup space)
        Long mainAdminUserId = iSpaceService.getSpaceMainAdminUserId(spaceId);
        SpaceGlobalFeature feature = SpaceGlobalFeature.builder().invitable(true).build();
        iSpaceService.switchSpacePros(mainAdminUserId, spaceId, feature);
        iAppInstanceService.deleteBySpaceIdAndAppType(spaceId, AppType.LARK_STORE.name());
        // Change the tenant stop enabling status
        updateTenantStatus(appId, tenantId, false);
        // Delete space binding
        iSocialTenantBindService.removeBySpaceIdAndTenantId(spaceId, tenantId);
        // Delete the tenant's department record and binding
        iSocialTenantDepartmentService.deleteByTenantId(spaceId, tenantId);
        // Delete tenant's user record
        iSocialTenantUserService.deleteByTenantId(appId, tenantId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeInternalTenant(String appId, String tenantId, String spaceId) {
        SocialTenantEntity tenant = socialTenantMapper.selectByAppIdAndTenantId(appId, tenantId);
        if (tenant != null) {
            removeById(tenant);
        }
        // Delete the bound open IDs of all members of the space
        List<MemberEntity> memberEntities = iMemberService.getMembersBySpaceId(spaceId, true);
        if (!memberEntities.isEmpty()) {
            memberEntities.forEach(
                memberEntity -> iMemberService.clearOpenIdById(memberEntity.getId()));
        }
        // Invitable status of all members of the recovery space (synchronize the default configuration of the startup space)
        Long mainAdminUserId = iSpaceService.getSpaceMainAdminUserId(spaceId);
        SpaceGlobalFeature feature = SpaceGlobalFeature.builder().invitable(true).build();
        iSpaceService.switchSpacePros(mainAdminUserId, spaceId, feature);
        iSocialTenantBindService.removeBySpaceIdAndTenantId(spaceId, tenantId);
        // Delete the tenant's department record and binding
        iSocialTenantDepartmentService.deleteByTenantIdAndSpaceId(tenantId, spaceId);
        // Delete tenant's user record
        iSocialTenantUserService.deleteByAppIdAndTenantId(appId, tenantId);
    }

    @Override
    public SocialTenantEntity getByAppIdAndTenantId(String appId, String tenantId) {
        return socialTenantMapper.selectByAppIdAndTenantId(appId, tenantId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeSpaceIdSocialBindInfo(String spaceId) {
        // DingTalk binding logic
        TenantBindDTO bindInfo = iSocialTenantBindService.getTenantBindInfoBySpaceId(spaceId);
        ExceptionUtil.isNotNull(bindInfo, TENANT_APP_BIND_INFO_NOT_EXISTS);
        String tenantId = bindInfo.getTenantId();
        // Get the number of current enterprise app to determine whether to delete enterprise organization information
        int appCount = socialTenantMapper.selectCountByTenantId(tenantId);
        // Invitable status of all members of the recovery space (synchronize the default configuration of the startup space)
        Long mainAdminUserId = iSpaceService.getSpaceMainAdminUserId(spaceId);
        SpaceGlobalFeature feature = SpaceGlobalFeature.builder().invitable(true).build();
        iSpaceService.switchSpacePros(mainAdminUserId, spaceId, feature);
        // Delete the tenant's department records and bindings to prevent multiple applications from being deleted by mistake
        iSocialTenantDepartmentService.deleteByTenantIdAndSpaceId(tenantId, spaceId);
        // Delete space binding
        iSocialTenantBindService.removeBySpaceId(spaceId);
        // When there is only one application, delete the tenant's user record.
        // Because the open ID of the same enterprise is the same, the member's open ID will not be deleted here, because there may be repeated binding
        if (appCount <= 1) {
            List<String> openIds = memberMapper.selectOpenIdBySpaceId(CollUtil.toList(spaceId));
            if (!openIds.isEmpty()) {
                iSocialTenantUserService.deleteByTenantIdAndOpenIds(bindInfo.getAppId(), tenantId,
                    openIds);
            }
        }
        String appId = bindInfo.getAppId();
        if (StrUtil.isNotBlank(appId)) {
            // Deactivate app
            socialTenantMapper.setTenantStop(appId, tenantId);
            // Delete callback
            dingTalkService.deleteCallbackUrl(
                dingTalkService.getAgentIdByAppIdAndTenantId(appId, tenantId));
        }
    }

    @Override
    public void createOrUpdateWithScope(SocialPlatformType socialType, SocialAppType appType,
                                        String appId,
                                        String tenantId, String scope, String authInfo) {
        // There is no enterprise application information and it needs to be created. If there is information, but the status is disabled, it needs to be re enabled. Here, an enterprise may have multiple applications
        SocialTenantEntity entity = getByAppIdAndTenantId(appId, tenantId);
        if (entity == null) {
            log.info(
                "Third party platform type:{}, Enterprise tenants opening applications: {}, open application: {}",
                socialType.getValue(), tenantId, appId);
            SocialTenantEntity tenant = new SocialTenantEntity();
            tenant.setAppId(appId);
            tenant.setAppType(appType.getType());
            tenant.setPlatform(socialType.getValue());
            tenant.setTenantId(tenantId);
            tenant.setContactAuthScope(scope);
            tenant.setAuthMode(SocialTenantAuthMode.ADMIN.getValue());
            tenant.setAuthInfo(authInfo);
            boolean flag = SqlHelper.retBool(socialTenantMapper.insert(tenant));
            if (!flag) {
                throw new RuntimeException("Failed to add tenant");
            }
        }
        if (entity != null) {
            entity.setStatus(true);
            if (authInfo != null) {
                entity.setAuthInfo(authInfo);
            }
            if (scope != null) {
                entity.setContactAuthScope(scope);
            }
            boolean flag = SqlHelper.retBool(socialTenantMapper.updateById(entity));
            if (!flag) {
                throw new RuntimeException("Failed to update tenant");
            }
        }
    }

    @Override
    public void createOrUpdateByTenantAndApp(SocialTenantEntity entity) {
        SocialTenantEntity existedEntity =
            getByAppIdAndTenantId(entity.getAppId(), entity.getTenantId());
        if (Objects.isNull(existedEntity)) {
            // The third-party platform information does not exist, new create
            boolean isSaved = save(entity);
            if (!isSaved) {
                throw new IllegalStateException("No tenant data saved.");
            }
        } else {
            // If the information already exists, update the relevant data
            existedEntity.setContactAuthScope(entity.getContactAuthScope());
            existedEntity.setAuthMode(entity.getAuthMode());
            existedEntity.setPermanentCode(entity.getPermanentCode());
            existedEntity.setAuthInfo(entity.getAuthInfo());
            existedEntity.setStatus(entity.getStatus());
            existedEntity.setUpdatedAt(LocalDateTime.now());
            boolean isUpdated = updateById(existedEntity);
            if (!isUpdated) {
                throw new IllegalStateException("No tenant data updated.");
            }
        }

    }

    @Override
    public String getDingTalkAppAgentId(String tenantId, String appId) {
        return baseMapper.selectAgentIdByTenantIdAndAppId(tenantId, appId);
    }

    @Override
    public TenantBaseInfoDto getTenantBaseInfo(String tenantId, String appId) {
        SocialTenantEntity entity = baseMapper.selectByAppIdAndTenantId(appId, tenantId);
        return SocialFactory.getTenantBaseInfoFromAuthInfo(entity.getAuthInfo(),
            SocialAppType.of(entity.getAppType()),
            SocialPlatformType.toEnum(entity.getPlatform()));
    }

    @Override
    public boolean isTenantActive(String tenantId, String appId) {
        Integer status = baseMapper.selectTenantStatusByTenantIdAndAppId(tenantId, appId);
        return SqlHelper.retBool(status);
    }

    @Override
    public List<SocialTenantEntity> getByTenantIds(List<String> tenantIds) {
        return socialTenantMapper.selectByTenantIds(tenantIds);
    }

    @Override
    public List<SocialTenantEntity> getByPlatformTypeAndAppType(SocialPlatformType platformType,
                                                                SocialAppType appType) {
        return socialTenantMapper.selectByPlatformTypeAndAppType(platformType, appType);
    }

    @Override
    public List<String> getSpaceIdsByPlatformTypeAndAppType(SocialPlatformType platformType,
                                                            SocialAppType appType) {
        List<SocialTenantEntity> tenants =
            socialTenantMapper.selectByPlatformTypeAndAppType(platformType, appType);
        Set<String> tenantIds = new HashSet<>();
        Set<String> appIds = new HashSet<>();
        tenants.forEach(i -> {
            tenantIds.add(i.getTenantId());
            appIds.add(i.getAppId());
        });
        return iSocialTenantBindService.getSpaceIdsByTenantIdsAndAppIds(new ArrayList<>(tenantIds),
            new ArrayList<>(appIds));
    }

    @Override
    public String getPermanentCodeByAppIdAndTenantId(String appId, String tenantId) {
        return socialTenantMapper.selectPermanentCodeByAppIdAndTenantId(appId, tenantId);
    }

    @Override
    public LocalDateTime getCreatedAtByAppIdAndTenantId(String appId, String tenantId) {
        return socialTenantMapper.selectCreatedAtByAppIdAndTenantId(appId, tenantId);
    }
}
