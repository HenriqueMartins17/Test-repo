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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

/**
 * <p>
 * Data access layer test: third-party platform integration - enterprise WeCom user binding table and related table test
 * </p>
 */
public class SocialCpUserBindMapperTest extends AbstractMyBatisMapperTest {

    @Autowired
    SocialCpUserBindMapper socialCpUserBindMapper;

    @Test
    @Sql("/enterprise/sql/social-cp-user-bind-data.sql")
    void testSelectUserIdByCpTenantUserId() {
        Long id = socialCpUserBindMapper.selectUserIdByCpTenantUserId(41L);
        assertThat(id).isEqualTo(41);
    }

    @Test
    @Sql({"/enterprise/sql/social-cp-tenant-user-data.sql",
        "/enterprise/sql/social-cp-user-bind-data.sql"})
    void testSelectUserIdByTenantIdAndCpUserId() {
        Long id = socialCpUserBindMapper.selectUserIdByTenantIdAndCpUserId("ww41", "ui41");
        assertThat(id).isEqualTo(41L);
    }

    @Test
    @Sql({"/enterprise/sql/social-cp-tenant-user-data.sql",
        "/enterprise/sql/social-cp-user-bind-data.sql"})
    void testSelectOpenIdByTenantIdAndUserId() {
        String cpUserId = socialCpUserBindMapper.selectOpenIdByTenantIdAndUserId("ww41", 41L);
        assertThat(cpUserId).isEqualTo("ui41");
    }

    @Test
    @Sql({"/enterprise/sql/social-cp-tenant-user-data.sql",
        "/enterprise/sql/social-cp-user-bind-data.sql"})
    void testCountTenantBindByUserId() {
        Long count = socialCpUserBindMapper.countTenantBindByUserId("ww41", 41L);
        assertThat(count).isEqualTo(1);
    }

    @Test
    @Sql({"/enterprise/sql/social-cp-user-bind-data.sql"})
    void testDeleteByUserId() {
        int res = socialCpUserBindMapper.deleteByUserId(41L);
        assertThat(res).isEqualTo(1);
    }

}
