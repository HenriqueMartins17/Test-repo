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
import com.apitable.enterprise.social.entity.SocialCpTenantUserEntity;
import com.apitable.enterprise.social.model.CpTenantUserDTO;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

/**
 * <p>
 * Data access layer test: third-party platform integration - enterprise WeCom tenant users and their related tables test
 * </p>
 */
public class SocialCpTenantUserMapperTest extends AbstractMyBatisMapperTest {

    @Autowired
    SocialCpTenantUserMapper socialCpTenantUserMapper;

    @Test
    @Sql("/enterprise/sql/social-cp-tenant-user-data.sql")
    void testSelectOpenIdsByTenantId() {
        List<CpTenantUserDTO> entities =
            socialCpTenantUserMapper.selectOpenIdsByTenantId("ww41", "ai41");
        assertThat(entities).isNotEmpty();
    }

    @Test
    @Sql("/enterprise/sql/social-cp-tenant-user-data.sql")
    void testSelectByTenantIdAndAppIdAndCpUserId() {
        SocialCpTenantUserEntity entity =
            socialCpTenantUserMapper.selectByTenantIdAndAppIdAndCpUserId("ww41", "ai41", "ui41");
        assertThat(entity).isNotNull();
    }

    @Test
    @Sql({"/enterprise/sql/social-cp-tenant-user-data.sql",
        "/enterprise/sql/social-cp-user-bind-data.sql"})
    void testSelectByTenantIdAndAppIdAndUserId() {
        SocialCpTenantUserEntity entity =
            socialCpTenantUserMapper.selectByTenantIdAndAppIdAndUserId("ww41", "ai41", 41L);
        assertThat(entity).isNotNull();
    }

    @Test
    @Sql("/enterprise/sql/social-cp-tenant-user-data.sql")
    void testSelectIdByTenantIdAndAppIdAndCpUserId() {
        Long id =
            socialCpTenantUserMapper.selectIdByTenantIdAndAppIdAndCpUserId("ww41", "ai41", "ui41");
        assertThat(id).isEqualTo(41L);
    }

}
