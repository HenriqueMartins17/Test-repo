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

import static java.util.stream.Collectors.groupingBy;

import cn.hutool.core.collection.CollUtil;
import com.apitable.enterprise.apitablebilling.core.Bundle;
import com.apitable.enterprise.apitablebilling.core.Subscription;
import com.apitable.enterprise.apitablebilling.entity.BundleEntity;
import com.apitable.enterprise.apitablebilling.enums.BundleState;
import com.apitable.enterprise.apitablebilling.enums.ChangeType;
import com.apitable.enterprise.apitablebilling.mapper.BundleInApitableMapper;
import com.apitable.enterprise.apitablebilling.service.IBundleHistoryInApitableService;
import com.apitable.enterprise.apitablebilling.service.IBundleInApitableService;
import com.apitable.enterprise.apitablebilling.service.ISubscriptionInApitableService;
import com.apitable.shared.clock.spring.ClockManager;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * <p>
 * Bundle Service Implement Class.
 * </p>
 */
@Service
@Slf4j
public class BundleInApitableServiceImpl extends ServiceImpl<BundleInApitableMapper, BundleEntity>
    implements IBundleInApitableService {

    @Resource
    private ISubscriptionInApitableService iSubscriptionInApitableService;

    @Resource
    private IBundleHistoryInApitableService iBundleHistoryInApitableService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(BundleEntity entity) {
        save(entity);
        iBundleHistoryInApitableService.saveHistory(entity, ChangeType.INSERT);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createBatch(List<BundleEntity> entities) {
        saveBatch(entities);
        iBundleHistoryInApitableService.saveBatchHistory(entities, ChangeType.INSERT);
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
    public Bundle getValidBundleBySpaceId(String spaceId) {
        List<Bundle> bundles = getBundlesBySpaceId(spaceId);
        Predicate<Bundle> condition = bundle -> {
            if (bundle.getState() == BundleState.CANCELED) {
                return false;
            }
            LocalDateTime now = ClockManager.me().getLocalDateTimeNow();
            Subscription base = bundle.getBaseSubscription();
            return !now.isBefore(base.getStartDate())
                && !now.isAfter(base.getExpireDate());
        };
        return bundles.stream().filter(condition)
            .findFirst().orElse(null);
    }

    @Override
    public List<Bundle> getBundlesBySpaceId(String spaceId) {
        List<BundleEntity> bundleEntities = getBySpaceId(spaceId);
        return fromBundleEntities(bundleEntities);
    }

    @Override
    public List<Bundle> getValidBundlesBySpaceId(List<String> spaceIds) {
        return getBundlesBySpaceIds(spaceIds).stream()
            .filter(bundle -> BundleState.CANCELED != bundle.getState())
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
                iSubscriptionInApitableService.getSubscriptionsByBundleIds(bundleIds);
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
        iBundleHistoryInApitableService.saveHistory(bundleEntity, ChangeType.UPDATE);
    }

    @Override
    public void restoreByBundleIds(List<String> bundleIds) {
        baseMapper.updateIsDeletedByBundleIds(bundleIds, false);
        List<BundleEntity> bundleEntities = baseMapper.selectByBundleIds(bundleIds);
        if (bundleEntities.isEmpty()) {
            return;
        }
        iBundleHistoryInApitableService.saveBatchHistory(bundleEntities, ChangeType.UPDATE);
    }

    @Override
    public void removeBatchByBundleIds(List<String> bundleIds) {
        if (CollUtil.isEmpty(bundleIds)) {
            return;
        }
        List<BundleEntity> bundleEntities = baseMapper.selectByBundleIds(bundleIds);
        if (bundleEntities.isEmpty()) {
            return;
        }
        baseMapper.updateIsDeletedByBundleIds(bundleIds, true);
        iBundleHistoryInApitableService.saveBatchHistory(bundleEntities, ChangeType.DELETE);
    }
}
