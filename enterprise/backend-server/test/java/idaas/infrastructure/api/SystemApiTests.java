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

package com.apitable.enterprise.idaas.infrastructure.api;

import cn.hutool.core.io.IoUtil;
import cn.hutool.json.JSONUtil;
import com.apitable.enterprise.idaas.infrastructure.IdaasApiException;
import com.apitable.enterprise.idaas.infrastructure.IdaasConfig;
import com.apitable.enterprise.idaas.infrastructure.IdaasTemplate;
import com.apitable.enterprise.idaas.infrastructure.model.TenantRequest;
import com.apitable.enterprise.idaas.infrastructure.model.TenantResponse;
import com.apitable.enterprise.idaas.infrastructure.model.WellKnowResponse;
import com.apitable.enterprise.idaas.infrastructure.support.ServiceAccount;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * IDaaS system config API test
 * </p>
 *
 */
@Disabled
class SystemApiTests {

    private static final String TEST_TENANT_NAME = "junit-test-20220606";

    private static SystemApi systemApi;

    private static ServiceAccount systemServiceAccount;

    @BeforeAll
    static void init() {
        IdaasConfig idaasConfig = new IdaasConfig();
        idaasConfig.setSystemHost("https://demo-admin.cig.tencentcs.com");
        idaasConfig.setContactHost("https://{tenantName}-admin.cig.tencentcs.com");
        IdaasTemplate idaasTemplate = new IdaasTemplate(idaasConfig);
        systemApi = idaasTemplate.getSystemApi();
        InputStream inputStream = FileHelper.getInputStreamFromResource("enterprise/idaas/service_account.json");
        String jsonString = IoUtil.read(inputStream, StandardCharsets.UTF_8);
        systemServiceAccount = JSONUtil.toBean(jsonString, ServiceAccount.class);
    }

    @Test
    void tenantTests() throws IdaasApiException {
        TenantRequest tenantRequest = new TenantRequest();
        tenantRequest.setName(TEST_TENANT_NAME);
        tenantRequest.setDisplayName("junit test user");
        TenantRequest.Admin tenantAdmin = new TenantRequest.Admin();
        tenantAdmin.setUsername("junit-test");
        tenantAdmin.setPassword("123456");
        tenantRequest.setAdmin(tenantAdmin);
        TenantResponse tenantResponse = systemApi.tenant(tenantRequest, systemServiceAccount);
        System.out.println(JSONUtil.toJsonStr(tenantResponse));

        Assertions.assertNotNull(tenantResponse);
    }

    @Test
    void fetchWellKnownTest() throws IdaasApiException {
        WellKnowResponse wellKnowResponse = systemApi.fetchWellKnown("https://junit-test-20220606-idp.cig.tencentcs.com/sso/tn-5ca4c595009a48b0bc90ff7cb14b6953/ai-4f094c3982594df4a65519db8aba8c43/oidc/.well-known/openid-configuration");
        System.out.println(JSONUtil.toJsonStr(wellKnowResponse));

        Assertions.assertNotNull(wellKnowResponse);
    }

}
