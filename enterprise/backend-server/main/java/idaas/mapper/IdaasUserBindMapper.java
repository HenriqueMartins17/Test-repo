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

package com.apitable.enterprise.idaas.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.apitable.enterprise.idaas.entity.IdaasUserBindEntity;

/**
 * <p>
 * IDaaS User binding information
 * </p>
 */
@Mapper
public interface IdaasUserBindMapper extends BaseMapper<IdaasUserBindEntity> {

    /**
     * Query binding information according to IDaaS user ID
     *
     * @param userId IDaaS user's id
     * @return bind information
     */
    IdaasUserBindEntity selectByUserId(String userId);

    /**
     * Query binding information according to IDaaS user ID, including deleted
     *
     * @param userIds IDaaS user's id
     * @return Binding information, including deleted
     */
    List<IdaasUserBindEntity> selectAllByUserIdsIgnoreDeleted(@Param("userIds") List<String> userIds);

    /**
     * Query the binding information according to the user ID of Vigor, including the deleted
     *
     * @param vikaUserIds vika user id
     * @return Binding information, including deleted
     */
    List<IdaasUserBindEntity> selectAllByVikaUserIdsIgnoreDeleted(@Param("vikaUserIds") List<Long> vikaUserIds);

}
