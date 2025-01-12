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

import static com.apitable.user.enums.UserException.SIGN_IN_ERROR;

import cn.hutool.core.collection.CollUtil;
import com.apitable.core.exception.BusinessException;
import com.apitable.enterprise.social.enums.SocialException;
import com.apitable.enterprise.social.model.User;
import com.apitable.enterprise.social.service.ISocialTenantUserService;
import com.apitable.enterprise.social.service.ISocialUserBindService;
import com.apitable.enterprise.user.service.IUserLinkService;
import com.apitable.interfaces.eventbus.facade.EventBusFacade;
import com.apitable.interfaces.eventbus.model.UserLoginEvent;
import com.apitable.organization.entity.MemberEntity;
import com.apitable.shared.component.TaskManager;
import com.apitable.shared.util.information.ClientOriginInfo;
import com.apitable.shared.util.information.InformationUtil;
import com.apitable.user.entity.UserEntity;
import com.apitable.user.enums.LinkType;
import java.util.List;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * DingTalk creates Social User policy implementation
 * </p>
 */
@Component
public class SocialDingTalkUserStrategey extends AbstractCreateSocialUser {
    @Resource
    private ISocialTenantUserService iSocialTenantUserService;

    @Resource
    private ISocialUserBindService iSocialUserBindService;

    @Resource
    private IUserLinkService iUserLinkService;

    @Resource
    private EventBusFacade eventBusFacade;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSocialUser(User user) {
        if (user.getUnionId() == null) {
            throw new BusinessException(SocialException.GET_USER_INFO_ERROR);
        }
        String unionId = user.getUnionId();
        String openId = user.getOpenId();
        String tenantId = user.getTenantId();
        Long userId = iSocialUserBindService.getUserIdByUnionId(unionId);
        if (userId == null) {
            // Create User
            UserEntity entity = createUserAndCopyAvatar(user, SIGN_IN_ERROR);
            userId = entity.getId();
            // Create user activity record
            iPlayerActivityService.createUserActivityRecord(userId);
            // Create personal invitation code
            ivCodeService.createPersonalInviteCode(userId);
            // Third party platform integration - user bind
            iSocialUserBindService.create(userId, unionId);
            ClientOriginInfo origin =
                InformationUtil.getClientOriginInfoInCurrentHttpContext(false, true);
            // Shence burial site - registration
            Long finalUserId = userId;
            String scene = "DingTalk";
            TaskManager.me().execute(() ->
                eventBusFacade.onEvent(new UserLoginEvent(finalUserId, scene, true, origin)));
        }
        if (!iUserLinkService.checkUserLinkExists(userId, unionId, openId)) {
            iUserLinkService.createThirdPartyLink(userId, openId, unionId, user.getNickName(),
                LinkType.DINGTALK.getType());
        }
        boolean isExistTenantUser =
            iSocialTenantUserService.isTenantUserOpenIdExist(user.getAppId(), tenantId, openId);
        if (!isExistTenantUser) {
            iSocialTenantUserService.create(user.getAppId(), tenantId, openId, unionId);
        }
        // Associate the members of the tenant space
        List<String> bindSpaceIds = iSocialTenantBindService.getSpaceIdsByTenantId(tenantId);
        if (CollUtil.isNotEmpty(bindSpaceIds)) {
            for (String bindSpaceId : bindSpaceIds) {
                // todo It is necessary to verify whether the open ID of the same enterprise downloading applications from two same service providers is the same
                MemberEntity member = iMemberService.getBySpaceIdAndOpenId(bindSpaceId, openId);
                if (member != null) {
                    MemberEntity updatedMember = new MemberEntity();
                    updatedMember.setId(member.getId());
                    updatedMember.setUserId(userId);
                    updatedMember.setIsActive(true);
                    iMemberService.updateById(updatedMember);
                }
            }
        }
        // New user ID
        return userId;
    }
}
