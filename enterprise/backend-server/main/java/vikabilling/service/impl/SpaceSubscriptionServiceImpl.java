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

import static com.apitable.enterprise.vikabilling.constants.BillingConstants.GIFT_ADVANCE_CAPACITY;
import static com.apitable.enterprise.vikabilling.constants.BillingConstants.GIFT_BASIC_CAPACITY;
import static com.apitable.enterprise.vikabilling.util.BillingConfigManager.buildPlanFeature;
import static com.apitable.enterprise.vikabilling.util.BillingConfigManager.getBillingConfig;
import static com.apitable.enterprise.vikabilling.util.BillingConfigManager.getFreePlan;
import static com.apitable.enterprise.vikabilling.util.BillingUtil.channelDefaultSubscription;
import static com.apitable.enterprise.vikabilling.util.BillingUtil.legacyPlanId;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.enterprise.gm.ro.CreateEntitlementWithAddOn;
import com.apitable.enterprise.social.enums.SocialAppType;
import com.apitable.enterprise.social.enums.SocialPlatformType;
import com.apitable.enterprise.social.service.ISocialTenantBindService;
import com.apitable.enterprise.social.service.ISocialTenantService;
import com.apitable.enterprise.vikabilling.core.Bundle;
import com.apitable.enterprise.vikabilling.core.Subscription;
import com.apitable.enterprise.vikabilling.entity.BundleEntity;
import com.apitable.enterprise.vikabilling.entity.SubscriptionEntity;
import com.apitable.enterprise.vikabilling.enums.BundleState;
import com.apitable.enterprise.vikabilling.enums.CapacityType;
import com.apitable.enterprise.vikabilling.enums.ProductChannel;
import com.apitable.enterprise.vikabilling.enums.SubscriptionPhase;
import com.apitable.enterprise.vikabilling.enums.SubscriptionState;
import com.apitable.enterprise.vikabilling.interfaces.model.BillingSubscriptionInfo;
import com.apitable.enterprise.vikabilling.mapper.BundleMapper;
import com.apitable.enterprise.vikabilling.mapper.SubscriptionMapper;
import com.apitable.enterprise.vikabilling.service.IBillingOfflineService;
import com.apitable.enterprise.vikabilling.service.IBundleService;
import com.apitable.enterprise.vikabilling.service.ISpaceSubscriptionService;
import com.apitable.enterprise.vikabilling.service.ISubscriptionService;
import com.apitable.enterprise.vikabilling.setting.BillingWhiteListConfig;
import com.apitable.enterprise.vikabilling.setting.BillingWhiteListConfigManager;
import com.apitable.enterprise.vikabilling.setting.FeatureSetting;
import com.apitable.enterprise.vikabilling.setting.Plan;
import com.apitable.enterprise.vikabilling.setting.Product;
import com.apitable.enterprise.vikabilling.util.BillingConfigManager;
import com.apitable.interfaces.billing.model.SubscriptionFeature;
import com.apitable.interfaces.billing.model.SubscriptionInfo;
import com.apitable.shared.clock.spring.ClockManager;
import com.apitable.space.enums.SpaceCertification;
import com.apitable.space.enums.SpaceException;
import com.apitable.space.mapper.SpaceMapper;
import com.apitable.space.vo.SpaceGlobalFeature;
import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.unit.DataSize;

/**
 * <p>
 * Space Subscription Service Implement Class.
 * </p>
 */
@Service
@Slf4j
public class SpaceSubscriptionServiceImpl implements ISpaceSubscriptionService {

    @Resource
    private ISocialTenantService iSocialTenantService;

    @Resource
    private ISocialTenantBindService iSocialTenantBindService;

    @Resource
    private IBundleService iBundleService;

    @Resource
    private SubscriptionMapper subscriptionMapper;

    @Value("${BILLING_CHANNEL:vika}")
    private ProductChannel defaultChannel;

    @Resource
    private IBillingOfflineService iBillingOfflineService;

    @Resource
    private ISubscriptionService iSubscriptionService;

    @Resource
    private BundleMapper bundleMapper;

    @Resource
    private SpaceMapper spaceMapper;

    @Override
    public Map<String, SubscriptionFeature> getSubscriptionFeatureBySpaceIds(
        List<String> spaceIds) {
        List<Bundle> bundles = iBundleService.getActivatedBundlesBySpaceId(spaceIds);
        Map<String, List<Bundle>> bundleMap = bundles.stream()
            .collect(Collectors.groupingBy(Bundle::getSpaceId));
        Map<String, SubscriptionFeature> planFeatureMap = new HashMap<>(spaceIds.size());
        for (String spaceId : spaceIds) {
            if (bundleMap.containsKey(spaceId)) {
                Subscription baseSubscription =
                    bundleMap.get(spaceId).iterator().next().getBaseSubscription();
                Plan basePlan =
                    getBillingConfig().getPlans().get(legacyPlanId(baseSubscription.getPlanId()));
                SubscriptionFeature feature = buildPlanFeature(basePlan, Collections.emptyList());
                planFeatureMap.put(spaceId, feature);
            } else {
                SubscriptionInfo freePlanInfo = channelDefaultSubscription(ProductChannel.VIKA);
                Plan basePlan = getBillingConfig().getPlans().get(freePlanInfo.getBasePlan());
                List<Plan> addOnPlans = freePlanInfo.getAddOnPlans().stream()
                    .map(plan -> getBillingConfig().getPlans().get(plan))
                    .collect(Collectors.toList());
                SubscriptionFeature freePlanFeature = buildPlanFeature(basePlan, addOnPlans);
                planFeatureMap.put(spaceId, freePlanFeature);
            }
        }
        return planFeatureMap;
    }

    @Override
    public SubscriptionInfo getPlanInfoBySpaceId(String spaceId) {
        log.info("Get a subscription plan for space「{}」", spaceId);
        Bundle bundle = iBundleService.getPossibleBundleBySpaceId(spaceId);
        if (bundle == null) {
            // Return to default free subscription plan
            BillingSubscriptionInfo defaultSubscriptionInfo = defaultSubscriptionInfo(spaceId);
            wrapperFeatureSpecification(spaceId, defaultSubscriptionInfo);
            return defaultSubscriptionInfo;
        }
        // Basic subscription
        Subscription baseSubscription = bundle.getBaseSubscription();
        LocalDate baseExpireDate = baseSubscription.getExpireDate().toLocalDate();
        LocalDate now = ClockManager.me().getLocalDateNow();
        boolean isBaseEntitlementExpire =
            bundle.isBaseForFree() || now.isAfter(baseExpireDate);
        // The value-added plan does not currently support third-party integration spaces
        Plan basePlan = isBaseEntitlementExpire ? getFreePlan(defaultChannel) :
            getBillingConfig().getPlans().get(legacyPlanId(baseSubscription.getPlanId()));
        Product baseProduct = getBillingConfig().getProducts().get(basePlan.getProduct());
        LocalDate endDate = isBaseEntitlementExpire ? null : baseExpireDate;
        // Add-on subscription
        List<Subscription> addOnSubscription = bundle.getAddOnSubscription();
        List<Plan> addOnPlans = addOnSubscription.stream()
            .filter(subscription -> {
                LocalDate today = ClockManager.me().getLocalDateNow();
                LocalDate startDate = subscription.getStartDate().toLocalDate();
                LocalDate expireDate = subscription.getExpireDate().toLocalDate();
                return !today.isBefore(startDate) && !today.isAfter(expireDate);
            })
            .map(subscription -> getBillingConfig().getPlans().get(subscription.getPlanId()))
            .toList();
        BillingSubscriptionInfo subscriptionInfo =
            new BillingSubscriptionInfo(basePlan.getProduct(), baseProduct.isFree(),
                SubscriptionPhase.TRIAL.equals(baseSubscription.getPhase()),
                basePlan.getId(), addOnPlans.stream().map(Plan::getId).collect(Collectors.toList()),
                bundle.getBundleStartDate().toLocalDate(), endDate);
        // wrapper subscription plan feature specially
        wrapperFeatureSpecification(spaceId, subscriptionInfo);
        return subscriptionInfo;
    }

    private BillingSubscriptionInfo defaultSubscriptionInfo(String spaceId) {
        // If a third party is bound, isv returns the corresponding base version
        // No equity, return to default configuration
        return Optional.ofNullable(iSocialTenantBindService.getTenantBindInfoBySpaceId(spaceId))
            .map(bind -> iSocialTenantService.getByAppIdAndTenantId(bind.getAppId(),
                bind.getTenantId()))
            .map(tenant -> {
                if (Boolean.TRUE.equals(tenant.getStatus())
                    && SocialAppType.ISV.getType() == tenant.getAppType()) {
                    if (SocialPlatformType.WECOM.getValue().equals(tenant.getPlatform())) {
                        return channelDefaultSubscription(ProductChannel.WECOM);
                    }
                    if (SocialPlatformType.DINGTALK.getValue().equals(tenant.getPlatform())) {
                        return channelDefaultSubscription(ProductChannel.DINGTALK);
                    }
                    if (SocialPlatformType.FEISHU.getValue().equals(tenant.getPlatform())) {
                        return channelDefaultSubscription(ProductChannel.LARK);
                    }
                }
                return channelDefaultSubscription(defaultChannel);
            })
            .orElse(channelDefaultSubscription(defaultChannel));
    }

    private void wrapperFeatureSpecification(String spaceId,
                                             BillingSubscriptionInfo subscriptionInfo) {
        log.info("gets space global properties，spaceId:{}", spaceId);
        String props = spaceMapper.selectPropsBySpaceId(spaceId);
        ExceptionUtil.isNotNull(props, SpaceException.SPACE_NOT_EXIST);
        // 1. plus capacity if space passing certification
        SpaceGlobalFeature spaceGlobalFeature = JSONUtil.toBean(props, SpaceGlobalFeature.class);
        if (SpaceCertification.BASIC.getLevel().equals(spaceGlobalFeature.getCertification())) {
            subscriptionInfo.getFeature().getCapacitySize()
                .plus(DataSize.ofGigabytes(GIFT_BASIC_CAPACITY));
        }
        if (SpaceCertification.SENIOR.getLevel().equals(spaceGlobalFeature.getCertification())) {
            subscriptionInfo.getFeature().getCapacitySize()
                .plus(DataSize.ofGigabytes(GIFT_ADVANCE_CAPACITY));
        }
        // 2. plus capacity if exist white list, this is history reason
        BillingWhiteListConfig config = BillingWhiteListConfigManager.getConfig();
        if (config.containsKey(spaceId)) {
            FeatureSetting setting = config.get(spaceId);
            LocalDate whiteEndDate = setting.getEndDate();
            LocalDate today = ClockManager.me().getLocalDateNow();
            if (!today.isAfter(whiteEndDate)) {
                // Additional value-added plan
                if (setting.getCapacity() != null) {
                    subscriptionInfo.getFeature().getCapacitySize()
                        .plus(DataSize.ofGigabytes(setting.getCapacity()));
                }
                if (setting.getNodes() != null) {
                    subscriptionInfo.getFeature().getFileNodeNums().plus(setting.getNodes());
                }
            }
        }
        // 3. plus gift capacity if exist
        long giftCapacitySize = this.getSpaceUnExpireGiftCapacity(spaceId);
        subscriptionInfo.getGiftCapacity().plus(DataSize.ofBytes(giftCapacitySize));
    }

    @Override
    public Long getSpaceUnExpireGiftCapacity(String spaceId) {
        log.info("Get the capacity of unexpired attachments given by space");
        // Free attachment capacity plan
        Plan giftPlan = BillingConfigManager.getGiftPlan();
        // Get the number of add-on subscription plans that give away add-on capacity
        Integer planCount =
            subscriptionMapper.selectUnExpireGiftCapacityBySpaceId(spaceId, giftPlan.getId(),
                SubscriptionState.ACTIVATED.name());
        // Returns the size of the gifted unexpired attachments
        return planCount * 1024L * 1024 * 300;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createAddOnWithGiftCapacity(Long userId, String userName, String spaceId) {
        log.info(
            "Order a 300MB add-on subscription plan to invite users to increase the capacity of "
                + "the add-on.");
        Plan giftPlan = BillingConfigManager.getGiftPlan();
        // Get space subscription info
        SubscriptionInfo planInfo = getPlanInfoBySpaceId(spaceId);
        // If the capacity of the space is unlimited, it will not be rewarded capacity
        if (planInfo.getFeature().getCapacitySize().isUnlimited()) {
            return;
        }
        CreateEntitlementWithAddOn createEntitlementWithAddOn = new CreateEntitlementWithAddOn();
        createEntitlementWithAddOn.setSpaceId(spaceId);
        createEntitlementWithAddOn.setPlanId(giftPlan.getId());
        createEntitlementWithAddOn.setMonths(12);
        // Build the remark information in the request body, including userId, userName, capacityType
        Map<String, Object> remarkMap = new HashMap<>();
        remarkMap.put("userId", userId);
        remarkMap.put("userName", userName);
        remarkMap.put("capacityType", CapacityType.PARTICIPATION_CAPACITY.getName());
        String remark = new JSONObject(remarkMap).toString();
        createEntitlementWithAddOn.setRemark(remark);
        // Add-on subscription plan to place an order
        iBillingOfflineService.createSubscriptionWithAddOn(createEntitlementWithAddOn, userId);
        // get space activated state bundle
        List<BundleEntity> activeBundles =
            bundleMapper.selectBySpaceIdAndState(spaceId, BundleState.ACTIVATED);
        // space current activated bundle
        BundleEntity bundle = activeBundles.get(0);
        if (activeBundles.size() != 1) {
            activeBundles.remove(0);
            // expire previous bundles
            activeBundles.forEach(bundleEntity -> {
                BundleEntity updateBundle = new BundleEntity()
                    .setCreatedBy(bundleEntity.getCreatedBy())
                    .setUpdatedBy(bundleEntity.getUpdatedBy());
                updateBundle.setState(BundleState.EXPIRED.name());
                iBundleService.updateByBundleId(bundleEntity.getBundleId(), updateBundle);
            });
        }
        // get unExpired add-on subscription except current bundle
        List<SubscriptionEntity> unExpiredSubscriptions =
            subscriptionMapper.selectUnExpiredSubscriptionByBundleIdAndSpaceId(
                bundle.getBundleId(), spaceId);
        Integer giftCount = subscriptionMapper.selectGiftCountByBundleIdAndPlanId(
            bundle.getBundleId(), giftPlan.getId());
        // transfer unExpired add-on subscription
        if (unExpiredSubscriptions != null && giftCount != unExpiredSubscriptions.size()) {
            handleUnExpiredAddOnSubscriptions(spaceId, bundle, unExpiredSubscriptions);
        }
    }

    private void handleUnExpiredAddOnSubscriptions(String spaceId, BundleEntity bundleEntity,
                                                   List<SubscriptionEntity> subscriptionEntities) {
        List<SubscriptionEntity> transferSubscriptions = new ArrayList<>();
        subscriptionEntities.forEach(addOnSub -> {
            // Transfer a non-expired add-on plan subscription to a new subscription
            SubscriptionEntity addOn = new SubscriptionEntity();
            addOn.setSpaceId(spaceId);
            addOn.setBundleId(bundleEntity.getBundleId());
            addOn.setSubscriptionId(addOnSub.getSubscriptionId());
            addOn.setProductName(addOnSub.getProductName());
            addOn.setProductCategory(addOnSub.getProductCategory());
            addOn.setPlanId(addOnSub.getPlanId());
            addOn.setState(SubscriptionState.ACTIVATED.name());
            addOn.setBundleStartDate(addOnSub.getStartDate());
            addOn.setStartDate(addOnSub.getStartDate());
            addOn.setExpireDate(addOnSub.getExpireDate());
            addOn.setCreatedBy(addOnSub.getCreatedBy());
            addOn.setUpdatedBy(addOnSub.getUpdatedBy());
            transferSubscriptions.add(addOn);
        });
        iSubscriptionService.createBatch(transferSubscriptions);
    }
}
