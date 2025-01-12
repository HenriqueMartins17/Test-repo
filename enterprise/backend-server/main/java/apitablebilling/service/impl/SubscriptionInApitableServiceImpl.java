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

import com.apitable.core.util.SqlTool;
import com.apitable.enterprise.apitablebilling.core.Subscription;
import com.apitable.enterprise.apitablebilling.entity.SubscriptionEntity;
import com.apitable.enterprise.apitablebilling.enums.BillingPeriod;
import com.apitable.enterprise.apitablebilling.enums.ChangeType;
import com.apitable.enterprise.apitablebilling.enums.ProductCategory;
import com.apitable.enterprise.apitablebilling.enums.SubscriptionState;
import com.apitable.enterprise.apitablebilling.mapper.SubscriptionInApitableMapper;
import com.apitable.enterprise.apitablebilling.service.IBundleInApitableService;
import com.apitable.enterprise.apitablebilling.service.ISubscriptionHistoryInApitableService;
import com.apitable.enterprise.apitablebilling.service.ISubscriptionInApitableService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Subscription Service Implement Class.
 * </p>
 */
@Service
@Slf4j
public class SubscriptionInApitableServiceImpl
    extends ServiceImpl<SubscriptionInApitableMapper, SubscriptionEntity>
    implements ISubscriptionInApitableService {

    @Resource
    private ISubscriptionHistoryInApitableService iSubscriptionHistoryInApitableService;

    @Resource
    private IBundleInApitableService iBundleInApitableService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(SubscriptionEntity entity) {
        save(entity);
        iSubscriptionHistoryInApitableService.saveHistory(entity, ChangeType.INSERT);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createBatch(List<SubscriptionEntity> entities) {
        saveBatch(entities);
        iSubscriptionHistoryInApitableService.saveBatchHistory(entities, ChangeType.INSERT);
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
                .stripeId(subscriptionEntity.getStripeId())
                .stripeSubId(subscriptionEntity.getStripeSubId())
                .productName(subscriptionEntity.getProductName())
                .productCategory(ProductCategory.valueOf(subscriptionEntity.getProductCategory()))
                .priceId(subscriptionEntity.getPriceId())
                .period(
                    BillingPeriod.valueOf(subscriptionEntity.getPeriod().toUpperCase(Locale.ROOT)))
                .quantity(subscriptionEntity.getQuantity())
                .state(SubscriptionState.valueOf(subscriptionEntity.getState()))
                .startDate(subscriptionEntity.getStartDate())
                .expireDate(subscriptionEntity.getExpireDate())
                .metadata(subscriptionEntity.getMetadata())
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
        if (updatedSubscription.getPriceId() != null) {
            subscriptionEntity.setPriceId(updatedSubscription.getPriceId());
        }
        if (updatedSubscription.getPeriod() != null) {
            subscriptionEntity.setPeriod(updatedSubscription.getPeriod());
        }
        if (updatedSubscription.getQuantity() != null) {
            subscriptionEntity.setQuantity(updatedSubscription.getQuantity());
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
        iSubscriptionHistoryInApitableService.saveHistory(subscriptionEntity, ChangeType.UPDATE);
    }

    @Override
    public List<SubscriptionEntity> getByBundleIdAndState(String bundleId,
                                                          SubscriptionState state) {
        return baseMapper.selectByBundleIdAndState(bundleId, state);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeBatchBySubscriptionIds(List<String> subscriptionIds) {
        List<SubscriptionEntity> subscriptionEntities =
            baseMapper.selectByBundleIds(subscriptionIds);
        baseMapper.updateIsDeletedBySubscriptionIds(subscriptionIds, true);
        if (!subscriptionEntities.isEmpty()) {
            iSubscriptionHistoryInApitableService.saveBatchHistory(subscriptionEntities,
                ChangeType.DELETE);
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
            iSubscriptionHistoryInApitableService.saveBatchHistory(subscriptionEntities,
                ChangeType.UPDATE);
        }
        if (!bundleIds.isEmpty()) {
            iBundleInApitableService.restoreByBundleIds(bundleIds);
        }
    }

    @Override
    public List<String> getBundleIdsBySubscriptionIds(List<String> subscriptionIds) {
        return baseMapper.selectBundleIdsBySubscriptionIds(subscriptionIds);
    }

    @Override
    public boolean bundlesHaveSubscriptions(List<String> bundleIds) {
        return SqlTool.retCount(baseMapper.selectCountByBundleIds(bundleIds)) > 0;
    }

    @Override
    public SubscriptionEntity getByStripeId(String stripeId) {
        List<SubscriptionEntity> entities =
            list(new QueryWrapper<SubscriptionEntity>().eq("stripe_id", stripeId));
        return entities.isEmpty() ? null : entities.get(0);
    }

    @Override
    public List<SubscriptionEntity> getValidSubscriptions() {
        return list(
            new QueryWrapper<SubscriptionEntity>().ne("state", SubscriptionState.CANCELED.name()));
    }
}
