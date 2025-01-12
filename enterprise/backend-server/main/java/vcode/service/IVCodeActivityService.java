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

import com.apitable.enterprise.vcode.ro.VCodeActivityRo;
import com.apitable.enterprise.vcode.vo.VCodeActivityPageVo;
import com.apitable.enterprise.vcode.vo.VCodeActivityVo;

/**
 * <p>
 * VCod eActivity Service
 * </p>
 */
public interface IVCodeActivityService {

    /**
     * Get basic event information
     */
    List<VCodeActivityVo> getVCodeActivityVo(String keyword);

    /**
     * Get active pagination view information
     */
    IPage<VCodeActivityPageVo> getVCodeActivityPageVo(Page<VCodeActivityPageVo> page, String keyword);

    /**
     * Check if activity exists
     */
    void checkActivityIfExist(Long activityId);

    /**
     * Create Activity
     */
    Long create(VCodeActivityRo ro);

    /**
     * Edit Activity
     */
    void edit(Long userId, Long activityId, VCodeActivityRo ro);

    /**
     * Delete Activity
     */
    void delete(Long userId, Long activityId);

}
