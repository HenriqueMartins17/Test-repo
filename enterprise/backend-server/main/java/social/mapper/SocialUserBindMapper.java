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

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import com.apitable.enterprise.social.entity.SocialUserBindEntity;

/**
 * Third party platform integration - user binding mapper
 */
public interface SocialUserBindMapper extends BaseMapper<SocialUserBindEntity> {

    /**
     * Query user ID
     *
     * @param unionId Third party user ID
     * @return User ID
     */
    Long selectUserIdByUnionId(@Param("unionId") String unionId);

    /**
     * Query user ID
     *
     * @param userId User ID
     * @return User ID
     */
    List<String> selectUnionIdByUserId(@Param("userId") Long userId);

    /**
     * Query by Union Id
     *
     * @param unionIds Third party platform user ID
     * @return SocialUserBindEntity List
     */
    List<SocialUserBindEntity> selectByUnionIds(@Param("unionIds") List<String> unionIds);

    /**
     * Batch Delete Records
     *
     * @param unionIds Third party platform user ID
     * @return Number of execution results
     */
    int deleteByUnionIds(@Param("unionIds") List<String> unionIds);

    /**
     * Physical deletion based on user ID
     *
     * @param userId
     * @return Number of execution results
     */
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * Get the user's open ID in the enterprise
     *
     * @param appId Application ID
     * @param tenantId Tenant ID
     * @param userId User
     * @return open id
     */
    String selectOpenIdByTenantIdAndUserId(@Param("appId") String appId, @Param("tenantId") String tenantId, @Param("userId") Long userId);
}
