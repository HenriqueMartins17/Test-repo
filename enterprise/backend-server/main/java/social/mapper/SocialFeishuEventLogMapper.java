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
import org.apache.ibatis.annotations.Param;

import com.apitable.enterprise.social.entity.SocialFeishuEventLogEntity;

/**
 * Lark Event Log Mapper
 */
public interface SocialFeishuEventLogMapper extends BaseMapper<SocialFeishuEventLogEntity> {

    /**
     * Query total number of UUIDs
     *
     * @param uuid Event unique identifier
     * @return Total
     */
    Integer selectCountByUuid(@Param("uuid") String uuid);

    /**
     * Query by UUID
     *
     * @param uuid Event unique identifier
     * @return Social Lark Event Log Entity
     */
    SocialFeishuEventLogEntity selectByUuid(@Param("uuid") String uuid);

    /**
     * Update event processing status
     *
     * @param uuid Event unique identifier
     * @return Number of execution results
     */
    int updateStatusTrueByUuid(@Param("uuid") String uuid);

    /**
     * Query the latest event type of the tenant
     * @param tenantKey Enterprise ID
     * @param type Event Type
     * @return Social Lark Event Log Entity
     */
    String selectLatestByTenantKeyAndType(@Param("tenantKey") String tenantKey, @Param("type") String type);
}
