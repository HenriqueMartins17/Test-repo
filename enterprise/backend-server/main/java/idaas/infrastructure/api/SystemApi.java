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

import com.apitable.enterprise.idaas.infrastructure.IdaasApiException;
import com.apitable.enterprise.idaas.infrastructure.IdaasTemplate;
import com.apitable.enterprise.idaas.infrastructure.constant.ApiUri;
import com.apitable.enterprise.idaas.infrastructure.model.TenantRequest;
import com.apitable.enterprise.idaas.infrastructure.model.TenantResponse;
import com.apitable.enterprise.idaas.infrastructure.model.WellKnowResponse;
import com.apitable.enterprise.idaas.infrastructure.support.ServiceAccount;

/**
 * <p>
 * System Manage API
 * </p>
 *
 */
public class SystemApi {

    private final IdaasTemplate idaasTemplate;
    private final String systemHost;

    public SystemApi(IdaasTemplate idaasTemplate, String systemHost) {
        this.idaasTemplate = idaasTemplate;
        this.systemHost = systemHost;
    }

    /**
     * Open the tenant, and preset the authentication source and single sign on application.
     * The authentication source or single sign on application configuration will be updated when this interface is called multiple times
     *
     * @param request request parameters
     * @param serviceAccount system ServiceAccount
     * @return tenant information
     */
    public TenantResponse tenant(TenantRequest request, ServiceAccount serviceAccount) throws IdaasApiException {
        return idaasTemplate.post(systemHost + ApiUri.TENANT, request, TenantResponse.class, null, serviceAccount, null);
    }

    /**
     * create tenant ServiceAccount
     *
     * @param tenantName tenant name to operate on
     * @param serviceAccount system ServiceAccount
     * @return tenant's ServiceAccount
     */
    public ServiceAccount serviceAccount(String tenantName, ServiceAccount serviceAccount) throws IdaasApiException {
        return idaasTemplate.post(systemHost + ApiUri.SERVICE_ACCOUNT, null, ServiceAccount.class, tenantName, serviceAccount, null);
    }

    /**
     * Get the return result of the Well known interface
     *
     * @param wellKnownUrl Well-known interface path
     * @return result
     */
    public WellKnowResponse fetchWellKnown(String wellKnownUrl) throws IdaasApiException {
        return idaasTemplate.getFromUrl(wellKnownUrl, null, WellKnowResponse.class);
    }

}
