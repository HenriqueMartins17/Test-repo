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

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.apitable.enterprise.vcode.entity.CodeEntity;
import com.apitable.enterprise.vcode.vo.VCodePageVo;

/**
 * <p>
 * VCode Mapper
 * </p>
 */
public interface VCodeMapper extends BaseMapper<CodeEntity> {

    /**
     * Query the number of specified VCode
     */
    Integer countByCode(@Param("code") String code);

    /**
     * Query entity
     */
    CodeEntity selectByCode(@Param("code") String code);

    /**
     * Query invitation code
     */
    Long selectRefIdByCodeAndType(@Param("code") String code, @Param("type") Integer type);

    /**
     * Query the total number of usable
     */
    Integer selectAvailableTimesByCode(@Param("code") String code);

    /**
     * Query code
     */
    String selectCodeByTypeAndRefId(@Param("type") Integer type, @Param("refId") Long refId);

    /**
     * Query type
     */
    Integer selectTypeByCode(@Param("code") String code);

    /**
     * Modify ref ID
     */
    int updateRefIdByCode(@Param("userId") Long userId, @Param("code") String code, @Param("refId") Long refId);

    /**
     * Modify the total number of available and remaining times
     */
    int updateAvailableTimesByCode(@Param("userId") Long userId, @Param("code") String code, @Param("avail") Integer avail, @Param("remain") Integer remain);

    /**
     * Modify the number of uses for a single person
     */
    int updateLimitTimesByCode(@Param("userId") Long userId, @Param("code") String code, @Param("times") Integer times);

    /**
     * Modify expiration time
     */
    int updateExpiredAtByCode(@Param("userId") Long userId, @Param("code") String code, @Param("expireTime") LocalDateTime expireTime);

    /**
     * Upadte delete status
     */
    int removeByCode(@Param("userId") Long userId, @Param("code") String code);

    /**
     * Batch insert
     */
    int insertList(@Param("entities") List<CodeEntity> entities);

    /**
     * Query the number of VCode for a specified activity
     */
    Integer countByActivityId(@Param("activityId") Long activityId);

    /**
     * Reduce the number of remaining
     */
    int subRemainTimes(@Param("code") String code);

    /**
     * Get the VCode available for the specified activity
     */
    List<String> getAvailableCode(@Param("activityId") Long activityId);

    /**
     * Get the VCode received by the specified operator at the specified event
     */
    String getAcquiredCode(@Param("activityId") Long activityId, @Param("operator") Long operator);

    /**
     * Paginate to get VCode details
     */
    IPage<VCodePageVo> selectDetailInfo(Page<VCodePageVo> page, @Param("type") Integer type, @Param("activityId") Long activityId);

    /**
     * Query the number of VCode exchanged for the exchange code
     */
    Integer selectIntegral(@Param("code") String code);
}
