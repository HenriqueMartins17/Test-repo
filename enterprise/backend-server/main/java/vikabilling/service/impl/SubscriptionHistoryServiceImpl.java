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

package com.apitable.enterprise.vikabilling.service.impl;

import java.util.ArrayList;
import java.util.List;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.apitable.enterprise.vikabilling.mapper.SubscriptionHistoryMapper;
import com.apitable.enterprise.vikabilling.enums.ChangeType;
import com.apitable.enterprise.vikabilling.service.ISubscriptionHistoryService;
import com.apitable.enterprise.vikabilling.entity.SubscriptionEntity;
import com.apitable.enterprise.vikabilling.entity.SubscriptionHistoryEntity;

import org.springframework.stereotype.Service;

/**
 * <p>
 * Subscription History Service Implement Class
 * </p>
 */
@Service
public class SubscriptionHistoryServiceImpl extends ServiceImpl<SubscriptionHistoryMapper, SubscriptionHistoryEntity> implements ISubscriptionHistoryService {

    @Override
    public void saveHistory(SubscriptionEntity entity, ChangeType changeType) {
        save(build(entity, changeType));
    }

    @Override
    public void saveBatchHistory(List<SubscriptionEntity> entities, ChangeType changeType) {
        List<SubscriptionHistoryEntity> historyEntities = new ArrayList<>();
        entities.forEach(entity -> historyEntities.add(build(entity, changeType)));
        saveBatch(historyEntities);
    }

    private SubscriptionHistoryEntity build(SubscriptionEntity entity, ChangeType changeType) {
        SubscriptionHistoryEntity subscriptionHistoryEntity = new SubscriptionHistoryEntity();
        subscriptionHistoryEntity.setTargetRowId(entity.getId());
        subscriptionHistoryEntity.setChangeType(changeType.name());
        subscriptionHistoryEntity.setSpaceId(entity.getSpaceId());
        subscriptionHistoryEntity.setBundleId(entity.getBundleId());
        subscriptionHistoryEntity.setSubscriptionId(entity.getSubscriptionId());
        subscriptionHistoryEntity.setProductName(entity.getProductName());
        subscriptionHistoryEntity.setProductCategory(entity.getProductCategory());
        subscriptionHistoryEntity.setPlanId(entity.getPlanId());
        subscriptionHistoryEntity.setState(entity.getState());
        subscriptionHistoryEntity.setPhase(entity.getPhase());
        subscriptionHistoryEntity.setBundleStartDate(entity.getBundleStartDate());
        subscriptionHistoryEntity.setStartDate(entity.getStartDate());
        subscriptionHistoryEntity.setExpireDate(entity.getExpireDate());
        subscriptionHistoryEntity.setIsDeleted(entity.getIsDeleted());
        subscriptionHistoryEntity.setCreatedBy(entity.getCreatedBy());
        subscriptionHistoryEntity.setUpdatedBy(entity.getUpdatedBy());
        return subscriptionHistoryEntity;
    }
}
