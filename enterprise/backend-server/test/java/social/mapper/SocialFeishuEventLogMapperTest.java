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

import org.junit.jupiter.api.Test;

import com.apitable.AbstractMyBatisMapperTest;
import com.apitable.enterprise.social.entity.SocialFeishuEventLogEntity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * <p>
 *    Data access layer test: third-party platform integration - Mark event log table test
 * </p>
 */
public class SocialFeishuEventLogMapperTest extends AbstractMyBatisMapperTest {

    @Autowired
    SocialFeishuEventLogMapper socialFeishuEventLogMapper;

    @Test
    @Sql("/enterprise/sql/social-feishu-event-log-data.sql")
    void testSelectCountByUuid() {
        Integer count = socialFeishuEventLogMapper.selectCountByUuid("uuid41");
        assertThat(count).isEqualTo(1);
    }
    @Test
    @Sql("/enterprise/sql/social-feishu-event-log-data.sql")
    void testSelectByUuid() {
        SocialFeishuEventLogEntity entity = socialFeishuEventLogMapper.selectByUuid("uuid41");
        assertThat(entity).isNotNull();
    }

}
