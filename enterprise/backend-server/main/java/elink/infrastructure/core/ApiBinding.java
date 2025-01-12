/*
 * APITable <https://github.com/apitable/apitable>
 * Copyright (C) 2022 APITable Ltd. <https://apitable.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.apitable.enterprise.elink.infrastructure.core;

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

/**
 * <p>
 * ApiBinding
 * </p>
 */
public abstract class ApiBinding {

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
            throw new RuntimeException("create elink HttpComponentsClientHttpRequestFactory fail",
                e);
        }

    }
}

