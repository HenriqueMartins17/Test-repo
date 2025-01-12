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

import java.util.List;

import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.apitable.AbstractMyBatisMapperTest;
import com.apitable.enterprise.appstore.entity.AppInstanceEntity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

/**
 * Data access layer test: application instance table test
 */
public class AppInstanceMapperTest extends AbstractMyBatisMapperTest {

    @Autowired
    private AppInstanceMapper appInstanceMapper;

    @Test
    @Sql("/enterprise/sql/appinstance-data.sql")
    void testSelectBySpaceIdAndAppId() {
        AppInstanceEntity appInstanceEntity = appInstanceMapper.selectBySpaceIdAndAppId("spc2", "app-ea1d17fd8a1449b3ac1a073077d385a5");
        Assertions.assertThat(appInstanceEntity).isNotNull();
    }

    @Test
    @Sql("/enterprise/sql/appinstance-data.sql")
    void testSelectBySpaceId() {
        List<AppInstanceEntity> appInstanceEntities = appInstanceMapper.selectBySpaceId("spc2");
        Assertions.assertThat(appInstanceEntities).isNotEmpty();
    }

    @Test
    @Sql("/enterprise/sql/appinstance-data.sql")
    void testSelectByAppInstanceId() {
        AppInstanceEntity appInstanceEntity = appInstanceMapper.selectByAppInstanceId("ai-948e4945130348f09c1f92f57d0bf655");
        Assertions.assertThat(appInstanceEntity).isNotNull();
    }

    @Test
    @Sql("/enterprise/sql/appinstance-data.sql")
    void testDeleteByAppInstanceId() {
        boolean deleteFlag = SqlHelper.retBool(appInstanceMapper.deleteByAppInstanceId("ai-948e4945130348f09c1f92f57d0bf655"));
        Assertions.assertThat(deleteFlag).isTrue();
    }

    @Test
    @Sql("/enterprise/sql/appinstance-data.sql")
    void testSelectCountByAppKey() {
        Integer count = appInstanceMapper.selectCountByAppKey("cli_a13c40d6a278d00c");
        Assertions.assertThat(count).isNotNull()
                .isEqualTo(1);
    }

    @Test
    @Sql("/enterprise/sql/appinstance-data.sql")
    void testSelectCountByAppInstanceIdAndAppKey() {
        Integer count = appInstanceMapper.selectCountByAppInstanceIdAndAppKey("ai-9d02328d04ef456c9097cfa2883b7cde", "cli_a13c40d6a278d00c");
        Assertions.assertThat(count).isNotNull()
                .isEqualTo(1);
    }
}
