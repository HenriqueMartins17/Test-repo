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
import com.apitable.enterprise.social.entity.SocialUserBindEntity;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

/**
 * <p>
 * Data access layer test: third-party platform integration - user binding table test
 * </p>
 */
public class SocialUserBindMapperTest extends AbstractMyBatisMapperTest {

    @Autowired
    SocialUserBindMapper socialUserBindMapper;

    @Test
    @Sql("/enterprise/sql/social-user-bind-data.sql")
    void testSelectUserIdByUnionId() {
        Long id = socialUserBindMapper.selectUserIdByUnionId("ui41");
        assertThat(id).isEqualTo(41L);
    }


    @Test
    @Sql("/enterprise/sql/social-user-bind-data.sql")
    void testSelectUnionIdByUserId() {
        List<String> ids = socialUserBindMapper.selectUnionIdByUserId(41L);
        assertThat(ids).isNotEmpty();
    }


    @Test
    @Sql("/enterprise/sql/social-user-bind-data.sql")
    void testSelectByUnionIds() {
        List<SocialUserBindEntity> entities =
            socialUserBindMapper.selectByUnionIds(CollUtil.newArrayList("ui41"));
        assertThat(entities).isNotEmpty();
    }


    @Test
    @Sql({"/enterprise/sql/social-user-bind-data.sql",
        "/enterprise/sql/social-tenant-user-data.sql"})
    void testSelectOpenIdByTenantIdAndUserId() {
        String id = socialUserBindMapper.selectOpenIdByTenantIdAndUserId("ai41", "ww41", 41L);
        assertThat(id).isEqualTo("oi41");
    }

}
