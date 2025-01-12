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

package com.apitable.enterprise.social.mapper;

import cn.hutool.core.collection.CollUtil;
import com.apitable.AbstractMyBatisMapperTest;
import com.apitable.enterprise.social.entity.SocialWecomPermitOrderEntity;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

/**
 * <p>
 * WeCom Service Provider Interface License Ordering Information
 * </p>
 */
class SocialWecomPermitOrderMapperTests extends AbstractMyBatisMapperTest {

    @Autowired
    private SocialWecomPermitOrderMapper socialWecomPermitOrderMapper;

    @Test
    @Sql("/enterprise/sql/social-wecom-permit-order-data.sql")
    void selectByOrderIdTest() {
        SocialWecomPermitOrderEntity orderEntity =
            socialWecomPermitOrderMapper.selectByOrderId("order123xxx");
        Assertions.assertNotNull(orderEntity);
    }

    @Test
    @Sql("/enterprise/sql/social-wecom-permit-order-data.sql")
    void selectByOrderStatusesTest() {
        List<SocialWecomPermitOrderEntity> orderEntities = socialWecomPermitOrderMapper
            .selectByOrderStatuses("wwxxx123", "wwcorpx123123", Collections.singletonList(0));
        Assertions.assertTrue(CollUtil.isNotEmpty(orderEntities));
    }

}
