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

package com.apitable.enterprise.social.strategey;

import java.time.LocalDateTime;

import jakarta.annotation.Resource;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;

import com.apitable.enterprise.social.enums.SocialException;
import com.apitable.enterprise.social.enums.SocialPlatformType;
import com.apitable.asset.service.IAssetService;
import com.apitable.organization.service.IMemberService;
import com.apitable.player.service.IPlayerActivityService;
import com.apitable.enterprise.social.enums.SocialAppType;
import com.apitable.interfaces.social.enums.SocialNameModified;
import com.apitable.enterprise.social.service.ISocialTenantBindService;
import com.apitable.enterprise.social.service.ISocialTenantService;
import com.apitable.enterprise.social.model.User;
import com.apitable.user.service.IUserService;
import com.apitable.enterprise.vcode.service.IVCodeService;
import com.apitable.core.exception.BaseException;
import com.apitable.core.exception.BusinessException;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.enterprise.social.entity.SocialTenantEntity;
import com.apitable.user.entity.UserEntity;

import org.springframework.transaction.annotation.Transactional;

import static com.apitable.workspace.enums.PermissionException.NODE_NOT_EXIST;

@Slf4j
public abstract class AbstractCreateSocialUser implements CreateSocialUserStrategey {

    @Resource
    private IAssetService iAssetService;

    @Resource
    protected IUserService iUserService;

    @Resource
    protected IMemberService iMemberService;

    @Resource
    protected ISocialTenantService iSocialTenantService;

    @Resource
    protected ISocialTenantBindService iSocialTenantBindService;

    @Resource
    protected IPlayerActivityService iPlayerActivityService;

    @Resource
    protected IVCodeService ivCodeService;

    /**
     * Check enterprise application status
     *
     * @param tenantId Application Enterprise ID
     * @param appId    App ID
     */
    protected void checkTenantAppStatus(String tenantId, String appId) {
        SocialTenantEntity tenant = iSocialTenantService.getByAppIdAndTenantId(appId, tenantId);
        if (tenant == null) {
            throw new BusinessException(SocialException.TENANT_NOT_EXIST);
        }
        if (BooleanUtil.isFalse(tenant.getStatus())) {
            throw new BusinessException(SocialException.TENANT_DISABLED);
        }
    }

    /**
     * Check whether the enterprise application is bound to the space
     *
     * @param tenantId Application Enterprise ID
     * @param appId    App ID
     * @return Bound space ID
     */
    protected String checkTenantAppBindSpace(String tenantId, String appId) {
        String bindSpaceId = iSocialTenantBindService.getTenantBindSpaceId(tenantId, appId);
        ExceptionUtil.isNotBlank(bindSpaceId, NODE_NOT_EXIST);
        return bindSpaceId;
    }

    /**
     * Create users and upload avatars
     *
     * @param user          User parameters
     * @param saveException Save Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public UserEntity createUserAndCopyAvatar(User user, BaseException saveException) {
        String avatar = iAssetService.downloadAndUploadUrl(user.getAvatar());
        UserEntity entity = UserEntity.builder()
            .uuid(IdUtil.fastSimpleUUID())
            .nickName(user.getNickName())
            .avatar(avatar)
            .lastLoginTime(LocalDateTime.now())
            // Currently, only enterprise WeChat service providers need to set this field
            .isSocialNameModified(user.getSocialPlatformType() == SocialPlatformType.WECOM &&
                user.getSocialAppType() == SocialAppType.ISV ?
                SocialNameModified.NO.getValue() : SocialNameModified.NO_SOCIAL.getValue())
            .build();
        boolean flag = iUserService.save(entity);
        if (!flag) {
            throw new BusinessException(saveException);
        }
        return entity;
    }

}
