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

package com.apitable.enterprise.appstore.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import com.apitable.enterprise.appstore.entity.MarketplaceSpaceAppRelEntity;


/**
 * <p>
 * Marketplace Space App Mapper
 * </p>
 */
public interface MarketplaceSpaceAppMapper extends BaseMapper<MarketplaceSpaceAppRelEntity> {

    /**
     * Query appid by space id
     */
    @Deprecated
    List<String> selectBySpaceId(@Param("spaceId") String spaceId);

    /**
     * Query count by space id and appid
     */
    @Deprecated
    Integer selectCountBySpaceIdAndAppId(@Param("spaceId") String spaceId, @Param("appId") String appId);

    /**
     * Delete by space id and appid
     */
    @Deprecated
    int deleteBySpaceIdAndAppId(@Param("spaceId") String spaceId, @Param("appId") String appId);

    /**
     * Query By space id and appid
     */
    MarketplaceSpaceAppRelEntity selectBySpaceIdAndAppId(@Param("spaceId") String spaceId, @Param("appId") String appId);
}
