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

import com.apitable.AbstractMyBatisMapperTest;
import com.apitable.enterprise.idaas.entity.IdaasTenantEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

/**
 * <p>
 * IDaaS Tenant Test
 * </p>
 */
class IdaasTenantMapperTests extends AbstractMyBatisMapperTest {

    @Autowired
    private IdaasTenantMapper idaasTenantMapper;

    @Test
    @Sql("/enterprise/sql/idaas-tenant-data.sql")
    void selectByTenantNameTest() {
        IdaasTenantEntity entity = idaasTenantMapper.selectByTenantName("test-20220617");

        Assertions.assertNotNull(entity);
        Assertions.assertEquals("test-20220617", entity.getTenantName());
    }

}
