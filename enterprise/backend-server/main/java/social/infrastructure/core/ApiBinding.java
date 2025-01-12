/*
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up
 * license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its
 * subdirectories does not constitute permission to use this code or APITable Enterprise Edition
 * features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

package com.apitable.enterprise.social.infrastructure.core;

import javax.net.ssl.SSLContext;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class ApiBinding {
    private final RestTemplate restTemplate;

    protected ApiBinding() {
        this.restTemplate = createRestTemplate();
    }

    private RestTemplate createRestTemplate() {
        RestTemplate client = new RestTemplate();
        client.setRequestFactory(generateHttpRequestFactory());
        return client;
    }

    protected RestTemplate getRestTemplate() {
        return restTemplate;
    }

    protected HttpComponentsClientHttpRequestFactory generateHttpRequestFactory() {
        try {
            TrustStrategy acceptingTrustStrategy = (x509Certificates, authType) -> true;
            SSLContext sslContext =
                SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
            SSLConnectionSocketFactory connectionSocketFactory =
                new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());

            CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(
                    PoolingHttpClientConnectionManagerBuilder.create()
                        .setSSLSocketFactory(connectionSocketFactory)
                        .build()
                )
                .build();
            HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory();
            factory.setHttpClient(httpClient);
            return factory;
        } catch (Exception e) {
            throw new RuntimeException("create woa HttpComponentsClientHttpRequestFactory fail", e);
        }
    }
}
