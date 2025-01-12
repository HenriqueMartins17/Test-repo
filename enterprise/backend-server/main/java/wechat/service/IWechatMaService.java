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

import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;

import com.apitable.enterprise.wechat.vo.WeChatLoginResultVo;
import com.apitable.enterprise.wechat.vo.WechatInfoVo;

/**
 * <p>
 * WeChat Ma Service
 * </p>
 */
public interface IWechatMaService {

    /**
     * Get login result
     */
    WeChatLoginResultVo getLoginResult(Long userId, Long wechatMemberId);

    /**
     * Ma login
     */
    WeChatLoginResultVo login(WxMaJscode2SessionResult result);

    /**
     * Sign in by wechat phone
     */
    WeChatLoginResultVo signIn(Long wechatMemberId, WxMaPhoneNumberInfo phoneNoInfo);

    /**
     * Get user info
     */
    WechatInfoVo getUserInfo(Long userId);
}
