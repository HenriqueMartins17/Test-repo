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

import static org.assertj.core.api.Assertions.assertThat;

import com.apitable.enterprise.AbstractVikaSaasIntegrationTest;
import com.apitable.enterprise.vikabilling.core.DefaultOrderArguments;
import com.apitable.enterprise.vikabilling.core.OrderArguments;
import com.apitable.enterprise.vikabilling.enums.ProductEnum;
import com.apitable.enterprise.vikabilling.setting.Price;
import com.apitable.enterprise.vikabilling.util.BillingConfigManager;
import com.apitable.enterprise.vikabilling.util.EntitlementChecker.ExpectedSubscriptionCheck;
import com.apitable.enterprise.vikabilling.util.model.BillingPlanPrice;
import com.apitable.mock.bean.MockUserSpace;
import com.apitable.shared.clock.spring.ClockManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderV2ServiceImplTest extends AbstractVikaSaasIntegrationTest {

    protected static final Logger log = LoggerFactory.getLogger(OrderV2ServiceImplTest.class);

    @Test
    public void testCreateOrderOnNewBuySenseWithTargetDate() {
        log.info("initial date on 2019-2-1 19:10:30 +08:00");
        final OffsetDateTime initialCreateDate = OffsetDateTime.of(2019, 2, 2, 19, 10, 30, 0, getTestTimeZone());
        getClock().setTime(initialCreateDate);

        log.info("initial user and space");
        final MockUserSpace mockUserSpace = createSingleUserAndSpace();
        // chose product to buy
        Price silverPrice = BillingConfigManager.getPriceBySeatAndMonths(ProductEnum.SILVER, 100, 12);
        assertThat(silverPrice).isNotNull();
        final OrderArguments orderArguments = new DefaultOrderArguments(mockUserSpace.getSpaceId(), silverPrice);
        // paid Time
        final OffsetDateTime paidTime = initialCreateDate.plusMinutes(5);
        autoOrderPayProcessor(mockUserSpace.getUserId(), orderArguments, paidTime);

        final LocalDate shouldExpireDate = LocalDate.of(2020, 2, 2);

        // check space entitlement
        entitlementChecker.checkSubscription(mockUserSpace.getSpaceId(),
                new ExpectedSubscriptionCheck(ProductEnum.SILVER, initialCreateDate.toLocalDate(), shouldExpireDate));

        // move clock to 2020-2-2, buy again should be renewal sense
        final OffsetDateTime ajTime = OffsetDateTime.of(2020, 2, 2, 20, 10, 30, 0, getTestTimeZone());
        getClock().setTime(ajTime);
        Price buyAgainPrice = BillingConfigManager.getPriceBySeatAndMonths(ProductEnum.SILVER, 100, 12);
        assertThat(buyAgainPrice).isNotNull();
        final OrderArguments buyAgainOrderArgs = new DefaultOrderArguments(mockUserSpace.getSpaceId(), buyAgainPrice);
        // paid Time
        final OffsetDateTime sendPaidTime = ajTime.plusMinutes(5);
        autoOrderPayProcessor(mockUserSpace.getUserId(), buyAgainOrderArgs, sendPaidTime);

        final LocalDate nowShouldExpireDate = LocalDate.of(2021, 2, 2);

        // check space entitlement
        entitlementChecker.checkSubscription(mockUserSpace.getSpaceId(),
                new ExpectedSubscriptionCheck(ProductEnum.SILVER, initialCreateDate.toLocalDate(), nowShouldExpireDate));
    }

    @Test
    public void testCreateOrderOnRenewSenseWithTargetDate() {
        log.info("initial date on 2019-2-1 00:00:00 +08:00");
        final OffsetDateTime initialCreateDate = OffsetDateTime.of(2019, 2, 1, 0, 0, 0, 0, getTestTimeZone());
        getClock().setTime(initialCreateDate);

        log.info("initial user and space");
        final MockUserSpace mockUserSpace = createSingleUserAndSpace();
        // chose product to buy
        Price silverPrice = BillingConfigManager.getPriceBySeatAndMonths(ProductEnum.SILVER, 100, 12);
        assertThat(silverPrice).isNotNull();
        final OrderArguments orderArguments = new DefaultOrderArguments(mockUserSpace.getSpaceId(), silverPrice);
        // paid Time
        final OffsetDateTime paidTime = initialCreateDate.plusMinutes(5);
        autoOrderPayProcessor(mockUserSpace.getUserId(), orderArguments, paidTime);

        final LocalDate shouldExpireDate = LocalDate.of(2020, 2, 1);
        entitlementChecker.checkSubscription(mockUserSpace.getSpaceId(),
                new ExpectedSubscriptionCheck(ProductEnum.SILVER, initialCreateDate.toLocalDate(), shouldExpireDate));

        // add 100 days in the future
        getClock().addDays(100);
        final OffsetDateTime renewOrderCreatedTime = getClock().getNow(getTestTimeZone());

        // assert renew order data is right
        final OffsetDateTime renewPaidTime = renewOrderCreatedTime.plusMinutes(5);
        autoOrderPayProcessor(mockUserSpace.getUserId(), orderArguments, renewPaidTime);

        final LocalDate nowShouldExpireDate = LocalDate.of(2021, 2, 1);
        entitlementChecker.checkSubscription(mockUserSpace.getSpaceId(),
                new ExpectedSubscriptionCheck(ProductEnum.SILVER, initialCreateDate.toLocalDate(), nowShouldExpireDate));
    }

    @Test
    public void testCreateOrderOnUpgradeSenseOnSameDay() {
        final OffsetDateTime initialCreateDate = OffsetDateTime.of(2022, 6, 9, 2, 0, 0, 0, getTestTimeZone());
        // Set clock to the initial start date - we implicitly assume here that the timezone is UTC
        getClock().setTime(initialCreateDate);

        log.info("initial user and space");
        final MockUserSpace mockUserSpace = createSingleUserAndSpace();
        // choose silver as first, we assume this plan paid amount is 4888
        Price silverPrice = BillingConfigManager.getPriceBySeatAndMonths(ProductEnum.SILVER, 100, 6);
        assertThat(silverPrice).isNotNull();
        BillingPlanPrice planPrice = BillingPlanPrice.of(silverPrice, ClockManager.me().getLocalDateNow());
        BigDecimal amount = planPrice.getActual();
        log.info("current plan amount is {}", amount);

        final OrderArguments orderArguments = new DefaultOrderArguments(mockUserSpace.getSpaceId(), silverPrice);
        // paid Time
        final OffsetDateTime paidTime = initialCreateDate.plusMinutes(1);
        autoOrderPayProcessor(mockUserSpace.getUserId(), orderArguments, paidTime);

        final LocalDate shouldExpireDate = LocalDate.of(2022, 12, 9);
        entitlementChecker.checkSubscription(mockUserSpace.getSpaceId(),
                new ExpectedSubscriptionCheck(ProductEnum.SILVER, initialCreateDate.toLocalDate(), shouldExpireDate));

        // now we take to operate upgrade request, we assume this plan
        Price upgradePrice = BillingConfigManager.getPriceBySeatAndMonths(ProductEnum.GOLD, 200, 6);
        assertThat(upgradePrice).isNotNull();
        final OrderArguments upgradeOrderArguments = new DefaultOrderArguments(mockUserSpace.getSpaceId(), upgradePrice);

        // assert upgrade order paid time
        final OffsetDateTime upgradePaidTime = initialCreateDate.plusMinutes(10);
        autoOrderPayProcessor(mockUserSpace.getUserId(), upgradeOrderArguments, upgradePaidTime);

        // the pro-rate piece
        entitlementChecker.checkSubscription(mockUserSpace.getSpaceId(),
                new ExpectedSubscriptionCheck(ProductEnum.GOLD, initialCreateDate.toLocalDate(), shouldExpireDate));
    }

    @Test
    public void testCreateOrderOnSimpleUpgradeSenseWithTargetDate() {
        log.info("initial date on 2019-4-1 00:00:00 +08:00");
        // We take april as it has 30 days (easier to play)
        final OffsetDateTime initialCreateDate = OffsetDateTime.of(2019, 4, 1, 0, 0, 0, 0, getTestTimeZone());
        // Set clock to the initial start date - we implicitly assume here that the timezone is UTC
        getClock().setTime(initialCreateDate);

        log.info("initial user and space");
        final MockUserSpace mockUserSpace = createSingleUserAndSpace();
        // chose product to buy
        ProductEnum toBuy = ProductEnum.SILVER;
        // choose silver as first, we assume this plan paid amount is 4888
        Price silverPrice = BillingConfigManager.getPriceBySeatAndMonths(toBuy, 100, 12);
        assertThat(silverPrice).isNotNull();
        BillingPlanPrice planPrice = BillingPlanPrice.of(silverPrice, ClockManager.me().getLocalDateNow());
        BigDecimal amount = planPrice.getActual();
        log.info("current plan amount is {}", amount);

        final OrderArguments orderArguments = new DefaultOrderArguments(mockUserSpace.getSpaceId(), silverPrice);
        // paid Time
        final OffsetDateTime paidTime = initialCreateDate.plusMinutes(5);
        autoOrderPayProcessor(mockUserSpace.getUserId(), orderArguments, paidTime);

        final LocalDate shouldExpireDate = LocalDate.of(2020, 4, 1);
        entitlementChecker.checkSubscription(mockUserSpace.getSpaceId(),
                new ExpectedSubscriptionCheck(toBuy, initialCreateDate.toLocalDate(), shouldExpireDate));

        // move clock to 2019-5-2
        getClock().addDays(31);
        final OffsetDateTime upgradeDate = getClock().getNow(getTestTimeZone());

        // upgrade product
        ProductEnum toUpgrade = ProductEnum.GOLD;
        // now we take to operate upgrade request, we assume this plan is 38888
        Price goldPrice = BillingConfigManager.getPriceBySeatAndMonths(toUpgrade, 200, 12);
        assertThat(goldPrice).isNotNull();
        final OrderArguments upgradeOrderArguments = new DefaultOrderArguments(mockUserSpace.getSpaceId(), goldPrice);

        // assert upgrade order paid time
        final OffsetDateTime upgradePaidTime = upgradeDate.plusMinutes(5);
        autoOrderPayProcessor(mockUserSpace.getUserId(), upgradeOrderArguments, upgradePaidTime);

        // the pro-rate piece
        final LocalDate nowShouldExpireDate = LocalDate.of(2020, 5, 2);
        entitlementChecker.checkSubscription(mockUserSpace.getSpaceId(),
                new ExpectedSubscriptionCheck(toUpgrade, upgradeDate.toLocalDate(), nowShouldExpireDate));
    }

    @Test
    public void testCreateOrderOnUpgradeSenseExpectProrated() {
        log.info("initial date on 2019-4-1 00:00:00 +08:00");
        // We take april as it has 30 days (easier to play)
        final OffsetDateTime initialCreateDate = OffsetDateTime.of(2019, 4, 1, 0, 0, 0, 0, getTestTimeZone());
        // Set clock to the initial start date - we implicitly assume here that the timezone is UTC
        getClock().setTime(initialCreateDate);

        log.info("initial user and space");
        final MockUserSpace mockUserSpace = createSingleUserAndSpace();
        // chose product to buy
        ProductEnum toBuy = ProductEnum.SILVER;
        // choose silver as first, we assume this plan paid amount is 288
        Price silverPrice = BillingConfigManager.getPriceBySeatAndMonths(toBuy, 100, 1);
        assertThat(silverPrice).isNotNull();
        BillingPlanPrice planPrice = BillingPlanPrice.of(silverPrice, ClockManager.me().getLocalDateNow());
        BigDecimal amount = planPrice.getActual();
        log.info("current plan amount is {}", amount);

        final OrderArguments orderArguments = new DefaultOrderArguments(mockUserSpace.getSpaceId(), silverPrice);
        // paid Time
        final OffsetDateTime paidTime = initialCreateDate.plusMinutes(5);
        autoOrderPayProcessor(mockUserSpace.getUserId(), orderArguments, paidTime);

        final LocalDate shouldExpireDate = LocalDate.of(2019, 5, 1);
        entitlementChecker.checkSubscription(mockUserSpace.getSpaceId(),
                new ExpectedSubscriptionCheck(toBuy, initialCreateDate.toLocalDate(), shouldExpireDate));

        // move clock to 2019-4-20
        getClock().addDays(20);
        final OffsetDateTime upgradeDate = getClock().getNow(getTestTimeZone());

        // now we take to operate upgrade request, we assume this plan is 588
        Price upgradePrice = BillingConfigManager.getPriceBySeatAndMonths(ProductEnum.GOLD, 200, 1);
        assertThat(upgradePrice).isNotNull();
        final OrderArguments upgradeOrderArguments = new DefaultOrderArguments(mockUserSpace.getSpaceId(), upgradePrice);

        // assert upgrade order paid time
        final OffsetDateTime upgradePaidTime = upgradeDate.plusMinutes(5);
        autoOrderPayProcessor(mockUserSpace.getUserId(), upgradeOrderArguments, upgradePaidTime);

        final LocalDate nowShouldExpireDate = LocalDate.of(2019, 5, 21);
        entitlementChecker.checkSubscription(mockUserSpace.getSpaceId(),
                new ExpectedSubscriptionCheck(ProductEnum.GOLD, upgradeDate.toLocalDate(), nowShouldExpireDate));
    }

    @Test
    public void testCreateOrderOnUpgradeComplexSense() {
        // New Purchase-Upgrade-Upgrade
        log.info("initial date on 2019-4-1 00:00:00 +08:00");
        // We take april as it has 30 days (easier to play)
        final OffsetDateTime initialCreateDate = OffsetDateTime.of(2019, 4, 1, 0, 0, 0, 0, getTestTimeZone());
        // Set clock to the initial start date - we implicitly assume here that the timezone is UTC
        getClock().setTime(initialCreateDate);

        log.info("initial user and space");
        final MockUserSpace mockUserSpace = createSingleUserAndSpace();
        // choose silver as first, we assume this plan paid amount is 288
        Price silverPrice = BillingConfigManager.getPriceBySeatAndMonths(ProductEnum.SILVER, 100, 1);
        assertThat(silverPrice).isNotNull();
        BillingPlanPrice planPrice = BillingPlanPrice.of(silverPrice, ClockManager.me().getLocalDateNow());
        BigDecimal amount = planPrice.getActual();
        log.info("current plan amount is {}", amount);

        final OrderArguments orderArguments = new DefaultOrderArguments(mockUserSpace.getSpaceId(), silverPrice);
        // paid Time
        final OffsetDateTime paidTime = initialCreateDate.plusMinutes(5);
        autoOrderPayProcessor(mockUserSpace.getUserId(), orderArguments, paidTime);

        final LocalDate shouldExpireDate = LocalDate.of(2019, 5, 1);
        entitlementChecker.checkSubscription(mockUserSpace.getSpaceId(),
                new ExpectedSubscriptionCheck(ProductEnum.SILVER, initialCreateDate.toLocalDate(), shouldExpireDate));

        // move clock to 2019-4-21
        getClock().addDays(20);
        final OffsetDateTime upgradeDate = getClock().getNow(getTestTimeZone());

        // now we take to operate upgrade request, we assume this plan is 588
        Price upgradePrice = BillingConfigManager.getPriceBySeatAndMonths(ProductEnum.GOLD, 200, 1);
        assertThat(upgradePrice).isNotNull();
        final OrderArguments upgradeOrderArguments = new DefaultOrderArguments(mockUserSpace.getSpaceId(), upgradePrice);

        // assert upgrade order paid time
        final OffsetDateTime upgradePaidTime = upgradeDate.plusMinutes(5);
        autoOrderPayProcessor(mockUserSpace.getUserId(), upgradeOrderArguments, upgradePaidTime);

        final LocalDate nowShouldExpireDate = LocalDate.of(2019, 5, 21);
        entitlementChecker.checkSubscription(mockUserSpace.getSpaceId(),
                new ExpectedSubscriptionCheck(ProductEnum.GOLD, upgradeDate.toLocalDate(), nowShouldExpireDate));

        // move clock to 2019-5-11
        // getClock().addDays(20);
        // final OffsetDateTime againUpgradeDate = getClock().getNow(getTestTimeZone());

        // now we take to operate upgrade request, we assume this plan is 888
        // Price upgradeAgainPrice = BillingConfigManager.getPriceBySeatAndMonths(toBuy, 100, 1);
        // assertThat(upgradeAgainPrice).isNotNull();
        // final OrderArguments upgradeAgainOrderArguments = new DefaultOrderArguments(mockUserSpace.getSpaceId(), upgradeAgainPrice);

        // assert upgrade order paid time
        // final OffsetDateTime upgradeAgainPaidTime = againUpgradeDate.plusMinutes(5);
        // autoOrderPayProcessor(mockUserSpace.getUserId(), upgradeAgainOrderArguments, upgradeAgainPaidTime);

        // final LocalDate nowAgainShouldExpireDate = LocalDate.of(2019, 6, 11);
        // entitlementChecker.checkSubscription(mockUserSpace.getSpaceId(),
        //         new ExpectedSubscriptionCheck(toBuy, againUpgradeDate.toLocalDate(), nowAgainShouldExpireDate));
    }
}
