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

package com.apitable.enterprise.user.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.apitable.AbstractMyBatisMapperTest;
import com.apitable.shared.cache.bean.AccountLinkDto;
import com.apitable.user.enums.LinkType;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

/**
 * <p>
 *     Data access layer test: basic user third-party platform association table test
 * </p>
 */
public class UserLinkMapperTest extends AbstractMyBatisMapperTest {

    @Autowired
    UserLinkMapper userLinkMapper;

    @Test
    @Sql("/enterprise/sql/user-link-data.sql")
    void testSelectUserIdByUnionIdAndType() {
        Long id = userLinkMapper.selectUserIdByUnionIdAndType("ui41", 0);
        assertThat(id).isEqualTo(41L);
    }

    @Test
    @Sql("/enterprise/sql/user-link-data.sql")
    void testSelectUnionIdByUserIdAndType() {
        String id = userLinkMapper.selectUnionIdByUserIdAndType(41L, 0);
        assertThat(id).isEqualTo("ui41");
    }

    @Test
    @Sql("/enterprise/sql/user-link-data.sql")
    void testSelectVoByUserId() {
        List<AccountLinkDto> entities = userLinkMapper.selectVoByUserId(41L);
        assertThat(entities).isNotEmpty();
    }

    @Test
    @Sql("/enterprise/sql/user-link-data.sql")
    void testSelectUserIdByUnionIdAndOpenIdAndType() {
        Long id = userLinkMapper.selectUserIdByUnionIdAndOpenIdAndType("ui41", "oi41", LinkType.DINGTALK);
        assertThat(id).isEqualTo(41L);
    }

}
