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
import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;
import com.apitable.enterprise.wechat.dto.ThirdPartyMemberInfo;
import me.chanjar.weixin.mp.bean.result.WxMpUser;

import com.vikadata.social.qq.model.TencentUserInfo;
import com.vikadata.social.qq.model.WebAppAuthInfo;

/**
 * <p>
 * Third party system - member information table service interface
 * </p>
 */
public interface IThirdPartyMemberService {

    /**
     * Get unionId
     *
     * @param appId  appId
     * @param openId openId
     * @param type   Type
     * @return unionId
     */
    String getUnionIdByCondition(String appId, String openId, Integer type);

    /**
     * Get third-party nicknames
     *
     * @param appId appId
     * @param unionId unionId
     * @param type Type
     * @return nickName
     */
    ThirdPartyMemberInfo getMemberInfoByCondition(String appId, String unionId, Integer type);

    /**
     * Create a WeChat official account member
     *
     * @param appId    appId
     * @param wxMpUser WeChat User information
     */
    void createMpMember(String appId, WxMpUser wxMpUser);

    /**
     * Create WeChat applet member
     *
     * @param appId  appId
     * @param result WeChat Login Return Results
     * @return ID
     */
    Long createMiniAppMember(String appId, WxMaJscode2SessionResult result);

    /**
     * Update WeChat applet member information
     *
     * @param id          WeChat member ID
     * @param result      WeChat Authorization return result
     * @param phoneNoInfo Mobile number information
     * @param userInfo    User information
     */
    void editMiniAppMember(Long id, WxMaJscode2SessionResult result, WxMaPhoneNumberInfo phoneNoInfo, WxMaUserInfo userInfo);

    /**
     * Create QQ member
     *
     * @param authInfo Authorization information
     * @param userInfo User information
     */
    void createTencentMember(WebAppAuthInfo authInfo, TencentUserInfo userInfo);
}
