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

import static org.assertj.core.api.Assertions.assertThat;

import cn.hutool.core.collection.CollUtil;
import com.apitable.AbstractMyBatisMapperTest;
import com.apitable.enterprise.social.entity.SocialTenantBindEntity;
import com.apitable.enterprise.social.enums.SocialAppType;
import com.apitable.enterprise.social.enums.SocialPlatformType;
import com.apitable.enterprise.social.model.SpaceBindTenantInfoDTO;
import com.apitable.enterprise.social.model.TenantBindDTO;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

/**
 * <p>
 * Data access layer test: third-party platform integration
 * enterprise tenant binding space table test
 * </p>
 */
public class SocialTenantBindMapperTest extends AbstractMyBatisMapperTest {

    @Autowired
    SocialTenantBindMapper socialTenantBindMapper;

    @Test
    @Sql("/enterprise/sql/social-tenant-bind-data.sql")
    void testSelectCountBySpaceId() {
        Integer count = socialTenantBindMapper.selectCountBySpaceId("spc41");
        assertThat(count).isEqualTo(1);
    }

    @Test
    @Sql("/enterprise/sql/social-tenant-bind-data.sql")
    void testSelectCountByTenantId() {
        Integer count = socialTenantBindMapper.selectCountByTenantId("ww41");
        assertThat(count).isEqualTo(1);
    }

    @Test
    @Sql("/enterprise/sql/social-tenant-bind-data.sql")
    void testSelectTenantIdBySpaceId() {
        List<String> ids = socialTenantBindMapper.selectTenantIdBySpaceId("spc41");
        assertThat(ids).isNotEmpty();
    }

    @Test
    @Sql("/enterprise/sql/social-tenant-bind-data.sql")
    void testSelectSpaceIdByTenantId() {
        List<String> ids = socialTenantBindMapper.selectSpaceIdByTenantId("ww41");
        assertThat(ids).isNotEmpty();
    }

    @Test
    @Sql("/enterprise/sql/social-tenant-bind-data.sql")
    void testSelectSpaceIdsByTenantIdAndAppId() {
        List<String> ids = socialTenantBindMapper.selectSpaceIdsByTenantIdAndAppId("ww41", "ai41");
        assertThat(ids).isNotEmpty();
    }

    @Test
    @Sql({"/enterprise/sql/social-tenant-bind-data.sql", "/enterprise/sql/social-tenant-data.sql"})
    void testSelectBaseInfoBySpaceId() {
        TenantBindDTO entity = socialTenantBindMapper.selectBaseInfoBySpaceId("spc41");
        assertThat(entity).isNotNull();
    }

    @Test
    @Sql("/enterprise/sql/social-tenant-bind-data.sql")
    void testSelectCountByTenantIdAndAppId() {
        Integer count = socialTenantBindMapper.selectCountByTenantIdAndAppId("ww41", "ai41");
        assertThat(count).isEqualTo(1);
    }

    @Test
    @Sql("/enterprise/sql/social-tenant-bind-data.sql")
    void testSelectSpaceIdByTenantIdAndAppId() {
        String id = socialTenantBindMapper.selectSpaceIdByTenantIdAndAppId("ww41", "ai41");
        assertThat(id).isEqualTo("spc41");
    }

    @Test
    @Sql({"/enterprise/sql/social-tenant-bind-data.sql", "/enterprise/sql/social-tenant-data.sql"})
    void testSelectCountBySpaceIdAndPlatform() {
        Integer count = socialTenantBindMapper.selectCountBySpaceIdAndPlatform("spc41", 2);
        assertThat(count).isEqualTo(1);
    }

    @Test
    @Sql({"/enterprise/sql/social-tenant-bind-data.sql", "/enterprise/sql/social-tenant-data.sql"})
    void testSelectSpaceBindTenantInfoByPlatform() {
        SpaceBindTenantInfoDTO entity =
            socialTenantBindMapper.selectSpaceBindTenantInfoByPlatform("spc41", 2);
        assertThat(entity).isNotNull();
    }

    @Test
    @Sql({"/enterprise/sql/social-tenant-bind-data.sql", "/enterprise/sql/social-tenant-data.sql"})
    void testSelectSpaceIdByPlatformTypeAndAppType() {
        List<String> ids = socialTenantBindMapper.selectSpaceIdByPlatformTypeAndAppType(
            SocialPlatformType.DINGTALK, SocialAppType.INTERNAL);
        assertThat(ids).isNotEmpty();
    }

    @Test
    @Sql("/enterprise/sql/social-tenant-bind-data.sql")
    void testSelectBySpaceIdAndTenantId() {
        List<SocialTenantBindEntity> entities =
            socialTenantBindMapper.selectBySpaceIdAndTenantId("spc41", "ww41");
        assertThat(entities).isNotEmpty();
    }

    @Test
    @Sql("/enterprise/sql/social-tenant-bind-data.sql")
    void testSelectBySpaceId() {
        SocialTenantBindEntity entity = socialTenantBindMapper.selectBySpaceId("spc41");
        assertThat(entity).isNotNull();
    }

    @Test
    @Sql("/enterprise/sql/social-tenant-bind-data.sql")
    void selectAllSpaceIdsByAppIdTest() {
        List<String> spaceIds = socialTenantBindMapper.selectAllSpaceIdsByAppId("ai41");
        Assertions.assertTrue(CollUtil.isNotEmpty(spaceIds));
    }

}
