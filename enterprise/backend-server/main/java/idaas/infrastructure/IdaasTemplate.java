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

package com.apitable.enterprise.idaas.infrastructure;

import cn.hutool.core.lang.Dict;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.apitable.enterprise.idaas.infrastructure.api.AuthApi;
import com.apitable.enterprise.idaas.infrastructure.api.GroupApi;
import com.apitable.enterprise.idaas.infrastructure.api.SystemApi;
import com.apitable.enterprise.idaas.infrastructure.api.UserApi;
import com.apitable.enterprise.idaas.infrastructure.support.ServiceAccount;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * <p>
 * IDaaS request template
 * </p>
 */
public class IdaasTemplate {

    private final IdaasConfig config;
    private final RestClient restClient;

    private AuthApi authApi;
    private GroupApi groupApi;
    private SystemApi systemApi;
    private UserApi userApi;

    public IdaasTemplate(IdaasConfig config) {
        this.config = config;
        restClient = RestClient.builder()
            .requestFactory(new HttpComponentsClientHttpRequestFactory())
            .build();
        initApi();
    }

    public IdaasConfig getConfig() {
        return config;
    }

    public AuthApi getAuthApi() {
        return authApi;
    }

    public GroupApi getGroupApi() {
        return groupApi;
    }

    public SystemApi getSystemApi() {
        return systemApi;
    }

    public UserApi getUserApi() {
        return userApi;
    }

    public <T> T get(String url, MultiValueMap<String, Object> payload, Class<T> responseType)
        throws IdaasApiException {
        return get(url, payload, responseType, null, null, null);
    }

    public <T> T get(String url, MultiValueMap<String, Object> payload, Class<T> responseType,
                     String tenantName, ServiceAccount serviceAccount, String tokenAudience)
        throws IdaasApiException {
        return execute(url, HttpMethod.GET, payload, responseType, tenantName, serviceAccount,
            tokenAudience);
    }

    public <T> T post(String url, Object payload, Class<T> responseType) throws IdaasApiException {
        return post(url, payload, responseType, null, null, null);
    }

    public <T> T post(String url, Object payload, Class<T> responseType, String tenantName,
                      ServiceAccount serviceAccount, String tokenAudience)
        throws IdaasApiException {
        return execute(url, HttpMethod.POST, payload, responseType, tenantName, serviceAccount,
            tokenAudience);
    }

    public <T> T getFromUrl(String url, HttpHeaders httpHeaders, Class<T> responseType)
        throws IdaasApiException {
        return execute(url, HttpMethod.GET, httpHeaders, null, responseType);
    }

    public <T> T postFromUrl(String url, HttpHeaders httpHeaders, Object payload,
                             Class<T> responseType) throws IdaasApiException {
        return execute(url, HttpMethod.POST, httpHeaders, payload, responseType);
    }

    private void initApi() {
        this.authApi = new AuthApi(this);
        this.groupApi = new GroupApi(this, config.getContactHost());
        this.systemApi = new SystemApi(this, config.getSystemHost());
        this.userApi = new UserApi(this, config.getContactHost());
    }

    private String getUrl(String url, String tenantName) {
        if (CharSequenceUtil.isBlank(tenantName)) {
            return url;
        } else {
            Dict variables = Dict.create()
                .set("tenantName", tenantName);

            return StrUtil.format(url, variables);
        }
    }

    /**
     * Execute API request
     *
     * @param url            interface path
     * @param httpMethod     request method
     * @param payload        request body
     * @param responseType   request type
     * @param tenantName     The tenant name of the operation. Optional
     * @param serviceAccount The ServiceAccount required when requesting to attach a Token. Optional
     * @param tokenAudience  Token Applicable personnel, blank if not specified
     * @return result
     * @throws IdaasApiException Interface request exception
     */
    private <T> T execute(String url, HttpMethod httpMethod, Object payload,
                          Class<T> responseType, String tenantName, ServiceAccount serviceAccount,
                          String tokenAudience) throws IdaasApiException {
        HttpHeaders httpHeaders = new HttpHeaders();
        if (Objects.nonNull(serviceAccount)) {
            try {
                httpHeaders.add("Authorization",
                    "Bearer " + jwtToken(serviceAccount, tokenAudience));
            } catch (JOSEException | ParseException ex) {
                throw new IdaasApiException(ex);
            }
        }

        try {
            ResponseEntity<T> responseEntity;
            UriComponentsBuilder uriComponentsBuilder =
                UriComponentsBuilder.fromUriString(getUrl(url, tenantName));
            if (httpMethod == HttpMethod.GET && payload instanceof MultiValueMap) {
                MultiValueMap<String, ?> multiValueMap = (MultiValueMap<String, ?>) payload;
                for (Map.Entry<String, ?> entry : multiValueMap.entrySet()) {
                    Object value = entry.getValue();
                    if (value instanceof Collection) {
                        uriComponentsBuilder.queryParam(entry.getKey(), (Collection) value);
                    } else {
                        uriComponentsBuilder.queryParam(entry.getKey(), value);
                    }
                }
                responseEntity = restClient
                    .method(httpMethod)
                    .uri(uriComponentsBuilder.build().encode(StandardCharsets.UTF_8).toUri())
                    .headers(headers -> headers.addAll(httpHeaders))
                    .retrieve()
                    .toEntity(responseType);
            } else {
                responseEntity = restClient
                    .method(httpMethod)
                    .uri(uriComponentsBuilder.build().encode(StandardCharsets.UTF_8).toUri())
                    .headers(headers -> headers.addAll(httpHeaders))
                    .body(payload)
                    .retrieve()
                    .toEntity(responseType);
            }
            if (responseEntity.getStatusCode() != HttpStatus.OK) {
                throw new IdaasApiException(
                    "IDaaS error, response code: " + responseEntity.getStatusCode());
            }

            return responseEntity.getBody();
        } catch (RestClientException ex) {
            throw new IdaasApiException(ex);
        }
    }

    /**
     * Execute full path request
     *
     * @param url          Full path of interface
     * @param httpMethod   request method
     * @param httpHeaders  request header
     * @param payload      request body
     * @param responseType request type
     * @return result
     * @throws IdaasApiException Interface request exception
     */
    private <T> T execute(String url, HttpMethod httpMethod, HttpHeaders httpHeaders,
                          Object payload, Class<T> responseType) throws IdaasApiException {
        try {
            ResponseEntity<T> responseEntity;
            if (payload == null) {
                responseEntity = restClient
                    .method(httpMethod)
                    .uri(url)
                    .headers(headers -> headers.addAll(httpHeaders))
                    .retrieve()
                    .toEntity(responseType);
            } else {
                responseEntity = restClient
                    .method(httpMethod)
                    .uri(url)
                    .headers(headers -> headers.addAll(httpHeaders))
                    .body(payload)
                    .retrieve()
                    .toEntity(responseType);
            }
            if (responseEntity.getStatusCode() != HttpStatus.OK) {
                throw new IdaasApiException(
                    "IDaaS error, response code: " + responseEntity.getStatusCode());
            }

            return responseEntity.getBody();
        } catch (RestClientException ex) {
            throw new IdaasApiException(ex);
        }
    }

    /**
     * Generate JWT Token of interface request
     *
     * @param serviceAccount Service provider account information
     * @param audience       token Applicable user's name, blank if no
     * @return JWT Token
     */
    private String jwtToken(ServiceAccount serviceAccount, String audience)
        throws JOSEException, ParseException {
        ServiceAccount.PrivateKey privateKey = serviceAccount.getPrivateKey();
        JWSHeader jwsHeader = new JWSHeader.Builder(JWSAlgorithm.RS256)
            .type(JOSEObjectType.JWT)
            .keyID(privateKey.getKid())
            .build();

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
            .issueTime(Date.from(Instant.now()))
            .expirationTime(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
            .audience(audience)
            .issuer(serviceAccount.getClientId())
            .claim("account_type", "serviceAccount")
            .build();

        SignedJWT jwt = new SignedJWT(jwsHeader, claimsSet);

        RSAKey rsaKey = RSAKey.parse(privateKey.toMap());
        JWSSigner signer = new RSASSASigner(rsaKey);
        jwt.sign(signer);

        return jwt.serialize();
    }

}
