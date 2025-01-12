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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import jakarta.annotation.Resource;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;

import com.apitable.shared.clock.spring.ClockManager;
import com.apitable.enterprise.vikabilling.core.Bundle;
import com.apitable.enterprise.vikabilling.core.Subscription;
import com.apitable.enterprise.vikabilling.mapper.BundleMapper;
import com.apitable.enterprise.vikabilling.service.IBundleHistoryService;
import com.apitable.enterprise.vikabilling.service.IBundleService;
import com.apitable.enterprise.vikabilling.service.ISubscriptionService;
import com.apitable.enterprise.vikabilling.enums.ChangeType;
import com.apitable.enterprise.vikabilling.enums.BundleState;
import com.apitable.enterprise.vikabilling.entity.BundleEntity;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static java.util.stream.Collectors.groupingBy;

/**
 * <p>
 * Bundle Service Implement Class
 * </p>
 */
@Service
@Slf4j
public class BundleServiceImpl extends ServiceImpl<BundleMapper, BundleEntity>
    implements IBundleService {

    @Resource
    private ISubscriptionService iSubscriptionService;

    @Resource
    private IBundleHistoryService iBundleHistoryService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(BundleEntity entity) {
        save(entity);
        iBundleHistoryService.saveHistory(entity, ChangeType.INSERT);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createBatch(List<BundleEntity> entities) {
        saveBatch(entities);
        iBundleHistoryService.saveBatchHistory(entities, ChangeType.INSERT);
    }

    @Override
    public BundleEntity getByBundleId(String bundleId) {
        return baseMapper.selectByBundleId(bundleId);
    }

    @Override
    public List<BundleEntity> getBySpaceId(String spaceId) {
        return baseMapper.selectBySpaceId(spaceId);
    }

    @Override
    public List<BundleEntity> getBySpaceIds(List<String> spaceIds) {
        return baseMapper.selectBySpaceIds(spaceIds);
    }

    @Override
    public Bundle getActivatedBundleBySpaceId(String spaceId) {
        // The base type subscription must not expire
        Predicate<Bundle> condition = bundle -> {
            if (bundle.getState() != BundleState.ACTIVATED) {
                return false;
            }
            LocalDate today = ClockManager.me().getLocalDateNow();
            LocalDate startDate = bundle.getBaseSubscription().getStartDate().toLocalDate();
            LocalDate endDate = bundle.getBaseSubscription().getExpireDate().toLocalDate();
            log.info("today is {}, bundle base subscription start date=[{}], endDate=[{}]", today,
                startDate, endDate);
            return today.compareTo(endDate) <= 0;
        };
        return getBundlesBySpaceId(spaceId).stream()
            .filter(condition)
            .findFirst().orElse(null);
    }

    @Override
    public Bundle getPossibleBundleBySpaceId(String spaceId) {
        List<Bundle> bundles = getBundlesBySpaceId(spaceId);
        return bundles.stream().filter(bundle -> {
                if (bundle.getState() != BundleState.ACTIVATED) {
                    return false;
                }
                LocalDate today = ClockManager.me().getLocalDateNow();
                Subscription base = bundle.getBaseSubscription();
                boolean found = today.compareTo(base.getStartDate().toLocalDate()) >= 0
                    && today.compareTo(base.getExpireDate().toLocalDate()) <= 0;
                if (!found) {
                    // The base subscription has expired, but the add-on subscription has not expired, keep looking
                    found = bundle.getAddOnSubscription().stream().anyMatch(subscription ->
                        today.compareTo(subscription.getExpireDate().toLocalDate()) <= 0);
                }
                return found;
            })
            .findFirst().orElse(null);
    }

    @Override
    public List<Bundle> getBundlesBySpaceId(String spaceId) {
        List<BundleEntity> bundleEntities = getBySpaceId(spaceId);
        return fromBundleEntities(bundleEntities);
    }

    @Override
    public List<Bundle> getActivatedBundlesBySpaceId(List<String> spaceIds) {
        return getBundlesBySpaceIds(spaceIds).stream()
            .filter(bundle -> BundleState.ACTIVATED == bundle.getState())
            .collect(Collectors.toList());
    }

    @Override
    public List<Bundle> getBundlesBySpaceIds(List<String> spaceIds) {
        List<BundleEntity> bundleEntities = getBySpaceIds(spaceIds);
        return fromBundleEntities(bundleEntities);
    }

    private List<Bundle> fromBundleEntities(List<BundleEntity> bundleEntities) {
        List<String> bundleIds = bundleEntities.stream()
            .map(BundleEntity::getBundleId).collect(Collectors.toList());
        MultiValueMap<String, Subscription> subscriptionMap =
            new LinkedMultiValueMap<>(bundleIds.size());
        if (!bundleIds.isEmpty()) {
            // Get a list of subscription entries
            List<Subscription> subscriptions =
                iSubscriptionService.getSubscriptionsByBundleIds(bundleIds);
            subscriptionMap.putAll(subscriptions.stream()
                .collect(groupingBy(Subscription::getBundleId)));
        }
        // convert bundle entity to viewable bundle
        Function<BundleEntity, Bundle> bundlerConverter = bundleEntity ->
            Bundle.builder()
                .spaceId(bundleEntity.getSpaceId())
                .bundleId(bundleEntity.getBundleId())
                .state(BundleState.valueOf(bundleEntity.getState()))
                .bundleStartDate(bundleEntity.getStartDate())
                .bundleEndDate(bundleEntity.getEndDate())
                .subscriptions(
                    subscriptionMap.getOrDefault(bundleEntity.getBundleId(), new ArrayList<>()))
                .build();
        return bundleEntities.stream()
            .map(bundlerConverter)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateByBundleId(String bundleId, BundleEntity updatedBundle) {
        BundleEntity bundleEntity = getByBundleId(bundleId);
        if (bundleEntity == null) {
            throw new RuntimeException("update bundle error");
        }
        updatedBundle.setId(bundleEntity.getId());
        updateById(updatedBundle);

        if (updatedBundle.getStartDate() != null) {
            bundleEntity.setStartDate(updatedBundle.getStartDate());
        }
        if (updatedBundle.getEndDate() != null) {
            bundleEntity.setEndDate(updatedBundle.getEndDate());
        }
        if (updatedBundle.getState() != null) {
            bundleEntity.setState(updatedBundle.getState());
        }
        iBundleHistoryService.saveHistory(bundleEntity, ChangeType.UPDATE);
    }

    @Override
    public List<BundleEntity> getBySpaceIdAndState(String spaceId, BundleState state) {
        return baseMapper.selectBySpaceIdAndByState(spaceId, state);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeBatchByBundleIds(List<String> bundleIds) {
        if (CollUtil.isEmpty(bundleIds)) {
            return;
        }
        baseMapper.updateIsDeletedByBundleIds(bundleIds, true);
        List<BundleEntity> bundleEntities = baseMapper.selectByBundleIds(bundleIds);
        if (bundleEntities.isEmpty()) {
            return;
        }
        iBundleHistoryService.saveBatchHistory(bundleEntities, ChangeType.DELETE);
    }

    @Override
    public void restoreByBundleIds(List<String> bundleIds) {
        baseMapper.updateIsDeletedByBundleIds(bundleIds, false);
        List<BundleEntity> bundleEntities = baseMapper.selectByBundleIds(bundleIds);
        if (bundleEntities.isEmpty()) {
            return;
        }
        iBundleHistoryService.saveBatchHistory(bundleEntities, ChangeType.UPDATE);
    }
}
