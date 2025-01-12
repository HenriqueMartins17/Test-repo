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

import cn.hutool.core.collection.CollUtil;
import org.junit.jupiter.api.Test;

import com.apitable.AbstractMyBatisMapperTest;
import com.apitable.enterprise.social.entity.SocialTenantEntity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * <p>
 *     Data access layer test: third-party platform integration - enterprise tenant table test
 * </p>
 */
public class SocialTenantMapperTest extends AbstractMyBatisMapperTest {

    @Autowired
    SocialTenantMapper socialTenantMapper;

    @Test
    @Sql("/enterprise/sql/social-tenant-data.sql")
    void testSelectByAppIdAndTenantId() {
        SocialTenantEntity entity = socialTenantMapper.selectByAppIdAndTenantId("ai41", "ww41");
        assertThat(entity).isNotNull();
    }

    @Test
    @Sql("/enterprise/sql/social-tenant-data.sql")
    void testSelectCountByAppIdAndTenantId() {
        Integer count = socialTenantMapper.selectCountByAppIdAndTenantId("ai41", "ww41");
        assertThat(count).isEqualTo(1);
    }

    @Test
    @Sql("/enterprise/sql/social-tenant-data.sql")
    void testSelectCountByTenantId() {
        Integer count = socialTenantMapper.selectCountByTenantId("ww41");
        assertThat(count).isEqualTo(1);
    }

    @Test
    @Sql("/enterprise/sql/social-tenant-data.sql")
    void testSelectAgentIdByTenantIdAndAppId() {
        String agentId = socialTenantMapper.selectAgentIdByTenantIdAndAppId("ww41", "ai41");
        assertThat(agentId).isEqualTo("41");
    }
    

    @Test
    @Sql("/enterprise/sql/social-tenant-data.sql")
    void testSelectTenantStatusByTenantIdAndAppId() {
        Integer status = socialTenantMapper.selectTenantStatusByTenantIdAndAppId("ww41", "ai41");
        assertThat(status).isEqualTo(1);
    }

    @Test
    @Sql("/enterprise/sql/social-tenant-data.sql")
    void testSelectIsvAgentIdByTenantIdAndAppId() {
        String agentId = socialTenantMapper.selectIsvAgentIdByTenantIdAndAppId("ww45", "ai45");
        assertThat(agentId).isEqualTo("[\"45\"]");
    }

    @Test
    @Sql("/enterprise/sql/social-tenant-data.sql")
    void testSelectByTenantIds() {
        List<SocialTenantEntity> entities = socialTenantMapper.selectByTenantIds(CollUtil.newArrayList("ww41"));
        assertThat(entities).isNotEmpty();
    }


}
