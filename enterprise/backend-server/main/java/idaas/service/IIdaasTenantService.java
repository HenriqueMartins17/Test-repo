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

package com.apitable.enterprise.idaas.service;

import com.baomidou.mybatisplus.extension.service.IService;

import com.apitable.enterprise.idaas.model.IdaasTenantCreateRo;
import com.apitable.enterprise.idaas.model.IdaasTenantCreateVo;
import com.apitable.enterprise.idaas.entity.IdaasTenantEntity;

/**
 * <p>
 * DaaS tenant information
 * </p>
 */
public interface IIdaasTenantService extends IService<IdaasTenantEntity> {

    /**
     * Create IDaaS tenant and its default administrator
     *
     * <p>
     * Called only for privatization deployment
     * </p>
     *
     * @param request Request parameters
     * @return Return Results
     */
    IdaasTenantCreateVo createTenant(IdaasTenantCreateRo request);

    /**
     * Query tenant information based on tenant name
     *
     * @param tenantName Tenant Name
     * @return Tenant information
     */
    IdaasTenantEntity getByTenantName(String tenantName);

    /**
     * Query the tenant information according to the space station bound by the application under the tenant
     *
     * @param spaceId space ID
     * @return tenant information
     */
    IdaasTenantEntity getBySpaceId(String spaceId);

}
