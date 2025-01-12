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

import com.apitable.enterprise.wechat.dto.ThirdPartyMemberInfo;
import jakarta.annotation.Resource;

import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.bean.result.WxMpUser;

import com.apitable.base.enums.DatabaseException;
import com.apitable.enterprise.social.entity.ThirdPartyMemberEntity;
import com.apitable.enterprise.user.mapper.UserLinkMapper;
import com.apitable.enterprise.wechat.mapper.ThirdPartyMemberMapper;
import com.apitable.enterprise.wechat.service.IThirdPartyMemberService;
import com.apitable.user.enums.LinkType;
import com.apitable.user.enums.ThirdPartyMemberType;
import com.apitable.core.util.ExceptionUtil;
import com.vikadata.social.qq.model.TencentUserInfo;
import com.vikadata.social.qq.model.WebAppAuthInfo;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.apitable.user.enums.UserException.UPDATE_WECHAT_MEMBER_ERROR;

/**
 * <p>
 * Third party system - member information table service implementation class
 * </p>
 */
@Slf4j
@Service
public class ThirdPartyMemberServiceImpl implements IThirdPartyMemberService {

    @Resource
    private ThirdPartyMemberMapper thirdPartyMemberMapper;

    @Resource
    private UserLinkMapper userLinkMapper;

    @Override
    public String getUnionIdByCondition(String appId, String openId, Integer type) {
        return thirdPartyMemberMapper.selectUnionIdByOpenIdAndType(appId, openId, type);
    }

    @Override
    public ThirdPartyMemberInfo getMemberInfoByCondition(String appId, String unionId,
                                                         Integer type) {
        return thirdPartyMemberMapper.selectInfo(appId, unionId, type);
    }

    @Override
    public void createMpMember(String appId, WxMpUser wxMpUser) {
        log.info("Create a WeChat official account member,wxMpUser：{}", wxMpUser);
        JSONObject extra = JSONUtil.createObj();
        // No regret about sensitive information after user information adjustment on WeChat public platform
        // extra.set("sexDesc", wxMpUser.getSexDesc());
        // extra.set("sex", wxMpUser.getSex());
        // extra.set("city", wxMpUser.getCity());
        // extra.set("province", wxMpUser.getProvince());
        // extra.set("country", wxMpUser.getCountry());
        extra.set("language", wxMpUser.getLanguage());
        extra.set("subscribeTime", wxMpUser.getSubscribeTime());
        extra.set("subscribeScene", wxMpUser.getSubscribeScene());
        extra.set("remark", wxMpUser.getRemark());
        extra.set("groupId", wxMpUser.getGroupId());
        extra.set("tagIds", wxMpUser.getTagIds());
        extra.set("qrScene", wxMpUser.getQrScene());
        extra.set("qrSceneStr", wxMpUser.getQrSceneStr());
        ThirdPartyMemberEntity entity = ThirdPartyMemberEntity.builder()
            .appId(appId)
            .type(ThirdPartyMemberType.WECHAT_PUBLIC_ACCOUNT.getType())
            .openId(wxMpUser.getOpenId())
            .unionId(wxMpUser.getUnionId())
            .extra(extra.toString())
            .build();
        boolean flag = SqlHelper.retBool(thirdPartyMemberMapper.insert(entity));
        ExceptionUtil.isTrue(flag, DatabaseException.INSERT_ERROR);
    }

    @Override
    public Long createMiniAppMember(String appId, WxMaJscode2SessionResult result) {
        log.info("Create WeChat applet members, WxMaJscode2SessionResult：{}", result);
        ThirdPartyMemberEntity entity = ThirdPartyMemberEntity.builder()
            .appId(appId)
            .type(ThirdPartyMemberType.WECHAT_MINIAPP.getType())
            .openId(result.getOpenid())
            .unionId(result.getUnionid())
            .sessionKey(result.getSessionKey())
            .build();
        boolean flag = SqlHelper.retBool(thirdPartyMemberMapper.insert(entity));
        ExceptionUtil.isTrue(flag, DatabaseException.INSERT_ERROR);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editMiniAppMember(Long id, WxMaJscode2SessionResult result,
                                  WxMaPhoneNumberInfo phoneNoInfo, WxMaUserInfo userInfo) {
        log.info(
            "Update WeChat applet member information, id:{}，result:{}，WxMaPhoneNumberInfo:{}，WxMaUserInfo:{}",
            id, result, phoneNoInfo, userInfo);
        ThirdPartyMemberEntity entity = ThirdPartyMemberEntity.builder()
            .id(id)
            .build();
        if (ObjectUtil.isNotNull(result)) {
            entity.setSessionKey(result.getSessionKey());
            entity.setUnionId(result.getUnionid());
        }
        if (ObjectUtil.isNotNull(phoneNoInfo)) {
            entity.setMobile(phoneNoInfo.getPurePhoneNumber());
            String extra = thirdPartyMemberMapper.selectExtraById(id);
            JSONObject extraJson = JSONUtil.parseObj(extra);
            extraJson.set("countryCode", phoneNoInfo.getCountryCode());
            extraJson.set("phoneNumber", phoneNoInfo.getPhoneNumber());
            entity.setExtra(extraJson.toString());
        }
        if (ObjectUtil.isNotNull(userInfo)) {
            entity.setUnionId(userInfo.getUnionId());
            entity.setNickName(userInfo.getNickName());
            entity.setAvatar(userInfo.getAvatarUrl());
            String extra = thirdPartyMemberMapper.selectExtraById(id);
            JSONObject extraJson = JSONUtil.parseObj(extra);
            extraJson.set("gender", userInfo.getGender());
            extraJson.set("language", userInfo.getLanguage());
            extraJson.set("country", userInfo.getCountry());
            extraJson.set("province", userInfo.getProvince());
            extraJson.set("city", userInfo.getCity());
            entity.setExtra(extraJson.toString());
            // Query whether the unionId is bound to the vika account. If yes, it is unnecessary to update the association system information between the original applet and the vika account to avoid two account association records for the same unionId
            Long linkUserId = userLinkMapper.selectUserIdByUnionIdAndType(userInfo.getUnionId(),
                LinkType.WECHAT.getType());
            if (linkUserId == null) {
                // Update account association information
                userLinkMapper.updateNickNameAndUnionIdByOpenId(userInfo.getNickName(),
                    userInfo.getUnionId(), userInfo.getOpenId(), LinkType.WECHAT.getType());
            }
        }
        boolean flag = SqlHelper.retBool(thirdPartyMemberMapper.updateById(entity));
        ExceptionUtil.isTrue(flag, UPDATE_WECHAT_MEMBER_ERROR);
    }

    @Override
    public void createTencentMember(WebAppAuthInfo authInfo, TencentUserInfo userInfo) {
        log.info("Create QQ members, authInfo:{}，userInfo:{}", authInfo.toString(),
            userInfo.toString());
        JSONObject extra = JSONUtil.createObj();
        extra.set("gender", userInfo.getGender());
        extra.set("genderType", userInfo.getGenderType());
        extra.set("province", userInfo.getProvince());
        extra.set("city", userInfo.getCity());
        extra.set("year", userInfo.getYear());
        extra.set("constellation", userInfo.getConstellation());
        ThirdPartyMemberEntity entity = ThirdPartyMemberEntity.builder()
            .appId(authInfo.getClientId())
            .type(ThirdPartyMemberType.TENCENT.getType())
            .openId(authInfo.getOpenId())
            .unionId(authInfo.getUnionId())
            .nickName(userInfo.getNickname())
            .avatar(userInfo.getFigureurlQq1())
            .extra(extra.toString())
            .build();
        boolean flag = SqlHelper.retBool(thirdPartyMemberMapper.insert(entity));
        ExceptionUtil.isTrue(flag, DatabaseException.INSERT_ERROR);
    }
}
