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

package com.apitable.enterprise.appstore.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;

import com.apitable.enterprise.appstore.entity.AppInstanceEntity;
import com.apitable.enterprise.appstore.enums.AppType;
import com.apitable.enterprise.appstore.model.AppInstance;
import com.apitable.enterprise.appstore.model.InstanceConfig;
import com.vikadata.social.feishu.config.FeishuConfigStorage;

/**
 * Application instance service
 */
public interface IAppInstanceService extends IService<AppInstanceEntity> {

    /**
     * Get space application examples
     *
     * @param spaceId Space ID
     * @return AppInstanceEntity List
     */
    List<AppInstanceEntity> getAppInstances(String spaceId);

    /**
     * Get the application instance according to the instance ID
     *
     * @param appInstanceId Instance ID
     * @return AppInstanceEntity
     */
    AppInstanceEntity getByAppInstanceId(String appInstanceId);

    /**
     * Whether there is an application key configured for the application instance
     *
     * @param appInstanceId Application Instance ID
     * @param appKey Application instance KEY
     * @return true | false
     */
    boolean isAppInstanceAppKeyExist(String appInstanceId, String appKey);

    /**
     * Whether there is an application key configured for the application instance
     *
     * @param appKey Application instance KEY
     * @return true | false
     */
    boolean isAppKeyExist(String appKey);

    /**
     * Get application instance
     *
     * @param spaceId Space ID
     * @param appId Application ID
     * @return AppInstanceEntity
     */
    AppInstanceEntity getBySpaceIdAndAppId(String spaceId, String appId);

    /**
     * Get the application instance of the specified type of space
     *
     * @param spaceId Space ID
     * @param appType Application Type
     * @return AppInstanceEntity
     */
    AppInstanceEntity getInstanceBySpaceIdAndAppType(String spaceId, AppType appType);

    /**
     * Build configuration storage according to application instance ID
     *
     * @param appInstanceId Application Instance ID
     * @return Application configuration storage
     */
    FeishuConfigStorage buildConfigStorageByInstanceId(String appInstanceId);

    /**
     * Compatible application market data
     *
     * @param spaceId Space ID
     */
    void compatibleMarketPlace(String spaceId);

    /**
     * Check whether the application of the specified Application Type has been opened
     *
     * @param spaceId Space ID
     * @param appType Application Type
     * @return true | false
     */
    boolean checkInstanceExist(String spaceId, String appType);

    /**
     * Get the list of application instances
     *
     * @param spaceId Space ID
     * @return AppInstance List
     */
    List<AppInstance> getAppInstancesBySpaceId(String spaceId);

    /**
     * Get the application instance view according to the instance ID
     *
     * @param appInstanceId Application Instance ID
     * @return AppInstance
     */
    AppInstance getAppInstance(String appInstanceId);

    /**
     * Create an application instance of the specified type
     *
     * @param spaceId Space ID
     * @param appType Application Type
     */
    void createInstanceByAppType(String spaceId, String appType);

    /**
     * Create an application instance
     *
     * @param spaceId Space ID
     * @param appId Application ID
     * @return AppInstance
     */
    AppInstance createInstance(String spaceId, String appId);

    /**
     * Create an application instance
     *
     * @param spaceId Space ID
     * @param appId Application ID
     * @return AppInstance
     */
    AppInstance create(String spaceId, String appId);

    /**
     * Update application instance configuration
     *
     * @param appInstanceId Application Instance ID
     * @param config Application instance configuration
     * @return AppInstance
     */
    AppInstance updateAppInstanceConfig(String appInstanceId, InstanceConfig config);

    /**
     * Change application instance status
     *
     * @param appInstanceId Application Instance ID
     * @param isEnabled Whether to open
     */
    void updateAppInstanceStatus(String appInstanceId, boolean isEnabled);

    /**
     * Delete Application Instance
     *
     * @param userId User ID
     * @param appInstanceId Application Instance ID
     */
    void deleteAppInstance(Long userId, String appInstanceId);

    /**
     * Delete the application of the specified type of space
     *
     * @param spaceId Space ID
     * @param appType Application Type
     */
    void deleteBySpaceIdAndAppType(String spaceId, String appType);

    /**
     * Build application instance view
     *
     * @param instanceEntity Instance Entity Class
     * @return AppInstance
     */
    AppInstance buildInstance(AppInstanceEntity instanceEntity);
}
