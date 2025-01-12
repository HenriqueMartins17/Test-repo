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

package com.apitable.enterprise.appstore.mapper;

import org.junit.jupiter.api.Test;

import com.apitable.AbstractMyBatisMapperTest;
import com.apitable.enterprise.appstore.entity.MarketplaceSpaceAppRelEntity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * <p>
 * Marketplace Space App Mapper Test
 * </p>
 */
public class MarketplaceSpaceAppMapperTest extends AbstractMyBatisMapperTest {

    @Autowired
    MarketplaceSpaceAppMapper marketPlaceSpaceAppMapper;

    @Test
    @Sql("/enterprise/sql/marketplace-space-app-rel-data.sql")
    void testSelectBySpaceIdAndAppId() {
        MarketplaceSpaceAppRelEntity entity = marketPlaceSpaceAppMapper.selectBySpaceIdAndAppId("spczdmQDfBAn5", "ina5645957505507647");
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(1385543224262451201L);
    }

}
