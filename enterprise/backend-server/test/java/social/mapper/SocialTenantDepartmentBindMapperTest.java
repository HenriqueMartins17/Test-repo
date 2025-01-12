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
import com.apitable.enterprise.social.entity.SocialTenantDepartmentBindEntity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * <p>
 *     Data access layer test: third-party platform integration - enterprise tenant department association table test
 * </p>
 */
public class SocialTenantDepartmentBindMapperTest extends AbstractMyBatisMapperTest {

    @Autowired
    SocialTenantDepartmentBindMapper socialTenantDepartmentBindMapper;

    @Test
    @Sql("/enterprise/sql/social-tenant-department-bind-data.sql")
    void testSelectTeamIdByTenantDepartmentId() {
        Long id = socialTenantDepartmentBindMapper.selectTeamIdByTenantDepartmentId("spc41", "ww41", "di41");
        assertThat(id).isEqualTo(41L);
    }

    @Test
    @Sql("/enterprise/sql/social-tenant-department-bind-data.sql")
    void testSelectTeamIdsByTenantDepartmentId() {
        List<Long> ids = socialTenantDepartmentBindMapper.selectTeamIdsByTenantDepartmentId("spc41", "ww41", CollUtil.newArrayList("di41"));
        assertThat(ids).isNotEmpty();
    }

    @Test
    @Sql("/enterprise/sql/social-tenant-department-bind-data.sql")
    void testSelectByTenantId() {
        List<SocialTenantDepartmentBindEntity> entities = socialTenantDepartmentBindMapper.selectByTenantId("ww41", "spc41");
        assertThat(entities).isNotEmpty();
    }

    @Test
    @Sql("/enterprise/sql/social-tenant-department-bind-data.sql")
    void testSelectSpaceTeamIdByTenantIdAndDepartmentId() {
        Long id = socialTenantDepartmentBindMapper.selectSpaceTeamIdByTenantIdAndDepartmentId("spc41", "ww41", "di41");
        assertThat(id).isEqualTo(41L);
    }

    @Test
    @Sql("/enterprise/sql/social-tenant-department-bind-data.sql")
    void testSelectSpaceTeamIdsByTenantIdAndDepartmentId() {
        List<Long> ids = socialTenantDepartmentBindMapper.selectSpaceTeamIdsByTenantIdAndDepartmentId("spc41", "ww41", CollUtil.newArrayList("di41"));
        assertThat(ids).isNotEmpty();
    }

}
