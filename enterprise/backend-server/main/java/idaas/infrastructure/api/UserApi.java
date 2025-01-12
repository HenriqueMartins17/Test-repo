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

import java.util.List;
import java.util.Objects;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;

import com.apitable.enterprise.idaas.infrastructure.IdaasApiException;
import com.apitable.enterprise.idaas.infrastructure.IdaasTemplate;
import com.apitable.enterprise.idaas.infrastructure.constant.ApiUri;
import com.apitable.enterprise.idaas.infrastructure.model.UsersRequest;
import com.apitable.enterprise.idaas.infrastructure.model.UsersResponse;
import com.apitable.enterprise.idaas.infrastructure.model.UsersResponse.UserResponse;
import com.apitable.enterprise.idaas.infrastructure.model.UsersResponse.UserResponse.Values;
import com.apitable.enterprise.idaas.infrastructure.support.ServiceAccount;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * <p>
 * User API
 * </p>
 *
 */
public class UserApi {

    private final IdaasTemplate idaasTemplate;
    private final String contactHost;

    public UserApi(IdaasTemplate idaasTemplate, String contactHost) {
        this.idaasTemplate = idaasTemplate;
        this.contactHost = contactHost;
    }

    /**
     * get user list
     *
     * @param request request parameters
     * @param serviceAccount tenant ServiceAccount
     * @param tenantName tenant name
     * @return result
     */
    public UsersResponse users(UsersRequest request, ServiceAccount serviceAccount, String tenantName) throws IdaasApiException {
        MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
        if (CharSequenceUtil.isNotBlank(request.getStatus())) {
            multiValueMap.set("status", request.getStatus());
        }
        if (CharSequenceUtil.isNotBlank(request.getDeptId())) {
            multiValueMap.set("dept_id", request.getDeptId());
        }
        if (CharSequenceUtil.isNotBlank(request.getPhoneNum())) {
            multiValueMap.set("phone_num", request.getPhoneNum());
        }
        if (Objects.nonNull(request.getStartTime())) {
            multiValueMap.set("start_time", request.getStartTime());
        }
        if (Objects.nonNull(request.getEndTime())) {
            multiValueMap.set("end_time", request.getEndTime());
        }
        multiValueMap.set("page_index", request.getPageIndex());
        multiValueMap.set("page_size", request.getPageSize());
        if (CollUtil.isNotEmpty(request.getOrderBy())) {
            multiValueMap.addAll("order_by", request.getOrderBy());
        }

        UsersResponse usersResponse = idaasTemplate.get(contactHost + ApiUri.USERS, multiValueMap, UsersResponse.class, tenantName, serviceAccount, null);
        List<UserResponse> userResponses = usersResponse.getData();
        if (CollUtil.isNotEmpty(userResponses)) {
            for (UserResponse userResponse : userResponses) {
                Values userValues = userResponse.getValues();
                // If a user does not have a mobile phone number or email address, empty characters will be transmitted.
                // Set this parameter to null to prevent field duplication in future saving.
                if (CharSequenceUtil.isBlank(userValues.getPhoneNum())) {
                    userValues.setPhoneNum(null);
                }
                if (CharSequenceUtil.isBlank(userValues.getPrimaryMail())) {
                    userValues.setPrimaryMail(null);
                }
            }
        }

        return usersResponse;
    }

}
