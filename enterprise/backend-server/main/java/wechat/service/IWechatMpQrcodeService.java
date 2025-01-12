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

package com.apitable.enterprise.wechat.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;

import com.apitable.enterprise.wechat.vo.QrCodePageVo;

/**
 * <p>
 * WeChat Mp Qrcode Service
 * </p>
 */
public interface IWechatMpQrcodeService {

    /**
     * Get qrcode page view information
     */
    IPage<QrCodePageVo> getQrCodePageVo(Page<QrCodePageVo> page, String appId);

    /**
     * Create qrcode
     */
    void save(String appId, String type, String scene, WxMpQrCodeTicket ticket);

    /**
     * Delete qrcode
     */
    void delete(Long userId, Long qrCodeId, String appId);
}
