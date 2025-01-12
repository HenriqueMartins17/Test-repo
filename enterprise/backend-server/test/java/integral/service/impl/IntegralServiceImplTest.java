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

package com.apitable.enterprise.integral.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import cn.hutool.core.collection.CollectionUtil;
import com.apitable.enterprise.AbstractVikaSaasIntegrationTest;
import com.apitable.shared.constants.IntegralActionCodeConstants;
import com.apitable.user.entity.UserEntity;
import org.junit.jupiter.api.Test;

public class IntegralServiceImplTest extends AbstractVikaSaasIntegrationTest {

    @Test
    public void testUseInviteReward() {
        // Prepare Users
        UserEntity user = iUserService.createUserByEmail("test@vikadata.com");
        // Users with invitation codes
        UserEntity useInviteCodeUser = iUserService.createUserByEmail("test1@vikadata.com");
        String readyUsedInviteCode = ivCodeService.getUserInviteCode(useInviteCodeUser.getId());
        assertThatNoException().isThrownBy(() -> iIntegralService.useInviteCodeReward(user.getId(), readyUsedInviteCode));

        // Check whether both parties have received bonus integral
        boolean usedInviteReward = iIntegralService.checkByUserIdAndActionCodes(user.getId(),
                CollectionUtil.newArrayList(IntegralActionCodeConstants.BE_INVITED_TO_REWARD, IntegralActionCodeConstants.OFFICIAL_INVITATION_REWARD));
        assertThat(usedInviteReward).isTrue();

        boolean inviteReward = iIntegralService.checkByUserIdAndActionCodes(useInviteCodeUser.getId(),
                CollectionUtil.newArrayList(IntegralActionCodeConstants.INVITATION_REWARD));
        assertThat(inviteReward).isTrue();
    }
}
