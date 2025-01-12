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

import com.apitable.enterprise.appstore.entity.AppInstanceEntity;
import com.apitable.enterprise.appstore.model.AppInstance;
import com.apitable.enterprise.appstore.model.LarkInstanceConfig;

/**
 * Lark self built application service interface
 */
public interface ILarkAppInstanceConfigService {

    /**
     * Get application instance configuration
     *
     * @param appInstanceId Application instance ID
     * @return LarkInstanceConfig
     */
    LarkInstanceConfig getLarkConfig(String appInstanceId);

    /**
     * Get application instance configuration
     *
     * @param instanceEntity Application instance entity
     * @return LarkInstanceConfig
     */
    LarkInstanceConfig getLarkConfig(AppInstanceEntity instanceEntity);

    /**
     * Update the basic configuration of flying book application example
     *
     * @param appInstanceId Application instance ID
     * @param appKey Lark self built application ID of user enterprise
     * @param appSecret Lark self built application key
     * @return AppInstance
     */
    AppInstance updateLarkBaseConfig(String appInstanceId, String appKey, String appSecret);

    /**
     * Update Lark application instance event configuration
     *
     * @param appInstanceId Application instance ID
     * @param eventEncryptKey Event Encryption Key
     * @param eventVerificationToken Event validation token
     * @return AppInstance
     */
    AppInstance updateLarkEventConfig(String appInstanceId, String eventEncryptKey, String eventVerificationToken);

    /**
     * Event check completed
     *
     * @param appInstanceId Application instance ID
     */
    void updateLarkEventCheckStatus(String appInstanceId);

    /**
     * Set completion status ID
     *
     * @param appInstanceId Application instance ID
     */
    void updateLarkConfigCompleteStatus(String appInstanceId);

    /**
     * Synchronization of address book status completed
     *
     * @param appInstanceId Application instance ID
     */
    void updateLarkContactSyncStatus(String appInstanceId);
}
