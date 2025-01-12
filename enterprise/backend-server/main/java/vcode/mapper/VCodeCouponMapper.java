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

import com.apitable.enterprise.vcode.vo.VCodeCouponPageVo;
import com.apitable.enterprise.vcode.vo.VCodeCouponVo;
import com.apitable.enterprise.vcode.entity.CodeCouponTemplateEntity;

/**
 * <p>
 * VCode Coupon Mapper
 * </p>
 */
public interface VCodeCouponMapper extends BaseMapper<CodeCouponTemplateEntity> {

    /**
     * Check if the specified activity exists
     */
    Integer countById(@Param("id") Long id);

    /**
     * Update total count
     */
    Integer updateTotalCountById(@Param("userId") Long userId, @Param("id") Long id, @Param("count") Integer count);

    /**
     * Update Comment
     */
    Integer updateCommentById(@Param("userId") Long userId, @Param("id") Long id, @Param("comment") String comment);

    /**
     * Update delete status
     */
    int removeById(@Param("userId") Long userId, @Param("id") Long id);

    /**
     * Get basic information of coupon template
     */
    List<VCodeCouponVo> selectBaseInfo(@Param("keyword") String keyword);

    /**
     * Paginate to get the details of the coupon template
     */
    IPage<VCodeCouponPageVo> selectDetailInfo(Page page, @Param("keyword") String keyword);
}
