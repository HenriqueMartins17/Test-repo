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

package com.apitable.enterprise.social.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.apitable.enterprise.AbstractVikaSaasIntegrationTest;
import com.apitable.enterprise.social.entity.SocialTenantBindEntity;
import com.apitable.enterprise.social.enums.SocialPlatformType;
import com.apitable.enterprise.vikabilling.setting.Price;
import com.apitable.enterprise.vikabilling.strategy.SocialOrderStrategyFactory;
import com.apitable.enterprise.vikabilling.util.LarkPlanConfigManager;
import com.apitable.interfaces.billing.model.SubscriptionInfo;
import com.apitable.space.entity.SpaceEntity;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.vikadata.social.feishu.enums.PricePlanType;
import com.vikadata.social.feishu.event.app.OrderPaidEvent;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

/**
 * Lark order service test
 */
public class LarkOrderServiceImplTest extends AbstractVikaSaasIntegrationTest {

    @Test
    public void testPriceTenAndOneYear() {
        OrderPaidEvent event = getOrderPaidEvent("enterprise/social/feishu/order/base_10_1_trail.json");
        assertThat(event).as("data could not be parsed:base_10_1_trail").isNotNull();

        Price price = LarkPlanConfigManager.getPriceByLarkPlanId(event.getPricePlanId());
        assertThat(price).as("Feishu Standard Edition (10 people) configuration error").isNotNull();
    }

    @Test
    public void testPriceTwentyAndOneYear() {
        OrderPaidEvent event = getOrderPaidEvent("enterprise/social/feishu/order/base_20_1_upgrade.json");
        assertThat(event).as("Data could not be parsed:base_20_1_upgrade").isNotNull();

        Price price = LarkPlanConfigManager.getPriceByLarkPlanId(event.getPricePlanId());
        assertThat(price).as("Feishu Standard Edition (20 people) is misconfigured").isNotNull();
    }

    @Test
    public void testPriceThirtyAndOneYear() {
        OrderPaidEvent event = getOrderPaidEvent("enterprise/social/feishu/order/base_30_1_renew_trail.json");
        assertThat(event).as("Data could not be parsed:base_30_1_renew_trail").isNotNull();

        Price price = LarkPlanConfigManager.getPriceByLarkPlanId(event.getPricePlanId());
        assertThat(price).as("Feishu Standard Edition (30 people) is misconfigured").isNotNull();
    }

    @Test
    public void testEnterprisePriceThirtyAndOneYear() {
        OrderPaidEvent event = getOrderPaidEvent("enterprise/social/feishu/order/enterprise_30_1_upgrade_after_renew.json");
        assertThat(event).as("data could not be parsed:enterprise_30_1_upgrade_after_renew").isNotNull();

        Price price = LarkPlanConfigManager.getPriceByLarkPlanId(event.getPricePlanId());
        assertThat(price).as("Feishu Enterprise Edition (30 people) configuration error").isNotNull();
    }

    @Test
    public void testTrailOrder() {
        OrderPaidEvent event = getOrderPaidEvent("enterprise/social/feishu/order/base_10_1_trail.json");
        Objects.requireNonNull(event).setPricePlanType(PricePlanType.TRIAL.getType());
        String spaceId = "spc" + IdWorker.get32UUID();
        prepareSocialBindInfo(spaceId, Objects.requireNonNull(event).getTenantKey(), event.getAppId());
        SocialOrderStrategyFactory.getService(SocialPlatformType.FEISHU).retrieveOrderPaidEvent(event);
        // init now time
        final OffsetDateTime nowTime = OffsetDateTime.of(2022, 6, 7, 19, 10, 30, 0, getTestTimeZone());
        getClock().setTime(nowTime);
        SubscriptionInfo subscriptionInfo = iSpaceSubscriptionService.getPlanInfoBySpaceId(spaceId);
        assertThat(subscriptionInfo.onTrial()).isTrue();
        LocalDateTime startDate =
                LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(event.getPayTime())), getTestTimeZone());
        LocalDateTime endDate = startDate.plusDays(15);
        assertThat(endDate.toLocalDate()).isEqualTo(subscriptionInfo.getEndDate());
    }

    @Test
    public void testPriceTenAndOneYearOrder() {
        String spaceId = "spc" + IdWorker.get32UUID();
        OrderPaidEvent trailEvent = getOrderPaidEvent("enterprise/social/feishu/order/base_10_1_trail.json");
        prepareSocialBindInfo(spaceId, Objects.requireNonNull(trailEvent).getTenantKey(), trailEvent.getAppId());
        SocialOrderStrategyFactory.getService(SocialPlatformType.FEISHU).retrieveOrderPaidEvent(trailEvent);

        OrderPaidEvent event = getOrderPaidEvent("enterprise/social/feishu/order/base_10_1_per_year.json");
        SocialOrderStrategyFactory.getService(SocialPlatformType.FEISHU).retrieveOrderPaidEvent(event);
        // set date for validation
        final OffsetDateTime nowDate = OffsetDateTime.of(2023, 5, 20, 0, 0, 0, 0, getTestTimeZone());
        getClock().setTime(nowDate);
        SubscriptionInfo subscriptionInfo = iSpaceSubscriptionService.getPlanInfoBySpaceId(spaceId);
        Price price = LarkPlanConfigManager.getPriceByLarkPlanId(Objects.requireNonNull(event).getPricePlanId());
        assertThat(subscriptionInfo.onTrial()).isFalse();
        assertThat(subscriptionInfo.getBasePlan()).isEqualTo(Objects.requireNonNull(price).getPlanId());
    }

    @Test
    public void testPriceTwentyAndOneYearOrderUpgrade() {
        String spaceId = "spc" + IdWorker.get32UUID();
        OrderPaidEvent trailEvent = getOrderPaidEvent("enterprise/social/feishu/order/base_10_1_trail.json");
        prepareSocialBindInfo(spaceId, Objects.requireNonNull(trailEvent).getTenantKey(), trailEvent.getAppId());
        SocialOrderStrategyFactory.getService(SocialPlatformType.FEISHU).retrieveOrderPaidEvent(trailEvent);

        SocialOrderStrategyFactory.getService(SocialPlatformType.FEISHU)
                .retrieveOrderPaidEvent(getOrderPaidEvent("enterprise/social/feishu/order/base_10_1_per_year.json"));
        // set date for validation
        final OffsetDateTime nowDate = OffsetDateTime.of(2022, 5, 29, 0, 0, 0, 0, ZoneOffset.ofHours(8));
        getClock().setTime(nowDate);
        OrderPaidEvent event = getOrderPaidEvent("enterprise/social/feishu/order/base_20_1_upgrade.json");
        SocialOrderStrategyFactory.getService(SocialPlatformType.FEISHU).retrieveOrderPaidEvent(event);
        SubscriptionInfo subscription = iSpaceSubscriptionService.getPlanInfoBySpaceId(spaceId);
        Price price = LarkPlanConfigManager.getPriceByLarkPlanId(Objects.requireNonNull(event).getPricePlanId());
        assertThat(subscription.getBasePlan()).isEqualTo(Objects.requireNonNull(price).getPlanId());
    }

    @Test
    public void testPriceThirtyAndOneYearOrderRenewTrail() {
        String spaceId = "spc" + IdWorker.get32UUID();
        OrderPaidEvent trailEvent = getOrderPaidEvent("enterprise/social/feishu/order/base_10_1_trail.json");
        prepareSocialBindInfo(spaceId, Objects.requireNonNull(trailEvent).getTenantKey(), trailEvent.getAppId());

        SocialOrderStrategyFactory.getService(SocialPlatformType.FEISHU).retrieveOrderPaidEvent(trailEvent);

        SocialOrderStrategyFactory.getService(SocialPlatformType.FEISHU)
                .retrieveOrderPaidEvent(getOrderPaidEvent("enterprise/social/feishu/order/base_10_1_per_year.json"));
        // set date for validation
        final OffsetDateTime nowDate = OffsetDateTime.of(2022, 5, 29, 0, 0, 0, 0, ZoneOffset.ofHours(8));
        getClock().setTime(nowDate);
        OrderPaidEvent event = getOrderPaidEvent("enterprise/social/feishu/order/base_20_1_upgrade.json");
        SocialOrderStrategyFactory.getService(SocialPlatformType.FEISHU).retrieveOrderPaidEvent(event);
        SubscriptionInfo before = iSpaceSubscriptionService.getPlanInfoBySpaceId(spaceId);
        // Renewal upgrade to 30 people for trial, to be effective
        SocialOrderStrategyFactory.getService(SocialPlatformType.FEISHU).retrieveOrderPaidEvent(getOrderPaidEvent(
                "enterprise/social/feishu/order/base_30_1_renew_trail.json"));
        SubscriptionInfo after = iSpaceSubscriptionService.getPlanInfoBySpaceId(spaceId);
        assertThat(after.getBasePlan()).isEqualTo(before.getBasePlan());
    }

    @Test
    public void testEnterprisePriceThirtyAndOneYearUpgradeAfterRenew() {
        String spaceId = "spc" + IdWorker.get32UUID();
        OrderPaidEvent trailEvent = getOrderPaidEvent("enterprise/social/feishu/order/base_10_1_trail.json");
        prepareSocialBindInfo(spaceId, Objects.requireNonNull(trailEvent).getTenantKey(), trailEvent.getAppId());
        // 1. ON TRIAL
        SocialOrderStrategyFactory.getService(SocialPlatformType.FEISHU).retrieveOrderPaidEvent(trailEvent);
        // 2. Buy
        SocialOrderStrategyFactory.getService(SocialPlatformType.FEISHU)
                .retrieveOrderPaidEvent(getOrderPaidEvent("enterprise/social/feishu/order/base_10_1_per_year.json"));
        // set date for validation
        final OffsetDateTime nowDate = OffsetDateTime.of(2022, 5, 29, 0, 0, 0, 0, ZoneOffset.ofHours(8));
        getClock().setTime(nowDate);
        // 3 Upgrade
        SocialOrderStrategyFactory.getService(SocialPlatformType.FEISHU)
                .retrieveOrderPaidEvent(getOrderPaidEvent("enterprise/social/feishu/order/base_20_1_upgrade.json"));
        // 4 Renewal upgrade to 30 people for trial, to be effective
        SocialOrderStrategyFactory.getService(SocialPlatformType.FEISHU).retrieveOrderPaidEvent(getOrderPaidEvent(
                "enterprise/social/feishu/order/base_30_1_renew_trail.json"));
        // 5 After renewal and upgrade, upgrade the original scheme again
        OrderPaidEvent event = getOrderPaidEvent("enterprise/social/feishu/order/enterprise_30_1_upgrade_after_renew.json");
        SocialOrderStrategyFactory.getService(SocialPlatformType.FEISHU).retrieveOrderPaidEvent(event);
        SubscriptionInfo subscription = iSpaceSubscriptionService.getPlanInfoBySpaceId(spaceId);
        Price price = LarkPlanConfigManager.getPriceByLarkPlanId(Objects.requireNonNull(event).getPricePlanId());
        assertThat(subscription.getBasePlan()).isEqualTo(Objects.requireNonNull(price).getPlanId());
    }

    @Test
    public void testNewPriceAndThirtyOneYear() {
        OrderPaidEvent trailEvent = getOrderPaidEvent("enterprise/social/feishu/order/base_new_30_1_trail.json");
        assertThat(trailEvent).as("data could not be parsed:base_new_30_1_trail").isNotNull();

        String spaceId = "spc" + IdUtil.fastSimpleUUID();
        prepareSocialBindInfo(spaceId, Objects.requireNonNull(trailEvent).getTenantKey(), trailEvent.getAppId());

        // 1. ON TRIAL
        final OffsetDateTime trialDate = OffsetDateTime.of(2023, 12, 22, 15, 9, 20, 0, ZoneOffset.ofHours(8));
        getClock().setTime(trialDate);
        SocialOrderStrategyFactory.getService(SocialPlatformType.FEISHU).retrieveOrderPaidEvent(trailEvent);

        // 2. Buy
        final OffsetDateTime buyDate = OffsetDateTime.of(2024, 1, 6, 15, 51, 19, 0, ZoneOffset.ofHours(8));
        getClock().setTime(buyDate);
        SocialOrderStrategyFactory.getService(SocialPlatformType.FEISHU)
            .retrieveOrderPaidEvent(getOrderPaidEvent("enterprise/social/feishu/order/base_new_30_1.json"));

        final OffsetDateTime nowDate = OffsetDateTime.of(2024, 1, 8, 15, 51, 19, 0, ZoneOffset.ofHours(8));
        getClock().setTime(nowDate);
        SubscriptionInfo subscription = iSpaceSubscriptionService.getPlanInfoBySpaceId(spaceId);
        assertThat(subscription.getEndDate()).isAfter(trialDate.plusYears(1).toLocalDate());

    }


    private OrderPaidEvent getOrderPaidEvent(String filePath) {
        InputStream resourceAsStream = ClassPathResource.class.getClassLoader().getResourceAsStream(filePath);
        if (resourceAsStream == null) {
            return null;
        }
        String jsonString = IoUtil.read(resourceAsStream, StandardCharsets.UTF_8);
        return JSONUtil.toBean(jsonString, OrderPaidEvent.class);
    }

    private void prepareSocialBindInfo(String spaceId, String tenantId, String appId) {
        prepareSpaceData(spaceId);
        SocialTenantBindEntity entity =
                SocialTenantBindEntity.builder().id(IdWorker.getId()).tenantId(tenantId).appId(appId).spaceId(spaceId).build();
        iSocialTenantBindService.save(entity);
    }

    private void prepareSpaceData(String spaceId) {
        // Initialize space information
        SpaceEntity spaceEntity = SpaceEntity.builder().spaceId(spaceId).name("test space").build();
        iSpaceService.save(spaceEntity);
    }
}
