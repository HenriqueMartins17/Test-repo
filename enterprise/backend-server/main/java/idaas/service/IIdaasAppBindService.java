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

import com.apitable.enterprise.idaas.model.IdaasAppBindRo;
import com.apitable.enterprise.idaas.model.IdaasAppBindVo;
import com.apitable.enterprise.idaas.entity.IdaasAppBindEntity;

/**
 * <p>
 * IDaaS application is bound to the space
 * </p>
 */
public interface IIdaasAppBindService extends IService<IdaasAppBindEntity> {

    /**
     * Query the binding information between the application and the space station
     *
     * @param spaceId Application's Client Secret
     * @return bound information
     */
    IdaasAppBindEntity getBySpaceId(String spaceId);

    /**
     * IDaaS Bind the application under the tenant
     *
     * <p>
     * Called only for privatization deployment
     * </p>
     *
     * @param request Request parameters
     * @return Binding Results
     */
    IdaasAppBindVo bindTenantApp(IdaasAppBindRo request);

}
