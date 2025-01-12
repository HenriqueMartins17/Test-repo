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

import com.apitable.enterprise.social.enums.SocialPlatformType;
import com.apitable.enterprise.social.entity.SocialTenantOrderEntity;

/**
 * <p>
 * Third party platform integration - enterprise tenant order mapper
 * </p>
 */
public interface SocialTenantOrderMapper extends BaseMapper<SocialTenantOrderEntity> {
    /**
     *  Find the order quantity according to the order number
     *
     * @param channelOrderId Channel order No
     * @param tenantId Enterprise ID
     * @param appId App ID
     * @param platform Application Type
     * @return Number
     */
    Integer selectCountByChannelOrderId(@Param("channelOrderId") String channelOrderId, @Param("tenantId") String tenantId,
            @Param("appId") String appId, @Param("platform") SocialPlatformType platform);

    /**
     * Get the order information of the third-party enterprise
     *
     * @param tenantId Enterprise ID
     * @param appId App ID
     * @param platform Application Type
     * @return orderData
     */
    List<String> selectOrderDataByTenantIdAndAppId(@Param("tenantId") String tenantId, @Param("appId") String appId,
            @Param("platform") SocialPlatformType platform);
}
