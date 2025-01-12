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

import static com.apitable.enterprise.vikabilling.util.BillingConfigManager.getBillingConfig;
import static com.apitable.enterprise.vikabilling.util.BillingConfigManager.getFreePlan;
import static com.apitable.enterprise.vikabilling.util.BillingConfigManager.getPlan;
import static java.lang.Thread.sleep;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.list;

import cn.hutool.core.collection.CollectionUtil;
import com.apitable.enterprise.AbstractVikaSaasIntegrationTest;
import com.apitable.enterprise.gm.ro.CreateBusinessOrderRo;
import com.apitable.enterprise.gm.ro.CreateEntitlementWithAddOn;
import com.apitable.enterprise.vikabilling.core.DefaultOrderArguments;
import com.apitable.enterprise.vikabilling.core.OrderArguments;
import com.apitable.enterprise.vikabilling.enums.OrderType;
import com.apitable.enterprise.vikabilling.enums.ProductChannel;
import com.apitable.enterprise.vikabilling.enums.ProductEnum;
import com.apitable.enterprise.vikabilling.setting.Price;
import com.apitable.enterprise.vikabilling.util.BillingConfigManager;
import com.apitable.enterprise.vikabilling.util.EntitlementChecker.ExpectedBundleCheck;
import com.apitable.enterprise.vikabilling.util.EntitlementChecker.ExpectedSpaceEntitlementCheck;
import com.apitable.enterprise.vikabilling.util.EntitlementChecker.ExpectedSubscriptionCheck;
import com.apitable.mock.bean.MockUserSpace;
import com.apitable.shared.clock.spring.ClockManager;
import com.apitable.space.vo.SpaceCapacityPageVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BillingOfflineServiceImplTest extends AbstractVikaSaasIntegrationTest {

    protected static final Logger log =
        LoggerFactory.getLogger(BillingOfflineServiceImplTest.class);

    @Test
    public void testCreateBusinessOrderOnNewBuyWithoutTargetDate() {
        log.info("initial user and space");
        final MockUserSpace mockUserSpace = createSingleUserAndSpace();
        // chose product to buy
        ProductEnum toBuy = ProductEnum.SILVER;

        CreateBusinessOrderRo data = new CreateBusinessOrderRo();
        data.setSpaceId(mockUserSpace.getSpaceId());
        data.setType(OrderType.BUY.name());
        data.setProduct(toBuy.name());
        data.setSeat(100);
        data.setMonths(1);

        final LocalDate nowToday = getClock().getToday(getTestTimeZone());

        iBillingOfflineService.createBusinessOrder(mockUserSpace.getUserId(), data);

        final LocalDate shouldExpireDate = nowToday.plusMonths(1);

        // check space entitlement
        entitlementChecker.checkBundle(mockUserSpace.getSpaceId(),
            new ExpectedBundleCheck(nowToday, shouldExpireDate));
        entitlementChecker.checkSubscription(mockUserSpace.getSpaceId(),
            new ExpectedSubscriptionCheck(toBuy, nowToday, shouldExpireDate));
    }

    @Test
    public void testCreateBusinessOrderOnNewBuyWithTargetDate() {
        log.info("initial user and space");
        final MockUserSpace mockUserSpace = createSingleUserAndSpace();
        // chose product to buy
        ProductEnum toBuy = ProductEnum.SILVER;

        final LocalDate nowToday = getClock().getToday(getTestTimeZone());
        LocalDate startDate = nowToday.minusDays(10);

        CreateBusinessOrderRo data = new CreateBusinessOrderRo();
        data.setSpaceId(mockUserSpace.getSpaceId());
        data.setType(OrderType.BUY.name());
        data.setProduct(toBuy.name());
        data.setSeat(100);
        data.setMonths(1);
        data.setStartDate(startDate.format(DateTimeFormatter.ISO_LOCAL_DATE));

        iBillingOfflineService.createBusinessOrder(mockUserSpace.getUserId(), data);

        final LocalDate shouldExpireDate = startDate.plusMonths(1);

        // check space entitlement
        entitlementChecker.checkBundle(mockUserSpace.getSpaceId(),
            new ExpectedBundleCheck(startDate, shouldExpireDate));
        entitlementChecker.checkSubscription(mockUserSpace.getSpaceId(),
            new ExpectedSubscriptionCheck(toBuy, startDate, shouldExpireDate));
    }

    @Test
    public void testCreateBusinessOrderOnRenew() {
        log.info("initial user and space");
        final MockUserSpace mockUserSpace = createSingleUserAndSpace();
        // chose product to buy
        ProductEnum toBuy = ProductEnum.GOLD;
        CreateBusinessOrderRo data = new CreateBusinessOrderRo();
        data.setSpaceId(mockUserSpace.getSpaceId());
        data.setType(OrderType.BUY.name());
        data.setProduct(toBuy.name());
        data.setSeat(200);
        data.setMonths(4);

        // create new buy order
        iBillingOfflineService.createBusinessOrder(mockUserSpace.getUserId(), data);

        final LocalDate nowToday = getClock().getToday(getTestTimeZone());
        final LocalDate shouldExpireDate = nowToday.plusMonths(4);

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
        renewData.setMonths(3);

        // create renew order
        iBillingOfflineService.createBusinessOrder(mockUserSpace.getUserId(), renewData);

        final LocalDate renewShouldExpireDate = shouldExpireDate.plusMonths(3);
        // check space entitlement
        entitlementChecker.checkBundle(mockUserSpace.getSpaceId(),
            new ExpectedBundleCheck(nowToday, renewShouldExpireDate));
        entitlementChecker.checkSubscription(mockUserSpace.getSpaceId(),
            new ExpectedSubscriptionCheck(toBuy, nowToday, renewShouldExpireDate));
    }

    @Test
    public void testCreateBusinessOrderOnUpgrade() {
        log.info("initial user and space");
        final MockUserSpace mockUserSpace = createSingleUserAndSpace();
        // chose product to buy
        ProductEnum toBuy = ProductEnum.GOLD;
        CreateBusinessOrderRo data = new CreateBusinessOrderRo();
        data.setSpaceId(mockUserSpace.getSpaceId());
        data.setType(OrderType.BUY.name());
        data.setProduct(toBuy.name());
        data.setSeat(200);
        data.setMonths(4);

        // create new buy order
        iBillingOfflineService.createBusinessOrder(mockUserSpace.getUserId(), data);

        final LocalDate nowToday = getClock().getToday(getTestTimeZone());
        final LocalDate shouldExpireDate = nowToday.plusMonths(4);

        // check space entitlement
        entitlementChecker.checkBundle(mockUserSpace.getSpaceId(),
            new ExpectedBundleCheck(nowToday, shouldExpireDate));
        entitlementChecker.checkSubscription(mockUserSpace.getSpaceId(),
            new ExpectedSubscriptionCheck(toBuy, nowToday, shouldExpireDate));

        // move clock
        getClock().addDays(100);

        ProductEnum upgrade = ProductEnum.ENTERPRISE;
        CreateBusinessOrderRo upgradeData = new CreateBusinessOrderRo();
        upgradeData.setSpaceId(mockUserSpace.getSpaceId());
        upgradeData.setType(OrderType.UPGRADE.name());
        upgradeData.setProduct(upgrade.name());
        upgradeData.setSeat(100);
        upgradeData.setMonths(3);

        // create upgrade order
        iBillingOfflineService.createBusinessOrder(mockUserSpace.getUserId(), upgradeData);

        final LocalDate nowTodayOfUpgrade = getClock().getToday(getTestTimeZone());
        final LocalDate renewShouldExpireDate = nowTodayOfUpgrade.plusMonths(3);
        // check space entitlement
        entitlementChecker.checkBundle(mockUserSpace.getSpaceId(),
            new ExpectedBundleCheck(nowTodayOfUpgrade, renewShouldExpireDate));
        entitlementChecker.checkSubscription(mockUserSpace.getSpaceId(),
            new ExpectedSubscriptionCheck(upgrade, nowTodayOfUpgrade, renewShouldExpireDate));
    }

    @Test
    public void testCreateSubscriptionWithAddWithoutTargetDateOnConditionOnNoSubscription() {
        log.info("initial user and space");
        final MockUserSpace mockUserSpace = createSingleUserAndSpace();

        // choose plan to reward
        CreateEntitlementWithAddOn data = new CreateEntitlementWithAddOn();
        data.setSpaceId(mockUserSpace.getSpaceId());
        data.setPlanId("capacity_100G");
        data.setMonths(3);

        final LocalDate nowToday = getClock().getToday(getTestTimeZone());

        iBillingOfflineService.createSubscriptionWithAddOn(data, mockUserSpace.getUserId());

        final LocalDate shouldExpireDate = nowToday.plusMonths(3);

        // check space entitlement
        entitlementChecker.checkBundle(mockUserSpace.getSpaceId(),
            new ExpectedBundleCheck(nowToday, shouldExpireDate));
        final List<ExpectedSubscriptionCheck> expectedSubscriptions = new ArrayList<>();
        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.BRONZE, nowToday, shouldExpireDate));
        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.CAPACITY, nowToday, shouldExpireDate));
        entitlementChecker.checkSubscription(mockUserSpace.getSpaceId(), expectedSubscriptions);
        // check space subscription
        entitlementChecker.checkSpaceEntitlement(mockUserSpace.getSpaceId(),
            new ExpectedSpaceEntitlementCheck(ProductEnum.BRONZE.getName(), null,
                getFreePlan(ProductChannel.VIKA),
                Collections.singletonList(getBillingConfig().getPlans().get("capacity_100G"))));
    }

    /**
     * test step:
     * 1. initial date on 2022-2-1, create future add-on subscription between (2022-2-2 and 2022-3-2)
     * 2. then create add-on subscription between (2022-2-1 and 2022-3-1)
     * 3. check entitlement on 2022-2-1, (2022-2-2 and 2022-3-2) should not be active, (2022-2-1 and 2022-3-1) should be active
     * 4. move clock on 2022-2-2, all should be active
     */
    @Test
    public void testCreateSubscriptionWithAddWithoutTargetDateDoubleTime() {
        // initial date on 2022-2-1 19:10:30
        final OffsetDateTime initialCreateDate =
            OffsetDateTime.of(2022, 2, 1, 19, 10, 30, 0, getTestTimeZone());
        getClock().setTime(initialCreateDate);
        log.info("initial user and space");
        final MockUserSpace mockUserSpace = createSingleUserAndSpace();

        // choose plan to reward
        CreateEntitlementWithAddOn first = new CreateEntitlementWithAddOn();
        first.setSpaceId(mockUserSpace.getSpaceId());
        first.setPlanId("capacity_100G");
        first.setStartDate("2022-02-02");
        first.setMonths(1);

        iBillingOfflineService.createSubscriptionWithAddOn(first, mockUserSpace.getUserId());

        // check space entitlement
        entitlementChecker.checkBundle(mockUserSpace.getSpaceId(),
            new ExpectedBundleCheck(LocalDate.of(2022, 2, 2), LocalDate.of(2022, 3, 2)));
        final List<ExpectedSubscriptionCheck> expectedSubscriptions = new ArrayList<>();
        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.BRONZE, LocalDate.of(2022, 2, 2),
                LocalDate.of(2022, 3, 2)));
        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.CAPACITY, LocalDate.of(2022, 2, 2),
                LocalDate.of(2022, 3, 2)));
        entitlementChecker.checkSubscription(mockUserSpace.getSpaceId(), expectedSubscriptions);
        // check space subscription
        entitlementChecker.checkSpaceEntitlement(mockUserSpace.getSpaceId(),
            new ExpectedSpaceEntitlementCheck(ProductEnum.BRONZE.getName(), null,
                getFreePlan(ProductChannel.VIKA),
                Collections.emptyList()
            )
        );

        // create again on today
        CreateEntitlementWithAddOn second = new CreateEntitlementWithAddOn();
        second.setSpaceId(mockUserSpace.getSpaceId());
        second.setPlanId("capacity_50G");
        second.setMonths(1);

        iBillingOfflineService.createSubscriptionWithAddOn(second, mockUserSpace.getUserId());

        // check space entitlement
        expectedSubscriptions.clear();
        entitlementChecker.checkBundle(mockUserSpace.getSpaceId(),
            new ExpectedBundleCheck(LocalDate.of(2022, 2, 2), LocalDate.of(2022, 3, 2)));
        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.BRONZE, LocalDate.of(2022, 2, 2),
                LocalDate.of(2022, 3, 2)));
        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.CAPACITY, LocalDate.of(2022, 2, 2),
                LocalDate.of(2022, 3, 2)));
        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.CAPACITY, LocalDate.of(2022, 2, 1),
                LocalDate.of(2022, 3, 1)));
        entitlementChecker.checkSubscription(mockUserSpace.getSpaceId(), expectedSubscriptions);
        // check space subscription
        entitlementChecker.checkSpaceEntitlement(mockUserSpace.getSpaceId(),
            new ExpectedSpaceEntitlementCheck(ProductEnum.BRONZE.getName(), null,
                getFreePlan(ProductChannel.VIKA),
                list(getBillingConfig().getPlans().get("capacity_50G"))
            )
        );

        // move clock to 2022-2-2
        getClock().setTime(OffsetDateTime.of(2022, 2, 2, 0, 1, 1, 0, getTestTimeZone()));
        expectedSubscriptions.clear();
        entitlementChecker.checkBundle(mockUserSpace.getSpaceId(),
            new ExpectedBundleCheck(LocalDate.of(2022, 2, 2), LocalDate.of(2022, 3, 2)));
        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.BRONZE, LocalDate.of(2022, 2, 2),
                LocalDate.of(2022, 3, 2)));
        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.CAPACITY, LocalDate.of(2022, 2, 2),
                LocalDate.of(2022, 3, 2)));
        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.CAPACITY, LocalDate.of(2022, 2, 1),
                LocalDate.of(2022, 3, 1)));
        entitlementChecker.checkSubscription(mockUserSpace.getSpaceId(), expectedSubscriptions);
        // check space subscription
        entitlementChecker.checkSpaceEntitlement(mockUserSpace.getSpaceId(),
            new ExpectedSpaceEntitlementCheck(ProductEnum.BRONZE.getName(), null,
                getFreePlan(ProductChannel.VIKA),
                list(
                    getBillingConfig().getPlans().get("capacity_50G"),
                    getBillingConfig().getPlans().get("capacity_100G")
                )
            )
        );
    }

    @Test
    public void testCreateSubscriptionWithAddWithTargetDateOnConditionOnNoSubscription() {
        final OffsetDateTime initialCreateDate =
            OffsetDateTime.of(2022, 2, 1, 19, 10, 30, 0, getTestTimeZone());
        getClock().setTime(initialCreateDate);
        log.info("initial user and space");
        final MockUserSpace mockUserSpace = createSingleUserAndSpace();

        // choose plan to reward
        CreateEntitlementWithAddOn data = new CreateEntitlementWithAddOn();
        data.setSpaceId(mockUserSpace.getSpaceId());
        data.setPlanId("capacity_100G");
        data.setStartDate("2022-02-02");
        data.setMonths(3);

        iBillingOfflineService.createSubscriptionWithAddOn(data, mockUserSpace.getUserId());

        // check space entitlement
        entitlementChecker.checkBundle(mockUserSpace.getSpaceId(),
            new ExpectedBundleCheck(LocalDate.of(2022, 2, 2), LocalDate.of(2022, 5, 2)));
        final List<ExpectedSubscriptionCheck> expectedSubscriptions = new ArrayList<>();
        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.BRONZE, LocalDate.of(2022, 2, 2),
                LocalDate.of(2022, 5, 2)));
        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.CAPACITY, LocalDate.of(2022, 2, 2),
                LocalDate.of(2022, 5, 2)));
        entitlementChecker.checkSubscription(mockUserSpace.getSpaceId(), expectedSubscriptions);
        // check space subscription
        entitlementChecker.checkSpaceEntitlement(mockUserSpace.getSpaceId(),
            new ExpectedSpaceEntitlementCheck(ProductEnum.BRONZE.getName(), null,
                getFreePlan(ProductChannel.VIKA),
                Collections.emptyList()));
        // move clock to 2022-02-02, add-on entitlement should be active
        getClock().setTime(OffsetDateTime.of(2022, 2, 2, 0, 10, 30, 0, getTestTimeZone()));
        entitlementChecker.checkSpaceEntitlement(mockUserSpace.getSpaceId(),
            new ExpectedSpaceEntitlementCheck(ProductEnum.BRONZE.getName(), null,
                getFreePlan(ProductChannel.VIKA),
                Collections.singletonList(getBillingConfig().getPlans().get("capacity_100G"))));
    }

    /**
     * test step:
     * 1. initial date on 2022-2-1, create future add-on subscription between (2022-2-2 and 2022-5-2)
     * 2. then pay charge subscription with 1 month silver(20) product on current date(2022-2-1), active date between 2022-2-1 and 2022-3-1
     * 3. and move clock to 2022-3-2, base subscription should expire, but add-on subscription still be active
     * 4. and move clock to 2022-5-3, all subscription should be expired
     */
    @Test
    public void testCreateAddOnSubscriptionWithTargetDateThenPayChargeSubscription() {
        // initial date on 2022-2-1 19:10:30
        final OffsetDateTime initialCreateDate =
            OffsetDateTime.of(2022, 2, 1, 19, 10, 30, 0, getTestTimeZone());
        getClock().setTime(initialCreateDate);

        log.info("initial user and space");
        final MockUserSpace mockUserSpace = createSingleUserAndSpace();

        // choose plan to reward
        CreateEntitlementWithAddOn data = new CreateEntitlementWithAddOn();
        data.setSpaceId(mockUserSpace.getSpaceId());
        data.setPlanId("capacity_100G");
        data.setStartDate("2022-02-02");
        data.setMonths(3);

        iBillingOfflineService.createSubscriptionWithAddOn(data, mockUserSpace.getUserId());

        // check space entitlement
        final List<ExpectedSubscriptionCheck> expectedSubscriptions = new ArrayList<>();
        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.BRONZE, LocalDate.of(2022, 2, 2),
                LocalDate.of(2022, 5, 2)));
        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.CAPACITY, LocalDate.of(2022, 2, 2),
                LocalDate.of(2022, 5, 2)));
        entitlementChecker.checkSubscription(mockUserSpace.getSpaceId(), expectedSubscriptions);
        entitlementChecker.checkSpaceEntitlement(mockUserSpace.getSpaceId(),
            new ExpectedSpaceEntitlementCheck(
                ProductEnum.BRONZE.getName(), null,
                getFreePlan(ProductChannel.VIKA),
                Collections.emptyList()
            ));

        expectedSubscriptions.clear();

        // buy charge product on current date
        Price silverPrice =
            BillingConfigManager.getPriceBySeatAndMonths(ProductEnum.SILVER, 100, 1);
        assertThat(silverPrice).isNotNull();
        final OrderArguments orderArguments =
            new DefaultOrderArguments(mockUserSpace.getSpaceId(), silverPrice);
        // paid Time
        final OffsetDateTime paidTime = getClock().getNow(getTestTimeZone()).plusMinutes(5);
        autoOrderPayProcessor(mockUserSpace.getUserId(), orderArguments, paidTime);

        // check subscription
        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.SILVER, LocalDate.of(2022, 2, 1),
                LocalDate.of(2022, 3, 1)));
        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.CAPACITY, LocalDate.of(2022, 2, 2),
                LocalDate.of(2022, 5, 2)));
        entitlementChecker.checkSubscription(mockUserSpace.getSpaceId(), expectedSubscriptions);
        entitlementChecker.checkSpaceEntitlement(mockUserSpace.getSpaceId(),
            new ExpectedSpaceEntitlementCheck(ProductEnum.SILVER.getName(),
                LocalDate.of(2022, 3, 1),
                getBillingConfig().getPlans().get(silverPrice.getPlanId()),
                Collections.emptyList()
            ));
        // move clock on 2022-02-02, add-on subscription should be activated
        expectedSubscriptions.clear();
        getClock().setTime(OffsetDateTime.of(2022, 2, 2, 0, 1, 1, 0, getTestTimeZone()));
        entitlementChecker.checkSpaceEntitlement(mockUserSpace.getSpaceId(),
            new ExpectedSpaceEntitlementCheck(ProductEnum.SILVER.getName(),
                LocalDate.of(2022, 3, 1),
                getBillingConfig().getPlans().get(silverPrice.getPlanId()),
                Collections.singletonList(getBillingConfig().getPlans().get("capacity_100G"))
            )
        );

        // move clock on 2022-03-02, make base subscription expire, but add-on subscription still be active
        expectedSubscriptions.clear();
        getClock().setTime(OffsetDateTime.of(2022, 3, 2, 0, 1, 1, 0, getTestTimeZone()));
        // check space entitlement
        entitlementChecker.checkSpaceEntitlement(mockUserSpace.getSpaceId(),
            new ExpectedSpaceEntitlementCheck(ProductEnum.BRONZE.getName(), null,
                getFreePlan(ProductChannel.VIKA),
                Collections.singletonList(getBillingConfig().getPlans().get("capacity_100G"))));

        // move clock on 2022-05-03, all subscription should be expired
        expectedSubscriptions.clear();
        getClock().setTime(OffsetDateTime.of(2022, 5, 3, 0, 1, 1, 0, getTestTimeZone()));
        // check space entitlement
        entitlementChecker.checkSpaceEntitlement(mockUserSpace.getSpaceId(),
            new ExpectedSpaceEntitlementCheck(ProductEnum.BRONZE.getName(), null,
                getFreePlan(ProductChannel.VIKA),
                Collections.emptyList()
            )
        );
    }

    /**
     * test step:
     * 1. create add-on subscription between (2022-2-1 and 2022-5-1)
     * 2. then pay charge subscription with 1 month silver(20) product after 5 days (2022-2-6), active date between 2022-2-6 and 2022-3-6
     * 3. and move clock to 2022-3-7, base subscription should expire, but add-on subscription still be active
     */
    @Test
    public void testCreateAddOnSubscriptionWithoutTargetDateThenPayChargeSubscriptionAfterFewDays() {
        // initial date on 2022-2-1 19:10:30
        final OffsetDateTime initialCreateDate =
            OffsetDateTime.of(2022, 2, 1, 19, 10, 30, 0, getTestTimeZone());
        getClock().setTime(initialCreateDate);

        log.info("initial user and space");
        final MockUserSpace mockUserSpace = createSingleUserAndSpace();

        // choose plan to reward
        CreateEntitlementWithAddOn data = new CreateEntitlementWithAddOn();
        data.setSpaceId(mockUserSpace.getSpaceId());
        data.setPlanId("capacity_100G");
        data.setMonths(3);

        iBillingOfflineService.createSubscriptionWithAddOn(data, mockUserSpace.getUserId());

        // check space entitlement
        final List<ExpectedSubscriptionCheck> expectedSubscriptions = new ArrayList<>();
        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.BRONZE, initialCreateDate.toLocalDate(),
                LocalDate.of(2022, 5, 1)));
        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.CAPACITY, initialCreateDate.toLocalDate(),
                LocalDate.of(2022, 5, 1)));
        entitlementChecker.checkSubscription(mockUserSpace.getSpaceId(), expectedSubscriptions);

        expectedSubscriptions.clear();
        // buy charge product after a few day, move clock on 2022-02-06
        getClock().addDays(5);

        Price silverPrice =
            BillingConfigManager.getPriceBySeatAndMonths(ProductEnum.SILVER, 100, 1);
        assertThat(silverPrice).isNotNull();
        final OrderArguments orderArguments =
            new DefaultOrderArguments(mockUserSpace.getSpaceId(), silverPrice);
        // paid Time
        final OffsetDateTime paidTime = getClock().getNow(getTestTimeZone()).plusMinutes(5);
        autoOrderPayProcessor(mockUserSpace.getUserId(), orderArguments, paidTime);

        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.SILVER, LocalDate.of(2022, 2, 6),
                LocalDate.of(2022, 3, 6)));
        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.CAPACITY, LocalDate.of(2022, 2, 1),
                LocalDate.of(2022, 5, 1)));
        // check subscription
        entitlementChecker.checkSubscription(mockUserSpace.getSpaceId(), expectedSubscriptions);
        entitlementChecker.checkSpaceEntitlement(mockUserSpace.getSpaceId(),
            new ExpectedSpaceEntitlementCheck(ProductEnum.SILVER.getName(),
                LocalDate.of(2022, 3, 6),
                getBillingConfig().getPlans().get(silverPrice.getPlanId()),
                Collections.singletonList(getBillingConfig().getPlans().get("capacity_100G"))));

        // move clock on 2022-03-07, make base subscription expire
        expectedSubscriptions.clear();
        final OffsetDateTime expireTime =
            OffsetDateTime.of(2022, 3, 7, 0, 1, 1, 0, getTestTimeZone());
        getClock().setTime(expireTime);
        // check space entitlement
        entitlementChecker.checkSpaceEntitlement(mockUserSpace.getSpaceId(),
            new ExpectedSpaceEntitlementCheck(ProductEnum.BRONZE.getName(), null,
                getFreePlan(ProductChannel.VIKA),
                Collections.singletonList(getBillingConfig().getPlans().get("capacity_100G"))));
    }

    /**
     * test step:
     * 1. create base subscription between 2022-2-1 and 2023-2-1
     * 2. then create add-on subscription between 2022-2-8 and 2022-5-8
     * 3. move clock to 2022-5-9, add-on subscription should be expired, but base subscription still be active
     */
    @Test
    public void testCreateSubscriptionWithAddOnAfterPayChargeSubscription() {
        // initial date on 2022-2-1 19:10:30
        final OffsetDateTime initialCreateDate =
            OffsetDateTime.of(2022, 2, 1, 19, 10, 30, 0, ZoneOffset.UTC);
        getClock().setTime(initialCreateDate);

        log.info("initial user and space");
        final MockUserSpace mockUserSpace = createSingleUserAndSpace();
        // chose product to buy
        Price silverPrice =
            BillingConfigManager.getPriceBySeatAndMonths(ProductEnum.SILVER, 100, 12);
        assertThat(silverPrice).isNotNull();
        final OrderArguments orderArguments =
            new DefaultOrderArguments(mockUserSpace.getSpaceId(), silverPrice);
        // paid Time
        final OffsetDateTime paidTime = initialCreateDate.plusMinutes(5);
        autoOrderPayProcessor(mockUserSpace.getUserId(), orderArguments, paidTime);

        // check space entitlement
        entitlementChecker.checkBundle(mockUserSpace.getSpaceId(),
            new ExpectedBundleCheck(initialCreateDate.toLocalDate(), LocalDate.of(2023, 2, 1)));
        entitlementChecker.checkSubscription(mockUserSpace.getSpaceId(),
            new ExpectedSubscriptionCheck(ProductEnum.SILVER, initialCreateDate.toLocalDate(),
                LocalDate.of(2023, 2, 1)));
        entitlementChecker.checkSpaceEntitlement(mockUserSpace.getSpaceId(),
            new ExpectedSpaceEntitlementCheck(
                ProductEnum.SILVER.getName(), LocalDate.of(2023, 2, 1),
                getBillingConfig().getPlans().get(silverPrice.getPlanId()),
                Collections.emptyList()
            ));

        // after a week, initial date on 2022-2-8 create add-on subscription
        getClock().addWeeks(1);
        CreateEntitlementWithAddOn data = new CreateEntitlementWithAddOn();
        data.setSpaceId(mockUserSpace.getSpaceId());
        data.setPlanId("capacity_100G");
        data.setMonths(3);

        iBillingOfflineService.createSubscriptionWithAddOn(data, mockUserSpace.getUserId());

        // check space entitlement
        entitlementChecker.checkBundle(mockUserSpace.getSpaceId(),
            new ExpectedBundleCheck(initialCreateDate.toLocalDate(), LocalDate.of(2023, 2, 1)));
        final List<ExpectedSubscriptionCheck> expectedSubscriptions = new ArrayList<>();
        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.SILVER, initialCreateDate.toLocalDate(),
                LocalDate.of(2023, 2, 1)));
        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.CAPACITY, LocalDate.of(2022, 2, 8),
                LocalDate.of(2022, 5, 8)));
        entitlementChecker.checkSubscription(mockUserSpace.getSpaceId(), expectedSubscriptions);
        entitlementChecker.checkSpaceEntitlement(mockUserSpace.getSpaceId(),
            new ExpectedSpaceEntitlementCheck(
                ProductEnum.SILVER.getName(), LocalDate.of(2023, 2, 1),
                getBillingConfig().getPlans().get(silverPrice.getPlanId()),
                Collections.singletonList(getBillingConfig().getPlans().get("capacity_100G"))
            ));

        // move clock to 2022-5-9
        final OffsetDateTime expireTime =
            OffsetDateTime.of(2022, 5, 9, 0, 1, 1, 0, getTestTimeZone());
        getClock().setTime(expireTime);
        entitlementChecker.checkSpaceEntitlement(mockUserSpace.getSpaceId(),
            new ExpectedSpaceEntitlementCheck(
                ProductEnum.SILVER.getName(), LocalDate.of(2023, 2, 1),
                getBillingConfig().getPlans().get(silverPrice.getPlanId()),
                Collections.emptyList()
            ));
    }

    /**
     * test step:
     * 1. create add-on subscription between (2022-2-1 and 2022-5-1)
     * 2. then pay charge subscription with 1 month enterprise(20) product after 5 days (2022-2-6), active date between 2022-2-6 and 2022-3-6
     * 3. and move clock to 2022-3-7, base subscription should expire, but add-on subscription still be active
     */
    @Test
    public void testCreateSubscriptionWithAddOnThenCreateOfflineOrder() {
        // initial date on 2022-2-1 19:10:30
        final OffsetDateTime initialCreateDate =
            OffsetDateTime.of(2022, 2, 1, 19, 10, 30, 0, getTestTimeZone());
        getClock().setTime(initialCreateDate);

        log.info("initial user and space");
        final MockUserSpace mockUserSpace = createSingleUserAndSpace();

        // choose plan to reward
        CreateEntitlementWithAddOn data = new CreateEntitlementWithAddOn();
        data.setSpaceId(mockUserSpace.getSpaceId());
        data.setPlanId("capacity_100G");
        data.setMonths(3);

        iBillingOfflineService.createSubscriptionWithAddOn(data, mockUserSpace.getUserId());

        log.info("mock clock utc now: {}", getClock().getUTCNow());
        log.info("clock instance utc now: {}", ClockManager.me().getLocalDateNow());

        // check space entitlement
        final List<ExpectedSubscriptionCheck> expectedSubscriptions = new ArrayList<>();
        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.BRONZE, LocalDate.of(2022, 2, 1),
                LocalDate.of(2022, 5, 1)));
        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.CAPACITY, LocalDate.of(2022, 2, 1),
                LocalDate.of(2022, 5, 1)));
        entitlementChecker.checkSubscription(mockUserSpace.getSpaceId(), expectedSubscriptions);
        // check entitlement
        entitlementChecker.checkSpaceEntitlement(mockUserSpace.getSpaceId(),
            new ExpectedSpaceEntitlementCheck(ProductEnum.BRONZE.getName(), null,
                getFreePlan(ProductChannel.VIKA),
                Collections.singletonList(getBillingConfig().getPlans().get("capacity_100G"))));

        expectedSubscriptions.clear();
        // buy charge product after a few day, move clock on 2022-02-06
        getClock().addDays(5);

        CreateBusinessOrderRo offlineRequest = new CreateBusinessOrderRo();
        offlineRequest.setSpaceId(mockUserSpace.getSpaceId());
        offlineRequest.setType(OrderType.BUY.name());
        offlineRequest.setProduct(ProductEnum.ENTERPRISE.name());
        offlineRequest.setSeat(20);
        offlineRequest.setMonths(1);

        iBillingOfflineService.createBusinessOrder(mockUserSpace.getUserId(), offlineRequest);

        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.ENTERPRISE, LocalDate.of(2022, 2, 6),
                LocalDate.of(2022, 3, 6)));
        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.CAPACITY, LocalDate.of(2022, 2, 1),
                LocalDate.of(2022, 5, 1)));
        // check subscription
        entitlementChecker.checkSubscription(mockUserSpace.getSpaceId(), expectedSubscriptions);
        entitlementChecker.checkSpaceEntitlement(mockUserSpace.getSpaceId(),
            new ExpectedSpaceEntitlementCheck(ProductEnum.ENTERPRISE.getName(),
                LocalDate.of(2022, 3, 6),
                getPlan(ProductEnum.ENTERPRISE, 20),
                Collections.singletonList(getBillingConfig().getPlans().get("capacity_100G"))));

        // move clock on 2022-03-07, make base subscription expire
        expectedSubscriptions.clear();
        final OffsetDateTime expireTime =
            OffsetDateTime.of(2022, 3, 7, 0, 1, 1, 0, getTestTimeZone());
        getClock().setTime(expireTime);
        // check space entitlement
        entitlementChecker.checkSpaceEntitlement(mockUserSpace.getSpaceId(),
            new ExpectedSpaceEntitlementCheck(ProductEnum.BRONZE.getName(), null,
                getFreePlan(ProductChannel.VIKA),
                Collections.singletonList(getBillingConfig().getPlans().get("capacity_100G"))));
    }

    /**
     * test step:
     * 1. pay charge base subscription(silver 100 seat) between 2022-2-3 and 2022-8-3 (6 month)
     * 2. and move clock to 2022-6-1, create add-on subscription between 2022-6-1 and 2023-6-1 (1 year)
     * 3. and move clock to 2022-8-4, base subscription should be expired, but add-on subscription still be active
     * 4. then pay charge subscription with 6 month silver(100) product on 2022-8-10, active date between 2022-8-10 and 2023-2-10
     * 5. and move clock to 2023-2-11, base subscription should expire, but add-on subscription still be active
     * 6. and move clock to 2023-6-2, all subscription should be expired
     */
    @Test
    public void testCreateSubscriptionWithAddOnThenPayChargeWhenSubscriptionExpired() {
        // step 1: initial date on 2022-2-3 19:10:30
        final OffsetDateTime initialCreateDate =
            OffsetDateTime.of(2022, 2, 3, 19, 10, 30, 0, getTestTimeZone());
        getClock().setTime(initialCreateDate);

        log.info("initial user and space");
        final MockUserSpace mockUserSpace = createSingleUserAndSpace();

        // chose product to buy
        ProductEnum toBuy = ProductEnum.SILVER;
        Price silverPrice = BillingConfigManager.getPriceBySeatAndMonths(toBuy, 100, 6);
        assertThat(silverPrice).isNotNull();
        final OrderArguments orderArguments =
            new DefaultOrderArguments(mockUserSpace.getSpaceId(), silverPrice);
        final OffsetDateTime paidTime = initialCreateDate.plusMinutes(5);
        autoOrderPayProcessor(mockUserSpace.getUserId(), orderArguments, paidTime);

        // check space entitlement
        final List<ExpectedSubscriptionCheck> expectedSubscriptions = new ArrayList<>();
        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.SILVER, LocalDate.of(2022, 2, 3),
                LocalDate.of(2022, 8, 3)));
        entitlementChecker.checkSubscription(mockUserSpace.getSpaceId(), expectedSubscriptions);
        // check entitlement
        entitlementChecker.checkSpaceEntitlement(
            mockUserSpace.getSpaceId(),
            new ExpectedSpaceEntitlementCheck(ProductEnum.SILVER.getName(),
                LocalDate.of(2022, 8, 3),
                getBillingConfig().getPlans().get(silverPrice.getPlanId()),
                Collections.emptyList())
        );

        expectedSubscriptions.clear();
        // step 2: move clock on 2022-06-01
        getClock().setTime(OffsetDateTime.of(2022, 6, 1, 19, 10, 30, 0, getTestTimeZone()));
        // choose plan to reward
        CreateEntitlementWithAddOn data = new CreateEntitlementWithAddOn();
        data.setSpaceId(mockUserSpace.getSpaceId());
        data.setPlanId("capacity_100G");
        data.setMonths(12);
        iBillingOfflineService.createSubscriptionWithAddOn(data, mockUserSpace.getUserId());

        // check space entitlement
        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.SILVER, LocalDate.of(2022, 2, 3),
                LocalDate.of(2022, 8, 3)));
        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.CAPACITY, LocalDate.of(2022, 6, 1),
                LocalDate.of(2023, 6, 1)));
        entitlementChecker.checkSubscription(mockUserSpace.getSpaceId(), expectedSubscriptions);
        // check entitlement
        entitlementChecker.checkSpaceEntitlement(mockUserSpace.getSpaceId(),
            new ExpectedSpaceEntitlementCheck(ProductEnum.SILVER.getName(),
                LocalDate.of(2022, 8, 3),
                getBillingConfig().getPlans().get(silverPrice.getPlanId()),
                Collections.singletonList(getBillingConfig().getPlans().get("capacity_100G"))
            )
        );

        // step 3: mock clock to 2022-8-4, base subscription should be expired, add-on subscription still be active
        getClock().setTime(OffsetDateTime.of(2022, 8, 4, 0, 0, 0, 0, getTestTimeZone()));
        entitlementChecker.checkSubscription(null);
        // check entitlement
        entitlementChecker.checkSpaceEntitlement(mockUserSpace.getSpaceId(),
            new ExpectedSpaceEntitlementCheck(ProductEnum.BRONZE.getName(), null,
                getFreePlan(ProductChannel.VIKA),
                Collections.singletonList(getBillingConfig().getPlans().get("capacity_100G"))));

        // step 4: mock clock to 2022-8-10, pay charge again
        getClock().setTime(OffsetDateTime.of(2022, 8, 10, 10, 0, 0, 0, getTestTimeZone()));
        Price silverPriceAgain =
            BillingConfigManager.getPriceBySeatAndMonths(ProductEnum.SILVER, 100, 6);
        assertThat(silverPriceAgain).isNotNull();
        final OffsetDateTime sencondPaidTime = initialCreateDate.plusMinutes(5);
        autoOrderPayProcessor(mockUserSpace.getUserId(),
            new DefaultOrderArguments(mockUserSpace.getSpaceId(), silverPriceAgain),
            sencondPaidTime);

        // check space entitlement
        expectedSubscriptions.clear();
        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.SILVER, LocalDate.of(2022, 8, 10),
                LocalDate.of(2023, 2, 10)));
        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.CAPACITY, LocalDate.of(2022, 6, 1),
                LocalDate.of(2023, 6, 1)));
        entitlementChecker.checkSubscription(mockUserSpace.getSpaceId(), expectedSubscriptions);
        // check entitlement
        entitlementChecker.checkSpaceEntitlement(
            mockUserSpace.getSpaceId(),
            new ExpectedSpaceEntitlementCheck(ProductEnum.SILVER.getName(),
                LocalDate.of(2023, 2, 10),
                getBillingConfig().getPlans().get(silverPrice.getPlanId()),
                Collections.singletonList(getBillingConfig().getPlans().get("capacity_100G"))
            )
        );

        // step 5: mock clock to 2023-2-11, base subscription should be expired
        getClock().setTime(OffsetDateTime.of(2023, 2, 11, 0, 0, 0, 0, getTestTimeZone()));
        // check entitlement
        entitlementChecker.checkSpaceEntitlement(mockUserSpace.getSpaceId(),
            new ExpectedSpaceEntitlementCheck(ProductEnum.BRONZE.getName(), null,
                getFreePlan(ProductChannel.VIKA),
                Collections.singletonList(getBillingConfig().getPlans().get("capacity_100G"))));

        // step 6. and move clock to 2023-6-2, all subscription should be expired
        getClock().setTime(OffsetDateTime.of(2023, 6, 2, 0, 0, 0, 0, getTestTimeZone()));
        // check entitlement
        entitlementChecker.checkSpaceEntitlement(mockUserSpace.getSpaceId(),
            new ExpectedSpaceEntitlementCheck(ProductEnum.BRONZE.getName(), null,
                getFreePlan(ProductChannel.VIKA),
                Collections.emptyList()
            )
        );
    }

    @Test
    public void testCreateGiftCapacityOrder() {
        MockUserSpace mockUserSpace = createSingleUserAndSpace();
        String userName = "testUserName";
        // Order a 300MB add-on subscription plan
        iSpaceSubscriptionService.createAddOnWithGiftCapacity(mockUserSpace.getUserId(), userName,
            mockUserSpace.getSpaceId());
        // Query space gift attachment capacity size
        Long number =
            iSpaceSubscriptionService.getSpaceUnExpireGiftCapacity(mockUserSpace.getSpaceId());
        assertThat(number).isEqualTo(314572800L);
        IPage<SpaceCapacityPageVO> spaceCapacityPageVOIPage =
            iBillingCapacityService.getSpaceCapacityDetail(mockUserSpace.getSpaceId(), false,
                new Page<>());
        assertThat(spaceCapacityPageVOIPage.getRecords().get(0).getExpireDate().length()).isEqualTo(
            19);
    }


    /**
     * test step:
     * 1. create two add-on subscription with 300MB
     * 2. move clock to 2023-2-8
     * 3. pay charge base subscription(enterprise 100 seat) between 2023-2-3 and 2023-3-3 (1 month)
     * 4. create two add-on subscription with 300MB again
     */
    @Test
    public void testRewardAgainGiftCapacityWhenBuyOrderAndAlreadyRewardGiftCapacity()
        throws InterruptedException {
        // initial date on 2023-2-3 19:10:30
        final OffsetDateTime initialCreateDate =
            OffsetDateTime.of(2023, 2, 3, 19, 10, 30, 0, getTestTimeZone());
        getClock().setTime(initialCreateDate);

        log.info("init user and space");
        MockUserSpace mainUser = createSingleUserAndSpace();

        // init invited user
        MockUserSpace invitedUserOne = createSingleUserAndSpace();
        MockUserSpace invitedUserTwo = createSingleUserAndSpace();
        List<MockUserSpace> inviteUserBeforeBuy =
            CollectionUtil.newArrayList(invitedUserOne, invitedUserTwo);

        // reward two 300MB add-on subscription
        for (MockUserSpace mockUserSpace : inviteUserBeforeBuy) {
            iSpaceSubscriptionService.createAddOnWithGiftCapacity(mockUserSpace.getUserId(),
                "inviteBefore", mainUser.getSpaceId());
        }

        // check space entitlement
        List<ExpectedSubscriptionCheck> expectedSubscriptions = new ArrayList<>();
        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.BRONZE, initialCreateDate.toLocalDate(),
                LocalDate.of(2024, 2, 3)));
        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.CAPACITY, initialCreateDate.toLocalDate(),
                LocalDate.of(2024, 2, 3)));
        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.CAPACITY, initialCreateDate.toLocalDate(),
                LocalDate.of(2024, 2, 3)));
        entitlementChecker.checkSubscription(mainUser.getSpaceId(), expectedSubscriptions);
        // check entitlement
        entitlementChecker.checkSpaceEntitlement(mainUser.getSpaceId(),
            new ExpectedSpaceEntitlementCheck(ProductEnum.BRONZE.getName(), null,
                getFreePlan(ProductChannel.VIKA),
                CollectionUtil.newArrayList(getBillingConfig().getPlans().get("capacity_300_MB"),
                    getBillingConfig().getPlans().get("capacity_300_MB"))));

        expectedSubscriptions.clear();

        // buy charge product after a few day, move clock on 2023-02-08
        getClock().addDays(5);

        // chose product to buy
        ProductEnum upgrade = ProductEnum.ENTERPRISE;
        CreateBusinessOrderRo data = new CreateBusinessOrderRo();
        data.setSpaceId(mainUser.getSpaceId());
        data.setType(OrderType.BUY.name());
        data.setProduct(upgrade.name());
        data.setSeat(100);
        data.setMonths(1);
        iBillingOfflineService.createBusinessOrder(mainUser.getUserId(), data);

        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.ENTERPRISE, LocalDate.of(2023, 2, 8),
                LocalDate.of(2023, 3, 8)));
        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.CAPACITY, LocalDate.of(2023, 2, 3),
                LocalDate.of(2024, 2, 3)));
        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.CAPACITY, LocalDate.of(2023, 2, 3),
                LocalDate.of(2024, 2, 3)));
        entitlementChecker.checkSubscription(mainUser.getSpaceId(), expectedSubscriptions);

        expectedSubscriptions.clear();

        // init invited user again
        MockUserSpace invitedUserThree = createSingleUserAndSpace();
        MockUserSpace invitedUserFour = createSingleUserAndSpace();
        List<MockUserSpace> inviteUserAfterBuy =
            CollectionUtil.newArrayList(invitedUserThree, invitedUserFour);

        // reward two 300MB add-on subscription again
        for (MockUserSpace mockUserSpace : inviteUserAfterBuy) {
            sleep(1000);
            iSpaceSubscriptionService.createAddOnWithGiftCapacity(mockUserSpace.getUserId(),
                "inviteAfter", mainUser.getSpaceId());
        }

        // check space entitlement
        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.ENTERPRISE, LocalDate.of(2023, 2, 8),
                LocalDate.of(2023, 3, 8)));
        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.CAPACITY, LocalDate.of(2023, 2, 3),
                LocalDate.of(2024, 2, 3)));
        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.CAPACITY, LocalDate.of(2023, 2, 3),
                LocalDate.of(2024, 2, 3)));
        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.CAPACITY, LocalDate.of(2023, 2, 8),
                LocalDate.of(2024, 2, 8)));
        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.CAPACITY, LocalDate.of(2023, 2, 8),
                LocalDate.of(2024, 2, 8)));
        entitlementChecker.checkSubscription(mainUser.getSpaceId(), expectedSubscriptions);

    }

    /**
     * test step:
     * 1. create two add-on subscription with 300MB
     * 2. pay charge base subscription(enterprise 100 seat) between 2023-2-3 and 2023-3-3 (1 month)
     * 3. move clock to 2023-4-3
     * 4. base subscription(enterprise 100 seat) should expire, add-on subscription still active
     * 5. create two add-on subscription with 300MB again
     */
    @Test
    public void testRewardAgainGiftCapacityWhenOrderExpireAndAlreadyRewardGiftCapacity()
        throws InterruptedException {
        // initial date on 2023-2-3 19:10:30
        final OffsetDateTime initialCreateDate =
            OffsetDateTime.of(2023, 2, 3, 19, 10, 30, 0, getTestTimeZone());
        getClock().setTime(initialCreateDate);

        log.info("init user and space");
        MockUserSpace mainUser = createSingleUserAndSpace();

        // init invited user
        MockUserSpace invitedUserOne = createSingleUserAndSpace();
        MockUserSpace invitedUserTwo = createSingleUserAndSpace();
        List<MockUserSpace> inviteUserBeforeBuy =
            CollectionUtil.newArrayList(invitedUserOne, invitedUserTwo);

        // reward two 300MB add-on subscription
        for (MockUserSpace mockUserSpace : inviteUserBeforeBuy) {
            sleep(1000);
            iSpaceSubscriptionService.createAddOnWithGiftCapacity(mockUserSpace.getUserId(),
                "inviteBefore", mainUser.getSpaceId());
        }

        // check space entitlement
        List<ExpectedSubscriptionCheck> expectedSubscriptions = new ArrayList<>();
        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.BRONZE, initialCreateDate.toLocalDate(),
                LocalDate.of(2024, 2, 3)));
        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.CAPACITY, initialCreateDate.toLocalDate(),
                LocalDate.of(2024, 2, 3)));
        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.CAPACITY, initialCreateDate.toLocalDate(),
                LocalDate.of(2024, 2, 3)));
        entitlementChecker.checkSubscription(mainUser.getSpaceId(), expectedSubscriptions);
        expectedSubscriptions.clear();

        // chose product to buy
        ProductEnum upgrade = ProductEnum.ENTERPRISE;
        CreateBusinessOrderRo data = new CreateBusinessOrderRo();
        data.setSpaceId(mainUser.getSpaceId());
        data.setType(OrderType.BUY.name());
        data.setProduct(upgrade.name());
        data.setSeat(100);
        data.setMonths(1);
        iBillingOfflineService.createBusinessOrder(mainUser.getUserId(), data);

        // check space entitlement
        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.ENTERPRISE, initialCreateDate.toLocalDate(),
                LocalDate.of(2023, 3, 3)));
        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.CAPACITY, initialCreateDate.toLocalDate(),
                LocalDate.of(2024, 2, 3)));
        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.CAPACITY, initialCreateDate.toLocalDate(),
                LocalDate.of(2024, 2, 3)));
        entitlementChecker.checkSubscription(mainUser.getSpaceId(), expectedSubscriptions);
        expectedSubscriptions.clear();

        // move clock to 2023-4-3, base subscription(enterprise 100 seat) expire
        getClock().addMonths(2);
        log.info("mock clock utc now: {}", getClock().getUTCNow());

        // check entitlement
        entitlementChecker.checkSpaceEntitlement(mainUser.getSpaceId(),
            new ExpectedSpaceEntitlementCheck(ProductEnum.BRONZE.getName(), null,
                getFreePlan(ProductChannel.VIKA),
                CollectionUtil.newArrayList(getBillingConfig().getPlans().get("capacity_300_MB"),
                    getBillingConfig().getPlans().get("capacity_300_MB"))));
        expectedSubscriptions.clear();

        // init invited user again
        MockUserSpace invitedUserThree = createSingleUserAndSpace();
        MockUserSpace invitedUserFour = createSingleUserAndSpace();
        List<MockUserSpace> inviteUserAfterBuy =
            CollectionUtil.newArrayList(invitedUserThree, invitedUserFour);

        // reward two 300MB add-on subscription again
        for (MockUserSpace mockUserSpace : inviteUserAfterBuy) {
            sleep(1000);
            iSpaceSubscriptionService.createAddOnWithGiftCapacity(mockUserSpace.getUserId(),
                "inviteAfter", mainUser.getSpaceId());
        }

        // check space entitlement
        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.BRONZE, LocalDate.of(2023, 4, 3),
                LocalDate.of(2024, 4, 3)));
        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.CAPACITY, LocalDate.of(2023, 2, 3),
                LocalDate.of(2024, 2, 3)));
        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.CAPACITY, LocalDate.of(2023, 2, 3),
                LocalDate.of(2024, 2, 3)));
        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.CAPACITY, LocalDate.of(2023, 4, 3),
                LocalDate.of(2024, 4, 3)));
        expectedSubscriptions.add(
            new ExpectedSubscriptionCheck(ProductEnum.CAPACITY, LocalDate.of(2023, 4, 3),
                LocalDate.of(2024, 4, 3)));
        entitlementChecker.checkSubscription(mainUser.getSpaceId(), expectedSubscriptions);
    }
}
