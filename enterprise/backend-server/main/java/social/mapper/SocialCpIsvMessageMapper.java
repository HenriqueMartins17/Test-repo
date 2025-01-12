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
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.apitable.enterprise.social.entity.SocialCpIsvEventLogEntity;

/**
 * <p>
 * Third party platform integration - WeCom third-party service provider application message notification information
 * </p>
 */
@Mapper
public interface SocialCpIsvMessageMapper extends BaseMapper<SocialCpIsvEventLogEntity> {
    /**
     * update status by id
     * @param id primary key
     * @param status status
     */
    int updateStatusById(@Param("id") Long id, @Param("status") int status);
}
