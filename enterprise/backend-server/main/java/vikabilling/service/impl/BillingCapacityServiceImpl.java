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
import static com.apitable.enterprise.vikabilling.util.BillingConfigManager.getBillingConfig;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.apitable.enterprise.vikabilling.enums.CapacityType;
import com.apitable.enterprise.vikabilling.enums.ProductCategory;
import com.apitable.enterprise.vikabilling.enums.SubscriptionState;
import com.apitable.enterprise.vikabilling.mapper.SubscriptionMapper;
import com.apitable.enterprise.vikabilling.service.IBillingCapacityService;
import com.apitable.enterprise.vikabilling.service.ISpaceSubscriptionService;
import com.apitable.enterprise.vikabilling.setting.Feature;
import com.apitable.enterprise.vikabilling.setting.Plan;
import com.apitable.enterprise.vikabilling.util.BillingConfigManager;
import com.apitable.shared.constants.DateFormatConstants;
import com.apitable.space.dto.SpaceSubscriptionDto;
import com.apitable.space.enums.SpaceCertification;
import com.apitable.space.service.ISpaceService;
import com.apitable.space.vo.InviteUserInfo;
import com.apitable.space.vo.SpaceCapacityPageVO;
import com.apitable.space.vo.SpaceGlobalFeature;
import com.apitable.user.mapper.UserMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BillingCapacityServiceImpl implements IBillingCapacityService {

    @Resource
    private SubscriptionMapper subscriptionMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private ISpaceService iSpaceService;

    @Resource
    private ISpaceSubscriptionService spaceSubscriptionService;

    @Override
    public IPage<SpaceCapacityPageVO> getSpaceCapacityDetail(String spaceId, Boolean isExpire,
                                                             Page page) {
        log.info("Query attachment capacity details");
        // Query expired attachment capacity
        if (isExpire) {
            // Query expired attachment capacity order information
            IPage<SpaceSubscriptionDto> expireList =
                subscriptionMapper.selectExpireCapacityBySpaceId(spaceId, page);
            return this.handleCapacitySubscription(expireList, page);
        }
        // Query unexpired attachment capacity order information
        IPage<SpaceSubscriptionDto> unExpirePage =
            subscriptionMapper.selectUnExpireCapacityBySpaceId(spaceId, page,
                SubscriptionState.ACTIVATED);
        IPage<SpaceCapacityPageVO> spaceCapacityPageVOIPage =
            this.handleCapacitySubscription(unExpirePage, page);
        // Handle the record of the attachment capacity given by the official,
        // and receive 5GB and 10GB attachment capacity respectively for basic space and advanced certification.
        if (this.checkOfficialGiftCapacity(spaceId) != null) {
            spaceCapacityPageVOIPage.getRecords().add(this.checkOfficialGiftCapacity(spaceId));
            spaceCapacityPageVOIPage.setTotal(spaceCapacityPageVOIPage.getTotal() + 1);
        }
        // Handle the attachment capacity record of the free subscription plan space station,
        // the default 1GB attachment capacity of the bronze-level space station
        Integer number = subscriptionMapper.selectUnExpireBaseProductBySpaceId(spaceId,
            SubscriptionState.ACTIVATED, ProductCategory.BASE);
        if (number == 0) {
            SpaceCapacityPageVO freeCapacity = new SpaceCapacityPageVO();
            freeCapacity.setQuota("1GB");
            freeCapacity.setQuotaSource(CapacityType.SUBSCRIPTION_PACKAGE_CAPACITY.getName());
            freeCapacity.setExpireDate("-1");
            spaceCapacityPageVOIPage.getRecords().add(freeCapacity);
            spaceCapacityPageVOIPage.setTotal(spaceCapacityPageVOIPage.getTotal() + 1);
        }
        return spaceCapacityPageVOIPage;
    }

    @Override
    public IPage<SpaceCapacityPageVO> handleCapacitySubscription(
        IPage<SpaceSubscriptionDto> spaceSubscriptionDtoIPage, Page page) {
        log.info("Process attachment capacity order information");
        Plan giftPlan = BillingConfigManager.getGiftPlan();
        // Build a collection of attachment capacity detail objects
        List<SpaceCapacityPageVO> spaceCapacityPageVos = new ArrayList<>();
        for (SpaceSubscriptionDto spaceSubscriptionDto : spaceSubscriptionDtoIPage.getRecords()) {
            if (giftPlan.getId().equals(spaceSubscriptionDto.getPlanId()) &&
                StrUtil.isEmpty(spaceSubscriptionDto.getMetadata())) {
                continue;
            }
            // Process planId, remove _monthly, _biannual, _annual, _v1
            List<String> removeStrings =
                CollUtil.newArrayList("_monthly", "_biannual", "_annual", "_v1");
            String planId = spaceSubscriptionDto.getPlanId();
            for (String removeString : removeStrings) {
                if (planId.contains(removeString)) {
                    planId = StrUtil.removeAll(planId, removeString);
                }
            }
            // Get plan package features
            String finalPlanId = planId;
            Plan plan = BillingConfigManager.getBillingConfig().getPlans().values().stream()
                .filter(e -> e.getId().contains(finalPlanId)).findFirst().get();
            Feature feature = BillingConfigManager.getBillingConfig().getFeatures().get(
                plan.getFeatures().stream().filter(e -> e.contains("capacity")).findFirst().get());
            // Build Attachment Capacity Detail Record
            SpaceCapacityPageVO spaceCapacityPageVO = new SpaceCapacityPageVO();
            // Attachment capacity quota
            if (feature.getSpecification() == -1) {
                spaceCapacityPageVO.setQuota("-1");
            } else if (Objects.equals(feature.getSpecification(),
                getBillingConfig().getFeatures().get("storage_capacity_300_mb")
                    .getSpecification())) {
                spaceCapacityPageVO.setQuota(StrUtil.format("{}MB", feature.getSpecification()));
            } else {
                spaceCapacityPageVO.setQuota(StrUtil.format("{}GB", feature.getSpecification()));
            }
            // Accessory capacity quota source
            if (StrUtil.isNotEmpty(spaceSubscriptionDto.getMetadata())) {
                // Parse metadata information, including new user ID, new user name, attachment capacity type
                JSONObject metadata = JSONUtil.parseObj(spaceSubscriptionDto.getMetadata());
                String capacityType = metadata.getStr("capacityType");
                // Determine if attachment capacity is a reward for inviting new users to the space station
                if (CapacityType.PARTICIPATION_CAPACITY.getName().equals(capacityType)) {
                    // Obtain invited user information through user ID, including user ID, user avatar
                    Long userId = Long.valueOf(metadata.getStr("userId"));
                    InviteUserInfo inviteUserInfo = userMapper.selectInviteUserInfoByUserId(userId);
                    spaceCapacityPageVO.setQuotaSource(
                        CapacityType.PARTICIPATION_CAPACITY.getName());
                    // Participate in the activity of inviting users to give away attachment capacity,
                    // and additionally return invited member information
                    spaceCapacityPageVO.setInviteUserInfo(inviteUserInfo);
                }
            }
            // Subscription package attachment capacity quota source
            if (ProductCategory.BASE.name().equals(spaceSubscriptionDto.getProductCategory())) {
                spaceCapacityPageVO.setQuotaSource(
                    CapacityType.SUBSCRIPTION_PACKAGE_CAPACITY.getName());
            }
            // Business order attachment capacity quota source
            if (ProductCategory.ADD_ON.name().equals(spaceSubscriptionDto.getProductCategory()) &&
                StrUtil.isEmpty(spaceSubscriptionDto.getMetadata())) {
                spaceCapacityPageVO.setQuotaSource(CapacityType.PURCHASE_CAPACITY.getName());
            }
            // Attachment capacity expiration time
            spaceCapacityPageVO.setExpireDate(spaceSubscriptionDto.getExpireTime()
                .format(DateTimeFormatter.ofPattern(DateFormatConstants.TIME_SIMPLE_PATTERN)));
            spaceCapacityPageVos.add(spaceCapacityPageVO);
        }
        // Build pagination return objects
        IPage<SpaceCapacityPageVO> spaceCapacityPageVO = new Page<>();
        spaceCapacityPageVO.setRecords(spaceCapacityPageVos);
        spaceCapacityPageVO.setCurrent(page.getCurrent());
        spaceCapacityPageVO.setSize(page.getSize());
        spaceCapacityPageVO.setTotal(page.getTotal());
        return spaceCapacityPageVO;
    }

    @Override
    public SpaceCapacityPageVO checkOfficialGiftCapacity(String spaceId) {
        log.info(
            "Check if the space station is certified to receive the official accessory capacity reward");
        SpaceGlobalFeature spaceGlobalFeature = iSpaceService.getSpaceGlobalFeature(spaceId);
        if (spaceGlobalFeature.getCertification() != null) {
            // Build official gift attachment capacity information
            SpaceCapacityPageVO officialGiftCapacity = new SpaceCapacityPageVO();
            // Basic certification 5GB capacity
            if (SpaceCertification.BASIC.getLevel().equals(spaceGlobalFeature.getCertification())) {
                officialGiftCapacity.setQuota(StrUtil.format("{}GB", GIFT_BASIC_CAPACITY));
                officialGiftCapacity.setQuotaSource(CapacityType.OFFICIAL_GIFT_CAPACITY.getName());
                officialGiftCapacity.setExpireDate("-1");
            }
            // Premium certified 10GB capacity
            if (SpaceCertification.SENIOR.getLevel()
                .equals(spaceGlobalFeature.getCertification())) {
                officialGiftCapacity.setQuota(StrUtil.format("{}GB", GIFT_ADVANCE_CAPACITY));
                officialGiftCapacity.setQuotaSource(CapacityType.OFFICIAL_GIFT_CAPACITY.getName());
                officialGiftCapacity.setExpireDate("-1");
            }
            return officialGiftCapacity;
        }
        return null;
    }
}
