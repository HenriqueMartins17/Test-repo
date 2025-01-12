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

package com.apitable.enterprise.vcode.service;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.apitable.enterprise.vcode.ro.VCodeCouponRo;
import com.apitable.enterprise.vcode.vo.VCodeCouponPageVo;
import com.apitable.enterprise.vcode.vo.VCodeCouponVo;

/**
 * <p>
 * VCode Coupon Service
 * </p>
 */
public interface IVCodeCouponService {

    /**
     * Get VCode Voucher Template Information
     */
    List<VCodeCouponVo> getVCodeCouponVo(String keyword);

    /**
     * Get the page view information of the VCode coupon template
     */
    IPage<VCodeCouponPageVo> getVCodeCouponPageVo(Page<VCodeCouponPageVo> page, String keyword);

    /**
     * Check whether the voucher model exists
     */
    void checkCouponIfExist(Long templateId);

    /**
     * Create coupon
     */
    Long create(VCodeCouponRo ro);

    /**
     * Edit coupon
     */
    void edit(Long userId, Long templateId, VCodeCouponRo ro);

    /**
     * Delete coupon
     */
    void delete(Long userId, Long templateId);
}
