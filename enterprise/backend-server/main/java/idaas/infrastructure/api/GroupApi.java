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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.StrUtil;

import com.apitable.enterprise.idaas.infrastructure.IdaasApiException;
import com.apitable.enterprise.idaas.infrastructure.IdaasTemplate;
import com.apitable.enterprise.idaas.infrastructure.constant.ApiUri;
import com.apitable.enterprise.idaas.infrastructure.model.AppGroupsRequest;
import com.apitable.enterprise.idaas.infrastructure.model.AppGroupsResponse;
import com.apitable.enterprise.idaas.infrastructure.model.GroupsRequest;
import com.apitable.enterprise.idaas.infrastructure.model.GroupsResponse;
import com.apitable.enterprise.idaas.infrastructure.support.ServiceAccount;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * <p>
 * User Group API
 * </p>
 *
 */
public class GroupApi {

    private final IdaasTemplate idaasTemplate;
    private final String contactHost;

    public GroupApi(IdaasTemplate idaasTemplate, String contactHost) {
        this.idaasTemplate = idaasTemplate;
        this.contactHost = contactHost;
    }

    /**
     * Get user group list
     *
     * @param request request parameters
     * @param serviceAccount tenant ServiceAccount
     * @param tenantName tenant name
     * @return user group
     */
    public GroupsResponse groups(GroupsRequest request, ServiceAccount serviceAccount, String tenantName) throws IdaasApiException {
        MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.set("page_index", request.getPageIndex());
        multiValueMap.set("page_size", request.getPageSize());
        if (CollUtil.isNotEmpty(request.getOrderBy())) {
            multiValueMap.addAll("order_by", request.getOrderBy());
        }

        return idaasTemplate.get(contactHost + ApiUri.GROUPS, multiValueMap, GroupsResponse.class, tenantName, serviceAccount, null);
    }

    /**
     * Get user group list in application
     *
     * @param request request parameters
     * @param serviceAccount tenant ServiceAccount
     * @param tenantId tenant ID
     * @param tenantName tenant name
     * @param clientId application ID
     * @return user group
     */
    public AppGroupsResponse appGroups(AppGroupsRequest request, ServiceAccount serviceAccount,
            String tenantId, String tenantName, String clientId) throws IdaasApiException {
        MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.set("page_index", request.getPageIndex());
        multiValueMap.set("page_size", request.getPageSize());
        if (CollUtil.isNotEmpty(request.getOrderBy())) {
            multiValueMap.addAll("order_by", request.getOrderBy());
        }

        Dict variables = Dict.create()
                .set("tenantId", tenantId)
                .set("clientId", clientId);
        String appGroupUri = StrUtil.format(ApiUri.APP_GROUPS, variables);

        return idaasTemplate.get(contactHost + appGroupUri, multiValueMap, AppGroupsResponse.class, tenantName, serviceAccount, null);
    }

}
