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

import java.util.List;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.apitable.enterprise.idaas.mapper.IdaasGroupBindMapper;
import com.apitable.enterprise.idaas.service.IIdaasGroupBindService;
import com.apitable.enterprise.idaas.entity.IdaasGroupBindEntity;

import org.springframework.stereotype.Service;

/**
 * <p>
 * IDaaS User group binding information
 * </p>
 */
@Service
public class IdaasGroupBindServiceImpl extends ServiceImpl<IdaasGroupBindMapper, IdaasGroupBindEntity> implements IIdaasGroupBindService {

    @Override
    public List<IdaasGroupBindEntity> getAllBySpaceId(String spaceId) {
        return getBaseMapper().selectAllBySpaceId(spaceId);
    }

    @Override
    public List<IdaasGroupBindEntity> getAllBySpaceIdIgnoreDeleted(String spaceId) {
        return getBaseMapper().selectAllBySpaceIdIgnoreDeleted(spaceId);
    }

}
