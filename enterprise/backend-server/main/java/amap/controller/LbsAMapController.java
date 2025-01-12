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

package com.apitable.enterprise.amap.controller;

import cn.hutool.core.util.URLUtil;
import com.apitable.enterprise.amap.properties.AMapProperties;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.GetResource;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * AutoNavi Interface.
 * </p>
 */
@RestController
@ApiResource
@Tag(name = "AutoNavi Interface")
@Slf4j
public class LbsAMapController {

    @Resource
    private AMapProperties aMapProperties;

    /**
     * proxy.
     */
    @GetResource(path = "/_AMapService/**", requiredLogin = false)
    public void proxy(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String query =
            request.getQueryString().concat("&jscode=").concat(aMapProperties.getJscode());
        String uri = request.getRequestURI();
        URI newUri = URLUtil.toURI(aMapProperties.getRestapi().getProxyPass() + "?" + query);
        if (uri.contains("styles")) {
            newUri = URLUtil.toURI(aMapProperties.getStyles().getProxyPass() + "?" + query);
        }
        if (uri.contains("vectormap")) {
            newUri = URLUtil.toURI(aMapProperties.getVectormap().getProxyPass() + "?" + query);
        }
        // Execute Proxy Query
        String methodName = request.getMethod();
        HttpMethod httpMethod = HttpMethod.valueOf(methodName);

        ClientHttpRequest delegate =
            new SimpleClientHttpRequestFactory().createRequest(newUri, httpMethod);
        Enumeration<String> headerNames = request.getHeaderNames();
        // Set request header
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            Enumeration<String> v = request.getHeaders(headerName);
            List<String> arr = new ArrayList<>();
            while (v.hasMoreElements()) {
                arr.add(v.nextElement());
            }
            delegate.getHeaders().addAll(headerName, arr);
        }

        StreamUtils.copy(request.getInputStream(), delegate.getBody());
        // Execute remote call
        try (ClientHttpResponse clientHttpResponse = delegate.execute()) {
            response.setStatus(clientHttpResponse.getStatusCode().value());
            // Set Response Header
            clientHttpResponse.getHeaders().forEach((key, value) -> value.forEach(it -> {
                response.setHeader(key, it);
            }));
            StreamUtils.copy(clientHttpResponse.getBody(), response.getOutputStream());
        }
    }
}
