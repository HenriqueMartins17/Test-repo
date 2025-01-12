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
import com.apitable.enterprise.wechat.vo.QrCodePageVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

/**
 * <p>
 * Wechat Mp Qrcode Mapper Test
 * </p>
 */
public class WechatMpQrcodeMapperTest extends AbstractMyBatisMapperTest {

    @Autowired
    WechatMpQrcodeMapper wechatMpQrcodeMapper;

    @Test
    @Sql("/enterprise/sql/wechat-mp-qrcode-data.sql")
    void testSelectDetailInfo() {
        IPage<QrCodePageVo> page = wechatMpQrcodeMapper.selectDetailInfo(new Page<>(), "wx41");
        assertThat(page.getTotal()).isEqualTo(1);
    }

}
