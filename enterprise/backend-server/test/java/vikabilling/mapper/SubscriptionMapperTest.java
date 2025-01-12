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

package com.apitable.enterprise.vikabilling.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.apitable.AbstractMyBatisMapperTest;
import com.apitable.enterprise.vikabilling.enums.ProductCategory;
import com.apitable.enterprise.vikabilling.enums.SubscriptionState;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

/**
 * <p>
 * Subscription Billing System - Subscription Mapper Test
 * </p>
 */
public class SubscriptionMapperTest extends AbstractMyBatisMapperTest {

    @Autowired
    private SubscriptionMapper subscriptionMapper;

    @Test
    @Sql("/enterprise/sql/billing-subscription-data.sql")
    public void testSelectUnExpireCapacityBySpaceId() {
        String spaceId = "spcSueRmAkuPP";
        assertThat(subscriptionMapper.selectUnExpireCapacityBySpaceId(spaceId, new Page<>(),
            SubscriptionState.ACTIVATED)).isNotNull();
    }

    @Test
    @Sql("/enterprise/sql/billing-subscription-data.sql")
    public void testSelectExpireCapacityBySpaceId() {
        String spaceId = "spcSueRmAkuPP";
        assertThat(subscriptionMapper.selectExpireCapacityBySpaceId(spaceId, new Page<>())
            .getRecords()).isEmpty();
    }

    @Test
    @Sql("/enterprise/sql/billing-subscription-data.sql")
    public void testSelectUnExpireGiftCapacityBySpaceId() {
        String spaceId = "spcSueRmAkuPP";
        String planId = "capacity_300_MB";
        assertThat(subscriptionMapper.selectUnExpireGiftCapacityBySpaceId(spaceId, planId,
            SubscriptionState.ACTIVATED.name())).isNotNull();
    }

    @Test
    @Sql("/enterprise/sql/billing-subscription-data.sql")
    public void testSelectUnExpireBaseProductBySpaceId() {
        String spaceId = "spcSueRmAkuPP";
        assertThat(subscriptionMapper.selectUnExpireBaseProductBySpaceId(spaceId,
            SubscriptionState.ACTIVATED, ProductCategory.BASE)).isEqualTo(0);
    }
}
