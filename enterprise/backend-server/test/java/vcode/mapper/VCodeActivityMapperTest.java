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
import com.apitable.enterprise.vcode.vo.VCodeActivityPageVo;
import com.apitable.enterprise.vcode.vo.VCodeActivityVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

/**
 * <p>
 * VCode Activity Mapper Test
 * </p>
 */
public class VCodeActivityMapperTest extends AbstractMyBatisMapperTest {

    @Autowired
    VCodeActivityMapper vCodeActivityMapper;

    @Test
    @Sql("/enterprise/sql/code-activity-data.sql")
    void testSelectAllScene() {
        List<String> strings = vCodeActivityMapper.selectAllScene();
        assertThat(strings).isNotEmpty();
    }

    @Test
    @Sql("/enterprise/sql/code-activity-data.sql")
    void testSelectIdByScene() {
        Long id = vCodeActivityMapper.selectIdByScene("test");
        assertThat(id).isEqualTo(41L);
    }

    @Test
    @Sql("/enterprise/sql/code-activity-data.sql")
    void testCountById() {
        Integer count = vCodeActivityMapper.countById(41L);
        assertThat(count).isEqualTo(1);
    }

    @Test
    @Sql("/enterprise/sql/code-activity-data.sql")
    void testSelectBaseInfo() {
        List<VCodeActivityVo> entities = vCodeActivityMapper.selectBaseInfo("test");
        assertThat(entities).isNotEmpty();
    }

    @Test
    @Sql("/enterprise/sql/code-activity-data.sql")
    void testSelectDetailInfo() {
        IPage<VCodeActivityPageVo> entities =
            vCodeActivityMapper.selectDetailInfo(new Page<>(), "test", "ai41");
        assertThat(entities).isNotNull();
    }

    @Test
    @Sql({"/enterprise/sql/code-activity-data.sql", "/enterprise/sql/wechat-mp-qrcode-data.sql"})
    void testCountQrCodeByIdAndAppId() {
        Integer count = vCodeActivityMapper.countQrCodeByIdAndAppId(41L, "wx41");
        assertThat(count).isEqualTo(1);
    }

}
