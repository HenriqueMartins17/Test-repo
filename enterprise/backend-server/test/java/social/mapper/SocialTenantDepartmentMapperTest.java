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

import com.apitable.AbstractMyBatisMapperTest;
import com.apitable.enterprise.social.entity.SocialTenantDepartmentEntity;
import com.apitable.enterprise.social.model.TenantDepartmentBindDTO;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

/**
 * <p>
 * Data layer access test: third-party platform integration
 * enterprise tenant department table test
 * </p>
 */
public class SocialTenantDepartmentMapperTest extends AbstractMyBatisMapperTest {

    @Autowired
    SocialTenantDepartmentMapper socialTenantDepartmentMapper;

    @Test
    @Sql("/enterprise/sql/social-tenant-department-data.sql")
    void testSelectIdByDepartmentId() {
        Long id = socialTenantDepartmentMapper.selectIdByDepartmentId("spc41", "ww41", "di41");
        assertThat(id).isEqualTo(41);
    }

    @Test
    @Sql("/enterprise/sql/social-tenant-department-data.sql")
    void testSelectByDepartmentId() {
        SocialTenantDepartmentEntity entity =
            socialTenantDepartmentMapper.selectByDepartmentId("spc41", "ww41", "di41");
        assertThat(entity).isNotNull();
    }

    @Test
    @Sql("/enterprise/sql/social-tenant-department-data.sql")
    void testSelectDepartmentIdsByTenantId() {
        List<String> ids =
            socialTenantDepartmentMapper.selectDepartmentIdsByTenantId("ww41", "spc41");
        assertThat(ids).isNotEmpty();
    }

    @Test
    @Sql("/enterprise/sql/social-tenant-department-data.sql")
    void testSelectByTenantIdAndDeptId() {
        SocialTenantDepartmentEntity entity =
            socialTenantDepartmentMapper.selectByTenantIdAndDeptId("spc41", "ww41", "odi41");
        assertThat(entity).isNotNull();
    }

    @Test
    @Sql({"/enterprise/sql/social-tenant-department-data.sql",
        "/enterprise/sql/social-tenant-department-bind-data.sql",
        "/sql/unit-team-data.sql"})
    void testSelectTenantBindTeamListBySpaceId() {
        List<TenantDepartmentBindDTO> entities =
            socialTenantDepartmentMapper.selectTenantBindTeamListBySpaceId("spc41");
        assertThat(entities).isNotEmpty();
    }

}
