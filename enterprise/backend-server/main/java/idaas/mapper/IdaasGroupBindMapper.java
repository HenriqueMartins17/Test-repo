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

import com.apitable.enterprise.idaas.entity.IdaasGroupBindEntity;

/**
 * <p>
 * IDaaS user group binding information
 * </p>
 */
@Mapper
public interface IdaasGroupBindMapper extends BaseMapper<IdaasGroupBindEntity> {

    /**
     * Get all user groups bound to the space
     *
     * @param spaceId space's id
     * @return all user groups bound to the space
     */
    List<IdaasGroupBindEntity> selectAllBySpaceId(@Param("spaceId") String spaceId);

    /**
     * Get all user groups bound to the space, include is deleted
     *
     * @param spaceId space's id
     * @return all user groups bound to the space
     */
    List<IdaasGroupBindEntity> selectAllBySpaceIdIgnoreDeleted(@Param("spaceId") String spaceId);

}
