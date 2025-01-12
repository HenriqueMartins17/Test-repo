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

package com.apitable.enterprise.social.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.apitable.enterprise.social.entity.SocialUserEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Third party platform integration - user mapper
 */
public interface SocialUserMapper extends BaseMapper<SocialUserEntity> {

    /**
     * Quick Bulk Insert
     *
     * @param entities Member List
     * @return Number of execution results
     */
    int insertBatch(@Param("entities") List<SocialUserEntity> entities);

    /**
     * Query according to OPEN ID
     *
     * @param unionId Third party platform user ID
     * @return SocialUserEntity
     */
    SocialUserEntity selectByUnionId(@Param("unionId") String unionId);

    /**
     * Batch Delete Records
     *
     * @param unionIds Third party platform user ID
     * @return Number of execution results
     */
    int deleteByUnionIds(@Param("unionIds") List<String> unionIds);
}
