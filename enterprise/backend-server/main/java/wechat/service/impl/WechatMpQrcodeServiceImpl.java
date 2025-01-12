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

package com.apitable.enterprise.wechat.service.impl;

import jakarta.annotation.Resource;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;

import com.apitable.base.enums.DatabaseException;
import com.apitable.enterprise.wechat.vo.QrCodePageVo;
import com.apitable.enterprise.wechat.mapper.WechatMpQrcodeMapper;
import com.apitable.enterprise.wechat.service.IWechatMpQrcodeService;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.enterprise.wechat.entity.WechatMpQrcodeEntity;

import org.springframework.stereotype.Service;

/**
 * <p>
 * WeChat Mp Qrcode Service Implement Class
 * </p>
 */
@Slf4j
@Service
public class WechatMpQrcodeServiceImpl implements IWechatMpQrcodeService {

    @Resource
    private WechatMpQrcodeMapper wechatMpQrcodeMapper;

    @Override
    public IPage<QrCodePageVo> getQrCodePageVo(Page<QrCodePageVo> page, String appId) {
        return wechatMpQrcodeMapper.selectDetailInfo(page, appId);
    }

    @Override
    public void save(String appId, String type, String scene, WxMpQrCodeTicket ticket) {
        log.info("Save QRcode information. appId:{}，type:{}，scene:{}，WxMpQrCodeTicket:{}", appId,
            type, scene, ticket);
        WechatMpQrcodeEntity entity = WechatMpQrcodeEntity.builder()
            .appId(appId)
            .type(type)
            .scene(scene)
            .ticket(ticket.getTicket())
            .expireSeconds(ticket.getExpireSeconds())
            .url(ticket.getUrl())
            .build();
        boolean flag = SqlHelper.retBool(wechatMpQrcodeMapper.insert(entity));
        ExceptionUtil.isTrue(flag, DatabaseException.INSERT_ERROR);
    }

    @Override
    public void delete(Long userId, Long qrCodeId, String appId) {
        boolean flag =
            SqlHelper.retBool(wechatMpQrcodeMapper.removeByIdAndAppId(userId, qrCodeId, appId));
        ExceptionUtil.isTrue(flag, DatabaseException.DELETE_ERROR);
    }
}
