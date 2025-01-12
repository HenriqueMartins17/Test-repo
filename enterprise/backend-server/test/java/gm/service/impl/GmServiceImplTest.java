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

package com.apitable.enterprise.gm.service.impl;

import static com.apitable.space.enums.SpaceException.NOT_SPACE_ADMIN;
import static com.apitable.space.enums.SpaceException.NO_ALLOW_OPERATE;
import static com.apitable.space.enums.SpaceException.SPACE_ALREADY_CERTIFIED;
import static com.apitable.user.enums.UserException.USER_NOT_EXIST;
import static com.apitable.workspace.enums.PermissionException.MEMBER_NOT_IN_SPACE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import com.apitable.core.exception.BusinessException;
import com.apitable.enterprise.AbstractVikaSaasIntegrationTest;
import com.apitable.enterprise.social.entity.SocialTenantBindEntity;
import com.apitable.enterprise.vika.core.model.UserContactInfo;
import com.apitable.interfaces.billing.model.SubscriptionInfo;
import com.apitable.organization.entity.MemberEntity;
import com.apitable.space.entity.SpaceEntity;
import com.apitable.space.enums.SpaceCertification;
import com.apitable.space.vo.SpaceGlobalFeature;
import com.apitable.user.entity.UserEntity;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.util.unit.DataSize;

public class GmServiceImplTest extends AbstractVikaSaasIntegrationTest {

    @Test
    public void testSocialSpaceSubscription() {
        String spaceId = IdUtil.fastSimpleUUID();
        SocialTenantBindEntity socialTenantBindEntity =
            SocialTenantBindEntity.builder().tenantId("testtenant").spaceId(spaceId).build();
        iSocialTenantBindService.save(socialTenantBindEntity);
        assertThatCode(() -> iGmService.spaceCertification(spaceId, "",
            SpaceCertification.BASIC)).as(NO_ALLOW_OPERATE.getMessage())
            .isInstanceOf(BusinessException.class);

    }

    @Test
    public void testSpaceHasSubscribed() {
        String spaceId = IdWorker.get32UUID();
        String uuid = IdWorker.get32UUID();
        Long userId = prepareUserData(uuid);
        prepareSpaceData(spaceId);
        prepareSpaceSubscriptionData(spaceId, userId);
        assertThatCode(() -> iGmService.spaceCertification(spaceId, uuid,
            SpaceCertification.BASIC)).as(SPACE_ALREADY_CERTIFIED.getMessage())
            .isInstanceOf(BusinessException.class);

    }

    @Test
    public void testSpaceSubscribedUserNotExists() {
        assertThatCode(() -> iGmService.spaceCertification("", "",
            SpaceCertification.BASIC)).as(USER_NOT_EXIST.getMessage())
            .isInstanceOf(BusinessException.class);
    }

    @Test
    public void testSpaceSubscribedMemberNotExists() {
        String spaceId = IdWorker.get32UUID();
        String uuid = IdWorker.get32UUID();
        prepareUserData(uuid);
        assertThatCode(() -> iGmService.spaceCertification(spaceId, uuid,
            SpaceCertification.BASIC)).as(MEMBER_NOT_IN_SPACE.getMessage())
            .isInstanceOf(BusinessException.class);
    }

    @Test
    public void testSpaceSubscribedMemberNotAdmin() {
        String spaceId = IdWorker.get32UUID();
        String uuid = IdWorker.get32UUID();
        Long userId = prepareUserData(uuid);
        prepareSpaceMemberData(spaceId, userId);
        assertThatCode(() -> iGmService.spaceCertification(spaceId, uuid,
            SpaceCertification.BASIC)).as(NOT_SPACE_ADMIN.getMessage())
            .isInstanceOf(BusinessException.class);
    }

    @Test
    public void testSpaceSubscriptionSuccessFeature() {
        String spaceId = IdWorker.get32UUID();
        String uuid = IdWorker.get32UUID();
        Long userId = prepareUserData(uuid);
        Long memberId = prepareSpaceMemberData(spaceId, userId);
        prepareSpaceDataWithOwner(spaceId, memberId);
        iGmService.spaceCertification(spaceId, uuid, SpaceCertification.BASIC);
        SpaceGlobalFeature feature = iSpaceService.getSpaceGlobalFeature(spaceId);
        assertThat(SpaceCertification.BASIC.getLevel()).isEqualTo(feature.getCertification());
    }

    @Test
    public void testSpaceSubscriptionSuccessCapacity() {
        String spaceId = IdWorker.get32UUID();
        String uuid = IdWorker.get32UUID();
        Long userId = prepareUserData(uuid);
        Long memberId = prepareSpaceMemberData(spaceId, userId);
        prepareSpaceDataWithOwner(spaceId, memberId);
        SubscriptionInfo beforeSubscribe = iSpaceSubscriptionService.getPlanInfoBySpaceId(spaceId);
        iGmService.spaceCertification(spaceId, uuid, SpaceCertification.BASIC);
        SubscriptionInfo afterSubscribe = iSpaceSubscriptionService.getPlanInfoBySpaceId(spaceId);
        long expectSize = afterSubscribe.getFeature().getCapacitySize().getValue().toBytes() -
            beforeSubscribe.getFeature().getCapacitySize().getValue().toBytes();
        assertThat(expectSize).isEqualTo(DataSize.ofGigabytes(5).toBytes());
    }

    @Test
    public void testGetUserPhoneAndEmailByUserIdThatUserIsExist() {
        // init user
        UserEntity user = UserEntity.builder().uuid("1").code("+86").mobilePhone("12312312312")
            .email("test@vikadata.com").build();
        iUserService.save(user);
        UserContactInfo userContactInfo = new UserContactInfo();
        userContactInfo.setRecordId("1");
        userContactInfo.setUuid("1");
        List<UserContactInfo> userContactInfos = CollUtil.newArrayList(userContactInfo);
        iGmService.getUserPhoneAndEmailByUserId(userContactInfos);
        assertThat(userContactInfos.get(0).getCode()).isEqualTo("+86");
        assertThat(userContactInfos.get(0).getMobilePhone()).isEqualTo("12312312312");
        assertThat(userContactInfos.get(0).getEmail()).isEqualTo("test@vikadata.com");
    }

    @Test
    public void testGetUserPhoneAndEmailByUserIdThatUserIsNotBindMobilePhone() {
        // init user
        UserEntity user = UserEntity.builder().uuid("1").email("test@vikadata.com").build();
        iUserService.save(user);
        UserContactInfo userContactInfo = new UserContactInfo();
        userContactInfo.setRecordId("1");
        userContactInfo.setUuid("1");
        List<UserContactInfo> userContactInfos = CollUtil.newArrayList(userContactInfo);
        iGmService.getUserPhoneAndEmailByUserId(userContactInfos);
        assertThat(userContactInfos.get(0).getCode()).isEqualTo("USER NOT BIND PHONE");
        assertThat(userContactInfos.get(0).getMobilePhone()).isEqualTo("USER NOT BIND PHONE");
        assertThat(userContactInfos.get(0).getEmail()).isEqualTo("test@vikadata.com");
    }

    @Test
    public void testGetUserPhoneAndEmailByUserIdThatUserIsNotBindEmail() {
        // init user
        UserEntity user =
            UserEntity.builder().uuid("1").code("+86").mobilePhone("12312312312").build();
        iUserService.save(user);
        UserContactInfo userContactInfo = new UserContactInfo();
        userContactInfo.setRecordId("1");
        userContactInfo.setUuid("1");
        List<UserContactInfo> userContactInfos = CollUtil.newArrayList(userContactInfo);
        iGmService.getUserPhoneAndEmailByUserId(userContactInfos);
        assertThat(userContactInfos.get(0).getCode()).isEqualTo("+86");
        assertThat(userContactInfos.get(0).getMobilePhone()).isEqualTo("12312312312");
        assertThat(userContactInfos.get(0).getEmail()).isEqualTo("USER NOT BIND EMAIL");
    }

    @Test
    public void testGetUserPhoneAndEmailByUserIdThatUserIsNotExist() {
        // init user
        UserEntity user = UserEntity.builder().id(1L).build();
        iUserService.save(user);
        UserContactInfo userContactInfo = new UserContactInfo();
        userContactInfo.setRecordId("1");
        userContactInfo.setUuid("1");
        List<UserContactInfo> userContactInfos = CollUtil.newArrayList(userContactInfo);
        iGmService.getUserPhoneAndEmailByUserId(userContactInfos);
        assertThat(userContactInfos.get(0).getCode()).isEqualTo("USER NOT BIND PHONE");
        assertThat(userContactInfos.get(0).getMobilePhone()).isEqualTo("USER NOT BIND PHONE");
        assertThat(userContactInfos.get(0).getEmail()).isEqualTo("USER NOT BIND EMAIL");
    }

    private void prepareSpaceSubscriptionData(String spaceId, Long userId) {
        SpaceGlobalFeature feature =
            SpaceGlobalFeature.builder().certification(SpaceCertification.BASIC.getLevel()).build();
        iSpaceService.switchSpacePros(userId, spaceId, feature);
    }

    private Long prepareUserData(String uuid) {
        // initialize user
        UserEntity userEntity = UserEntity.builder().id(IdWorker.getId()).uuid(uuid).build();
        iUserService.save(userEntity);
        return userEntity.getId();
    }


    private void prepareSpaceData(String spaceId) {
        // initialize spatial information
        SpaceEntity spaceEntity = SpaceEntity.builder().spaceId(spaceId).name("test space").build();
        iSpaceService.save(spaceEntity);
    }

    private Long prepareSpaceMemberData(String spaceId, Long userId) {
        // initialize member information
        MemberEntity memberEntity = MemberEntity.builder().userId(userId).spaceId(spaceId).build();
        iMemberService.save(memberEntity);
        return memberEntity.getId();
    }

    private void prepareSpaceDataWithOwner(String spaceId, Long memberId) {
        // initialize spatial information
        SpaceEntity spaceEntity =
            SpaceEntity.builder().spaceId(spaceId).name("test space").owner(memberId).build();
        iSpaceService.save(spaceEntity);
    }
}
