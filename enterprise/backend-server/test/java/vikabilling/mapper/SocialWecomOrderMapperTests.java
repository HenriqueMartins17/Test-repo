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

import cn.hutool.core.collection.CollUtil;
import com.apitable.AbstractMyBatisMapperTest;
import com.apitable.enterprise.vikabilling.entity.SocialWecomOrderEntity;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

/**
 * <p>
 * Subscription Billing System - Wecom Order Mapper Test
 * </p>
 */
class SocialWecomOrderMapperTests extends AbstractMyBatisMapperTest {

    @Autowired
    private SocialWecomOrderMapper socialWecomOrderMapper;

    @Test
    @Sql("/enterprise/sql/social-wecom-order-data.sql")
    void selectAllOrdersTest() {
        // test without orderStatuses
        List<SocialWecomOrderEntity> orderEntities = socialWecomOrderMapper
            .selectAllOrders("testSuiteId", "testPaidCorpId", null);
        Assertions.assertTrue(CollUtil.isNotEmpty(orderEntities));
        // test with one orderStatus
        orderEntities = socialWecomOrderMapper
            .selectAllOrders("testSuiteId", "testPaidCorpId", Collections.singletonList(1));
        Assertions.assertTrue(CollUtil.isNotEmpty(orderEntities));
        // test with one more orderStatuses
        orderEntities = socialWecomOrderMapper
            .selectAllOrders("testSuiteId", "testPaidCorpId", Arrays.asList(0, 1));
        Assertions.assertTrue(CollUtil.isNotEmpty(orderEntities));
    }

    @Test
    @Sql("/enterprise/sql/social-wecom-order-data.sql")
    void selectByOrderIdTest() {
        // test not existed
        SocialWecomOrderEntity orderEntity = socialWecomOrderMapper.selectByOrderId("testOrderId");
        Assertions.assertNull(orderEntity);
        // test existed
        orderEntity = socialWecomOrderMapper.selectByOrderId("testOrderId1");
        Assertions.assertNotNull(orderEntity);
    }

    @Test
    @Sql("/enterprise/sql/social-wecom-order-data.sql")
    void selectFirstPaidOrderTest() {
        // test not existed
        SocialWecomOrderEntity orderEntity =
            socialWecomOrderMapper.selectFirstPaidOrder("testSuiteId", "testPaidCorpId1");
        Assertions.assertNull(orderEntity);
        // test existed
        orderEntity = socialWecomOrderMapper.selectFirstPaidOrder("testSuiteId", "testPaidCorpId");
        Assertions.assertNotNull(orderEntity);
    }

    @Test
    @Sql("/enterprise/sql/social-wecom-order-data.sql")
    void selectLastPaidOrderTest() {
        // test not existed
        SocialWecomOrderEntity orderEntity =
            socialWecomOrderMapper.selectLastPaidOrder("testSuiteId", "testPaidCorpId1");
        Assertions.assertNull(orderEntity);
        // test existed
        orderEntity = socialWecomOrderMapper.selectLastPaidOrder("testSuiteId", "testPaidCorpId");
        Assertions.assertNotNull(orderEntity);
    }

}
