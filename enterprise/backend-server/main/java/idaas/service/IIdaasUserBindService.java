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

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;

import com.apitable.enterprise.idaas.entity.IdaasUserBindEntity;

/**
 * <p>
 * IDaaS User binding information
 * </p>
 */
public interface IIdaasUserBindService extends IService<IdaasUserBindEntity> {

    /**
     * Query binding information according to IDaaS user ID
     *
     * @param userId IDaaS user ID
     * @return Binding information
     */
    IdaasUserBindEntity getByUserId(String userId);

    /**
     * Query binding information according to IDaaS user ID, including deleted
     *
     * @param userIds IDaaS user ID list
     * @return Binding information, including deleted
     */
    List<IdaasUserBindEntity> getAllByUserIdsIgnoreDeleted(List<String> userIds);

    /**
     * Query binding information according to vika user ID, including deleted
     *
     * @param vikaUserIds vika user ID list
     * @return Binding information, including deleted
     */
    List<IdaasUserBindEntity> getAllByVikaUserIdsIgnoreDeleted(List<Long> vikaUserIds);

}
