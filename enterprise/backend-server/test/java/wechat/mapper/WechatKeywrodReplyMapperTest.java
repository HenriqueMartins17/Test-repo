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

import java.util.List;

import org.junit.jupiter.api.Test;

import com.apitable.AbstractMyBatisMapperTest;
import com.apitable.enterprise.wechat.entity.WechatKeywordReplyEntity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * <p>
 * Wechat Keywrod Reply Mapper Test
 * </p>
 */
public class WechatKeywrodReplyMapperTest extends AbstractMyBatisMapperTest {

    @Autowired
    WechatKeywordReplyMapper wechatKeywordReplyMapper;

    @Test
    @Sql("/enterprise/sql/wechat-keyword-reply-data.sql")
    void testFindRepliesByKeyword() {
        List<WechatKeywordReplyEntity> entities = wechatKeywordReplyMapper.findRepliesByKeyword("app_id", "keyword");
        assertThat(entities).isNotEmpty();
    }

}
