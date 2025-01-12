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

package com.apitable.enterprise.idaas.service;

import cn.hutool.core.io.IoUtil;
import cn.hutool.json.JSONUtil;
import com.apitable.FileHelper;
import com.apitable.enterprise.AbstractVikaSaasIntegrationTest;
import com.apitable.enterprise.idaas.model.IdaasAppBindRo;
import com.apitable.enterprise.idaas.model.IdaasAppBindVo;
import com.apitable.enterprise.idaas.model.IdaasTenantCreateRo;
import com.apitable.enterprise.idaas.model.IdaasTenantCreateRo.ServiceAccount;
import com.apitable.enterprise.idaas.model.IdaasTenantCreateVo;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * IDaaS Tenant and application related tests
 * </p>
 */
@Disabled
class IdaasServiceTests extends AbstractVikaSaasIntegrationTest {

    private static final String TEST_TENANT_NAME = "junit-test-20220620";

    private static final String TEST_SPACE_ID = "spcqk4UBqLRP1";

    private static ServiceAccount systemServiceAccount;

    @BeforeAll
    static void init() {
        InputStream inputStream = FileHelper.getInputStreamFromResource("enterprise/idaas/system_service_account.json");
        String jsonString = IoUtil.read(inputStream, StandardCharsets.UTF_8);
        systemServiceAccount = JSONUtil.toBean(jsonString, ServiceAccount.class);
    }

    /**
     * Test the process of creating tenants, binding applications, and synchronizing address books
     *
     * <p>
     * Each of the current processes depends on the data in the previous step, so they are put into the same test
     * </p>
     */
    @Test
    void tenantAppContactTest() {
        // 1 Create Tenant
        IdaasTenantCreateRo idaasTenantCreateRo = new IdaasTenantCreateRo();
        idaasTenantCreateRo.setTenantName(TEST_TENANT_NAME);
        idaasTenantCreateRo.setCorpName("junit test tenant");
        idaasTenantCreateRo.setAdminUsername("junit-test");
        idaasTenantCreateRo.setAdminPassword("123456");
        idaasTenantCreateRo.setServiceAccount(systemServiceAccount);
        IdaasTenantCreateVo idaasTenantCreateVo = idaasTenantService.createTenant(idaasTenantCreateRo);
        Assertions.assertNotNull(idaasTenantCreateVo);
        // 2 Bind application to space station
        IdaasAppBindRo idaasAppBindRo = new IdaasAppBindRo();
        idaasAppBindRo.setTenantName(TEST_TENANT_NAME);
        idaasAppBindRo.setAppClientId("ai-96154ae6a70741b0bc4e9cbc59745b37");
        idaasAppBindRo.setAppClientSecret("7nVSfISobnLOIU2qVVBTJj9U");
        idaasAppBindRo.setAppWellKnown("https://junit-test-20220620-idp.cig.tencentcs.com/sso/tn-f774a958f7bf48a9b9a73774d5b53e9b/ai-96154ae6a70741b0bc4e9cbc59745b37/oidc/.well-known/openid-configuration");
        idaasAppBindRo.setSpaceId(TEST_SPACE_ID);
        IdaasAppBindVo idaasAppBindVo = idaasAppBindService.bindTenantApp(idaasAppBindRo);
        Assertions.assertNotNull(idaasAppBindVo);
        // 3 Synchronize contacts
        idaasContactService.syncContact(TEST_SPACE_ID, null);
        Assertions.assertTrue(true);
    }

}
