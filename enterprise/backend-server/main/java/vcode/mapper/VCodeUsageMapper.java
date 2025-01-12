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

package com.apitable.enterprise.vcode.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.apitable.enterprise.vcode.dto.VCodeDTO;
import com.apitable.enterprise.vcode.entity.CodeUsageEntity;

import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * VCode Usage Mapper
 * </p>
 */
public interface VCodeUsageMapper extends BaseMapper<CodeUsageEntity> {

    /**
     * Get the number of operations of the specified type of VCode
     */
    Integer countByCodeAndType(@Param("code") String code, @Param("type") Integer type, @Param("operator") Long operator);

    /**
     * Get the user information of the inviter
     */
    VCodeDTO selectInvitorUserId(@Param("userId") Long userId);
}
