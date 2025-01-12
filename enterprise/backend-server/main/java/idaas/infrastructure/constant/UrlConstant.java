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

package com.apitable.enterprise.idaas.infrastructure.constant;

import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.StrUtil;

/**
 * <p>
 * Path constant
 * </p>
 *
 */
public class UrlConstant {

    private static final String IDAAS_AUTHORIZE_URL = "{authorizeEndpoint}?response_type=code&client_id={clientId}&redirect_uri={redirectUri}&scope=openid%20offline_access&state={state}";

    /**
     * Get the final IDaaS login authorization address
     *
     * @param authorizeEndpoint IDaaS login path
     * @param clientId application Client ID
     * @param redirectUri Callback address after successful login
     * @param state random character
     * @return final IDaaS login authorization address
     */
    public static String getAuthorizationUrl(String authorizeEndpoint,
            String clientId, String redirectUri, String state) {
        Dict variable = Dict.create()
                .set("authorizeEndpoint", authorizeEndpoint)
                .set("clientId", clientId)
                .set("redirectUri", redirectUri)
                .set("state", state);

        return StrUtil.format(IDAAS_AUTHORIZE_URL, variable);
    }

}
