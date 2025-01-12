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

package com.apitable.enterprise.social.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.apitable.core.exception.BusinessException;
import com.apitable.enterprise.AbstractVikaSaasIntegrationTest;
import com.apitable.enterprise.social.enums.SocialAppType;
import com.apitable.enterprise.social.enums.SocialPlatformType;
import com.apitable.mock.bean.MockUserSpace;
import com.apitable.space.enums.SpaceUpdateOperate;
import com.apitable.space.vo.SpaceGlobalFeature;
import org.junit.jupiter.api.Test;

public class SocialServiceImplTest extends AbstractVikaSaasIntegrationTest {

    @Test
    void testGetFalseRootManageable() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        iSpaceService.switchSpacePros(userSpace.getUserId(), userSpace.getSpaceId(),
            SpaceGlobalFeature.builder().rootManageable(false).build());
        SpaceGlobalFeature globalFeature =
            iSpaceService.getSpaceGlobalFeature(userSpace.getSpaceId());
        assertThat(globalFeature.rootManageableOrDefault()).isFalse();
    }

    @Test
    void testGetDefaultRootManageable() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        SpaceGlobalFeature globalFeature =
            iSpaceService.getSpaceGlobalFeature(userSpace.getSpaceId());
        assertThat(globalFeature.rootManageableOrDefault()).isTrue();
    }

    @Test
    void checkOperateUpdateMemberWithoutSocialBind() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        assertDoesNotThrow(() -> iSocialService.checkCanOperateSpaceUpdate(userSpace.getSpaceId(),
            SpaceUpdateOperate.UPDATE_MEMBER));
    }

    @Test
    void checkOperateAddTeamWithoutSocialBind() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        assertDoesNotThrow(() -> iSocialService.checkCanOperateSpaceUpdate(userSpace.getSpaceId(),
            SpaceUpdateOperate.ADD_TEAM));
    }

    @Test
    void checkOperateDeleteSpaceWithoutSocialBind() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        assertDoesNotThrow(() -> iSocialService.checkCanOperateSpaceUpdate(userSpace.getSpaceId(),
            SpaceUpdateOperate.DELETE_SPACE));
    }

    @Test
    void checkOperateUpdateMemberWithLarkIsvBind() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        iSocialTenantService.createTenant(SocialPlatformType.FEISHU, SocialAppType.ISV, "app01",
            "lark01",
            "{}");
        iSocialTenantBindService.addTenantBind("app01", "lark01", userSpace.getSpaceId());
        assertDoesNotThrow(() -> iSocialService.checkCanOperateSpaceUpdate(userSpace.getSpaceId(),
            SpaceUpdateOperate.UPDATE_MEMBER));
    }

    @Test
    void checkOperateUpdateMemberWithLarkIsvBindAndAppStop() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        iSocialTenantService.createTenant(SocialPlatformType.FEISHU, SocialAppType.ISV, "app01",
            "lark01",
            "{}");
        iSocialTenantBindService.addTenantBind("app01", "lark01", userSpace.getSpaceId());
        iSocialTenantService.updateTenantStatus("app01", "lark01", false);
        assertDoesNotThrow(() -> iSocialService.checkCanOperateSpaceUpdate(userSpace.getSpaceId(),
            SpaceUpdateOperate.UPDATE_MEMBER));
    }

    @Test
    void checkOperateUpdateMemberWithLarkInternalBind() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        iSocialTenantService.createTenant(SocialPlatformType.FEISHU, SocialAppType.INTERNAL,
            "app01", "lark01",
            "{}");
        iSocialTenantBindService.addTenantBind("app01", "lark01", userSpace.getSpaceId());
        BusinessException exception =
            assertThrows(BusinessException.class,
                () -> iSocialService.checkCanOperateSpaceUpdate(userSpace.getSpaceId(),
                    SpaceUpdateOperate.UPDATE_MEMBER));
        assertEquals(411, exception.getCode());
    }

    @Test
    void checkOperateUpdateMemberWithLarkInternalBindAndAppStop() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        iSocialTenantService.createTenant(SocialPlatformType.FEISHU, SocialAppType.INTERNAL,
            "app01", "lark01",
            "{}");
        iSocialTenantBindService.addTenantBind("app01", "lark01", userSpace.getSpaceId());
        iSocialTenantService.updateTenantStatus("app01", "lark01", false);
        BusinessException exception =
            assertThrows(BusinessException.class,
                () -> iSocialService.checkCanOperateSpaceUpdate(userSpace.getSpaceId(),
                    SpaceUpdateOperate.UPDATE_MEMBER));
        assertEquals(411, exception.getCode());
    }

    @Test
    void checkOperateAddTeamWithLarkIsvBind() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        iSocialTenantService.createTenant(SocialPlatformType.FEISHU, SocialAppType.ISV, "app01",
            "lark01",
            "{}");
        iSocialTenantBindService.addTenantBind("app01", "lark01", userSpace.getSpaceId());
        BusinessException exception =
            assertThrows(BusinessException.class,
                () -> iSocialService.checkCanOperateSpaceUpdate(userSpace.getSpaceId(),
                    SpaceUpdateOperate.ADD_TEAM));
        assertEquals(411, exception.getCode());
    }

    @Test
    void checkOperateAddTeamWithLarkIsvBindAndAppStop() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        iSocialTenantService.createTenant(SocialPlatformType.FEISHU, SocialAppType.ISV, "app01",
            "lark01",
            "{}");
        iSocialTenantBindService.addTenantBind("app01", "lark01", userSpace.getSpaceId());
        iSocialTenantService.updateTenantStatus("app01", "lark01", false);
        BusinessException exception =
            assertThrows(BusinessException.class,
                () -> iSocialService.checkCanOperateSpaceUpdate(userSpace.getSpaceId(),
                    SpaceUpdateOperate.ADD_TEAM));
        assertEquals(411, exception.getCode());
    }

    @Test
    void checkOperateAddTeamWithLarkInternalBind() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        iSocialTenantService.createTenant(SocialPlatformType.FEISHU, SocialAppType.INTERNAL,
            "app01", "lark01",
            "{}");
        iSocialTenantBindService.addTenantBind("app01", "lark01", userSpace.getSpaceId());
        BusinessException exception =
            assertThrows(BusinessException.class,
                () -> iSocialService.checkCanOperateSpaceUpdate(userSpace.getSpaceId(),
                    SpaceUpdateOperate.ADD_TEAM));
        assertEquals(411, exception.getCode());
    }

    @Test
    void checkOperateAddTeamWithLarkInternalBindAndAppStop() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        iSocialTenantService.createTenant(SocialPlatformType.FEISHU, SocialAppType.INTERNAL,
            "app01", "lark01",
            "{}");
        iSocialTenantBindService.addTenantBind("app01", "lark01", userSpace.getSpaceId());
        iSocialTenantService.updateTenantStatus("app01", "lark01", false);
        BusinessException exception =
            assertThrows(BusinessException.class,
                () -> iSocialService.checkCanOperateSpaceUpdate(userSpace.getSpaceId(),
                    SpaceUpdateOperate.ADD_TEAM));
        assertEquals(411, exception.getCode());
    }

    @Test
    void checkOperateUpdateMemberWithWecomIsvBind() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        iSocialTenantService.createTenant(SocialPlatformType.WECOM, SocialAppType.ISV, "app01",
            "wecom01",
            "{}");
        iSocialTenantBindService.addTenantBind("app01", "wecom01", userSpace.getSpaceId());
        assertDoesNotThrow(() -> iSocialService.checkCanOperateSpaceUpdate(userSpace.getSpaceId(),
            SpaceUpdateOperate.UPDATE_MEMBER));
    }

    @Test
    void checkOperateUpdateMemberWithWecomIsvBindAndStopApp() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        iSocialTenantService.createTenant(SocialPlatformType.WECOM, SocialAppType.ISV, "app01",
            "wecom01",
            "{}");
        iSocialTenantBindService.addTenantBind("app01", "wecom01", userSpace.getSpaceId());
        iSocialTenantService.updateTenantStatus("app01", "wecom01", false);
        assertDoesNotThrow(() -> iSocialService.checkCanOperateSpaceUpdate(userSpace.getSpaceId(),
            SpaceUpdateOperate.UPDATE_MEMBER));
    }

    @Test
    void checkOperateUpdateMemberWithWecomInternalBind() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        iSocialTenantService.createTenant(SocialPlatformType.WECOM, SocialAppType.INTERNAL, "app01",
            "wecom01",
            "{}");
        iSocialTenantBindService.addTenantBind("app01", "wecom01", userSpace.getSpaceId());
        BusinessException exception = assertThrows(BusinessException.class,
            () -> iSocialService.checkCanOperateSpaceUpdate(userSpace.getSpaceId(),
                SpaceUpdateOperate.UPDATE_MEMBER));
        assertEquals(411, (int) exception.getCode());
    }

    @Test
    void checkOperateUpdateMemberWithWecomInternalBindAndStopApp() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        iSocialTenantService.createTenant(SocialPlatformType.WECOM, SocialAppType.INTERNAL, "app01",
            "wecom01",
            "{}");
        iSocialTenantBindService.addTenantBind("app01", "wecom01", userSpace.getSpaceId());
        iSocialTenantService.updateTenantStatus("app01", "wecom01", false);
        BusinessException exception = assertThrows(BusinessException.class,
            () -> iSocialService.checkCanOperateSpaceUpdate(userSpace.getSpaceId(),
                SpaceUpdateOperate.UPDATE_MEMBER));
        assertEquals(411, (int) exception.getCode());
    }

    @Test
    void checkOperateUpdateMemberWithDingTalkIsvBind() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        iSocialTenantService.createTenant(SocialPlatformType.DINGTALK, SocialAppType.ISV, "app01",
            "ding01",
            "{}");
        iSocialTenantBindService.addTenantBind("app01", "ding01", userSpace.getSpaceId());
        assertDoesNotThrow(() -> iSocialService.checkCanOperateSpaceUpdate(userSpace.getSpaceId(),
            SpaceUpdateOperate.UPDATE_MEMBER));
    }

    @Test
    void checkOperateUpdateMemberWithDingTalkIsvBindAndStopApp() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        iSocialTenantService.createTenant(SocialPlatformType.DINGTALK, SocialAppType.ISV, "app01",
            "ding01",
            "{}");
        iSocialTenantBindService.addTenantBind("app01", "ding01", userSpace.getSpaceId());
        iSocialTenantService.updateTenantStatus("app01", "ding01", false);
        assertDoesNotThrow(() -> iSocialService.checkCanOperateSpaceUpdate(userSpace.getSpaceId(),
            SpaceUpdateOperate.UPDATE_MEMBER));
    }

    @Test
    void checkOperateUpdateMemberWithDingTalkInternalBind() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        iSocialTenantService.createTenant(SocialPlatformType.DINGTALK, SocialAppType.INTERNAL,
            "app01", "ding01",
            "{}");
        iSocialTenantBindService.addTenantBind("app01", "ding01", userSpace.getSpaceId());
        BusinessException exception = assertThrows(BusinessException.class,
            () -> iSocialService.checkCanOperateSpaceUpdate(userSpace.getSpaceId(),
                SpaceUpdateOperate.UPDATE_MEMBER));
        assertEquals(411, (int) exception.getCode());
    }

    @Test
    void checkOperateUpdateMemberWithDingTalkInternalBindAndStopApp() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        iSocialTenantService.createTenant(SocialPlatformType.DINGTALK, SocialAppType.INTERNAL,
            "app01", "ding01",
            "{}");
        iSocialTenantBindService.addTenantBind("app01", "ding01", userSpace.getSpaceId());
        iSocialTenantService.updateTenantStatus("app01", "ding01", false);
        BusinessException exception = assertThrows(BusinessException.class,
            () -> iSocialService.checkCanOperateSpaceUpdate(userSpace.getSpaceId(),
                SpaceUpdateOperate.UPDATE_MEMBER));
        assertEquals(411, (int) exception.getCode());
    }

    @Test
    void checkOperateAddTeamWithWecomIsvBind() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        iSocialTenantService.createTenant(SocialPlatformType.WECOM, SocialAppType.ISV, "app01",
            "wecom01",
            "{}");
        iSocialTenantBindService.addTenantBind("app01", "wecom01", userSpace.getSpaceId());
        assertDoesNotThrow(() -> iSocialService.checkCanOperateSpaceUpdate(userSpace.getSpaceId(),
            SpaceUpdateOperate.ADD_TEAM));
    }

    @Test
    void checkOperateAddTeamWithWecomIsvBindAndStopApp() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        iSocialTenantService.createTenant(SocialPlatformType.WECOM, SocialAppType.ISV, "app01",
            "wecom01",
            "{}");
        iSocialTenantBindService.addTenantBind("app01", "wecom01", userSpace.getSpaceId());
        iSocialTenantService.updateTenantStatus("app01", "wecom01", false);
        assertDoesNotThrow(() -> iSocialService.checkCanOperateSpaceUpdate(userSpace.getSpaceId(),
            SpaceUpdateOperate.ADD_TEAM));
    }

    @Test
    void checkOperateAddTeamWithWecomInternalBind() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        iSocialTenantService.createTenant(SocialPlatformType.WECOM, SocialAppType.INTERNAL, "app01",
            "wecom01",
            "{}");
        iSocialTenantBindService.addTenantBind("app01", "wecom01", userSpace.getSpaceId());
        BusinessException exception = assertThrows(BusinessException.class,
            () -> iSocialService.checkCanOperateSpaceUpdate(userSpace.getSpaceId(),
                SpaceUpdateOperate.ADD_TEAM));
        assertEquals(411, (int) exception.getCode());
    }

    @Test
    void checkOperateAddTeamWithWecomInternalBindAndStopApp() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        iSocialTenantService.createTenant(SocialPlatformType.WECOM, SocialAppType.INTERNAL, "app01",
            "wecom01",
            "{}");
        iSocialTenantBindService.addTenantBind("app01", "wecom01", userSpace.getSpaceId());
        iSocialTenantService.updateTenantStatus("app01", "wecom01", false);
        BusinessException exception = assertThrows(BusinessException.class,
            () -> iSocialService.checkCanOperateSpaceUpdate(userSpace.getSpaceId(),
                SpaceUpdateOperate.ADD_TEAM));
        assertEquals(411, (int) exception.getCode());
    }

    @Test
    void checkOperateAddTeamWithDingTalkIsvBind() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        iSocialTenantService.createTenant(SocialPlatformType.DINGTALK, SocialAppType.ISV, "app01",
            "ding01",
            "{}");
        iSocialTenantBindService.addTenantBind("app01", "ding01", userSpace.getSpaceId());
        assertDoesNotThrow(() -> iSocialService.checkCanOperateSpaceUpdate(userSpace.getSpaceId(),
            SpaceUpdateOperate.ADD_TEAM));
    }

    @Test
    void checkOperateAddTeamWithDingTalkIsvBindAndStopApp() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        iSocialTenantService.createTenant(SocialPlatformType.DINGTALK, SocialAppType.ISV, "app01",
            "ding01",
            "{}");
        iSocialTenantBindService.addTenantBind("app01", "ding01", userSpace.getSpaceId());
        iSocialTenantService.updateTenantStatus("app01", "ding01", false);
        assertDoesNotThrow(() -> iSocialService.checkCanOperateSpaceUpdate(userSpace.getSpaceId(),
            SpaceUpdateOperate.ADD_TEAM));
    }

    @Test
    void checkOperateAddTeamWithDingTalkInternalBind() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        iSocialTenantService.createTenant(SocialPlatformType.DINGTALK, SocialAppType.INTERNAL,
            "app01", "ding01",
            "{}");
        iSocialTenantBindService.addTenantBind("app01", "ding01", userSpace.getSpaceId());
        BusinessException exception = assertThrows(BusinessException.class,
            () -> iSocialService.checkCanOperateSpaceUpdate(userSpace.getSpaceId(),
                SpaceUpdateOperate.ADD_TEAM));
        assertEquals(411, (int) exception.getCode());
    }

    @Test
    void checkOperateAddTeamWithDingTalkInternalBindAndStopApp() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        iSocialTenantService.createTenant(SocialPlatformType.DINGTALK, SocialAppType.INTERNAL,
            "app01", "ding01",
            "{}");
        iSocialTenantBindService.addTenantBind("app01", "ding01", userSpace.getSpaceId());
        iSocialTenantService.updateTenantStatus("app01", "ding01", false);
        BusinessException exception = assertThrows(BusinessException.class,
            () -> iSocialService.checkCanOperateSpaceUpdate(userSpace.getSpaceId(),
                SpaceUpdateOperate.ADD_TEAM));
        assertEquals(411, (int) exception.getCode());
    }


    @Test
    void checkOperateDeleteSpaceWithDingTalkIsvBind() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        iSocialTenantService.createTenant(SocialPlatformType.DINGTALK, SocialAppType.ISV, "app01",
            "ding01",
            "{}");
        iSocialTenantBindService.addTenantBind("app01", "ding01", userSpace.getSpaceId());
        BusinessException exception =
            assertThrows(BusinessException.class,
                () -> iSocialService.checkCanOperateSpaceUpdate(userSpace.getSpaceId(),
                    SpaceUpdateOperate.DELETE_SPACE));
        assertEquals(411, exception.getCode());
    }

    @Test
    void checkOperateDeleteSpaceWithDingTalkIsvBindAndStopApp() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        iSocialTenantService.createTenant(SocialPlatformType.DINGTALK, SocialAppType.ISV, "app01",
            "ding01",
            "{}");
        iSocialTenantBindService.addTenantBind("app01", "ding01", userSpace.getSpaceId());
        iSocialTenantService.updateTenantStatus("app01", "ding01", false);
        BusinessException exception =
            assertThrows(BusinessException.class,
                () -> iSocialService.checkCanOperateSpaceUpdate(userSpace.getSpaceId(),
                    SpaceUpdateOperate.DELETE_SPACE));
        assertEquals(411, exception.getCode());
    }

    @Test
    void checkOperateDeleteSpaceWithDingTalkInternalBind() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        iSocialTenantService.createTenant(SocialPlatformType.DINGTALK, SocialAppType.INTERNAL,
            "app01", "ding01",
            "{}");
        iSocialTenantBindService.addTenantBind("app01", "ding01", userSpace.getSpaceId());
        BusinessException exception = assertThrows(BusinessException.class,
            () -> iSocialService.checkCanOperateSpaceUpdate(userSpace.getSpaceId(),
                SpaceUpdateOperate.DELETE_SPACE));
        assertEquals(411, (int) exception.getCode());
    }

    @Test
    void checkOperateDeleteSpaceDingTalkInternalBindAndStopApp() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        iSocialTenantService.createTenant(SocialPlatformType.DINGTALK, SocialAppType.INTERNAL,
            "app01", "ding01",
            "{}");
        iSocialTenantBindService.addTenantBind("app01", "ding01", userSpace.getSpaceId());
        iSocialTenantService.updateTenantStatus("app01", "ding01", false);
        assertDoesNotThrow(() -> iSocialService.checkCanOperateSpaceUpdate(userSpace.getSpaceId(),
            SpaceUpdateOperate.DELETE_SPACE));
    }


    @Test
    void checkOperateDeleteSpaceWithLarkIsvBind() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        iSocialTenantService.createTenant(SocialPlatformType.FEISHU, SocialAppType.ISV, "app01",
            "feishu01",
            "{}");
        iSocialTenantBindService.addTenantBind("app01", "feishu01", userSpace.getSpaceId());
        BusinessException exception =
            assertThrows(BusinessException.class,
                () -> iSocialService.checkCanOperateSpaceUpdate(userSpace.getSpaceId(),
                    SpaceUpdateOperate.DELETE_SPACE));
        assertEquals(411, exception.getCode());
    }

    @Test
    void checkOperateDeleteSpaceWithLarkBindAndStopApp() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        iSocialTenantService.createTenant(SocialPlatformType.FEISHU, SocialAppType.ISV, "app01",
            "feishu01",
            "{}");
        iSocialTenantBindService.addTenantBind("app01", "feishu01", userSpace.getSpaceId());
        iSocialTenantService.updateTenantStatus("app01", "feishu01", false);
        BusinessException exception =
            assertThrows(BusinessException.class,
                () -> iSocialService.checkCanOperateSpaceUpdate(userSpace.getSpaceId(),
                    SpaceUpdateOperate.DELETE_SPACE));
        assertEquals(411, exception.getCode());
    }

    @Test
    void checkOperateDeleteSpaceWithLarkInternalBind() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        iSocialTenantService.createTenant(SocialPlatformType.FEISHU, SocialAppType.INTERNAL,
            "app01", "feishu01",
            "{}");
        iSocialTenantBindService.addTenantBind("app01", "feishu01", userSpace.getSpaceId());
        BusinessException exception = assertThrows(BusinessException.class,
            () -> iSocialService.checkCanOperateSpaceUpdate(userSpace.getSpaceId(),
                SpaceUpdateOperate.DELETE_SPACE));
        assertEquals(411, (int) exception.getCode());
    }

    @Test
    void checkOperateDeleteSpaceLarkInternalBindAndStopApp() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        iSocialTenantService.createTenant(SocialPlatformType.FEISHU, SocialAppType.INTERNAL,
            "app01", "ding01",
            "{}");
        iSocialTenantBindService.addTenantBind("app01", "ding01", userSpace.getSpaceId());
        iSocialTenantService.updateTenantStatus("app01", "ding01", false);
        assertDoesNotThrow(() -> iSocialService.checkCanOperateSpaceUpdate(userSpace.getSpaceId(),
            SpaceUpdateOperate.DELETE_SPACE));
    }
}
