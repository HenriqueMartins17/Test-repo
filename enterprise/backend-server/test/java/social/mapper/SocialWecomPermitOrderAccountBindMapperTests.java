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
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

/**
 * <p>
 * WeCom service provider interface license account binding information
 * </p>
 */
class SocialWecomPermitOrderAccountBindMapperTests extends AbstractMyBatisMapperTest {

    @Autowired
    private SocialWecomPermitOrderAccountBindMapper socialWecomPermitOrderAccountBindMapper;

    @Test
    @Sql("/enterprise/sql/social-wecom-permit-order-account-bind-data.sql")
    void selectActiveCodesByOrderIdTest() {
        List<String> activeCodes = socialWecomPermitOrderAccountBindMapper
            .selectActiveCodesByOrderId("order123xxx");
        Assertions.assertTrue(CollUtil.isNotEmpty(activeCodes));
    }

    @Test
    @Sql("/enterprise/sql/social-wecom-permit-order-account-bind-data.sql")
    void selectCountByOrderIdTest() {
        int count = socialWecomPermitOrderAccountBindMapper.selectCountByOrderId("order123xxx");
        Assertions.assertNotEquals(0, count);
    }

}
