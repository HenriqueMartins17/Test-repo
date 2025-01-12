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

package com.apitable.enterprise.social.service;

import java.util.List;

import com.apitable.enterprise.social.model.FeishuTenantContact;
import com.vikadata.social.feishu.exception.ContactAccessDeniesException;
import com.vikadata.social.feishu.model.v3.FeishuDeptObject;
import com.vikadata.social.feishu.model.v3.FeishuUserObject;

import org.springframework.util.MultiValueMap;

/**
 * Lark Enterprise Address Book Service
 */
public interface IFeishuTenantContactService {

    /**
     * Pull enterprise address book and assemble data structure
     *
     * @param tenantKey Tenant
     * @return MultiValueMap<FeishuDeptObject, FeishuUserObject>
     */
    MultiValueMap<FeishuDeptObject, FeishuUserObject> fetchTenantContact(String tenantKey) throws ContactAccessDeniesException;

    /**
     * Request tenant address book
     *
     * @param tenantKey Tenant
     * @return FeishuTenantContact
     */
    FeishuTenantContact requestTenantContact(String tenantKey);

    /**
     * Assemble tenant address book structured data
     *
     * @param tenantKey Tenant
     * @param deptObjects List of first level departments
     * @param userObjects First level user list
     * @return Structured data
     */
    MultiValueMap<FeishuDeptObject, FeishuUserObject> fetchTenantContact(String tenantKey, List<FeishuDeptObject> deptObjects, List<FeishuUserObject> userObjects);
}
