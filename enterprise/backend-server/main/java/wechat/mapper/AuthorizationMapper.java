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

package com.apitable.enterprise.wechat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import com.apitable.enterprise.wechat.entity.WechatAuthorizationEntity;

/**
 * <p>
 * Authorization Mapper
 * </p>
 */
public interface AuthorizationMapper extends BaseMapper<WechatAuthorizationEntity> {

    /**
     * Query count
     */
    Integer countByAuthorizerAppid(@Param("authorizerAppid") String authorizerAppid);

    /**
     * Query entity
     */
    WechatAuthorizationEntity findByAuthorizerAppid(@Param("authorizerAppid") String authorizerAppid);
}
