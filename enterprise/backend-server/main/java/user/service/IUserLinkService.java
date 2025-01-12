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

package com.apitable.enterprise.user.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;

import com.apitable.enterprise.user.entity.UserLinkEntity;
import com.apitable.enterprise.user.ro.DingTalkBindOpRo;
import com.apitable.shared.cache.bean.SocialAuthInfo;
import com.apitable.shared.cache.bean.UserLinkInfo;
import com.apitable.user.enums.LinkType;

/**
 * <p>
 * Basic - Account Association Table Service Class
 * </p>
 */
public interface IUserLinkService extends IService<UserLinkEntity> {

    UserLinkInfo getUserLinkInfo(Long userId);

    /**
     * Create associations
     *
     * @param userId         User ID
     * @param wechatMemberId WeChat member ID
     */
    void create(Long userId, Long wechatMemberId);

    /**
     * Check whether the third-party account is associated with other vika accounts
     *
     * @param unionId unionId
     * @param type    Third party association type
     */
    void checkThirdPartyLinkOtherUser(String unionId, Integer type);

    void createUserLink(Long userId, SocialAuthInfo authInfo);

    /**
     * Create Third Party Association
     *
     * @param userId   User ID
     * @param authInfo User authorization information
     * @param type     Third party association type
     */
    void createUserLink(Long userId, SocialAuthInfo authInfo, Integer type);

    void wrapperSocialAuthInfo(SocialAuthInfo authInfo);

    void useThirdPartyInfo(SocialAuthInfo authInfo);

    /**
     * Create third-party account association
     *
     * @param userId   User ID
     * @param openId   Third party platform user ID
     * @param unionId  Third party platform unified ID
     * @param nickName User nickname
     * @param type     {@code LinkType} Association Type
     */
    void createThirdPartyLink(Long userId, String openId, String unionId, String nickName, int type);

    /**
     * Whether it is associated with a third-party account
     *
     * @param unionId  Third party platform unified ID
     * @param linkType Association Type
     * @return True | False
     */
    boolean isUserLink(String unionId, int linkType);

    /**
     * Batch delete
     *
     * @param unionIds Third party platform user ID
     */
    void deleteBatchByUnionId(List<String> unionIds);

    /**
     * Check whether the user is bound to a third-party account
     *
     * @param userId User ID
     * @param unionId Third party account unique ID
     * @param openId Third party account ID
     * @return boolean
     */
    Boolean checkUserLinkExists(Long userId, String unionId, String openId);

    /**
     * Batch delete
     *
     * @param openIds Unique identification within open applications
     * @param type Application User Type
     */
    void deleteBatchOpenId(List<String> openIds, int type);

    /**
     * Batch delete
     *
     * @param unionId Unique identification within open applications
     * @param openId Unique identification within open applications
     * @param linkType Application User Type
     */
    Long getUserIdByUnionIdAndOpenId(String unionId, String openId, LinkType linkType);

    /**
     * Unbind Third Party
     *
     * @param userId    User ID
     * @param type      Third party type
     */
    void unbind(Long userId, Integer type);

    /**
     * Associated DingTalk
     *
     * @param opRo Request parameters
     */
    void bindDingTalk(DingTalkBindOpRo opRo);

    /**
     * delete user all link
     * @param userId user id
     */
    void deleteByUserId(Long userId);
}
