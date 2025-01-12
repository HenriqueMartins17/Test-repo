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

import java.util.List;

import org.junit.jupiter.api.Test;

import com.apitable.AbstractMyBatisMapperTest;
import com.apitable.enterprise.social.enums.SocialPlatformType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * <p>
 *     Data access layer test: third-party platform integration - order table test
 * </p>
 */
public class SocialTenantOrderMapperTest extends AbstractMyBatisMapperTest {

    @Autowired
    SocialTenantOrderMapper socialTenantOrderMapper;

    @Test
    @Sql("/enterprise/sql/social-tenant-order-data.sql")
    void testSelectCountByChannelOrderId() {
        Integer count = socialTenantOrderMapper.selectCountByChannelOrderId("coi41", "ww41", "ai41", SocialPlatformType.DINGTALK);
        assertThat(count).isEqualTo(1);
    }

    @Test
    @Sql("/enterprise/sql/social-tenant-order-data.sql")
    void testSelectOrderDataByTenantIdAndAppId() {
        List<String> orderData = socialTenantOrderMapper.selectOrderDataByTenantIdAndAppId("ww41", "ai41", SocialPlatformType.DINGTALK);
        assertThat(orderData).isNotEmpty();
    }

}
