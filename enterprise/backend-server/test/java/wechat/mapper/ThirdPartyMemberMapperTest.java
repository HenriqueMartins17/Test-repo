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

package com.apitable.enterprise.wechat.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.apitable.AbstractMyBatisMapperTest;
import com.apitable.enterprise.wechat.dto.ThirdPartyMemberInfo;
import com.apitable.enterprise.wechat.dto.WechatMemberDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

/**
 * <p>
 * Data access layer test: third-party system member information table test
 * </p>
 */
public class ThirdPartyMemberMapperTest extends AbstractMyBatisMapperTest {

    @Autowired
    ThirdPartyMemberMapper thirdPartyMemberMapper;

    @Test
    @Sql("/enterprise/sql/third-party-member-data.sql")
    void testSelectUnionIdByOpenIdAndType() {
        String id = thirdPartyMemberMapper.selectUnionIdByOpenIdAndType("ai41", "oi41", 0);
        assertThat(id).isEqualTo("ui41");
    }

    @Test
    @Sql("/enterprise/sql/third-party-member-data.sql")
    void testSelectNickNameByUnionIdAndType() {
        String name = thirdPartyMemberMapper.selectNickNameByUnionIdAndType("ai41", "ui41", 0);
        assertThat(name).isEqualTo("apitable body");
    }


    @Test
    @Sql("/enterprise/sql/third-party-member-data.sql")
    void testSelectExtraById() {
        String extra = thirdPartyMemberMapper.selectExtraById(41L);
        assertThat(extra).isEqualTo(
            "{\"city\": \"Shenzhen\", \"gender\": \"2\", \"country\": \"China\", \"language\": \"zh_CN\", \"province\": \"Guangdong\", \"countryCode\": \"86\", \"phoneNumber\": \"18622510531\"}");
    }


    @Test
    @Sql("/enterprise/sql/third-party-member-data.sql")
    void testSelectSessionKeyById() {
        String key = thirdPartyMemberMapper.selectSessionKeyById(41L);
        assertThat(key).isEqualTo("sk41");
    }


    @Test
    @Sql("/enterprise/sql/third-party-member-data.sql")
    void testSelectInfo() {
        ThirdPartyMemberInfo entity = thirdPartyMemberMapper.selectInfo("ai41", "ui41", 0);
        assertThat(entity).isNotNull();
    }


    @Test
    @Sql({"/enterprise/sql/third-party-member-data.sql", "/sql/user-data.sql",
        "/enterprise/sql/user-link-data.sql"})
    void testSelectUserIdByIdAndLinkType() {
        Long id = thirdPartyMemberMapper.selectUserIdByIdAndLinkType(41L, 0);
        assertThat(id).isEqualTo(41L);
    }


    @Test
    @Sql("/enterprise/sql/third-party-member-data.sql")
    void testSelectWechatMemberDto() {
        WechatMemberDto entity = thirdPartyMemberMapper.selectWechatMemberDto(0, "ai41", "oi41");
        assertThat(entity).isNotNull();
    }


    @Test
    @Sql("/sql/user-data.sql")
    void testSelectUserLinkedWechatMemberDto() {
        WechatMemberDto entity =
            thirdPartyMemberMapper.selectUserLinkedWechatMemberDto("ai41", "41");
        assertThat(entity).isNotNull();
    }

}
