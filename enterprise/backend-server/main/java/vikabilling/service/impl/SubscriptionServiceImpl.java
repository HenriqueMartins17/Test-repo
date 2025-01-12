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

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.annotation.Resource;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;

import com.apitable.enterprise.vikabilling.core.Subscription;
import com.apitable.enterprise.vikabilling.mapper.SubscriptionMapper;
import com.apitable.enterprise.vikabilling.service.IBundleService;
import com.apitable.enterprise.vikabilling.service.ISubscriptionService;
import com.apitable.enterprise.vikabilling.enums.ChangeType;
import com.apitable.enterprise.vikabilling.enums.SubscriptionPhase;
import com.apitable.enterprise.vikabilling.enums.SubscriptionState;
import com.apitable.enterprise.vikabilling.service.ISubscriptionHistoryService;
import com.apitable.enterprise.vikabilling.enums.ProductCategory;
import com.apitable.core.util.SqlTool;
import com.apitable.enterprise.vikabilling.entity.SubscriptionEntity;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Subscription Service Implement Class
 * </p>
 */
@Service
@Slf4j
public class SubscriptionServiceImpl extends ServiceImpl<SubscriptionMapper, SubscriptionEntity>
    implements ISubscriptionService {

    @Resource
    private ISubscriptionHistoryService iSubscriptionHistoryService;

    @Resource
    private IBundleService iBundleService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(SubscriptionEntity entity) {
        save(entity);
        iSubscriptionHistoryService.saveHistory(entity, ChangeType.INSERT);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createBatch(List<SubscriptionEntity> entities) {
        saveBatch(entities);
        iSubscriptionHistoryService.saveBatchHistory(entities, ChangeType.INSERT);
    }

    @Override
    public SubscriptionEntity getBySubscriptionId(String subscriptionId) {
        return baseMapper.selectBySubscriptionId(subscriptionId);
    }

    @Override
    public List<SubscriptionEntity> getByBundleIds(List<String> bundleIds) {
        return baseMapper.selectByBundleIds(bundleIds);
    }

    @Override
    public List<Subscription> getSubscriptionsByBundleIds(List<String> bundleIds) {
        List<SubscriptionEntity> subscriptionEntities = getByBundleIds(bundleIds);
        // convert subscription entity to viewable subscription
        Function<SubscriptionEntity, Subscription> subscriptionConverter = subscriptionEntity ->
            Subscription.builder()
                .spaceId(subscriptionEntity.getSpaceId())
                .bundleId(subscriptionEntity.getBundleId())
                .subscriptionId(subscriptionEntity.getSubscriptionId())
                .productName(subscriptionEntity.getProductName())
                .productCategory(ProductCategory.valueOf(subscriptionEntity.getProductCategory()))
                .planId(subscriptionEntity.getPlanId())
                .state(SubscriptionState.valueOf(subscriptionEntity.getState()))
                .startDate(subscriptionEntity.getStartDate())
                .expireDate(subscriptionEntity.getExpireDate())
                .phase(SubscriptionPhase.of(subscriptionEntity.getPhase()))
                .build();
        return subscriptionEntities.stream()
            .map(subscriptionConverter).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBySubscriptionId(String subscriptionId,
                                       SubscriptionEntity updatedSubscription) {
        SubscriptionEntity subscriptionEntity = getBySubscriptionId(subscriptionId);
        if (subscriptionEntity == null) {
            throw new RuntimeException("update subscription error");
        }
        updatedSubscription.setId(subscriptionEntity.getId());
        updateById(updatedSubscription);
        if (updatedSubscription.getProductName() != null) {
            subscriptionEntity.setProductName(updatedSubscription.getProductName());
        }
        if (updatedSubscription.getProductCategory() != null) {
            subscriptionEntity.setProductCategory(updatedSubscription.getProductCategory());
        }
        if (updatedSubscription.getPlanId() != null) {
            subscriptionEntity.setPlanId(updatedSubscription.getPlanId());
        }
        if (updatedSubscription.getStartDate() != null) {
            subscriptionEntity.setStartDate(updatedSubscription.getStartDate());
        }
        if (updatedSubscription.getExpireDate() != null) {
            subscriptionEntity.setExpireDate(updatedSubscription.getExpireDate());
        }
        if (updatedSubscription.getState() != null) {
            subscriptionEntity.setState(updatedSubscription.getState());
        }
        if (updatedSubscription.getPhase() != null) {
            subscriptionEntity.setPhase(updatedSubscription.getPhase());
        }
        iSubscriptionHistoryService.saveHistory(subscriptionEntity, ChangeType.UPDATE);
    }

    @Override
    public List<SubscriptionEntity> getByBundleIdAndState(String bundleId,
                                                          SubscriptionState state) {
        return baseMapper.selectByBundleIdAndState(bundleId, state);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeBatchBySubscriptionIds(List<String> subscriptionIds) {
        baseMapper.updateIsDeletedBySubscriptionIds(subscriptionIds, true);
        List<SubscriptionEntity> subscriptionEntities =
            baseMapper.selectByBundleIds(subscriptionIds);
        if (!subscriptionEntities.isEmpty()) {
            iSubscriptionHistoryService.saveBatchHistory(subscriptionEntities, ChangeType.DELETE);
        }
    }

    @Override
    public void restoreBySubscriptionIds(List<String> subscriptionIds) {
        baseMapper.updateIsDeletedBySubscriptionIds(subscriptionIds, false);
        List<SubscriptionEntity> subscriptionEntities =
            baseMapper.selectBySubscriptionIds(subscriptionIds);
        List<String> bundleIds =
            subscriptionEntities.stream().map(SubscriptionEntity::getBundleId)
                .collect(Collectors.toList());
        if (!subscriptionEntities.isEmpty()) {
            iSubscriptionHistoryService.saveBatchHistory(subscriptionEntities, ChangeType.UPDATE);
        }
        if (!bundleIds.isEmpty()) {
            iBundleService.restoreByBundleIds(bundleIds);
        }
    }

    @Override
    public String getActiveTrailSubscriptionIdBySpaceId(String spaceId) {
        return baseMapper.selectSubscriptionIdBySpaceIdAndPhaseIgnoreDeleted(spaceId,
            SubscriptionPhase.TRIAL.getName());
    }

    @Override
    public List<String> getBundleIdsBySubscriptionIds(List<String> subscriptionIds) {
        return baseMapper.selectBundleIdsBySubscriptionIds(subscriptionIds);
    }

    @Override
    public boolean bundlesHaveSubscriptions(List<String> bundleIds) {
        return SqlTool.retCount(baseMapper.selectCountByBundleIds(bundleIds)) > 0;
    }
}
