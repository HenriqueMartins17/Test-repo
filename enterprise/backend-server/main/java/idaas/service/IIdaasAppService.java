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

import com.apitable.enterprise.idaas.entity.IdaasAppEntity;

/**
 * <p>
 * IDaaS Application information
 * </p>
 */
public interface IIdaasAppService extends IService<IdaasAppEntity> {

    /**
     * Get application information
     *
     * @param clientId Application's Client ID
     * @return Application
     */
    IdaasAppEntity getByClientId(String clientId);

}
