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
import com.apitable.enterprise.vcode.vo.VCodeCouponPageVo;
import com.apitable.enterprise.vcode.vo.VCodeCouponVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

/**
 * <p>
 * VCode Coupon Mapper Test
 * </p>
 */
public class VCodeCouponMapperTest extends AbstractMyBatisMapperTest {

    @Autowired
    VCodeCouponMapper vCodeCouponMapper;

    @Test
    @Sql("/enterprise/sql/code-coupon-template-data.sql")
    void testCountById() {
        Integer count = vCodeCouponMapper.countById(41L);
        assertThat(count).isEqualTo(1);
    }

    @Test
    @Sql("/enterprise/sql/code-coupon-template-data.sql")
    void testSelectBaseInfo() {
        List<VCodeCouponVo> entities = vCodeCouponMapper.selectBaseInfo("comment");
        assertThat(entities).isNotEmpty();
    }

    @Test
    @Sql("/enterprise/sql/code-coupon-template-data.sql")
    void testSelectDetailInfo() {
        IPage<VCodeCouponPageVo> page = vCodeCouponMapper.selectDetailInfo(new Page(), "comment");
        assertThat(page.getTotal()).isEqualTo(2);
    }

}
