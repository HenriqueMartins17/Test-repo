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

import java.nio.charset.StandardCharsets;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.text.CharSequenceUtil;

import com.apitable.enterprise.idaas.infrastructure.IdaasApiException;
import com.apitable.enterprise.idaas.infrastructure.IdaasTemplate;
import com.apitable.enterprise.idaas.infrastructure.model.AccessTokenRequest;
import com.apitable.enterprise.idaas.infrastructure.model.AccessTokenResponse;
import com.apitable.enterprise.idaas.infrastructure.model.UserInfoResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * <p>
 * Login authorization API
 * </p>
 */
public class AuthApi {

    private final IdaasTemplate idaasTemplate;

    public AuthApi(IdaasTemplate idaasTemplate) {
        this.idaasTemplate = idaasTemplate;
    }

    /**
     * Get access user information's access_token
     *
     * @param tokenUrl access_token's path
     * @param clientId IDaaS application Client ID
     * @param clientSecret IDaaS application Client Secret
     * @param request request parameters
     * @return Access user information's access_token
     */
    public AccessTokenResponse accessToken(String tokenUrl, String clientId, String clientSecret, AccessTokenRequest request) throws IdaasApiException {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/x-www-form-urlencoded");
        httpHeaders.add("Authorization", "Basic " +
                Base64.encode(CharSequenceUtil.join(":", clientId, clientSecret), StandardCharsets.UTF_8));
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>(4);
        form.set("grant_type", request.getGrantType());
        form.set("code", request.getCode());
        form.set("redirect_uri", request.getRedirectUri());

        return idaasTemplate.postFromUrl(tokenUrl, httpHeaders, form, AccessTokenResponse.class);
    }

    /**
     * Get user information
     *
     * @param userInfoUrl Path to obtain user information
     * @param accessToken Access to user information_ token
     * @return user information
     */
    public UserInfoResponse userInfo(String userInfoUrl, String accessToken) throws IdaasApiException {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("Authorization", "Bearer " + accessToken);

        return idaasTemplate.getFromUrl(userInfoUrl, httpHeaders, UserInfoResponse.class);
    }

}
