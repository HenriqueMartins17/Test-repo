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
import com.apitable.enterprise.idaas.entity.IdaasUserBindEntity;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

/**
 * <p>
 * IDaaS Bind User Test
 * </p>
 */
class IdaasUserBindMapperTests extends AbstractMyBatisMapperTest {

    @Autowired
    private IdaasUserBindMapper idaasUserBindMapper;

    @Test
    @Sql("/enterprise/sql/idaas-user-bind-data.sql")
    void selectByUserIdTest() {
        IdaasUserBindEntity entity =
            idaasUserBindMapper.selectByUserId("us-fd0a293b0c934707ba744682418d2685");

        Assertions.assertNotNull(entity);
        Assertions.assertEquals("us-fd0a293b0c934707ba744682418d2685", entity.getUserId());
    }

    @Test
    @Sql("/enterprise/sql/idaas-user-bind-data.sql")
    void selectAllByUserIdsIgnoreDeletedTest() {
        List<IdaasUserBindEntity> entities = idaasUserBindMapper
            .selectAllByUserIdsIgnoreDeleted(
                Collections.singletonList("us-9bf50b5d19554ae597397ead5e9ebb27"));

        Assertions.assertTrue(CollUtil.isNotEmpty(entities));
    }

    @Test
    @Sql("/enterprise/sql/idaas-user-bind-data.sql")
    void selectAllByVikaUserIdsIgnoreDeletedTest() {
        List<IdaasUserBindEntity> entities = idaasUserBindMapper
            .selectAllByVikaUserIdsIgnoreDeleted(Collections.singletonList(1537680923105239041L));

        Assertions.assertTrue(CollUtil.isNotEmpty(entities));
    }

}
