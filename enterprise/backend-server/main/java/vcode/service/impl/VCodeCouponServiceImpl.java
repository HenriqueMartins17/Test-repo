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

package com.apitable.enterprise.vcode.service.impl;

import java.util.List;

import jakarta.annotation.Resource;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import lombok.extern.slf4j.Slf4j;

import com.apitable.enterprise.vcode.mapper.VCodeCouponMapper;
import com.apitable.base.enums.DatabaseException;
import com.apitable.base.enums.ParameterException;
import com.apitable.enterprise.vcode.ro.VCodeCouponRo;
import com.apitable.enterprise.vcode.vo.VCodeCouponPageVo;
import com.apitable.enterprise.vcode.vo.VCodeCouponVo;
import com.apitable.enterprise.vcode.service.IVCodeCouponService;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.core.util.SqlTool;
import com.apitable.enterprise.vcode.entity.CodeCouponTemplateEntity;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.apitable.enterprise.vcode.enums.VCodeException.COUPON_TEMPLATE_NOT_EXIST;

/**
 * <p>
 * VCode Coupon Service Implement Class
 * </p>
 */
@Slf4j
@Service
public class VCodeCouponServiceImpl implements IVCodeCouponService {

    @Resource
    private VCodeCouponMapper vCodeCouponMapper;

    @Override
    public List<VCodeCouponVo> getVCodeCouponVo(String keyword) {
        return vCodeCouponMapper.selectBaseInfo(keyword);
    }

    @Override
    public IPage<VCodeCouponPageVo> getVCodeCouponPageVo(Page<VCodeCouponPageVo> page,
                                                         String keyword) {
        return vCodeCouponMapper.selectDetailInfo(page, keyword);
    }

    @Override
    public void checkCouponIfExist(Long templateId) {
        int count = SqlTool.retCount(vCodeCouponMapper.countById(templateId));
        ExceptionUtil.isTrue(count > 0, COUPON_TEMPLATE_NOT_EXIST);
    }

    @Override
    public Long create(VCodeCouponRo ro) {
        CodeCouponTemplateEntity entity = CodeCouponTemplateEntity.builder()
            .totalCount(ro.getCount())
            .comment(ro.getComment())
            .build();
        boolean flag = SqlHelper.retBool(vCodeCouponMapper.insert(entity));
        ExceptionUtil.isTrue(flag, DatabaseException.INSERT_ERROR);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void edit(Long userId, Long templateId, VCodeCouponRo ro) {
        ExceptionUtil.isTrue(
            ObjectUtil.isNotNull(ro.getCount()) || StrUtil.isNotBlank(ro.getComment()),
            ParameterException.NO_ARG);
        // Check whether the voucher model exists
        this.checkCouponIfExist(templateId);
        if (ObjectUtil.isNotNull(ro.getCount())) {
            boolean flag = SqlHelper.retBool(
                vCodeCouponMapper.updateTotalCountById(userId, templateId, ro.getCount()));
            ExceptionUtil.isTrue(flag, DatabaseException.EDIT_ERROR);
        }
        if (StrUtil.isNotBlank(ro.getComment())) {
            // Check if the scene value has been used
            boolean flag = SqlHelper.retBool(
                vCodeCouponMapper.updateCommentById(userId, templateId, ro.getComment()));
            ExceptionUtil.isTrue(flag, DatabaseException.EDIT_ERROR);
        }
    }

    @Override
    public void delete(Long userId, Long templateId) {
        boolean flag = SqlHelper.retBool(vCodeCouponMapper.removeById(userId, templateId));
        ExceptionUtil.isTrue(flag, DatabaseException.DELETE_ERROR);
    }
}
