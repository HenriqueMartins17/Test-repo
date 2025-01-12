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
import cn.hutool.json.JSONUtil;
import com.apitable.FileHelper;
import com.apitable.enterprise.AbstractVikaSaasIntegrationTest;
import com.apitable.enterprise.gm.ro.CreateBusinessOrderRo;
import com.apitable.enterprise.social.entity.SocialTenantBindEntity;
import com.apitable.enterprise.social.entity.SocialTenantEntity;
import com.apitable.enterprise.social.enums.SocialAppType;
import com.apitable.enterprise.social.enums.SocialPlatformType;
import com.apitable.enterprise.vikabilling.enums.OrderType;
import com.apitable.enterprise.vikabilling.enums.ProductChannel;
import com.apitable.enterprise.vikabilling.enums.ProductEnum;
import com.apitable.enterprise.vikabilling.setting.Plan;
import com.apitable.enterprise.vikabilling.setting.Price;
import com.apitable.enterprise.vikabilling.strategy.SocialOrderStrategyFactory;
import com.apitable.enterprise.vikabilling.util.BillingConfigManager;
import com.apitable.enterprise.vikabilling.util.DingTalkPlanConfigManager;
import com.apitable.enterprise.vikabilling.util.EntitlementChecker.ExpectedBundleCheck;
import com.apitable.enterprise.vikabilling.util.EntitlementChecker.ExpectedSubscriptionCheck;
import com.apitable.interfaces.billing.model.SubscriptionInfo;
import com.apitable.mock.bean.MockUserSpace;
import com.apitable.space.entity.SpaceEntity;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.vikadata.social.dingtalk.event.order.SyncHttpMarketOrderEvent;
import com.vikadata.social.dingtalk.event.order.SyncHttpMarketServiceCloseEvent;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * DingTalk Order Service Test
 */
@Disabled
public class DingTalkOrderServiceImplTest extends AbstractVikaSaasIntegrationTest {

    @Test
    public void testTrailPlan() {
        SyncHttpMarketOrderEvent event =
            getOrderPaidEvent("enterprise/social/dingtalk/order/base_trail.json");
        assertThat(event).as("data could not be parsed:base_trail").isNotNull();
        // Payment scheme for order purchase
        Price price = DingTalkPlanConfigManager.getPriceByItemCodeAndMonth(event.getItemCode());
        assertThat(price).as("dingtalk trial plan configuration error").isNotNull();
    }

    @Test
    public void testTrailOrder() {
        SyncHttpMarketOrderEvent event =
            getOrderPaidEvent("enterprise/social/dingtalk/order/base_trail.json");
        String spaceId = "spc" + IdWorker.get32UUID();
        prepareSocialBindInfo(spaceId, Objects.requireNonNull(event).getCorpId(),
            event.getSuiteId());
        // Purchase trial order
        LocalDateTime now = getClock().getNow(getTestTimeZone()).toLocalDateTime();
        event.setServiceStartTime(now.toInstant(getTestTimeZone()).toEpochMilli());
        event.setServiceStopTime(now.plusMonths(1).toInstant(getTestTimeZone()).toEpochMilli());
        SocialOrderStrategyFactory.getService(SocialPlatformType.DINGTALK)
            .retrieveOrderPaidEvent(event);
        // Payment scheme for order purchase
        Price price = DingTalkPlanConfigManager.getPriceByItemCodeAndMonth(event.getItemCode());
        SubscriptionInfo info = iSpaceSubscriptionService.getPlanInfoBySpaceId(spaceId);
        assertThat(info.onTrial()).isTrue();
        assertThat(info.getBasePlan()).isEqualTo(Objects.requireNonNull(price).getPlanId());
    }

    @Test
    public void testPriceTenAndOneYearOrder() {
        String spaceId = "spc" + IdWorker.get32UUID();
        SyncHttpMarketOrderEvent trailEvent =
            getOrderPaidEvent("enterprise/social/dingtalk/order/base_trail.json");
        trailEvent.setServiceStartTime(
            getClock().getNow(getTestTimeZone()).minusDays(15).toInstant().toEpochMilli());
        trailEvent.setServiceStopTime(
            getClock().getNow(getTestTimeZone()).minusDays(1).toInstant().toEpochMilli());
        prepareSocialBindInfo(spaceId, Objects.requireNonNull(trailEvent).getCorpId(),
            trailEvent.getSuiteId());
        SocialOrderStrategyFactory.getService(SocialPlatformType.DINGTALK)
            .retrieveOrderPaidEvent(trailEvent);

        SyncHttpMarketOrderEvent event =
            getOrderPaidEvent("enterprise/social/dingtalk/order/base_10_1_per_year.json");
        event.setServiceStartTime(getClock().getNow(getTestTimeZone()).toInstant().toEpochMilli());
        event.setServiceStopTime(
            getClock().getNow(getTestTimeZone()).plusYears(1).toInstant().toEpochMilli());
        SocialOrderStrategyFactory.getService(SocialPlatformType.DINGTALK)
            .retrieveOrderPaidEvent(event);
        SubscriptionInfo subscriptionInfo = iSpaceSubscriptionService.getPlanInfoBySpaceId(spaceId);
        Price price = DingTalkPlanConfigManager.getPriceByItemCodeAndMonth(event.getItemCode());
        assertThat(subscriptionInfo.onTrial()).isFalse();
        assertThat(subscriptionInfo.getBasePlan()).isEqualTo(
            Objects.requireNonNull(price).getPlanId());
    }

    @Test
    public void testPriceHundredV1AndOneYearOrder() {
        String spaceId = "spc" + IdWorker.get32UUID();
        SyncHttpMarketOrderEvent trailEvent =
            getOrderPaidEvent("enterprise/social/dingtalk/order/base_trail.json");
        trailEvent.setServiceStartTime(
            getClock().getNow(getTestTimeZone()).minusDays(15).toInstant().toEpochMilli());
        trailEvent.setServiceStopTime(
            getClock().getNow(getTestTimeZone()).minusDays(1).toInstant().toEpochMilli());
        prepareSocialBindInfo(spaceId, Objects.requireNonNull(trailEvent).getCorpId(),
            trailEvent.getSuiteId());
        SocialOrderStrategyFactory.getService(SocialPlatformType.DINGTALK)
            .retrieveOrderPaidEvent(trailEvent);

        SyncHttpMarketOrderEvent event =
            getOrderPaidEvent("enterprise/social/dingtalk/order/base_100_1_per_year.json");
        event.setServiceStartTime(getClock().getNow(getTestTimeZone()).toInstant().toEpochMilli());
        event.setServiceStopTime(
            getClock().getNow(getTestTimeZone()).plusYears(1).toInstant().toEpochMilli());
        SocialOrderStrategyFactory.getService(SocialPlatformType.DINGTALK)
            .retrieveOrderPaidEvent(event);
        SubscriptionInfo subscriptionInfo = iSpaceSubscriptionService.getPlanInfoBySpaceId(spaceId);
        Price price = DingTalkPlanConfigManager.getPriceByItemCodeAndMonth(event.getItemCode());
        assertThat(subscriptionInfo.onTrial()).isFalse();
        assertThat(subscriptionInfo.getBasePlan()).isEqualTo(
            Objects.requireNonNull(price).getPlanId());
    }

    @Test
    public void testPriceTenAndOneYearOrderActivity() {
        String spaceId = "spc" + IdWorker.get32UUID();
        SyncHttpMarketOrderEvent trailEvent =
            getOrderPaidEvent("enterprise/social/dingtalk/order/base_trail.json");
        trailEvent.setServiceStartTime(
            getClock().getNow(getTestTimeZone()).minusYears(1).minusDays(15).toInstant()
                .toEpochMilli());
        trailEvent.setServiceStopTime(
            getClock().getNow(getTestTimeZone()).minusYears(1).toInstant().toEpochMilli());
        prepareSocialBindInfo(spaceId, Objects.requireNonNull(trailEvent).getCorpId(),
            trailEvent.getSuiteId());
        SocialOrderStrategyFactory.getService(SocialPlatformType.DINGTALK)
            .retrieveOrderPaidEvent(trailEvent);

        SyncHttpMarketOrderEvent tmpEvent =
            getOrderPaidEvent("enterprise/social/dingtalk/order/base_10_1_per_year.json");
        tmpEvent.setServiceStartTime(
            getClock().getNow(getTestTimeZone()).minusYears(1).toInstant().toEpochMilli());
        tmpEvent.setServiceStopTime(
            getClock().getNow(getTestTimeZone()).toInstant().toEpochMilli());
        SocialOrderStrategyFactory.getService(SocialPlatformType.DINGTALK)
            .retrieveOrderPaidEvent(tmpEvent);

        SyncHttpMarketOrderEvent event =
            getOrderPaidEvent("enterprise/social/dingtalk/order/base_10_1_per_year_activity.json");
        event.setServiceStartTime(getClock().getNow(getTestTimeZone()).toInstant().toEpochMilli());
        event.setServiceStopTime(
            getClock().getNow(getTestTimeZone()).plusMonths(3).toInstant().toEpochMilli());
        SocialOrderStrategyFactory.getService(SocialPlatformType.DINGTALK)
            .retrieveOrderPaidEvent(event);

        SubscriptionInfo subscriptionInfo = iSpaceSubscriptionService.getPlanInfoBySpaceId(spaceId);
        Price price = DingTalkPlanConfigManager.getPriceByItemCodeAndMonth(event.getItemCode());
        assertThat(subscriptionInfo.getBasePlan()).isEqualTo(
            Objects.requireNonNull(price).getPlanId());
    }

    @Test
    public void testPriceTenAndOneYearOrderRefund() {
        String spaceId = "spc" + IdWorker.get32UUID();
        SyncHttpMarketOrderEvent trailEvent =
            getOrderPaidEvent("enterprise/social/dingtalk/order/base_trail.json");
        prepareSocialBindInfo(spaceId, Objects.requireNonNull(trailEvent).getCorpId(),
            trailEvent.getSuiteId());
        SocialOrderStrategyFactory.getService(SocialPlatformType.DINGTALK)
            .retrieveOrderPaidEvent(trailEvent);

        SocialOrderStrategyFactory.getService(SocialPlatformType.DINGTALK).retrieveOrderPaidEvent(
            getOrderPaidEvent("enterprise/social/dingtalk/order/base_10_1_per_year.json"));

        SocialOrderStrategyFactory.getService(SocialPlatformType.DINGTALK).retrieveOrderPaidEvent(
            getOrderPaidEvent("enterprise/social/dingtalk/order/base_10_1_per_year_activity.json"));

        SyncHttpMarketServiceCloseEvent event =
            getOrderRefundEvent("enterprise/social/dingtalk/order/base_10_1_per_year_refund.json");
        SocialOrderStrategyFactory.getService(SocialPlatformType.DINGTALK)
            .retrieveOrderRefundEvent(event);

        SubscriptionInfo subscriptionInfo = iSpaceSubscriptionService.getPlanInfoBySpaceId(spaceId);
        // Payment scheme for order purchase
        Plan plan = BillingConfigManager.getFreePlan(ProductChannel.DINGTALK);
        assertThat(subscriptionInfo.onTrial()).isFalse();
        assertThat(subscriptionInfo.getBasePlan()).isEqualTo(Objects.requireNonNull(plan).getId());
    }

    @Test
    public void testPriceTenAndOneYearOrderAgain() {
        String spaceId = "spc" + IdWorker.get32UUID();
        SyncHttpMarketOrderEvent trailEvent =
            getOrderPaidEvent("enterprise/social/dingtalk/order/base_trail.json");
        prepareSocialBindInfo(spaceId, Objects.requireNonNull(trailEvent).getCorpId(),
            trailEvent.getSuiteId());
        trailEvent.setServiceStartTime(
            getClock().getNow(getTestTimeZone()).minusYears(1).minusDays(15).toInstant()
                .toEpochMilli());
        trailEvent.setServiceStopTime(
            getClock().getNow(getTestTimeZone()).minusYears(1).minusDays(1).toInstant()
                .toEpochMilli());
        SocialOrderStrategyFactory.getService(SocialPlatformType.DINGTALK)
            .retrieveOrderPaidEvent(trailEvent);

        SocialOrderStrategyFactory.getService(SocialPlatformType.DINGTALK).retrieveOrderPaidEvent(
            getOrderPaidEvent("enterprise/social/dingtalk/order/base_10_1_per_year.json"));

        SocialOrderStrategyFactory.getService(SocialPlatformType.DINGTALK).retrieveOrderPaidEvent(
            getOrderPaidEvent("enterprise/social/dingtalk/order/base_10_1_per_year_activity.json"));

        SocialOrderStrategyFactory.getService(SocialPlatformType.DINGTALK).retrieveOrderRefundEvent(
            getOrderRefundEvent("enterprise/social/dingtalk/order/base_10_1_per_year_refund.json"));

        SyncHttpMarketOrderEvent event =
            getOrderPaidEvent("enterprise/social/dingtalk/order/base_10_1_per_year_twice.json");
        event.setServiceStartTime(getClock().getNow(getTestTimeZone()).toInstant().toEpochMilli());
        event.setServiceStopTime(
            getClock().getNow(getTestTimeZone()).plusYears(1).toInstant().toEpochMilli());
        SocialOrderStrategyFactory.getService(SocialPlatformType.DINGTALK)
            .retrieveOrderPaidEvent(event);

        SubscriptionInfo subscriptionInfo = iSpaceSubscriptionService.getPlanInfoBySpaceId(spaceId);
        Price price = DingTalkPlanConfigManager.getPriceByItemCodeAndMonth(event.getItemCode());
        assertThat(subscriptionInfo.getBasePlan()).isEqualTo(
            Objects.requireNonNull(price).getPlanId());
    }

    @Test
    public void testPriceTwentyAndOneYearOrderUpgrade() {
        String spaceId = "spc" + IdWorker.get32UUID();
        SyncHttpMarketOrderEvent trailEvent =
            getOrderPaidEvent("enterprise/social/dingtalk/order/base_trail.json");
        prepareSocialBindInfo(spaceId, Objects.requireNonNull(trailEvent).getCorpId(),
            trailEvent.getSuiteId());
        SocialOrderStrategyFactory.getService(SocialPlatformType.DINGTALK)
            .retrieveOrderPaidEvent(trailEvent);

        SocialOrderStrategyFactory.getService(SocialPlatformType.DINGTALK).retrieveOrderPaidEvent(
            getOrderPaidEvent("enterprise/social/dingtalk/order/base_10_1_per_year.json"));

        SocialOrderStrategyFactory.getService(SocialPlatformType.DINGTALK).retrieveOrderPaidEvent(
            getOrderPaidEvent("enterprise/social/dingtalk/order/base_10_1_per_year_activity.json"));

        SocialOrderStrategyFactory.getService(SocialPlatformType.DINGTALK).retrieveOrderRefundEvent(
            getOrderRefundEvent("enterprise/social/dingtalk/order/base_10_1_per_year_refund.json"));

        SocialOrderStrategyFactory.getService(SocialPlatformType.DINGTALK).retrieveOrderPaidEvent(
            getOrderPaidEvent("enterprise/social/dingtalk/order/base_10_1_per_year_twice.json"));

        SyncHttpMarketOrderEvent event =
            getOrderPaidEvent("enterprise/social/dingtalk/order/base_20_1_per_year_upgrade.json");
        event.setServiceStartTime(getClock().getNow(getTestTimeZone()).toInstant().toEpochMilli());
        event.setServiceStopTime(
            getClock().getNow(getTestTimeZone()).plusYears(1).toInstant().toEpochMilli());
        SocialOrderStrategyFactory.getService(SocialPlatformType.DINGTALK)
            .retrieveOrderPaidEvent(event);

        SubscriptionInfo subscriptionInfo = iSpaceSubscriptionService.getPlanInfoBySpaceId(spaceId);
        Price price = DingTalkPlanConfigManager.getPriceByItemCodeAndMonth(event.getItemCode());
        assertThat(subscriptionInfo.getBasePlan()).isEqualTo(
            Objects.requireNonNull(price).getPlanId());
    }

    @Test
    public void testPriceTwentyAndOneYearOrderRefund() {
        String spaceId = "spc" + IdWorker.get32UUID();
        SyncHttpMarketOrderEvent trailEvent =
            getOrderPaidEvent("enterprise/social/dingtalk/order/base_trail.json");
        prepareSocialBindInfo(spaceId, Objects.requireNonNull(trailEvent).getCorpId(),
            trailEvent.getSuiteId());
        SocialOrderStrategyFactory.getService(SocialPlatformType.DINGTALK)
            .retrieveOrderPaidEvent(trailEvent);

        SocialOrderStrategyFactory.getService(SocialPlatformType.DINGTALK).retrieveOrderPaidEvent(
            getOrderPaidEvent("enterprise/social/dingtalk/order/base_10_1_per_year.json"));

        SocialOrderStrategyFactory.getService(SocialPlatformType.DINGTALK).retrieveOrderPaidEvent(
            getOrderPaidEvent("enterprise/social/dingtalk/order/base_10_1_per_year_activity.json"));

        SocialOrderStrategyFactory.getService(SocialPlatformType.DINGTALK).retrieveOrderRefundEvent(
            getOrderRefundEvent("enterprise/social/dingtalk/order/base_10_1_per_year_refund.json"));

        SocialOrderStrategyFactory.getService(SocialPlatformType.DINGTALK).retrieveOrderPaidEvent(
            getOrderPaidEvent("enterprise/social/dingtalk/order/base_10_1_per_year_twice.json"));

        SocialOrderStrategyFactory.getService(SocialPlatformType.DINGTALK).retrieveOrderPaidEvent(
            getOrderPaidEvent("enterprise/social/dingtalk/order/base_20_1_per_year_upgrade.json"));

        SyncHttpMarketServiceCloseEvent event =
            getOrderRefundEvent("enterprise/social/dingtalk/order/base_20_1_per_year_upgrade.json");
        SocialOrderStrategyFactory.getService(SocialPlatformType.DINGTALK)
            .retrieveOrderRefundEvent(event);

        SubscriptionInfo subscriptionInfo = iSpaceSubscriptionService.getPlanInfoBySpaceId(spaceId);
        // Payment scheme for order purchase
        Plan plan = BillingConfigManager.getFreePlan(ProductChannel.DINGTALK);
        assertThat(subscriptionInfo.onTrial()).isFalse();
        assertThat(subscriptionInfo.getBasePlan()).isEqualTo(Objects.requireNonNull(plan).getId());
    }

    @Test
    public void testPriceTwoHundredAndOneYearOrder() {
        String spaceId = "spc" + IdWorker.get32UUID();
        SyncHttpMarketOrderEvent event =
            getOrderPaidEvent("enterprise/social/dingtalk/order/standard_200_1_per_year.json");
        event.setServiceStartTime(getClock().getNow(getTestTimeZone()).toInstant().toEpochMilli());
        event.setServiceStopTime(
            getClock().getNow(getTestTimeZone()).plusYears(1).toInstant().toEpochMilli());

        prepareSocialBindInfo(spaceId, Objects.requireNonNull(event).getCorpId(),
            event.getSuiteId());
        SocialOrderStrategyFactory.getService(SocialPlatformType.DINGTALK)
            .retrieveOrderPaidEvent(event);

        SocialOrderStrategyFactory.getService(SocialPlatformType.DINGTALK).retrieveOrderPaidEvent(
            getOrderPaidEvent("enterprise/social/dingtalk/order/standard_200_3_per_month.json"));

        SocialOrderStrategyFactory.getService(SocialPlatformType.DINGTALK).retrieveOrderPaidEvent(
            getOrderPaidEvent(
                "enterprise/social/dingtalk/order/standard_200_1_renew_per_year.json"));

        SocialOrderStrategyFactory.getService(SocialPlatformType.DINGTALK).retrieveOrderPaidEvent(
            getOrderPaidEvent(
                "enterprise/social/dingtalk/order/standard_200_3_renew_per_month.json"));

        SubscriptionInfo subscriptionInfo = iSpaceSubscriptionService.getPlanInfoBySpaceId(spaceId);
        Price price = DingTalkPlanConfigManager.getPriceByItemCodeAndMonth(event.getItemCode());
        assertThat(subscriptionInfo.onTrial()).isFalse();
        assertThat(subscriptionInfo.getBasePlan()).isEqualTo(
            Objects.requireNonNull(price).getPlanId());
    }

    @Test
    public void testCreateBusinessOrderOnNewBuy() {
        final MockUserSpace mockUserSpace = createSingleUserAndSpace();
        // chose product to buy
        ProductEnum toBuy = ProductEnum.DINGTALK_ENTERPRISE;

        CreateBusinessOrderRo data = new CreateBusinessOrderRo();
        data.setSpaceId(mockUserSpace.getSpaceId());
        data.setType(OrderType.BUY.name());
        data.setProduct(toBuy.name());
        data.setSeat(1000);
        data.setMonths(12);

        final LocalDate nowToday = getClock().getToday(getTestTimeZone());

        iBillingOfflineService.createBusinessOrder(mockUserSpace.getUserId(), data);

        final LocalDate shouldExpireDate = nowToday.plusMonths(12);

        // check space entitlement
        entitlementChecker.checkBundle(mockUserSpace.getSpaceId(),
            new ExpectedBundleCheck(nowToday, shouldExpireDate));
        entitlementChecker.checkSubscription(mockUserSpace.getSpaceId(),
            new ExpectedSubscriptionCheck(toBuy, nowToday, shouldExpireDate));
    }

    @Test
    public void testCreateBusinessOrderOnRenew() {
        final MockUserSpace mockUserSpace = createSingleUserAndSpace();
        // chose product to buy
        ProductEnum toBuy = ProductEnum.DINGTALK_ENTERPRISE;
        CreateBusinessOrderRo data = new CreateBusinessOrderRo();
        data.setSpaceId(mockUserSpace.getSpaceId());
        data.setType(OrderType.BUY.name());
        data.setProduct(toBuy.name());
        data.setSeat(300);
        data.setMonths(12);

        // create new buy order
        iBillingOfflineService.createBusinessOrder(mockUserSpace.getUserId(), data);

        final LocalDate nowToday = getClock().getToday(getTestTimeZone());
        final LocalDate shouldExpireDate = nowToday.plusMonths(12);

        // check space entitlement
        entitlementChecker.checkBundle(mockUserSpace.getSpaceId(),
            new ExpectedBundleCheck(nowToday, shouldExpireDate));
        entitlementChecker.checkSubscription(mockUserSpace.getSpaceId(),
            new ExpectedSubscriptionCheck(toBuy, nowToday, shouldExpireDate));

        // move clock
        getClock().addDays(100);

        CreateBusinessOrderRo renewData = new CreateBusinessOrderRo();
        renewData.setSpaceId(mockUserSpace.getSpaceId());
        renewData.setType(OrderType.RENEW.name());
        renewData.setMonths(12);

        // create renew order
        iBillingOfflineService.createBusinessOrder(mockUserSpace.getUserId(), renewData);

        final LocalDate renewShouldExpireDate = shouldExpireDate.plusMonths(12);
        // check space entitlement
        entitlementChecker.checkBundle(mockUserSpace.getSpaceId(),
            new ExpectedBundleCheck(nowToday, renewShouldExpireDate));
        entitlementChecker.checkSubscription(mockUserSpace.getSpaceId(),
            new ExpectedSubscriptionCheck(toBuy, nowToday, renewShouldExpireDate));
    }

    @Test
    public void testCreateBusinessOrderOnUpgrade() {
        final MockUserSpace mockUserSpace = createSingleUserAndSpace();
        // chose product to buy
        ProductEnum toBuy = ProductEnum.DINGTALK_ENTERPRISE;
        CreateBusinessOrderRo data = new CreateBusinessOrderRo();
        data.setSpaceId(mockUserSpace.getSpaceId());
        data.setType(OrderType.BUY.name());
        data.setProduct(toBuy.name());
        data.setSeat(300);
        data.setMonths(12);

        // create new buy order
        iBillingOfflineService.createBusinessOrder(mockUserSpace.getUserId(), data);

        final LocalDate nowToday = getClock().getToday(getTestTimeZone());
        final LocalDate shouldExpireDate = nowToday.plusMonths(12);

        // check space entitlement
        entitlementChecker.checkBundle(mockUserSpace.getSpaceId(),
            new ExpectedBundleCheck(nowToday, shouldExpireDate));
        entitlementChecker.checkSubscription(mockUserSpace.getSpaceId(),
            new ExpectedSubscriptionCheck(toBuy, nowToday, shouldExpireDate));

        // move clock
        getClock().addDays(100);

        ProductEnum upgrade = ProductEnum.DINGTALK_ENTERPRISE;
        CreateBusinessOrderRo upgradeData = new CreateBusinessOrderRo();
        upgradeData.setSpaceId(mockUserSpace.getSpaceId());
        upgradeData.setType(OrderType.UPGRADE.name());
        upgradeData.setProduct(upgrade.name());
        upgradeData.setSeat(500);
        upgradeData.setMonths(12);

        // create upgrade order
        iBillingOfflineService.createBusinessOrder(mockUserSpace.getUserId(), upgradeData);

        final LocalDate nowTodayOfUpgrade = getClock().getToday(getTestTimeZone());
        final LocalDate renewShouldExpireDate = nowTodayOfUpgrade.plusMonths(12);
        // check space entitlement
        entitlementChecker.checkBundle(mockUserSpace.getSpaceId(),
            new ExpectedBundleCheck(nowTodayOfUpgrade, renewShouldExpireDate));
        entitlementChecker.checkSubscription(mockUserSpace.getSpaceId(),
            new ExpectedSubscriptionCheck(upgrade, nowTodayOfUpgrade, renewShouldExpireDate));
    }

    @Test
    public void testPriceTwentyAndOneYearAndRenew() {
        String spaceId = "spc" + IdWorker.get32UUID();
        SyncHttpMarketOrderEvent trailEvent =
            getOrderPaidEvent("enterprise/social/dingtalk/order/base_trail.json");
        prepareSocialBindInfo(spaceId, Objects.requireNonNull(trailEvent).getCorpId(),
            trailEvent.getSuiteId());
        SocialOrderStrategyFactory.getService(SocialPlatformType.DINGTALK)
            .retrieveOrderPaidEvent(trailEvent);
        final LocalDate nowToday = getClock().getToday(getTestTimeZone());
        // buy
        SocialOrderStrategyFactory.getService(SocialPlatformType.DINGTALK).retrieveOrderPaidEvent(
            getOrderPaidEvent("enterprise/social/dingtalk/order/base_20_1_per_year.json",
                nowToday.minusYears(1), nowToday));
        // renew
        SyncHttpMarketOrderEvent event =
            getOrderPaidEvent("enterprise/social/dingtalk/order/base_20_1_per_year_upgrade.json",
                nowToday, nowToday.plusYears(1));
        SocialOrderStrategyFactory.getService(SocialPlatformType.DINGTALK)
            .retrieveOrderPaidEvent(event);

        SubscriptionInfo subscriptionInfo = iSpaceSubscriptionService.getPlanInfoBySpaceId(spaceId);
        // Payment scheme for order purchase
        Price price = DingTalkPlanConfigManager.getPriceByItemCodeAndMonth(event.getItemCode());
        assertThat(subscriptionInfo.onTrial()).isFalse();
        assert price != null;
        assertThat(subscriptionInfo.getBasePlan()).isEqualTo(Objects.requireNonNull(price.getPlanId()));
    }


    private SyncHttpMarketOrderEvent getOrderPaidEvent(String filePath) {
        InputStream resourceAsStream = FileHelper.getInputStreamFromResource(filePath);
        String jsonString = IoUtil.read(resourceAsStream, StandardCharsets.UTF_8);
        SyncHttpMarketOrderEvent event =
            JSONUtil.toBean(jsonString, SyncHttpMarketOrderEvent.class);
        iSocialDingTalkOrderService.createOrder(event);
        return event;
    }


    private SyncHttpMarketOrderEvent getOrderPaidEvent(String filePath, LocalDate startAt,
                                                       LocalDate endAt) {
        InputStream resourceAsStream = FileHelper.getInputStreamFromResource(filePath);
        String jsonString = IoUtil.read(resourceAsStream, StandardCharsets.UTF_8);
        SyncHttpMarketOrderEvent event =
            JSONUtil.toBean(jsonString, SyncHttpMarketOrderEvent.class);
        event.setServiceStartTime(startAt.atTime(0, 0, 0).toInstant(getTestTimeZone()).toEpochMilli());
        event.setServiceStopTime(endAt.atTime(23, 59, 59).toInstant(getTestTimeZone()).toEpochMilli());
        iSocialDingTalkOrderService.createOrder(event);
        return event;
    }

    private SyncHttpMarketServiceCloseEvent getOrderRefundEvent(String filePath) {
        InputStream resourceAsStream = FileHelper.getInputStreamFromResource(filePath);
        String jsonString = IoUtil.read(resourceAsStream, StandardCharsets.UTF_8);
        SyncHttpMarketServiceCloseEvent event =
            JSONUtil.toBean(jsonString, SyncHttpMarketServiceCloseEvent.class);
        iSocialDingTalkRefundService.createRefund(event);
        return event;
    }

    private void prepareSocialBindInfo(String spaceId, String tenantId, String appId) {
        prepareSpaceData(spaceId);
        SocialTenantBindEntity entity =
            SocialTenantBindEntity.builder().id(IdWorker.getId()).tenantId(tenantId).appId(appId)
                .spaceId(spaceId).build();
        iSocialTenantBindService.save(entity);
        iSocialTenantService.save(
            SocialTenantEntity.builder().id(IdWorker.getId()).tenantId(tenantId).appId(appId)
                .platform(SocialPlatformType.DINGTALK.getValue())
                .appType(SocialAppType.ISV.getType()).build());
    }

    private void prepareSpaceData(String spaceId) {
        // Initialize space information
        SpaceEntity spaceEntity = SpaceEntity.builder().spaceId(spaceId).name("test space").build();
        iSpaceService.save(spaceEntity);
    }
}
