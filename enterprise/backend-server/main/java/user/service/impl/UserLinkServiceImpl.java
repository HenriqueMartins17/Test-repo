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

package com.apitable.enterprise.user.service.impl;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.Resource;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;

import com.apitable.asset.service.IAssetService;
import com.apitable.asset.vo.AssetUploadResult;
import com.apitable.core.exception.BaseException;
import com.apitable.core.exception.BusinessException;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.enterprise.social.autoconfigure.qq.WebAppProperties;
import com.apitable.enterprise.social.entity.ThirdPartyMemberEntity;
import com.apitable.enterprise.social.service.ISocialUserBindService;
import com.apitable.enterprise.user.entity.UserLinkEntity;
import com.apitable.enterprise.user.mapper.UserLinkMapper;
import com.apitable.enterprise.user.ro.DingTalkBindOpRo;
import com.apitable.enterprise.user.service.IUserLinkService;
import com.apitable.enterprise.vcode.service.IVCodeService;
import com.apitable.enterprise.wechat.autoconfigure.mp.WxMpProperties;
import com.apitable.enterprise.wechat.dto.ThirdPartyMemberInfo;
import com.apitable.enterprise.wechat.mapper.ThirdPartyMemberMapper;
import com.apitable.player.mapper.PlayerActivityMapper;
import com.apitable.shared.cache.bean.AccountLinkDto;
import com.apitable.shared.cache.bean.SocialAuthInfo;
import com.apitable.shared.cache.bean.UserLinkInfo;
import com.apitable.shared.context.SessionContext;
import com.apitable.user.entity.DeveloperEntity;
import com.apitable.user.entity.UserEntity;
import com.apitable.user.enums.LinkType;
import com.apitable.user.enums.ThirdPartyMemberType;
import com.apitable.user.enums.UserException;
import com.apitable.user.mapper.DeveloperMapper;
import com.apitable.user.service.IUserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.apitable.user.enums.LinkType.DINGTALK;
import static com.apitable.user.enums.LinkType.FEISHU;
import static com.apitable.user.enums.LinkType.TENCENT;
import static com.apitable.user.enums.LinkType.WECHAT;
import static com.apitable.user.enums.UserException.DINGTALK_LINK_OTHER;
import static com.apitable.user.enums.UserException.FEISHU_LINK_OTHER;
import static com.apitable.user.enums.UserException.LINK_FAILURE;
import static com.apitable.user.enums.UserException.MOBILE_HAS_BOUND_DINGTALK;
import static com.apitable.user.enums.UserException.MOBILE_HAS_BOUND_FEISHU;
import static com.apitable.user.enums.UserException.MOBILE_HAS_BOUND_TENCENT;
import static com.apitable.user.enums.UserException.MOBILE_HAS_BOUND_WECHAT;
import static com.apitable.user.enums.UserException.MOBILE_NO_EXIST;
import static com.apitable.user.enums.UserException.TENCENT_LINK_OTHER;
import static com.apitable.user.enums.UserException.WECHAT_LINK_OTHER;

/**
 * <p>
 * Basic - Account Association Table Service Implementation Class
 * </p>
 */
@Slf4j
@Service
public class UserLinkServiceImpl extends ServiceImpl<UserLinkMapper, UserLinkEntity>
    implements IUserLinkService {

    @Resource
    private UserLinkMapper userLinkMapper;

    @Resource
    private ThirdPartyMemberMapper thirdPartyMemberMapper;

    @Resource
    private IUserService iUserService;

    @Resource
    private ISocialUserBindService iSocialUserBindService;

    @Resource
    private IAssetService iAssetService;

    @Resource
    private IVCodeService ivCodeService;

    @Resource
    private DeveloperMapper developerMapper;

    @Resource
    private PlayerActivityMapper playerActivityMapper;

    @Autowired(required = false)
    private WxMpProperties wxMpProperties;

    @Autowired(required = false)
    private WebAppProperties webAppProperties;

    @Override
    public UserLinkInfo getUserLinkInfo(Long userId) {
        UserLinkInfo info = new UserLinkInfo();
        DeveloperEntity developerEntity = developerMapper.selectByUserId(userId);
        if (developerEntity != null) {
            info.setApiKey(developerEntity.getApiKey());
        }
        List<AccountLinkDto> accountLinkList = userLinkMapper.selectVoByUserId(userId);
        info.setAccountLinkList(accountLinkList);
        String actions = playerActivityMapper.selectActionsByUserId(userId);
        info.setWizards(actions);
        String invitationCode = ivCodeService.getUserInviteCode(userId);
        info.setInviteCode(invitationCode);
        return info;
    }

    @Override
    public void create(Long userId, Long wechatMemberId) {
        log.info("Create Associations");
        ThirdPartyMemberEntity wechatMember = thirdPartyMemberMapper.selectById(wechatMemberId);
        UserLinkEntity entity = UserLinkEntity.builder()
            .userId(userId)
            .openId(wechatMember.getOpenId())
            .unionId(wechatMember.getUnionId())
            .nickName(wechatMember.getNickName())
            .type(WECHAT.getType())
            .build();
        boolean flag = save(entity);
        ExceptionUtil.isTrue(flag, LINK_FAILURE);
    }

    @Override
    public void checkThirdPartyLinkOtherUser(String unionId, Integer type) {
        log.info("Check whether the third-party account is associated with other vika accounts");
        Long linkUserId = userLinkMapper.selectUserIdByUnionIdAndType(unionId, type);
        throwEx(type, linkUserId != null, DINGTALK_LINK_OTHER, WECHAT_LINK_OTHER,
            TENCENT_LINK_OTHER, FEISHU_LINK_OTHER);
    }

    @Override
    public void createUserLink(Long userId, SocialAuthInfo authInfo) {
        if (wxMpProperties == null) {
            return;
        }
        if (authInfo.getType().equals(LinkType.WECHAT.getType())) {
            String nickName =
                thirdPartyMemberMapper.selectNickNameByUnionIdAndType(wxMpProperties.getAppId(),
                    authInfo.getUnionId(), ThirdPartyMemberType.WECHAT_PUBLIC_ACCOUNT.getType());
            authInfo.setNickName(nickName);
        }
        createUserLink(userId, authInfo, authInfo.getType());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createUserLink(Long userId, SocialAuthInfo authInfo, Integer type) {
        log.info("Create Third Party Association");
        if (type != FEISHU.getType()) {
            // Query whether the vika account has been associated with other third-party accounts
            String linkUnionId = userLinkMapper.selectUnionIdByUserIdAndType(userId, type);
            if (authInfo.getUnionId().equals(linkUnionId)) {
                return;
            }
            throwEx(type, linkUnionId != null, MOBILE_HAS_BOUND_DINGTALK, MOBILE_HAS_BOUND_WECHAT,
                MOBILE_HAS_BOUND_TENCENT, MOBILE_HAS_BOUND_FEISHU);
            // Check whether the third-party account is associated with other vika accounts
            checkThirdPartyLinkOtherUser(authInfo.getUnionId(), type);
        }
        // Third party platform account bind
        boolean isExist = iSocialUserBindService.isUnionIdBind(userId, authInfo.getUnionId());
        if (!isExist) {
            iSocialUserBindService.create(userId, authInfo.getUnionId());
        }
        // TODO Change during reconstruction
        createThirdPartyLink(userId, authInfo.getOpenId(), authInfo.getUnionId(),
            authInfo.getNickName(), type);
    }

    @Override
    public void wrapperSocialAuthInfo(SocialAuthInfo authInfo) {
        checkThirdPartyLinkOtherUser(authInfo.getUnionId(), authInfo.getType());
        // Load and obtain third-party nicknames and avatars
        useThirdPartyInfo(authInfo);
    }

    @Override
    public void useThirdPartyInfo(SocialAuthInfo authInfo) {
        Integer type = null;
        String appId = null;
        switch (LinkType.toEnum(authInfo.getType())) {
            case WECHAT:
                if (wxMpProperties != null) {
                    type = ThirdPartyMemberType.WECHAT_PUBLIC_ACCOUNT.getType();
                    appId = wxMpProperties.getAppId();
                }
                break;
            case TENCENT:
                type = ThirdPartyMemberType.TENCENT.getType();
                if (webAppProperties != null) {
                    appId = webAppProperties.getAppId();
                }
                break;
            default:
                break;
        }
        if (type != null && appId != null) {
            ThirdPartyMemberInfo info =
                thirdPartyMemberMapper.selectInfo(appId, authInfo.getUnionId(), type);
            if (info == null) {
                return;
            }
            if (info.getNickName() != null) {
                authInfo.setNickName(info.getNickName());
            }
            if (info.getAvatar() == null) {
                return;
            }
            // upload third party avatars to cloud storage
            try {
                URL url = URLUtil.url(info.getAvatar());
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                String fileName = StrUtil.subAfter(info.getAvatar(), StrUtil.SLASH, true);
                String mimeType = fileName.contains(StrUtil.DOT) ? FileUtil.extName(fileName) :
                    urlConnection.getContentType();
                long contentLength = urlConnection.getContentLengthLong();
                // If the read request header is -1, go directly to the estimated size of the stream
                if (-1 == contentLength) {
                    contentLength = inputStream.available();
                }
                AssetUploadResult uploadResult =
                    iAssetService.uploadFile(inputStream, contentLength, mimeType);
                authInfo.setAvatar(uploadResult.getToken());
            } catch (Exception e) {
                log.warn("third party avatar url cannot be read skip", e);
            }
        }
    }

    @Override
    public void createThirdPartyLink(Long userId, String openId, String unionId, String nickName,
                                     int type) {
        UserLinkEntity entity = new UserLinkEntity();
        entity.setUserId(userId);
        entity.setOpenId(openId);
        entity.setUnionId(unionId);
        entity.setNickName(nickName);
        entity.setType(type);
        boolean flag = save(entity);
        ExceptionUtil.isTrue(flag, LINK_FAILURE);
    }

    private void throwEx(int type, boolean b, UserException dingtalkLinkOther,
                         UserException wechatLinkOther, UserException tencentLinkOther,
                         UserException feishuLinkOther) {
        if (b) {
            Map<LinkType, BaseException> thxMap = new HashMap<>(4);
            thxMap.put(DINGTALK, dingtalkLinkOther);
            thxMap.put(WECHAT, wechatLinkOther);
            thxMap.put(TENCENT, tencentLinkOther);
            thxMap.put(FEISHU, feishuLinkOther);
            throw new BusinessException(thxMap.get(LinkType.toEnum(type)));
        }
    }

    @Override
    public boolean isUserLink(String unionId, int linkType) {
        return userLinkMapper.selectUserIdByUnionIdAndType(unionId, linkType) != null;
    }

    @Override
    public void deleteBatchByUnionId(List<String> unionIds) {
        userLinkMapper.deleteByUnionIds(unionIds);
    }

    @Override
    public Boolean checkUserLinkExists(Long userId, String unionId, String openId) {
        Long existUserId = userLinkMapper.selectUserIdByUnionIdAndOpenIdAndType(unionId, openId,
            LinkType.DINGTALK);
        return ObjectUtil.equal(userId, existUserId);
    }

    @Override
    public void deleteBatchOpenId(List<String> openIds, int type) {
        userLinkMapper.deleteByOpenIds(openIds, type);
    }

    @Override
    public Long getUserIdByUnionIdAndOpenId(String unionId, String openId, LinkType linkType) {
        return baseMapper.selectUserIdByUnionIdAndOpenIdAndType(unionId, openId, linkType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbind(Long userId, Integer type) {
        String linkUnionId = userLinkMapper.selectUnionIdByUserIdAndType(userId, type);
        // Delete third-party integration association
        iSocialUserBindService.deleteBatchByUnionId(Collections.singletonList(linkUnionId));
        // Delete account association
        userLinkMapper.deleteByUserIdAndType(userId, type);
    }

    @Override
    public void bindDingTalk(DingTalkBindOpRo opRo) {
        log.info("Associated DingTalk");
        // Judge whether it exists
        Long id = iUserService.getUserIdByMobile(opRo.getPhone());
        ExceptionUtil.isNotNull(id, MOBILE_NO_EXIST);
        UserEntity user = UserEntity.builder()
            .id(id)
            .dingOpenId(opRo.getOpenId())
            .dingUnionId(opRo.getUnionId())
            .build();

        boolean flag = iUserService.updateById(user);
        ExceptionUtil.isTrue(flag, LINK_FAILURE);
        // Bind successfully, and automatically log in to save the session
        SessionContext.setUserId(id);
    }

    @Override
    public void deleteByUserId(Long userId) {
        userLinkMapper.deleteByUserId(userId);
    }
}
