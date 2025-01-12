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

package com.apitable.enterprise.idaas.mapper;

import cn.hutool.core.collection.CollUtil;
import com.apitable.AbstractMyBatisMapperTest;
import com.apitable.enterprise.idaas.entity.IdaasGroupBindEntity;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

/**
 * <p>
 * IDaaS Bind User Group Test
 * </p>
 */
class IdaasGroupBindMapperTests extends AbstractMyBatisMapperTest {

    @Autowired
    private IdaasGroupBindMapper idaasGroupBindMapper;

    @Test
    @Sql("/enterprise/sql/idaas-group-bind-data.sql")
    void selectAllBySpaceIdTest() {
        List<IdaasGroupBindEntity> entities =
            idaasGroupBindMapper.selectAllBySpaceId("spc6jJS5lX9UJ");

        Assertions.assertTrue(CollUtil.isNotEmpty(entities));
    }

    @Test
    @Sql("/enterprise/sql/idaas-group-bind-data.sql")
    void selectAllBySpaceIdIgnoreDeletedTest() {
        List<IdaasGroupBindEntity> entities =
            idaasGroupBindMapper.selectAllBySpaceIdIgnoreDeleted("spc6jJS5lX9UJ");

        Assertions.assertTrue(CollUtil.isNotEmpty(entities));
    }

}
