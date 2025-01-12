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

import static com.apitable.enterprise.social.enums.SocialException.USER_NOT_EXIST_WECOM;
import static com.apitable.user.enums.UserException.SIGN_IN_ERROR;
import static com.apitable.user.enums.UserException.USER_ALREADY_LINK_SAME_TYPE_ERROR_WECOM;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.apitable.asset.service.IAssetService;
import com.apitable.core.exception.BusinessException;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.enterprise.social.enums.SocialAppType;
import com.apitable.enterprise.social.enums.SocialPlatformType;
import com.apitable.enterprise.social.model.SocialUser;
import com.apitable.enterprise.social.model.User;
import com.apitable.enterprise.social.service.ISocialCpTenantUserService;
import com.apitable.enterprise.social.service.ISocialCpUserBindService;
import com.apitable.enterprise.track.enums.TrackEventType;
import com.apitable.interfaces.eventbus.facade.EventBusFacade;
import com.apitable.interfaces.eventbus.model.UserLoginEvent;
import com.apitable.organization.entity.MemberEntity;
import com.apitable.shared.component.TaskManager;
import com.apitable.shared.util.information.ClientOriginInfo;
import com.apitable.shared.util.information.InformationUtil;
import com.apitable.user.entity.UserEntity;
import com.apitable.user.enums.UserException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * WeCom creates Social User policy implementation
 * </p>
 */
@Slf4j
@Component
public class SocialWeComUserStrategey extends AbstractCreateSocialUser {

    @Resource
    private IAssetService iAssetService;
    @Resource
    private ISocialCpTenantUserService iSocialCpTenantUserService;

    @Resource
    private ISocialCpUserBindService iSocialCpUserBindService;

    @Resource
    private RedisLockRegistry redisLockRegistry;

    @Resource
    private EventBusFacade eventBusFacade;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSocialUser(User user) {
        SocialUser su = (SocialUser) user;

        // TODO First test the locking of WeChat users, and then uniformly lock in front of the policy factory
        String lockKey =
            StrUtil.format("createSocialUser:wecom_{}_{}:{}", su.getTenantId(), su.getAppId(),
                su.getOpenId());
        Lock lock = redisLockRegistry.obtain(lockKey);
        try {
            if (lock.tryLock(100, TimeUnit.MILLISECONDS)) {
                try {
                    // Check tenant application status
                    this.checkTenantAppStatus(su.getTenantId(), su.getAppId());
                    // Check the status of application binding space
                    String bindSpaceId =
                        this.checkTenantAppBindSpace(su.getTenantId(), su.getAppId());
                    // Query whether WeComUser is bound to vika user under different applications of the same enterprise.
                    // If the binding does not create a user, the binding relationship is established directly
                    Long bindUserId =
                        iSocialCpUserBindService.getUserIdByTenantIdAndAppIdAndCpUserId(
                            su.getTenantId(), su.getAppId(), su.getOpenId());
                    // Member information of tenant space
                    MemberEntity member =
                        iMemberService.getBySpaceIdAndOpenId(bindSpaceId, su.getOpenId());
                    ExceptionUtil.isNotNull(member, USER_NOT_EXIST_WECOM);
                    if (null != bindUserId) {
                        // Check whether the binding relationship has not been deleted unexpectedly, but the member information has been deleted. Re establish the association relationship
                        if (null == member.getUserId()) {
                            // Modify the vika user id associated with a Member (make up operation)
                            MemberEntity condition = MemberEntity.builder()
                                .id(member.getId())
                                .memberName(su.getNickName())
                                .userId(bindUserId)
                                .isActive(true)
                                .build();
                            iMemberService.updateById(condition);
                        }
                        if (CharSequenceUtil.isNotBlank(su.getAvatar()) &&
                            su.getSocialPlatformType() == SocialPlatformType.WECOM &&
                            su.getSocialAppType() == SocialAppType.ISV) {
                            // WeCom service provider needs to judge whether to update the avatar
                            UserEntity userEntity = iUserService.getById(bindUserId);
                            if (CharSequenceUtil.isBlank(userEntity.getAvatar())) {
                                iUserService.updateById(UserEntity.builder()
                                    .id(bindUserId)
                                    .avatar(iAssetService.downloadAndUploadUrl(su.getAvatar()))
                                    .build());
                            }
                        }

                        return bindUserId;
                    }

                    // Query whether there is a binding between WeChat members of enterprises under the same enterprise. If there is a binding relationship, establish a binding relationship directly.
                    // If not, create a user and establish a binding relationship
                    Long tenantAgentOtherBindUserId =
                        iSocialCpUserBindService.getUserIdByTenantIdAndCpUserId(su.getTenantId(),
                            su.getOpenId());
                    if (null == tenantAgentOtherBindUserId) {
                        // Create User
                        UserEntity entity = this.createUserAndCopyAvatar(user, SIGN_IN_ERROR);
                        // Create user activity record
                        iPlayerActivityService.createUserActivityRecord(entity.getId());
                        // Create personal invitation code
                        ivCodeService.createPersonalInviteCode(entity.getId());
                        bindUserId = entity.getId();
                        ClientOriginInfo origin =
                            InformationUtil.getClientOriginInfoInCurrentHttpContext(false, true);
                        // Shence burial site - registration
                        Long finalBindUserId = bindUserId;
                        String scene = "Wecom";
                        TaskManager.me().execute(() ->
                            eventBusFacade.onEvent(
                                new UserLoginEvent(finalBindUserId, scene, true, origin)));
                    } else {
                        bindUserId = tenantAgentOtherBindUserId;
                        if (CharSequenceUtil.isNotBlank(su.getAvatar()) &&
                            su.getSocialPlatformType() == SocialPlatformType.WECOM &&
                            su.getSocialAppType() == SocialAppType.ISV) {
                            // WeCom service provider needs to judge whether to update the avatar
                            UserEntity userEntity = iUserService.getById(bindUserId);
                            if (CharSequenceUtil.isBlank(userEntity.getAvatar())) {
                                iUserService.updateById(UserEntity.builder()
                                    .id(bindUserId)
                                    .avatar(iAssetService.downloadAndUploadUrl(su.getAvatar()))
                                    .build());
                            }
                        }
                    }

                    // Bind household member association
                    Long cpTenantUserId =
                        iSocialCpTenantUserService.getCpTenantUserId(su.getTenantId(),
                            su.getAppId(), su.getOpenId());
                    if (null == cpTenantUserId) {
                        cpTenantUserId =
                            iSocialCpTenantUserService.create(su.getTenantId(), su.getAppId(),
                                su.getOpenId(), su.getUnionId());
                    }
                    boolean isBind =
                        iSocialCpUserBindService.isCpTenantUserIdBind(bindUserId, cpTenantUserId);
                    if (!isBind) {
                        iSocialCpUserBindService.create(bindUserId, cpTenantUserId);
                    }

                    // Bottom line logic: one user can only bind to one enterprise WeChat account (TenantId+OpenId)
                    String linkedWeComUserId =
                        iSocialCpUserBindService.getOpenIdByTenantIdAndUserId(su.getTenantId(),
                            bindUserId);
                    if (null != linkedWeComUserId) {
                        ExceptionUtil.isTrue(linkedWeComUserId.equalsIgnoreCase(su.getOpenId()),
                            USER_ALREADY_LINK_SAME_TYPE_ERROR_WECOM);
                    }
                    long tenantBindUserNum =
                        iSocialCpUserBindService.countTenantBindByUserId(su.getTenantId(),
                            bindUserId);
                    ExceptionUtil.isFalse(tenantBindUserNum > 1,
                        USER_ALREADY_LINK_SAME_TYPE_ERROR_WECOM);

                    // Modify the vika user id associated with a Member
                    iMemberService.updateById(
                        MemberEntity.builder().id(member.getId()).userId(bindUserId).isActive(true)
                            .build());
                    return bindUserId;
                } catch (Exception e) {
                    log.error("User login operation failed", e);
                    throw e;
                } finally {
                    lock.unlock();
                }
            } else {
                throw new BusinessException(UserException.REFRESH_MA_CODE_OFTEN);
            }
        } catch (InterruptedException e) {
            log.error("Frequent user login operations", e);
            // Obtaining lock is interrupted
            throw new BusinessException(UserException.REFRESH_MA_CODE_OFTEN);
        }
    }

}
