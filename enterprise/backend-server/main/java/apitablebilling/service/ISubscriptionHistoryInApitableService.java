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

package com.apitable.enterprise.apitablebilling.service;

import com.apitable.enterprise.apitablebilling.entity.SubscriptionEntity;
import com.apitable.enterprise.apitablebilling.entity.SubscriptionHistoryEntity;
import com.apitable.enterprise.apitablebilling.enums.ChangeType;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * <p>
 * Subscription History Service
 * </p>
 */
public interface ISubscriptionHistoryInApitableService extends IService<SubscriptionHistoryEntity> {

    /**
     * save history
     *
     * @param entity     subscription
     * @param changeType change type
     */
    void saveHistory(SubscriptionEntity entity, ChangeType changeType);

    /**
     * Batch save
     *
     * @param entities   subscriptions
     * @param changeType change type
     */
    void saveBatchHistory(List<SubscriptionEntity> entities, ChangeType changeType);
}
