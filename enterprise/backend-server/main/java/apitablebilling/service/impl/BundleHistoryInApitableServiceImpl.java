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

package com.apitable.enterprise.apitablebilling.service.impl;

import com.apitable.enterprise.apitablebilling.entity.BundleEntity;
import com.apitable.enterprise.apitablebilling.entity.BundleHistoryEntity;
import com.apitable.enterprise.apitablebilling.enums.ChangeType;
import com.apitable.enterprise.apitablebilling.mapper.BundleHistoryInApitableMapper;
import com.apitable.enterprise.apitablebilling.service.IBundleHistoryInApitableService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Bundle History Service Implement Class
 * </p>
 */
@Service
public class BundleHistoryInApitableServiceImpl
    extends ServiceImpl<BundleHistoryInApitableMapper, BundleHistoryEntity> implements
    IBundleHistoryInApitableService {

    @Override
    public void saveHistory(BundleEntity bundle, ChangeType changeType) {
        save(build(bundle, changeType));
    }

    @Override
    public void saveBatchHistory(List<BundleEntity> entities, ChangeType changeType) {
        List<BundleHistoryEntity> historyEntities = new ArrayList<>();
        entities.forEach(entity -> historyEntities.add(build(entity, changeType)));
        saveBatch(historyEntities);
    }

    private BundleHistoryEntity build(BundleEntity entity, ChangeType changeType) {
        BundleHistoryEntity bundleHistoryEntity = new BundleHistoryEntity();
        bundleHistoryEntity.setTargetRowId(entity.getId());
        bundleHistoryEntity.setChangeType(changeType.name());
        bundleHistoryEntity.setBundleId(entity.getBundleId());
        bundleHistoryEntity.setSpaceId(entity.getSpaceId());
        bundleHistoryEntity.setState(entity.getState());
        bundleHistoryEntity.setStartDate(entity.getStartDate());
        bundleHistoryEntity.setEndDate(entity.getEndDate());
        bundleHistoryEntity.setIsDeleted(entity.getIsDeleted());
        bundleHistoryEntity.setCreatedBy(entity.getCreatedBy());
        bundleHistoryEntity.setUpdatedBy(entity.getUpdatedBy());
        return bundleHistoryEntity;
    }
}
