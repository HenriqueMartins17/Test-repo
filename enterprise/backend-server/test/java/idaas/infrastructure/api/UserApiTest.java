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

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;

import cn.hutool.core.io.IoUtil;
import cn.hutool.json.JSONUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.apitable.enterprise.idaas.infrastructure.IdaasApiException;
import com.apitable.enterprise.idaas.infrastructure.IdaasConfig;
import com.apitable.enterprise.idaas.infrastructure.IdaasTemplate;
import com.apitable.enterprise.idaas.infrastructure.model.UsersRequest;
import com.apitable.enterprise.idaas.infrastructure.model.UsersResponse;
import com.apitable.enterprise.idaas.infrastructure.support.ServiceAccount;

/**
 * <p>
 * IDaaS user API test
 * </p>
 *
 */
@Disabled
class UserApiTest {

    private static final String TEST_TENANT_NAME = "test-20220606";

    private static UserApi userApi;

    private static ServiceAccount tenantServiceAccount;

    @BeforeAll
    static void init() {
        IdaasConfig idaasConfig = new IdaasConfig();
        idaasConfig.setSystemHost("https://demo-admin.cig.tencentcs.com");
        idaasConfig.setContactHost("https://{tenantName}-admin.cig.tencentcs.com");
        IdaasTemplate idaasTemplate = new IdaasTemplate(idaasConfig);
        userApi = idaasTemplate.getUserApi();
        InputStream inputStream = FileHelper.getInputStreamFromResource("enterprise/idaas/tenant_service_account.json");
        String jsonString = IoUtil.read(inputStream, StandardCharsets.UTF_8);
        tenantServiceAccount = JSONUtil.toBean(jsonString, ServiceAccount.class);
    }

    @Test
    void usersTest() throws IdaasApiException {
        UsersRequest usersRequest = new UsersRequest();
        usersRequest.setStatus("ACTIVE");
        usersRequest.setEndTime(Instant.now().toEpochMilli());
        usersRequest.setPageIndex(0);
        usersRequest.setPageSize(500);
        usersRequest.setOrderBy(Collections.singletonList("_createdOn"));
        UsersResponse usersResponse = userApi.users(usersRequest, tenantServiceAccount, TEST_TENANT_NAME);
        System.out.println(JSONUtil.toJsonStr(usersResponse));

        Assertions.assertNotNull(usersResponse);
    }

}
