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

import com.apitable.enterprise.idaas.model.IdaasAuthLoginVo;

/**
 * <p>
 * IDaaS Login authorization
 * </p>
 */
public interface IIdaasAuthService {

    /**
     * Get the address of the vika one click login page
     *
     * @param clientId IDaaS Application's Client ID
     * @return Address of vika one click login page
     */
    String getVikaLoginUrl(String clientId);

    /**
     * Get the address of vika's IDaaS login callback page
     *
     * @param clientId IDaaS Application's Client ID
     * @param spaceId Bound space ID, this field is not required for privatization deployment
     * @return vika handles the address of the IDaaS login callback page
     */
    String getVikaCallbackUrl(String clientId, String spaceId);

    /**
     * Get IDaaS login path
     *
     * @param clientId IDaaS Application's Client ID
     * @return IDaaS login path
     */
    IdaasAuthLoginVo idaasLoginUrl(String clientId);

    /**
     * IDaaS After login, callback to complete subsequent operations
     *
     * @param clientId IDaaS Application's Client ID
     * @param spaceId bound space ID, this field is not required for privatization deployment
     * @param authCode Authorization code returned by callback
     * @param state Random string returned by callback
     */
    void idaasLoginCallback(String clientId, String spaceId, String authCode, String state);

}
