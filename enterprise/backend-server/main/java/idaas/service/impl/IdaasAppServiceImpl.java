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

package com.apitable.enterprise.idaas.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.apitable.enterprise.idaas.mapper.IdaasAppMapper;
import com.apitable.enterprise.idaas.service.IIdaasAppService;
import com.apitable.enterprise.idaas.entity.IdaasAppEntity;

import org.springframework.stereotype.Service;

/**
 * <p>
 * IDaaS application information
 * </p>
 */
@Service
public class IdaasAppServiceImpl extends ServiceImpl<IdaasAppMapper, IdaasAppEntity> implements IIdaasAppService {

    @Override
    public IdaasAppEntity getByClientId(String clientId) {
        return getBaseMapper().selectByClientId(clientId);
    }

}
