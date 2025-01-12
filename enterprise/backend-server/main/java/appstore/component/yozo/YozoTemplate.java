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

package com.apitable.enterprise.appstore.component.yozo;

import java.net.URI;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

public class YozoTemplate {

    private final YozoConfig config;

    private final RestClient restClient;

    public YozoTemplate(YozoConfig config) {
        this.config = config;
        restClient = RestClient.builder()
            .requestFactory(new HttpComponentsClientHttpRequestFactory())
            .build();
    }

    public YozoConfig getConfig() {
        return config;
    }

    public String preview(String fileUrl) throws YozoApiException {
        String officePreviewApiUrl = config.getUri().getPreview() + "?k=%s&url=%s";
        String previewApiUrl = String.format(officePreviewApiUrl, config.getKey(), fileUrl);
        try {
            YozoPreviewResponse response = restClient
                .get()
                .uri(URI.create(previewApiUrl))
                .retrieve()
                .body(YozoPreviewResponse.class);
            if (response == null) {
                throw new YozoApiException("no response content");
            }
            handleResponse(response);
            if (response.getData() == null) {
                throw new YozoApiException("file preview failed");
            }
            return response.getData().getData();
        } catch (RestClientException exception) {
            // request was aborted
            throw new YozoApiException("request was aborted", exception);
        }
    }

    protected <T extends YozoBaseResponse> void handleResponse(T response) {
        if (response.getErrorCode() != 0) {
            throw new YozoApiException(
                String.format("request was aborted, ErrorCode: %d, Message: %s",
                    response.getErrorCode(), response.getMessage()));
        }
    }
}
