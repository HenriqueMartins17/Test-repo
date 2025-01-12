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

import com.apitable.enterprise.apitablebilling.entity.BundleEntity;
import com.apitable.enterprise.apitablebilling.entity.BundleHistoryEntity;
import com.apitable.enterprise.apitablebilling.enums.ChangeType;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * <p>
 * Bundle History Service
 * </p>
 */
public interface IBundleHistoryInApitableService extends IService<BundleHistoryEntity> {

    /**
     * Save history
     *
     * @param entity     bundle entity
     * @param changeType change type
     */
    void saveHistory(BundleEntity entity, ChangeType changeType);

    /**
     * Batch save history
     *
     * @param entities   bundle entities
     * @param changeType change type
     */
    void saveBatchHistory(List<BundleEntity> entities, ChangeType changeType);
}
