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

import cn.hutool.core.util.StrUtil;
import com.apitable.core.exception.BusinessException;
import com.apitable.enterprise.social.enums.SocialException;
import com.apitable.enterprise.social.model.User;
import com.apitable.enterprise.social.service.ISocialUserBindService;
import com.apitable.enterprise.track.enums.TrackEventType;
import com.apitable.enterprise.user.service.IUserLinkService;
import com.apitable.interfaces.eventbus.facade.EventBusFacade;
import com.apitable.interfaces.eventbus.model.UserLoginEvent;
import com.apitable.organization.entity.MemberEntity;
import com.apitable.organization.service.IMemberService;
import com.apitable.shared.component.TaskManager;
import com.apitable.shared.util.information.ClientOriginInfo;
import com.apitable.shared.util.information.InformationUtil;
import com.apitable.user.entity.UserEntity;
import com.apitable.user.enums.LinkType;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Lark creates social user policy implementation
 * </p>
 */
@Component
public class SocialFeishuUserStrategey extends AbstractCreateSocialUser {

    @Resource
    private ISocialUserBindService iSocialUserBindService;

    @Resource
    private IUserLinkService iUserLinkService;

    @Resource
    private IMemberService iMemberService;

    @Resource
    private EventBusFacade eventBusFacade;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSocialUser(User user) {
        if (user.getUnionId() == null) {
            throw new BusinessException(SocialException.GET_USER_INFO_ERROR);
        }
        Long userId = iSocialUserBindService.getUserIdByUnionId(user.getUnionId());
        String bindSpaceId =
            iSocialTenantBindService.getTenantDepartmentBindSpaceId(user.getAppId(),
                user.getTenantId());
        MemberEntity member = null;
        if (StrUtil.isNotBlank(bindSpaceId)) {
            member = iMemberService.getBySpaceIdAndOpenId(bindSpaceId, user.getOpenId());
        }
        // Compatible with the change of the subject information, resulting in the change of the union ID
        if (userId == null && member != null && member.getUserId() != null) {
            // Create an associated user and associate the user ID with the new union ID
            iSocialUserBindService.create(member.getUserId(), user.getUnionId());
            boolean isLink =
                iUserLinkService.isUserLink(user.getUnionId(), LinkType.FEISHU.getType());
            if (!isLink) {
                iUserLinkService.createThirdPartyLink(member.getUserId(), user.getOpenId(),
                    user.getUnionId(), user.getNickName(), LinkType.FEISHU.getType());
            }
            userId = member.getUserId();
        }
        if (null == userId) {
            // Create User
            UserEntity entity = this.createUserAndCopyAvatar(user, SIGN_IN_ERROR);
            // Create user activity record
            iPlayerActivityService.createUserActivityRecord(entity.getId());
            // Create personal invitation code
            ivCodeService.createPersonalInviteCode(entity.getId());
            // Create Associated User
            iSocialUserBindService.create(entity.getId(), user.getUnionId());
            boolean isLink =
                iUserLinkService.isUserLink(user.getUnionId(), LinkType.FEISHU.getType());
            if (!isLink) {
                iUserLinkService.createThirdPartyLink(entity.getId(), user.getOpenId(),
                    user.getUnionId(), user.getNickName(), LinkType.FEISHU.getType());
            }
            userId = entity.getId();
            ClientOriginInfo origin =
                InformationUtil.getClientOriginInfoInCurrentHttpContext(false, true);
            // Shence burial site - registration
            Long finalUserId = userId;
            String scene = "Lark ISV";
            TaskManager.me().execute(() ->
                eventBusFacade.onEvent(new UserLoginEvent(finalUserId, scene, true, origin)));
        }
        // Associate the members of the tenant space
        if (member != null) {
            MemberEntity updatedMember = new MemberEntity();
            updatedMember.setId(member.getId());
            updatedMember.setUserId(userId);
            updatedMember.setIsActive(true);
            iMemberService.updateById(updatedMember);
        }
        return userId;
    }

}
