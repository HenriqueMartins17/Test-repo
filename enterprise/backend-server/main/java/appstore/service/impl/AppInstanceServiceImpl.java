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

package com.apitable.enterprise.appstore.service.impl;

import static com.apitable.auth.enums.AuthException.UNAUTHORIZED;
import static com.apitable.enterprise.appstore.enums.AppException.APP_EXIST;
import static com.apitable.enterprise.appstore.enums.AppException.APP_NOT_EXIST;
import static com.apitable.space.enums.SpaceException.NOT_SPACE_MAIN_ADMIN;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.core.util.SqlTool;
import com.apitable.enterprise.appstore.entity.AppInstanceEntity;
import com.apitable.enterprise.appstore.entity.MarketplaceSpaceAppRelEntity;
import com.apitable.enterprise.appstore.enums.AppException;
import com.apitable.enterprise.appstore.enums.AppType;
import com.apitable.enterprise.appstore.factory.LarkConfigFactory;
import com.apitable.enterprise.appstore.mapper.AppInstanceMapper;
import com.apitable.enterprise.appstore.model.AppInstance;
import com.apitable.enterprise.appstore.model.InstanceConfig;
import com.apitable.enterprise.appstore.model.LarkInstanceConfig;
import com.apitable.enterprise.appstore.model.LarkInstanceConfigProfile;
import com.apitable.enterprise.appstore.service.IAppInstanceService;
import com.apitable.enterprise.appstore.service.IMarketplaceAppService;
import com.apitable.enterprise.appstore.setting.App;
import com.apitable.enterprise.appstore.setting.AppStore;
import com.apitable.enterprise.appstore.setting.AppStoreConfig;
import com.apitable.enterprise.appstore.setting.AppStoreConfigLoader;
import com.apitable.enterprise.appstore.setting.MarketPlaceConfig;
import com.apitable.enterprise.appstore.setting.MarketPlaceConfigLoader;
import com.apitable.enterprise.social.entity.SocialTenantBindEntity;
import com.apitable.enterprise.social.entity.SocialTenantEntity;
import com.apitable.enterprise.social.enums.SocialAppType;
import com.apitable.enterprise.social.enums.SocialPlatformType;
import com.apitable.enterprise.social.service.ISocialTenantBindService;
import com.apitable.enterprise.social.service.ISocialTenantService;
import com.apitable.enterprise.social.service.IWeComService;
import com.apitable.organization.service.IMemberService;
import com.apitable.space.service.ISpaceService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vikadata.social.feishu.config.FeishuConfigStorage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application instance service implementation
 */
@Service
@Slf4j
public class AppInstanceServiceImpl extends ServiceImpl<AppInstanceMapper, AppInstanceEntity>
    implements IAppInstanceService {

    @Resource
    private AppInstanceMapper appInstanceMapper;

    @Resource
    private ISocialTenantService iSocialTenantService;

    @Resource
    private ISocialTenantBindService iSocialTenantBindService;

    @Resource
    private IMarketplaceAppService iMarketplaceAppService;

    @Resource
    private IMemberService iMemberService;

    @Resource
    private ISpaceService iSpaceService;

    @Resource
    private IWeComService iWeComService;

    @Override
    public List<AppInstanceEntity> getAppInstances(String spaceId) {
        return appInstanceMapper.selectBySpaceId(spaceId);
    }

    @Override
    public AppInstanceEntity getByAppInstanceId(String appInstanceId) {
        return appInstanceMapper.selectByAppInstanceId(appInstanceId);
    }

    @Override
    public boolean isAppInstanceAppKeyExist(String appInstanceId, String appKey) {
        return SqlTool.retCount(
            appInstanceMapper.selectCountByAppInstanceIdAndAppKey(appInstanceId, appKey)) > 0;
    }

    @Override
    public boolean isAppKeyExist(String appKey) {
        return SqlTool.retCount(appInstanceMapper.selectCountByAppKey(appKey)) > 0;
    }

    @Override
    public AppInstanceEntity getBySpaceIdAndAppId(String spaceId, String appId) {
        return appInstanceMapper.selectBySpaceIdAndAppId(spaceId, appId);
    }

    @Override
    public AppInstanceEntity getInstanceBySpaceIdAndAppType(String spaceId, AppType appType) {
        AppStoreConfig appStoreConfig = AppStoreConfigLoader.getConfig();
        List<AppInstanceEntity> instanceEntities = getAppInstances(spaceId);
        if (CollUtil.isEmpty(instanceEntities)) {
            return null;
        }
        List<AppInstanceEntity> larkInstanceEntities = instanceEntities.stream()
            .filter(instance -> AppType.of(appStoreConfig.get(instance.getAppId()).getType()) ==
                appType)
            .collect(Collectors.toList());
        if (CollUtil.isEmpty(larkInstanceEntities)) {
            log.error(
                "The space is not bound to application instances of Lark's self built application type: {}",
                spaceId);
            return null;
        }
        if (larkInstanceEntities.size() > 1) {
            log.error("There are multiple application instances of the same type in the space: {}",
                spaceId);
            return null;
        }
        return CollUtil.getFirst(larkInstanceEntities);
    }

    @Override
    public FeishuConfigStorage buildConfigStorageByInstanceId(String appInstanceId) {
        AppInstanceEntity appInstanceEntity = getByAppInstanceId(appInstanceId);
        ExceptionUtil.isNotNull(appInstanceEntity, AppException.APP_INSTANCE_NOT_EXIST);
        AppStoreConfig appStore = AppStoreConfigLoader.getConfig();
        AppType appType = AppType.of(appStore.get(appInstanceEntity.getAppId()).getType());
        if (appType == AppType.LARK) {
            LarkInstanceConfig instanceConfig =
                LarkInstanceConfig.fromJsonString(appInstanceEntity.getConfig());
            // Set Dynamic Properties
            LarkInstanceConfigProfile profile =
                (LarkInstanceConfigProfile) instanceConfig.getProfile();
            if (StrUtil.isNotBlank(profile.getAppKey()) &&
                StrUtil.isNotBlank(profile.getAppSecret()) &&
                StrUtil.isNotBlank(profile.getEventVerificationToken())) {
                return profile.buildConfigStorage();
            }
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void compatibleMarketPlace(String spaceId) {
        // Query the list of store apps
        String officePreviewOldId = "ina5645957505507647";
        List<String> marketPlaceAppIds = iMarketplaceAppService.getAppIdsBySpaceId(spaceId);
        // Exclude others
        marketPlaceAppIds.removeIf(appId -> !appId.equals(officePreviewOldId));
        MarketPlaceConfig marketPlaceConfig = MarketPlaceConfigLoader.getConfig();
        Set<String> marketAppType = new HashSet<>(marketPlaceAppIds.size());
        marketPlaceAppIds.forEach(appId -> {
            App app = marketPlaceConfig.get(appId);
            marketAppType.add(AppType.valueOf(app.getAppType()).name());
        });
        SocialTenantEntity socialTenant = getSocialTenantBind(spaceId);
        if (socialTenant != null) {
            AppType appType = getSocialTenantAppType(socialTenant);
            if (appType != null) {
                marketAppType.add(appType.name());
            }
        }
        List<AppInstanceEntity> instanceEntities = getAppInstances(spaceId);
        AppStoreConfig appStoreConfig = AppStoreConfigLoader.getConfig();
        List<String> existInstance = new ArrayList<>();
        instanceEntities.forEach(
            appInstance -> existInstance.add(appStoreConfig.get(appInstance.getAppId()).getType()));
        Map<String, AppStore> appStoreMap = new HashMap<>();
        appStoreConfig.values().forEach(appStore -> appStoreMap.put(appStore.getType(), appStore));
        // Migrate the data of marketplace
        marketAppType.forEach(appType -> {
            if (!existInstance.contains(appType)) {
                // Not included, create directly
                create(spaceId, appStoreMap.get(appType).getId());
            }
        });
    }

    public SocialTenantEntity getSocialTenantBind(String spaceId) {
        SocialTenantBindEntity tenantBindEntity = iSocialTenantBindService.getBySpaceId(spaceId);
        if (tenantBindEntity == null) {
            return null;
        }
        return iSocialTenantService.getByAppIdAndTenantId(tenantBindEntity.getAppId(),
            tenantBindEntity.getTenantId());
    }

    public AppType getSocialTenantAppType(SocialTenantEntity socialTenant) {
        SocialPlatformType socialPlatformType =
            SocialPlatformType.toEnum(socialTenant.getPlatform());
        SocialAppType socialAppType = SocialAppType.of(socialTenant.getAppType());
        if (socialPlatformType == SocialPlatformType.DINGTALK) {
            if (socialAppType == SocialAppType.INTERNAL) {
                return AppType.DINGTALK;
            } else if (socialAppType == SocialAppType.ISV) {
                return AppType.DINGTALK_STORE;
            }
        } else if (socialPlatformType == SocialPlatformType.FEISHU) {
            if (socialAppType == SocialAppType.INTERNAL) {
                return AppType.LARK;
            } else if (socialAppType == SocialAppType.ISV) {
                return AppType.LARK_STORE;
            }
        } else if (socialPlatformType == SocialPlatformType.WECOM) {
            if (socialAppType == SocialAppType.INTERNAL) {
                return AppType.WECOM;
            } else if (socialAppType == SocialAppType.ISV) {
                return AppType.WECOM_STORE;
            }
        }
        return null;
    }

    @Override
    public boolean checkInstanceExist(String spaceId, String appType) {
        AppInstanceEntity appInstance = getInstanceBySpaceAndAppType(spaceId, appType);
        if (appInstance == null) {
            MarketPlaceConfig marketPlaceConfig = MarketPlaceConfigLoader.getConfig();
            App app = marketPlaceConfig.ofAppType(appType);
            if (app != null) {
                // Synchronize data
                return iMarketplaceAppService.checkBySpaceIdAndAppId(spaceId, app.getAppId());
            }
            return false;
        }
        return true;
    }

    public AppInstanceEntity getInstanceBySpaceAndAppType(String spaceId, String appType) {
        List<AppInstanceEntity> instanceEntities = getAppInstances(spaceId);
        AppStoreConfig appStoreConfig = AppStoreConfigLoader.getConfig();
        for (AppInstanceEntity appInstance : instanceEntities) {
            AppStore appStore = appStoreConfig.get(appInstance.getAppId());
            AppType type = AppType.valueOf(appStore.getType());
            if (type.name().equalsIgnoreCase(appType)) {
                return appInstance;
            }
        }
        return null;
    }

    @Override
    public List<AppInstance> getAppInstancesBySpaceId(String spaceId) {
        // Query the application instance list
        List<AppInstanceEntity> instanceEntities = getAppInstances(spaceId);
        List<AppInstance> appInstances = new ArrayList<>();
        if (CollUtil.isEmpty(instanceEntities)) {
            return appInstances;
        }
        instanceEntities.forEach(instance -> appInstances.add(buildInstance(instance)));
        return appInstances;
    }

    @Override
    public AppInstance getAppInstance(String appInstanceId) {
        AppInstanceEntity appInstanceEntity = getByAppInstanceId(appInstanceId);
        ExceptionUtil.isNotNull(appInstanceEntity, AppException.APP_INSTANCE_NOT_EXIST);
        return buildInstance(appInstanceEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createInstanceByAppType(String spaceId, String appType) {
        AppStoreConfig appStoreConfig = AppStoreConfigLoader.getConfig();
        AppStore appStore = appStoreConfig.ofType(appType);
        if (appStore == null) {
            throw new RuntimeException("App does not exist");
        }
        // Determine whether the space has been opened
        AppInstanceEntity instance = getBySpaceIdAndAppId(spaceId, appStore.getId());
        ExceptionUtil.isNull(instance, APP_EXIST);
        create(spaceId, appStore.getId());
        // Redundant data to prevent version rollback
        MarketPlaceConfig marketPlaceConfig = MarketPlaceConfigLoader.getConfig();
        ;
        App app = marketPlaceConfig.ofAppType(appType);
        if (app != null) {
            iMarketplaceAppService.openSpaceApp(spaceId, app.getAppId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppInstance createInstance(String spaceId, String appId) {
        // Judge whether the application exists
        AppStoreConfig appStore = AppStoreConfigLoader.getConfig();
        ExceptionUtil.isTrue(appStore.containsKey(appId), APP_NOT_EXIST);
        // Determine whether the space has been opened
        AppInstanceEntity instance = getBySpaceIdAndAppId(spaceId, appId);
        ExceptionUtil.isNull(instance, APP_EXIST);
        // Redundant data to prevent version rollback
        MarketPlaceConfig marketPlaceConfig = MarketPlaceConfigLoader.getConfig();
        App app = marketPlaceConfig.ofAppType(appStore.get(appId).getType());
        if (app != null) {
            iMarketplaceAppService.openSpaceApp(spaceId, app.getAppId());
        }
        // Create Instance
        return create(spaceId, appId);
    }

    @Override
    public AppInstance create(String spaceId, String appId) {
        AppInstanceEntity instanceEntity = new AppInstanceEntity();
        instanceEntity.setSpaceId(spaceId);
        instanceEntity.setAppId(appId);
        instanceEntity.setAppInstanceId(String.format("ai-%s", IdUtil.fastSimpleUUID()));
        AppStoreConfig appStoreConfig = AppStoreConfigLoader.getConfig();
        AppStore appStore = appStoreConfig.get(appId);
        AppType appType = AppType.of(appStore.getType());
        instanceEntity.setType(appType.name());
        if (appType == AppType.LARK) {
            LarkInstanceConfigProfile profile = new LarkInstanceConfigProfile("", "");
            LarkInstanceConfig config = new LarkInstanceConfig(profile);
            instanceEntity.setConfig(config.toJsonString());
        }
        save(instanceEntity);
        return buildInstance(instanceEntity);
    }

    @Override
    public AppInstance updateAppInstanceConfig(String appInstanceId, InstanceConfig config) {
        AppInstanceEntity instanceEntity = appInstanceMapper.selectByAppInstanceId(appInstanceId);
        ExceptionUtil.isNotNull(instanceEntity, AppException.APP_INSTANCE_NOT_EXIST);
        AppInstanceEntity appInstanceEntity = new AppInstanceEntity();
        appInstanceEntity.setId(instanceEntity.getId());
        appInstanceEntity.setConfig(config.toJsonString());
        updateById(appInstanceEntity);
        instanceEntity.setConfig(config.toJsonString());
        return buildInstance(instanceEntity);
    }

    @Override
    public void updateAppInstanceStatus(String appInstanceId, boolean isEnabled) {
        AppInstanceEntity instanceEntity = appInstanceMapper.selectByAppInstanceId(appInstanceId);
        ExceptionUtil.isNotNull(instanceEntity, AppException.APP_INSTANCE_NOT_EXIST);
        AppInstanceEntity appInstanceEntity = new AppInstanceEntity();
        appInstanceEntity.setId(instanceEntity.getId());
        appInstanceEntity.setIsEnabled(isEnabled);
        updateById(appInstanceEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAppInstance(Long userId, String appInstanceId) {
        AppInstanceEntity instanceEntity = appInstanceMapper.selectByAppInstanceId(appInstanceId);
        ExceptionUtil.isNotNull(instanceEntity, AppException.APP_INSTANCE_NOT_EXIST);
        // Check
        Long memberId =
            iMemberService.getMemberIdByUserIdAndSpaceId(userId, instanceEntity.getSpaceId());
        // Detect whether the deactivated space is the space of the current user
        ExceptionUtil.isNotNull(memberId, UNAUTHORIZED);
        // Detect whether it is the primary administrator
        Long mainMemberId = iSpaceService.getSpaceMainAdminMemberId(instanceEntity.getSpaceId());
        ExceptionUtil.isTrue(ObjectUtil.equal(memberId, mainMemberId), NOT_SPACE_MAIN_ADMIN);
        // Delete related apps
        AppStoreConfig appStoreConfig = AppStoreConfigLoader.getConfig();
        AppStore appStore = appStoreConfig.get(instanceEntity.getAppId());
        AppType appType = AppType.of(appStore.getType());
        if (appType == AppType.LARK) {
            // Delete Lark Tenant Configuration
            LarkInstanceConfig instanceConfig =
                LarkInstanceConfig.fromJsonString(instanceEntity.getConfig());
            // Set Dynamic Properties
            LarkInstanceConfigProfile profile =
                (LarkInstanceConfigProfile) instanceConfig.getProfile();
            if (StrUtil.isNotBlank(profile.getAppKey()) &&
                StrUtil.isNotBlank(profile.getAppSecret()) &&
                StrUtil.isNotBlank(profile.getEventVerificationToken())) {
                String spaceId = instanceEntity.getSpaceId();
                List<String> tenantIds = iSocialTenantBindService.getTenantIdBySpaceId(spaceId);
                if (CollUtil.isNotEmpty(tenantIds)) {
                    // Query tenant information
                    List<SocialTenantEntity> tenantEntities =
                        iSocialTenantService.getByTenantIds(tenantIds);
                    if (CollUtil.isNotEmpty(tenantEntities)) {
                        List<SocialTenantEntity> feishuTenants = tenantEntities.stream()
                            .filter(tenant -> SocialPlatformType.toEnum(tenant.getPlatform()) ==
                                SocialPlatformType.FEISHU
                                && SocialAppType.of(tenant.getAppType()) == SocialAppType.INTERNAL)
                            .collect(Collectors.toList());
                        feishuTenants.forEach(
                            tenant -> iSocialTenantService.removeInternalTenant(profile.getAppKey(),
                                tenant.getTenantId(), spaceId));
                    }
                }
            }
        } else if (appType == AppType.DINGTALK) {
            // DingTalk application deactivation logic
            iSocialTenantService.removeSpaceIdSocialBindInfo(instanceEntity.getSpaceId());
        } else if (appType == AppType.WECOM) {
            // WeCom deactivation logic
            iWeComService.stopWeComApp(instanceEntity.getSpaceId());
        }
        appInstanceMapper.deleteByAppInstanceId(appInstanceId);
        MarketPlaceConfig marketPlaceConfig = MarketPlaceConfigLoader.getConfig();
        App app = marketPlaceConfig.ofAppType(appType.name());
        if (app != null) {
            iMarketplaceAppService.removeBySpaceIdAndAppId(instanceEntity.getSpaceId(),
                app.getAppId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBySpaceIdAndAppType(String spaceId, String appType) {
        AppInstanceEntity appInstance = getInstanceBySpaceAndAppType(spaceId, appType);
        if (appInstance != null) {
            appInstanceMapper.deleteByAppInstanceId(appInstance.getAppInstanceId());
        }
        MarketPlaceConfig marketPlaceConfig = MarketPlaceConfigLoader.getConfig();
        App app = marketPlaceConfig.ofAppType(appType);
        if (app != null) {
            // Delete data synchronously
            MarketplaceSpaceAppRelEntity marketplaceSpaceAppRelEntity =
                iMarketplaceAppService.getBySpaceIdAndAppId(spaceId, app.getAppId());
            if (marketplaceSpaceAppRelEntity != null) {
                iMarketplaceAppService.removeById(marketplaceSpaceAppRelEntity.getId());
            }
        }
    }

    @Override
    public AppInstance buildInstance(AppInstanceEntity instanceEntity) {
        AppInstance appInstance = new AppInstance();
        appInstance.setSpaceId(instanceEntity.getSpaceId());
        appInstance.setAppId(instanceEntity.getAppId());
        appInstance.setAppInstanceId(instanceEntity.getAppInstanceId());
        appInstance.setIsEnabled(instanceEntity.getIsEnabled());
        appInstance.setCreatedAt(instanceEntity.getCreatedAt());
        AppStoreConfig appStore = AppStoreConfigLoader.getConfig();
        appInstance.setType(appStore.get(instanceEntity.getAppId()).getType());
        AppType appType = AppType.of(appInstance.getType());
        if (appType == AppType.LARK) {
            LarkInstanceConfig instanceConfig =
                LarkInstanceConfig.fromJsonString(instanceEntity.getConfig());
            // Set Dynamic Properties
            LarkInstanceConfigProfile profile =
                (LarkInstanceConfigProfile) instanceConfig.getProfile();
            if (StrUtil.isNotBlank(profile.getAppKey()) &&
                StrUtil.isNotBlank(profile.getAppSecret())) {
                String redirectUri =
                    LarkConfigFactory.createRedirectUri(instanceEntity.getAppInstanceId());
                profile.setRedirectUrl(redirectUri);
                String entryUrl = LarkConfigFactory.createAuthUrl(profile.getAppKey(), redirectUri);
                profile.setPcUrl(entryUrl);
                profile.setMobileUrl(entryUrl);
            }
            if (StrUtil.isNotBlank(profile.getEventVerificationToken())) {
                profile.setEventUrl(
                    LarkConfigFactory.createEventUri(instanceEntity.getAppInstanceId()));
            }
            instanceConfig.setProfile(profile);
            appInstance.setConfig(instanceConfig);
        }
        return appInstance;
    }
}
