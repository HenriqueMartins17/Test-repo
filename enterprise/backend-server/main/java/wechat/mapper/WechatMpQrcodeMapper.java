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

package com.apitable.enterprise.wechat.mapper;

import java.util.List;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.apitable.enterprise.wechat.vo.QrCodeBaseInfo;
import com.apitable.enterprise.wechat.vo.QrCodePageVo;
import com.apitable.enterprise.wechat.entity.WechatMpQrcodeEntity;

/**
 * <p>
 * WeChat Mp Qrcode Mapper
 * </p>
 */
public interface WechatMpQrcodeMapper extends BaseMapper<WechatMpQrcodeEntity> {

    /**
     * Query Info
     */
    List<QrCodeBaseInfo> selectBaseInfo(@Param("appId") String appId, @Param("scene") String scene);

    /**
     * Query detail page
     */
    @InterceptorIgnore(illegalSql = "true")
    IPage<QrCodePageVo> selectDetailInfo(Page<QrCodePageVo> page, @Param("appId") String appId);

    /**
     * Update delete status
     */
    Integer removeByIdAndAppId(@Param("userId") Long userId, @Param("id") Long id, @Param("appId") String appId);
}
