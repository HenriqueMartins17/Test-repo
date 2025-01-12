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

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.apitable.enterprise.vcode.entity.CodeActivityEntity;
import com.apitable.enterprise.vcode.vo.VCodeActivityPageVo;
import com.apitable.enterprise.vcode.vo.VCodeActivityVo;

/**
 * <p>
 * VCode Activity Mapper
 * </p>
 */
public interface VCodeActivityMapper extends BaseMapper<CodeActivityEntity> {

    /**
     * Get all active scene values
     */
    List<String> selectAllScene();

    /**
     * Query table id
     */
    Long selectIdByScene(@Param("scene") String scene);

    /**
     * Update name
     */
    int updateNameById(@Param("userId") Long userId, @Param("id") Long id, @Param("name") String name);

    /**
     * Update scene
     */
    int updateSceneById(@Param("userId") Long userId, @Param("id") Long id, @Param("scene") String scene);

    /**
     * Update delete status
     */
    int removeById(@Param("userId") Long userId, @Param("id") Long id);

    /**
     * Query count(Check if the specified activity exists)
     */
    Integer countById(@Param("id") Long id);

    /**
     * Get basic event information
     */
    List<VCodeActivityVo> selectBaseInfo(@Param("keyword") String keyword);

    /**
     * Paginate to get event details
     */
    IPage<VCodeActivityPageVo> selectDetailInfo(Page<VCodeActivityPageVo> page, @Param("keyword") String keyword, @Param("appId") String appId);

    /**
     * Query the number of QR codes corresponding to the activity
     */
    Integer countQrCodeByIdAndAppId(@Param("id") Long id, @Param("appId") String appId);
}
