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

package com.apitable.enterprise.integral.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;

import com.apitable.AbstractMyBatisMapperTest;
import com.apitable.user.vo.IntegralRecordVO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

public class IntegralHistoryMapperTest extends AbstractMyBatisMapperTest {

    @Autowired
    IntegralHistoryMapper historyMapper;

    @Test
    @Sql("/enterprise/sql/integral-history-data.sql")
    void testSelectTotalIntegralValueByUserId() {
        Integer count = historyMapper.selectTotalIntegralValueByUserId(1306146059515744257L);
        assertThat(count).isEqualTo(1000);
    }

    @Test
    @Sql("/enterprise/sql/integral-history-data.sql")
    void testSelectPageByUserId() {
        IPage<IntegralRecordVO> page = historyMapper.selectPageByUserId(new Page<>(), 1306146059515744257L);
        assertThat(page.getTotal()).isEqualTo(1);
    }
    @Test
    @Sql("/enterprise/sql/integral-history-data.sql")
    void testSelectCountByUserIdAndKeyValue() {
        Integer count = historyMapper.selectCountByUserIdAndKeyValue(1306146059515744257L, "key", "value");
        assertThat(count).isEqualTo(1);
    }
    @Test
    @Sql("/enterprise/sql/integral-history-data.sql")
    void testCountUserHistoryNum() {
        historyMapper.selectCountByUserIdAndActionCode(1306146059515744257L, "be_invited_to_reward");
    }

}
