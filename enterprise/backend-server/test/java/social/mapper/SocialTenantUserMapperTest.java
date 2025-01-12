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
import com.apitable.enterprise.social.enums.SocialPlatformType;
import com.apitable.enterprise.social.model.SocialTenantUserDTO;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

/**
 * <p>
 * Data access layer test: third-party platform integration
 * enterprise tenant user table test
 * </p>
 */
public class SocialTenantUserMapperTest extends AbstractMyBatisMapperTest {

    @Autowired
    SocialTenantUserMapper socialTenantUserMapper;

    @Test
    @Sql("/enterprise/sql/social-tenant-user-data.sql")
    void testSelectUnionIdsByTenantId() {
        List<String> ids = socialTenantUserMapper.selectUnionIdsByTenantId("ai41", "ww41");
        assertThat(ids).isNotEmpty();
    }

    @Test
    @Sql("/enterprise/sql/social-tenant-user-data.sql")
    void testSelectOpenIdsByTenantId() {
        List<String> ids = socialTenantUserMapper.selectOpenIdsByTenantId("ai41", "ww41");
        assertThat(ids).isNotEmpty();
    }

    @Test
    @Sql("/enterprise/sql/social-tenant-user-data.sql")
    void testSelectCountByTenantIdAndOpenId() {
        Integer count =
            socialTenantUserMapper.selectCountByTenantIdAndOpenId("ai41", "ww41", "oi41");
        assertThat(count).isEqualTo(1);
    }

    @Test
    @Sql("/enterprise/sql/social-tenant-user-data.sql")
    void testSelectUnionIdByOpenId() {
        String id = socialTenantUserMapper.selectUnionIdByOpenId("ai41", "ww41", "oi41");
        assertThat(id).isEqualTo("ui41");
    }

    @Test
    @Sql("/enterprise/sql/social-tenant-user-data.sql")
    void testSelectUnionIdsByOpenIds() {
        List<String> ids = socialTenantUserMapper.selectUnionIdsByOpenIds("ai41", "ww41",
            CollUtil.newArrayList("oi41"));
        assertThat(ids).isNotEmpty();
    }

    @Test
    @Sql("/enterprise/sql/social-tenant-user-data.sql")
    void testSelectOpenIdByAppIdAndTenantIdAndUnionIds() {
        List<String> ids =
            socialTenantUserMapper.selectOpenIdByAppIdAndTenantIdAndUnionIds("ai41", "ww41",
                CollUtil.newArrayList("ui41"));
        assertThat(ids).isNotEmpty();
    }

    @Test
    @Sql({"/enterprise/sql/social-tenant-user-data.sql", "/enterprise/sql/social-tenant-data.sql"})
    void testSelectOpenIdByUnionIdAndPlatform() {
        String id = socialTenantUserMapper.selectOpenIdByUnionIdAndPlatform("ui41",
            SocialPlatformType.DINGTALK);
        assertThat(id).isEqualTo("oi41");
    }

    @Test
    @Sql("/enterprise/sql/social-tenant-user-data.sql")
    void testSelectOpenIdAndUnionIdByTenantId() {
        List<SocialTenantUserDTO> entities =
            socialTenantUserMapper.selectOpenIdAndUnionIdByTenantId("ww41", "ai41");
        assertThat(entities).isNotEmpty();
    }

}
