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

import com.baomidou.mybatisplus.extension.service.IService;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;

import com.apitable.enterprise.wechat.entity.WechatAuthorizationEntity;

/**
 * <p>
 * WeChat Open Service
 * </p>
 */
public interface IWechatOpenService extends IService<WechatAuthorizationEntity> {

    /**
     * Add auth info
     */
    WechatAuthorizationEntity addAuthInfo(String authorizationCode);

    /**
     * Add authorize info
     */
    WechatAuthorizationEntity addAuthorizeInfo(String authorizeAppId);

    /**
     * Mp text message handling
     */
    String mpTextMessageProcess(String appId, String openId, WxMpXmlMessage inMessage) throws WxErrorException;

    /**
     * Mp event handling
     */
    String mpEventProcess(String appId, String openId, WxMpXmlMessage inMessage) throws WxErrorException;

}
