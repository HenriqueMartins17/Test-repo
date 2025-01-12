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

import jakarta.annotation.Resource;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import com.apitable.enterprise.appstore.entity.AppInstanceEntity;
import com.apitable.enterprise.appstore.enums.AppException;
import com.apitable.enterprise.appstore.enums.AppType;
import com.apitable.enterprise.appstore.model.AppInstance;
import com.apitable.enterprise.appstore.model.LarkInstanceConfig;
import com.apitable.enterprise.appstore.model.LarkInstanceConfigProfile;
import com.apitable.enterprise.appstore.service.IAppInstanceService;
import com.apitable.enterprise.appstore.service.ILarkAppInstanceConfigService;
import com.apitable.core.exception.BusinessException;
import com.apitable.core.util.ExceptionUtil;

import org.springframework.stereotype.Service;

/**
 * Lark Self built Application Service Interface Implementation
 */
@Service
@Slf4j
public class LarkAppInstanceConfigServiceImpl implements ILarkAppInstanceConfigService {

    @Resource
    private IAppInstanceService iAppInstanceService;

    @Override
    public LarkInstanceConfig getLarkConfig(String appInstanceId) {
        AppInstanceEntity instanceEntity = iAppInstanceService.getByAppInstanceId(appInstanceId);
        ExceptionUtil.isNotNull(instanceEntity, AppException.APP_INSTANCE_NOT_EXIST);
        return getLarkConfig(instanceEntity);
    }

    @Override
    public LarkInstanceConfig getLarkConfig(AppInstanceEntity instanceEntity) {
        LarkInstanceConfig config = LarkInstanceConfig.fromJsonString(instanceEntity.getConfig());
        if (config.getType() != AppType.LARK) {
            throw new BusinessException(AppException.NOT_LARK_APP_TYPE);
        }
        return config;
    }

    @Override
    public AppInstance updateLarkBaseConfig(String appInstanceId, String appKey, String appSecret) {
        log.info("Initialize Lark configuration");
        AppInstanceEntity instanceEntity = iAppInstanceService.getByAppInstanceId(appInstanceId);
        ExceptionUtil.isNotNull(instanceEntity, AppException.APP_INSTANCE_NOT_EXIST);
        // Check whether it is the configuration application KEY of the current application instance
        boolean isAppInstanceAppKeyExist = StrUtil.isNotBlank(instanceEntity.getAppKey()) &&
            instanceEntity.getAppKey().equals(appKey);
        if (!isAppInstanceAppKeyExist) {
            // It is not the configuration of this instance. Check whether the App Key already exists globally
            boolean isAppKeyExist = iAppInstanceService.isAppKeyExist(appKey);
            ExceptionUtil.isFalse(isAppKeyExist, AppException.APP_KEY_EXIST);
        }
        AppInstanceEntity updatedAppInstance = new AppInstanceEntity();
        updatedAppInstance.setId(instanceEntity.getId());
        updatedAppInstance.setAppKey(appKey);
        updatedAppInstance.setAppSecret(appSecret);
        LarkInstanceConfig config = getLarkConfig(instanceEntity);
        LarkInstanceConfigProfile profile = (LarkInstanceConfigProfile) config.getProfile();
        profile.setAppKey(appKey);
        profile.setAppSecret(appSecret);
        updatedAppInstance.setConfig(config.toJsonString());
        iAppInstanceService.updateById(updatedAppInstance);
        instanceEntity.setAppKey(appKey);
        instanceEntity.setAppSecret(appSecret);
        instanceEntity.setConfig(config.toJsonString());
        return iAppInstanceService.buildInstance(instanceEntity);
    }

    @Override
    public AppInstance updateLarkEventConfig(String appInstanceId, String eventEncryptKey,
                                             String eventVerificationToken) {
        LarkInstanceConfig config = getLarkConfig(appInstanceId);
        LarkInstanceConfigProfile profile = (LarkInstanceConfigProfile) config.getProfile();
        profile.setEventEncryptKey(eventEncryptKey);
        profile.setEventVerificationToken(eventVerificationToken);
        return iAppInstanceService.updateAppInstanceConfig(appInstanceId, config);
    }

    @Override
    public void updateLarkEventCheckStatus(String appInstanceId) {
        LarkInstanceConfig config = getLarkConfig(appInstanceId);
        LarkInstanceConfigProfile profile = (LarkInstanceConfigProfile) config.getProfile();
        profile.setEventCheck(true);
        iAppInstanceService.updateAppInstanceConfig(appInstanceId, config);
    }

    @Override
    public void updateLarkConfigCompleteStatus(String appInstanceId) {
        LarkInstanceConfig config = getLarkConfig(appInstanceId);
        LarkInstanceConfigProfile profile = (LarkInstanceConfigProfile) config.getProfile();
        profile.setConfigComplete(true);
        iAppInstanceService.updateAppInstanceConfig(appInstanceId, config);
    }

    @Override
    public void updateLarkContactSyncStatus(String appInstanceId) {
        LarkInstanceConfig config = getLarkConfig(appInstanceId);
        LarkInstanceConfigProfile profile = (LarkInstanceConfigProfile) config.getProfile();
        profile.setConfigComplete(true);
        profile.setContactSyncDone(true);
        iAppInstanceService.updateAppInstanceConfig(appInstanceId, config);
    }
}
