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

package com.apitable.enterprise.teg.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apitable.enterprise.teg.autoconfigure.TegProperties;
import com.apitable.enterprise.teg.autoconfigure.TegProperties.SmartProxyHeaderProperty;
import com.apitable.enterprise.teg.autoconfigure.UnauthorizedResponseCustomizer;

import org.springframework.core.Ordered;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * smartProxy Filter.
 */
public class SmartProxyFilter extends OncePerRequestFilter implements Ordered {

    private static final Logger LOG = LoggerFactory.getLogger(SmartProxyFilter.class);

    private final UnauthorizedResponseCustomizer customizer;

    private final TegProperties properties;

    private static final PathMatcher MATCHER = new AntPathMatcher();

    private static final int VALIDTTIME = 180;

    private static final int SMARTPROXYORDER = 100;

    public SmartProxyFilter(final UnauthorizedResponseCustomizer customizer,
                            final TegProperties properties) {
        this.customizer = customizer;
        this.properties = properties;
    }

    /**
     * Set the order value of this object.
     *
     * @see #getOrder()
     */
    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE - SMARTPROXYORDER;
    }

    /**
     * DoFilterInternal with teg Check.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    final FilterChain filterChain)
        throws ServletException, IOException {
        try {
            if (LOG.isDebugEnabled()) {
                httpHeaderLog(request);
            }
            String serverPath = request.getServletPath();
            if (isIgnoreUrl(serverPath)) {
                LOG.debug("Paths configured to ignore,url:{}", serverPath);
                request.setAttribute(SmartProxyHeaderProperty.REQUEST_IGNORE_URL, serverPath);
                filterChain.doFilter(request, response);
                return;
            }
            // Remove ignore Url in the request to prevent forgery
            request.removeAttribute(SmartProxyHeaderProperty.REQUEST_IGNORE_URL);

            // Mandatory URL for authentication checks
            request.setAttribute(SmartProxyHeaderProperty.REQUEST_CHECK_URL,
                isCheckUrl(serverPath));

            // Check the signature to confirm the identity of the jwt
            String signature = request.getHeader(SmartProxyHeaderProperty.REQUEST_SIGNATURE);
            String staffId = request.getHeader(SmartProxyHeaderProperty.REQUEST_STAFFID);
            String timestampStr = request.getHeader(SmartProxyHeaderProperty.REQUEST_TIMESTAMP);
            String xRioSeq = request.getHeader(SmartProxyHeaderProperty.REQUEST_X_RIO_SEQ);
            String staffName = request.getHeader(SmartProxyHeaderProperty.REQUEST_STAFFNAME);
            String extData = request.getHeader(SmartProxyHeaderProperty.REQUEST_X_EXT_DATA);
            if (!StringUtils.hasText(signature) && !StringUtils.hasText(staffName)) {
                // No signature information, no verification
                LOG.info("No signature data identifying teg-Smart Proxy,url:{}", serverPath);
                // Does not identify teg-Smart Proxy identity, returns custom content
                filterChain.doFilter(request, response);
                return;
            }

            long timestamp = Long.parseLong(timestampStr);
            long now = System.currentTimeMillis() / 1000;
            if (Math.abs(now - timestamp) > VALIDTTIME || !signature.equalsIgnoreCase(toSHA256(
                timestampStr + properties.getTokenKey() + xRioSeq + "," + staffId
                    + "," + staffName + "," + extData + timestampStr))) {
                LOG.info("SmartProxy checkSignature Verification failed ,timestamp = {} , "
                    + "signature = {} , url= {}", timestampStr, signature, serverPath);
                customizer.customize(response);
                return;
            }

            filterChain.doFilter(request, response);
        } catch (Throwable e) {
            LOG.error("SmartProxyFilter check exception", e);
            throw new ServletException(e);
        }
    }

    /**
     * Get url is ingoreUrl?.
     *
     * @param url url string
     * @return boolean
     */
    private boolean isIgnoreUrl(final String url) {
        if (properties.getIgnoreUrls() == null || properties.getIgnoreUrls().size() == 0) {
            return false;
        }
        for (String ignoreUrl : properties.getIgnoreUrls()) {
            if (MATCHER.match(ignoreUrl, url)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get url is checkUrl?.
     *
     * @param url url string
     * @return boolean
     */
    private boolean isCheckUrl(final String url) {
        if (properties.getCheckUrls() == null || properties.getCheckUrls().size() == 0) {
            return false;
        }
        for (String ignoreUrl : properties.getCheckUrls()) {
            if (MATCHER.match(ignoreUrl, url)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Debug output request header.
     *
     * @param request HttpServletRequest
     */
    private void httpHeaderLog(final HttpServletRequest request) {
        Map<String, Object> map = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String header = headerNames.nextElement();
            map.put(header, request.getHeader(header));
        }
        Gson gson = new Gson();
        LOG.debug("Http Headers >>>>{}", gson.toJson(map));
    }


    public static String toSHA256(final String str) {
        String encodeStr = "";
        try {
            MessageDigest messageDigest;
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(str.getBytes(StandardCharsets.UTF_8));
            encodeStr = byte2Hex(messageDigest.digest());
        } catch (Exception e) {
            LOG.error("toSHA256 str:{},url:{}", str, e.getMessage());
        }
        return encodeStr;
    }

    private static String byte2Hex(final byte[] bytes) {
        StringBuilder result = new StringBuilder();
        String temp;
        for (byte aByte : bytes) {
            temp = Integer.toHexString(aByte & 0xFF);
            if (temp.length() == 1) {
                result.append("0");
            }
            result.append(temp);
        }
        return result.toString();
    }

}