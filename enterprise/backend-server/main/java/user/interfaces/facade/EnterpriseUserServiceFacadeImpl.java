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

package com.apitable.enterprise.user.interfaces.facade;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.BooleanUtil;
import com.apitable.enterprise.integral.service.IIntegralService;
import com.apitable.enterprise.vcode.service.IVCodeService;
import com.apitable.interfaces.user.facade.AbstractUserServiceFacadeImpl;
import com.apitable.interfaces.user.model.InvitationCode;
import com.apitable.interfaces.user.model.RewardedUser;
import com.apitable.shared.constants.IntegralActionCodeConstants;

/**
 * enterprise user service facade implement.
 *
 * @author Shawn Deng
 */
public class EnterpriseUserServiceFacadeImpl extends AbstractUserServiceFacadeImpl {

    private final IVCodeService ivCodeService;

    private final IIntegralService iIntegralService;

    public EnterpriseUserServiceFacadeImpl(IVCodeService ivCodeService,
                                           IIntegralService integralService) {
        this.ivCodeService = ivCodeService;
        this.iIntegralService = integralService;
    }

    @Override
    public void onUserChangeNicknameAction(Long userId, String nickname, Boolean init) {
        if (BooleanUtil.isTrue(init)) {
            iIntegralService.updateInvitationUserNickNameInParams(userId, nickname);
        }
    }

    @Override
    public InvitationCode getUserInvitationCode(Long userId) {
        String inviteCode = ivCodeService.getUserInviteCode(userId);
        return new InvitationCode(inviteCode);
    }

    @Override
    public boolean getInvitationReward(Long userId) {
        return iIntegralService.checkByUserIdAndActionCodes(userId,
            CollectionUtil.newArrayList(IntegralActionCodeConstants.BE_INVITED_TO_REWARD,
                IntegralActionCodeConstants.OFFICIAL_INVITATION_REWARD));
    }

    @Override
    public void createInvitationCode(Long userId) {
        ivCodeService.createPersonalInviteCode(userId);
    }

    @Override
    public void rewardUserInfoUpdateAction(RewardedUser rewardedUser) {
        ivCodeService.rewardForUserUpdateMobile(rewardedUser.getUserId(),
            rewardedUser.getNickName());
    }
}
