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

package com.apitable.enterprise.social.infrastructure;

import cn.hutool.core.util.StrUtil;
import com.apitable.enterprise.social.infrastructure.core.ApiBinding;
import com.apitable.enterprise.social.infrastructure.model.WoaAppVisibleRangeResponse;
import com.apitable.enterprise.social.infrastructure.model.WoaCompanyResponse;
import com.apitable.enterprise.social.infrastructure.model.WoaCompanyUserResponse;
import com.apitable.enterprise.social.infrastructure.model.WoaCompanyUserResponse.CompanyUser;
import com.apitable.enterprise.social.infrastructure.model.WoaDepartmentResponse;
import com.apitable.enterprise.social.infrastructure.model.WoaDepartmentResponse.Dept;
import com.apitable.enterprise.social.infrastructure.model.WoaUserInfoResponse;
import com.apitable.enterprise.social.infrastructure.model.WoaUserInfoResponse.User;
import com.apitable.enterprise.social.infrastructure.model.WoaUserTokenResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

/**
 * Woa template.
 */
public class WoaTemplate extends ApiBinding {

    private final String baseUrl;

    private final String tenantId;

    private final String status;

    private static final Logger LOGGER = LoggerFactory.getLogger(WoaTemplate.class);

    private static final String ACCESS_TOKEN_URI = "/auth/v1/user/token?app_id=%s&code=%s";

    private static final String USER_INFO_URI = "/account/v1/user?access_token=%s";

    private static final String COMPANY_TOKEN_URI = "/auth/v1/company/inner/token?app_id=%s";

    private static final String WOA_COMPANY_TOKEN_KEY = "social:woa:company_token:%s";

    private static final String COMPANY_USER_PAGE_URI =
        "/plus/v1/company/company_users?company_token=%s&offset=%s&limit=%s";

    private static final String COMPANY_USER_QUERY_URI =
        "/plus/v1/batch/company/company_users?company_token=%s&company_uids=%s&status=%s";

    private static final String DEPT_USER_PAGE_URI =
        "/plus/v1/company/depts/%s/company_users?company_token=%s&offset=%s&limit=%s&status=%s";

    private static final String DEPT_LIST_URI =
        "/plus/v1/company/depts/%s/children?company_token=%s&offset=%s&limit=%s";

    private static final String DEPT_QUERY_URI =
        "/plus/v1/batch/company/depts?company_token=%s&dept_ids=%s";

    private static final String VISIBLE_RANGE_URI =
        "/kopen/woa/api/v1/developer/app/visible_range?company_token=%s";

    public static final String ROOT_DEPARTMENT_ID = "0";

    public static final int PAGE_QUERY_SIZE = 1000;

    public static final int SPECIFIED_QUERY_MAX_NUMBER = 100;

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    public WoaTemplate(String baseUrl, String tenantId, String queryableUserStatus) {
        this.baseUrl = baseUrl;
        this.tenantId = tenantId;
        this.status = queryableUserStatus;
    }

    public String getDefaultTenantId(){
        return this.tenantId;
    }

    /**
     * Get Access token by auth code.
     */
    public String getUserAccessTokenByCode(String appId, String appKey, String code) {
        String uri = String.format(ACCESS_TOKEN_URI, appId, code);
        HttpHeaders headers = wps3SignHeader(StrUtil.EMPTY, appId, appKey, uri);
        try {
            String url = baseUrl + uri;
            ResponseEntity<WoaUserTokenResponse> response =
                getRestTemplate().exchange(url, HttpMethod.GET,
                    new HttpEntity<>(headers), WoaUserTokenResponse.class);
            LOGGER.info("getAccessToken url:{}, response:{}", url, response);
            assert response.getBody() != null;
            if (response.getBody().getResult() != 0) {
                throw new RuntimeException("Result is" + response.getBody().getResult());
            }
            return response.getBody().getToken().getAccessToken();
        } catch (RestClientException e) {
            throw new RestClientException("Get accessToken Authentication request error. msg: "
                + e.getMessage());
        }
    }

    /**
     * Get Woa user by accessToken.
     */
    public User getUserInfo(String appId, String appKey, String accessToken) {
        String uri = String.format(USER_INFO_URI, accessToken);
        HttpHeaders headers = wps3SignHeader(StrUtil.EMPTY, appId, appKey, uri);
        try {
            String url = baseUrl + uri;
            ResponseEntity<WoaUserInfoResponse> response =
                getRestTemplate().exchange(url, HttpMethod.GET,
                    new HttpEntity<>(headers), WoaUserInfoResponse.class);
            LOGGER.info("WoaUserInfoResponse url:{},res:{}", url, response.getBody());
            assert response.getBody() != null;
            if (response.getBody().getResult() != 0) {
                throw new RuntimeException("Result is" + response.getBody().getResult());
            }
            return response.getBody().getUser();
        } catch (RestClientException e) {
            throw new RestClientException("get userInfo Authentication request error. msg: "
                + e.getMessage());
        }
    }

    /**
     * Get company token by appId.
     */
    public String getCompanyToken(String appId, String appKey) {
        String cacheKey = String.format(WOA_COMPANY_TOKEN_KEY, appId);
        String token = stringRedisTemplate.opsForValue().get(cacheKey);
        if (StrUtil.isNotEmpty(token)) {
            return token;
        }
        String uri = String.format(COMPANY_TOKEN_URI, appId);
        HttpHeaders headers = wps3SignHeader(StrUtil.EMPTY, appId, appKey, uri);
        try {
            String url = baseUrl + uri;
            ResponseEntity<WoaCompanyResponse> response =
                getRestTemplate().exchange(url, HttpMethod.GET,
                    new HttpEntity<>(headers), WoaCompanyResponse.class);
            LOGGER.info("getAccessToken url:{},response:{}", url, response);
            assert response.getBody() != null;
            if (response.getBody().getResult() != 0) {
                throw new RuntimeException("Result is" + response.getBody().getResult());
            }
            WoaCompanyResponse.Token tokenObj = response.getBody().getToken();
            // Cache access_token
            stringRedisTemplate.opsForValue().set(cacheKey, tokenObj.getCompanyToken(),
                tokenObj.getExpiresIn() - 30, TimeUnit.SECONDS);
            return tokenObj.getCompanyToken();
        } catch (RestClientException e) {
            throw new RestClientException("Get accessToken Authentication request error. msg: "
                + e.getMessage());
        }
    }

    public List<CompanyUser> getCompanyUser(String appId, String appKey,
        String companyToken, int pageIndex) {
        int offset = pageIndex * PAGE_QUERY_SIZE;
        String uri = String.format(COMPANY_USER_PAGE_URI, companyToken, offset, PAGE_QUERY_SIZE);
        return this.getWoaUsers(appId, appKey, uri);
    }

    public List<CompanyUser> getCompanyUser(String appId, String appKey,
        String companyToken, String companyUidList) {
        String uri = String.format(COMPANY_USER_QUERY_URI, companyToken,
            companyUidList, status);
        return this.getWoaUsers(appId, appKey, uri);
    }

    public List<CompanyUser> getDeptUsers(String appId, String appKey,
        String companyToken, String deptId, int pageIndex) {
        int offset = pageIndex * PAGE_QUERY_SIZE;
        String uri = String.format(DEPT_USER_PAGE_URI, deptId, companyToken,
            offset, PAGE_QUERY_SIZE, status);
        return this.getWoaUsers(appId, appKey, uri);
    }

    private List<CompanyUser> getWoaUsers(String appId, String appKey, String uri) {
        HttpHeaders headers = wps3SignHeader(StrUtil.EMPTY, appId, appKey, uri);
        try {
            String url = baseUrl + uri;
            ResponseEntity<WoaCompanyUserResponse> response =
                getRestTemplate().exchange(url, HttpMethod.GET,
                    new HttpEntity<>(headers), WoaCompanyUserResponse.class);
            LOGGER.info("getCompanyUser url:{}, response:{}", url, response);
            assert response.getBody() != null;
            if (response.getBody().getResult() != 0) {
                throw new RuntimeException("Result is" + response.getBody().getResult());
            }
            return response.getBody().getCompanyUsers();
        } catch (RestClientException e) {
            throw new RestClientException("Get company users error. Msg: " + e.getMessage());
        }
    }

    public List<Dept> getDepartments(String appId, String appKey,
        String companyToken, String deptId, int pageIndex) {
        int offset = pageIndex * PAGE_QUERY_SIZE;
        String uri = String.format(DEPT_LIST_URI, deptId, companyToken,
            offset, PAGE_QUERY_SIZE);
        return this.getDepartments(appId, appKey, uri);
    }

    public List<Dept> getDepartments(String appId, String appKey,
        String companyToken, String deptIds) {
        String uri = String.format(DEPT_QUERY_URI, companyToken, deptIds);
        return this.getDepartments(appId, appKey, uri);
    }

    private List<Dept> getDepartments(String appId, String appKey, String uri) {
        HttpHeaders headers = wps3SignHeader(StrUtil.EMPTY, appId, appKey, uri);
        try {
            String url = baseUrl + uri;
            ResponseEntity<WoaDepartmentResponse> response =
                getRestTemplate().exchange(url, HttpMethod.GET,
                    new HttpEntity<>(headers), WoaDepartmentResponse.class);
            LOGGER.info("getDepartments url:{}, response:{}", url, response);
            assert response.getBody() != null;
            if (response.getBody().getResult() != 0) {
                throw new RuntimeException("Result is" + response.getBody().getResult());
            }
            return response.getBody().getDepts();
        } catch (RestClientException e) {
            throw new RestClientException("Get company users error. Msg: " + e.getMessage());
        }
    }

    public WoaAppVisibleRangeResponse getAppVisibleRange(String appId, String appKey, String companyToken) {
        String uri = String.format(VISIBLE_RANGE_URI, companyToken);
        HttpHeaders headers = wps3SignHeader(StrUtil.EMPTY, appId, appKey, uri);
        try {
            String url = baseUrl + uri;
            ResponseEntity<WoaAppVisibleRangeResponse> response =
                getRestTemplate().exchange(url, HttpMethod.GET,
                    new HttpEntity<>(headers), WoaAppVisibleRangeResponse.class);
            LOGGER.info("getAppVisibleRange url:{}, response:{}", url, response);
            assert response.getBody() != null;
            if (response.getBody().getResult() != 0) {
                throw new RuntimeException("Result is" + response.getBody().getResult());
            }
            return response.getBody();
        } catch (RestClientException e) {
            throw new RestClientException("Get company users error. Msg: " + e.getMessage());
        }
    }

    /**
     * WPS-3 sign headers.
     */
    private HttpHeaders wps3SignHeader(String data, String appId, String appKey, String uri) {
        HttpHeaders headers = new HttpHeaders();
        String contentMd5 = DigestUtils.md5Hex(data);
        SimpleDateFormat dateFormat =
            new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String rfc1123Date = dateFormat.format(new Date());
        String contentType = "application/json";
        String sign = DigestUtils.sha1Hex(
            Strings.toLowerCase(appKey) + contentMd5 + uri + contentType + rfc1123Date);
        String auth = "WPS-3:" + appId + ":" + sign;
        headers.add("Date", rfc1123Date);
        headers.add("Content-Md5", contentMd5);
        headers.add("Content-Type", contentType);
        headers.add("X-Auth", auth);
        return headers;
    }
}
