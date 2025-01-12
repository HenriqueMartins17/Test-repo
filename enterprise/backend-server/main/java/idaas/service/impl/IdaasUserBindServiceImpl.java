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

import com.apitable.enterprise.idaas.mapper.IdaasUserBindMapper;
import com.apitable.enterprise.idaas.service.IIdaasUserBindService;
import com.apitable.enterprise.idaas.entity.IdaasUserBindEntity;

import org.springframework.stereotype.Service;

/**
 * <p>
 * IDaaS User bound information
 * </p>
 */
@Service
public class IdaasUserBindServiceImpl extends ServiceImpl<IdaasUserBindMapper, IdaasUserBindEntity> implements IIdaasUserBindService {

    @Override
    public IdaasUserBindEntity getByUserId(String userId) {
        return getBaseMapper().selectByUserId(userId);
    }

    @Override
    public List<IdaasUserBindEntity> getAllByUserIdsIgnoreDeleted(List<String> userIds) {
        return getBaseMapper().selectAllByUserIdsIgnoreDeleted(userIds);
    }

    @Override
    public List<IdaasUserBindEntity> getAllByVikaUserIdsIgnoreDeleted(List<Long> vikaUserIds) {
        return getBaseMapper().selectAllByVikaUserIdsIgnoreDeleted(vikaUserIds);
    }

}
