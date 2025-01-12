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
import com.apitable.enterprise.idaas.infrastructure.model.AppGroupsRequest;
import com.apitable.enterprise.idaas.infrastructure.model.AppGroupsResponse;
import com.apitable.enterprise.idaas.infrastructure.model.GroupsRequest;
import com.apitable.enterprise.idaas.infrastructure.model.GroupsResponse;
import com.apitable.enterprise.idaas.infrastructure.support.ServiceAccount;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * IDaaS user list API test.
 * </p>
 */
@Disabled
public class GroupApiTest {

    private static final String TEST_TENANT_ID = "tn-fa1f54f3c16f45278d2dab23c494d043";
    private static final String TEST_TENANT_NAME = "test-20220606";
    private static final String TEST_CLIENT_ID = "ai-7ec0777fc8c6481e9398eda687582603";

    private static GroupApi groupApi;

    private static ServiceAccount tenantServiceAccount;

    @BeforeAll
    static void init() {
        IdaasConfig idaasConfig = new IdaasConfig();
        idaasConfig.setSystemHost("https://demo-admin.cig.tencentcs.com");
        idaasConfig.setContactHost("https://{tenantName}-admin.cig.tencentcs.com");
        IdaasTemplate idaasTemplate = new IdaasTemplate(idaasConfig);
        groupApi = idaasTemplate.getGroupApi();
        InputStream inputStream =
            FileHelper.getInputStreamFromResource("enterprise/idaas/tenant_service_account.json");
        String jsonString = IoUtil.read(inputStream, StandardCharsets.UTF_8);
        tenantServiceAccount = JSONUtil.toBean(jsonString, ServiceAccount.class);
    }

    @Test
    void groupsTest() throws IdaasApiException {
        GroupsRequest groupsRequest = new GroupsRequest();
        groupsRequest.setPageIndex(0);
        groupsRequest.setPageSize(500);
        groupsRequest.setOrderBy(Arrays.asList("_udOrder", "_createdOn"));
        GroupsResponse groupsResponse =
            groupApi.groups(groupsRequest, tenantServiceAccount, TEST_TENANT_NAME);
        System.out.println(JSONUtil.toJsonStr(groupsResponse));

        Assertions.assertNotNull(groupsResponse);
    }

    @Test
    void appGroupsTest() throws IdaasApiException {
        AppGroupsRequest appGroupsRequest = new AppGroupsRequest();
        appGroupsRequest.setPageIndex(0);
        appGroupsRequest.setPageSize(500);
        appGroupsRequest.setOrderBy(Arrays.asList("_udOrder", "_createdOn"));
        AppGroupsResponse appGroupsResponse =
            groupApi.appGroups(appGroupsRequest, tenantServiceAccount, TEST_TENANT_ID,
                TEST_TENANT_NAME, TEST_CLIENT_ID);
        System.out.println(JSONUtil.toJsonStr(appGroupsResponse));

        Assertions.assertNotNull(appGroupsResponse);
    }

}
