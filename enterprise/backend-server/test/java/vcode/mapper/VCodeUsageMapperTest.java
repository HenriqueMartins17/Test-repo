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

package com.apitable.enterprise.vcode.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.apitable.AbstractMyBatisMapperTest;
import com.apitable.enterprise.vcode.dto.VCodeDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

/**
 * <p>
 * VCode Usage Mapper Test
 * </p>
 */
public class VCodeUsageMapperTest extends AbstractMyBatisMapperTest {

    @Autowired
    VCodeUsageMapper vCodeUsageMapper;

    @Test
    @Sql("/enterprise/sql/code-usage-data.sql")
    void testCountByCodeAndType() {
        Integer count = vCodeUsageMapper.countByCodeAndType("41", 0, 41L);
        assertThat(count).isEqualTo(1);
    }

    @Test
    @Sql({"/enterprise/sql/code-usage-data.sql", "/enterprise/sql/code-data.sql"})
    void testSelectInvitorUserId() {
        VCodeDTO entity = vCodeUsageMapper.selectInvitorUserId(41L);
        assertThat(entity).isNotNull();
    }

}
