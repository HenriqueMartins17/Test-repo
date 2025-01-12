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

import static com.apitable.shared.constants.SpaceConstants.SPACE_NAME_DEFAULT_SUFFIX;
import static com.vikadata.social.dingtalk.constants.DingTalkConst.ROOT_DEPARTMENT_ID;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.apitable.core.exception.BusinessException;
import com.apitable.enterprise.appstore.enums.AppType;
import com.apitable.enterprise.appstore.service.IAppInstanceService;
import com.apitable.enterprise.social.service.*;
import com.apitable.enterprise.vikabilling.enums.DingTalkOrderType;
import com.apitable.enterprise.vikabilling.enums.ProductChannel;
import com.apitable.enterprise.vikabilling.service.ISocialDingTalkOrderService;
import com.apitable.enterprise.vikabilling.service.ISocialDingTalkRefundService;
import com.apitable.enterprise.vikabilling.setting.Plan;
import com.apitable.enterprise.vikabilling.setting.Price;
import com.apitable.enterprise.vikabilling.strategy.SocialOrderStrategyFactory;
import com.apitable.enterprise.vikabilling.util.BillingConfigManager;
import com.apitable.enterprise.vikabilling.util.DingTalkPlanConfigManager;
import com.apitable.enterprise.grpc.DingTalkUserDto;
import com.apitable.enterprise.grpc.TenantInfoResult;
import com.apitable.enterprise.social.autoconfigure.dingtalk.DingTalkProperties.IsvAppProperty;
import com.apitable.enterprise.social.entity.SocialTenantEntity;
import com.apitable.enterprise.social.entity.SocialTenantUserEntity;
import com.apitable.enterprise.social.enums.SocialAppType;
import com.apitable.enterprise.social.enums.SocialPlatformType;
import com.apitable.enterprise.social.event.dingtalk.DingTalkCardFactory;
import com.apitable.enterprise.social.factory.SocialFactory;
import com.apitable.enterprise.social.factory.VikaFactory;
import com.apitable.enterprise.social.mapper.SocialTenantUserMapper;
import com.apitable.enterprise.social.notification.SocialNotificationManagement;
import com.apitable.enterprise.user.service.IUserLinkService;
import com.apitable.enterprise.vika.core.model.DingTalkSubscriptionInfo;
import com.apitable.interfaces.social.enums.SocialNameModified;
import com.apitable.organization.entity.MemberEntity;
import com.apitable.organization.entity.TeamMemberRelEntity;
import com.apitable.organization.enums.UnitType;
import com.apitable.organization.enums.UserSpaceStatus;
import com.apitable.organization.factory.OrganizationFactory;
import com.apitable.organization.mapper.MemberMapper;
import com.apitable.organization.service.IMemberService;
import com.apitable.organization.service.ITeamMemberRelService;
import com.apitable.organization.service.ITeamService;
import com.apitable.organization.service.IUnitService;
import com.apitable.shared.cache.service.UserSpaceCacheService;
import com.apitable.shared.component.TaskManager;
import com.apitable.shared.exception.LimitException;
import com.apitable.shared.util.IdUtil;
import com.apitable.shared.util.RandomExtendUtil;
import com.apitable.space.entity.SpaceEntity;
import com.apitable.space.mapper.SpaceMapper;
import com.apitable.space.service.ISpaceService;
import com.apitable.space.vo.SpaceGlobalFeature;
import com.apitable.template.service.ITemplateService;
import com.apitable.user.enums.LinkType;
import com.apitable.workspace.dto.CreateNodeDto;
import com.apitable.workspace.dto.NodeCopyOptions;
import com.apitable.workspace.enums.NodeType;
import com.apitable.workspace.service.INodeService;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.vikadata.social.dingtalk.event.order.BaseOrderEvent;
import com.vikadata.social.dingtalk.event.order.SyncHttpMarketOrderEvent;
import com.vikadata.social.dingtalk.event.order.SyncHttpMarketServiceCloseEvent;
import com.vikadata.social.dingtalk.event.sync.http.BaseOrgSuiteEvent;
import com.vikadata.social.dingtalk.event.sync.http.BaseOrgSuiteEvent.Agent;
import com.vikadata.social.dingtalk.event.sync.http.BaseOrgSuiteEvent.AuthInfo;
import com.vikadata.social.dingtalk.event.sync.http.BaseOrgSuiteEvent.AuthOrgScopes;
import com.vikadata.social.dingtalk.event.sync.http.OrgSuiteAuthEvent;
import com.vikadata.social.dingtalk.event.sync.http.contact.BaseOrgUserContactEvent;
import com.vikadata.social.dingtalk.event.sync.http.contact.SyncHttpUserLeaveOrgEvent;
import com.vikadata.social.dingtalk.message.Message;
import com.vikadata.social.dingtalk.model.DingTalkUserDetail;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import jakarta.annotation.Resource;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * ISV DingTalk Implementation of event service interface.
 * </p>
 */
@Service
@Slf4j
public class DingTalkIsvEventServiceImpl implements IDingTalkIsvEventService {
    @Resource
    private IDingTalkInternalService iDingTalkInternalService;

    @Resource
    private ISpaceService iSpaceService;

    @Resource
    private ITeamService iTeamService;

    @Resource
    private IUserLinkService iUserLinkService;

    @Resource
    private ISocialTenantBindService iSocialTenantBindService;

    @Resource
    private IAppInstanceService iAppInstanceService;

    @Resource
    private IUnitService iUnitService;

    @Resource
    private INodeService iNodeService;

    @Resource
    private ITemplateService iTemplateService;

    @Resource
    private SpaceMapper spaceMapper;

    @Resource
    private IDingTalkService dingTalkService;

    @Resource
    private SocialTenantUserMapper socialTenantUserMapper;

    @Resource
    private IMemberService iMemberService;

    @Resource
    private ISocialTenantUserService iSocialTenantUserService;

    @Resource
    private ITeamMemberRelService iTeamMemberRelService;

    @Resource
    private ISocialTenantService iSocialTenantService;

    @Resource
    private UserSpaceCacheService userSpaceCacheService;

    @Resource
    private MemberMapper memberMapper;

    @Resource
    private ISocialService iSocialService;

    @Resource
    private ISocialDingTalkOrderService iSocialDingTalkOrderService;

    @Resource
    private ISocialDingTalkRefundService iSocialDingTalkRefundService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleOrgSuiteAuthEvent(String suiteId, BaseOrgSuiteEvent event) {
        String corpId = event.getAuthCorpInfo().getCorpid();
        SocialTenantEntity entity = iSocialTenantService.getByAppIdAndTenantId(suiteId, corpId);
        // Already exists, and is in the enabled state repeatedly
        if (entity != null && entity.getStatus()) {
            return;
        }
        // There may be re-authorization. The local status is 0. Create a new space to bind
        String authUserOpenId = event.getAuthUserInfo().getUserId();
        // First save the information of authorized personnel, initialize the space station, and then synchronize the member information
        AuthOrgScopes scopes = new AuthOrgScopes();
        scopes.setAuthedUser(Collections.singletonList(authUserOpenId));
        scopes.setAuthedDept(Collections.emptyList());
        // Initialize a new space
        SpaceContext spaceContext = new SpaceContext(null, event.getAuthCorpInfo().getCorpName(),
            event.getAuthCorpInfo().getCorpLogoUrl());
        spaceContext.prepare();
        String spaceId = spaceContext.spaceId;
        ContactMeta contactMeta = new ContactMeta(spaceId, corpId, suiteId, authUserOpenId);
        handleIsvOrgContactData(contactMeta, spaceContext, scopes);
        String agentId = event.getAuthInfo().getAgent().get(0).getAgentid().toString();
        // Check third-party status
        TenantInfoResult tenantInfo = iDingTalkInternalService.getSocialTenantInfo(corpId, suiteId);
        // Correct agent id before saving
        if (StrUtil.isNotBlank(tenantInfo.getTenantId())) {
            agentId = tenantInfo.getAgentId();
            AuthInfo authInfo = event.getAuthInfo();
            Agent agent = authInfo.getAgent().get(0);
            agent.setAgentid(Long.parseLong(agentId));
            authInfo.setAgent(Collections.singletonList(agent));
            event.setAuthInfo(authInfo);
        }
        // Save or update tenant information
        iSocialTenantService.createOrUpdateWithScope(SocialPlatformType.DINGTALK, SocialAppType.ISV,
            suiteId, corpId, JSONUtil.toJsonStr(scopes), JSONUtil.toJsonStr(event));
        // Bind the tenant and this space station
        iSocialTenantBindService.addTenantBind(suiteId, corpId, spaceId);
        // Save address book information
        contactMeta.doSaveOrUpdate(agentId);
        // ID of the main administrator member of the space (the new space randomly references the template method, including GRPC calls, and places the last)
        spaceContext.after(contactMeta.openIdMap.get(authUserOpenId).getMemberId());
        // If there is order information before authorization, it needs to be completed
        handleTenantOrders(corpId, suiteId);
        // Send start notification
        String finalAgentId = agentId;
        TaskManager.me().execute(() -> {
            IsvAppProperty app = iDingTalkInternalService.getIsvAppConfig(suiteId);
            iDingTalkInternalService.sendMessageToUserByTemplateId(suiteId, corpId,
                app.getMsgTplId().getWelcome(),
                DingTalkCardFactory.createIsvEntryCardData(suiteId, corpId, app.getAppId()),
                ListUtil.toList(contactMeta.openIds), finalAgentId);
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleOrgSuiteChangeEvent(String suiteId, BaseOrgSuiteEvent event) {
        String corpId = event.getAuthCorpInfo().getCorpid();
        // Update tenant changes
        String authUserOpenId = event.getAuthUserInfo().getUserId();
        AuthOrgScopes scopes = event.getAuthScope().getAuthOrgScopes();
        String spaceId = iSocialTenantBindService.getTenantBindSpaceId(corpId, suiteId);
        // Initialize a new space
        SpaceContext spaceContext = new SpaceContext(spaceId, event.getAuthCorpInfo().getCorpName(),
            event.getAuthCorpInfo().getCorpLogoUrl());
        spaceContext.prepare();
        ContactMeta contactMeta = new ContactMeta(spaceId, corpId, suiteId, authUserOpenId);
        handleIsvOrgContactData(contactMeta, spaceContext, scopes);
        // Save or update tenant information
        iSocialTenantService.createOrUpdateWithScope(SocialPlatformType.DINGTALK, SocialAppType.ISV,
            suiteId, corpId, JSONUtil.toJsonStr(scopes), JSONUtil.toJsonStr(event));
        // Save address book information
        contactMeta.deleteMembers();
        String agentId = event.getAuthInfo().getAgent().get(0).getAgentid().toString();
        contactMeta.doSaveOrUpdate(agentId);
        // The master administrator moved out of the visible range
        Long owner = contactMeta.openIds.contains(spaceContext.oldAdminOpenId)
            ? contactMeta.openIdMap.get(spaceContext.oldAdminOpenId).getMemberId() : null;
        // ID of the main administrator member of the space (the new space randomly references the template method, including GRPC calls, and places the last)
        spaceContext.after(owner);
        // Send start notification
        TaskManager.me().execute(() -> {
            IsvAppProperty app = iDingTalkInternalService.getIsvAppConfig(suiteId);
            List<String> openIds = contactMeta.tenantUserMap.values().stream().filter(i -> i.isNew)
                .map(SocialTenantUserDTO::getOpenId).collect(Collectors.toList());
            if (!openIds.isEmpty()) {
                iDingTalkInternalService.sendMessageToUserByTemplateId(suiteId, corpId,
                    app.getMsgTplId().getWelcome(),
                    DingTalkCardFactory.createIsvEntryCardData(suiteId, corpId, app.getAppId()),
                    openIds);
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleOrgSuiteRelieveEvent(String suiteId, String corpId) {
        List<String> spaceIds =
            iSocialTenantBindService.getSpaceIdsByTenantIdAndAppId(corpId, suiteId);
        if (CollUtil.isNotEmpty(spaceIds)) {
            for (String spaceId : spaceIds) {
                iAppInstanceService.deleteBySpaceIdAndAppType(spaceId,
                    AppType.DINGTALK_STORE.name());
                Long mainAdminUserId = iSpaceService.getSpaceMainAdminUserId(spaceId);
                SpaceGlobalFeature feature = SpaceGlobalFeature.builder().invitable(true).build();
                iSpaceService.switchSpacePros(mainAdminUserId, spaceId, feature);
                // Delete space binding
                iSocialTenantBindService.removeBySpaceId(spaceId);
                List<String> openIds = memberMapper.selectOpenIdBySpaceId(CollUtil.toList(spaceId));
                if (!openIds.isEmpty()) {
                    iSocialTenantUserService.deleteByTenantIdAndOpenIds(suiteId, corpId, openIds);
                }
            }
        }
        iSocialTenantService.updateTenantStatus(suiteId, corpId, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleOrgMicroAppRestoreEvent(String suiteId, String corpId) {
        List<String> spaceIds =
            iSocialTenantBindService.getSpaceIdsByTenantIdAndAppId(corpId, suiteId);
        if (CollUtil.isNotEmpty(spaceIds)) {
            for (String spaceId : spaceIds) {
                iAppInstanceService.createInstanceByAppType(spaceId, AppType.DINGTALK_STORE.name());
            }
            // Reopen the tenant
            iSocialTenantService.updateTenantStatus(suiteId, corpId, true);
            // todo Send Message Card
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleOrgMicroAppStopEvent(String suiteId, String corpId) {
        List<String> spaceIds =
            iSocialTenantBindService.getSpaceIdsByTenantIdAndAppId(corpId, suiteId);
        if (CollUtil.isNotEmpty(spaceIds)) {
            for (String spaceId : spaceIds) {
                iAppInstanceService.deleteBySpaceIdAndAppType(spaceId,
                    AppType.DINGTALK_STORE.name());
            }
            // Update application status
            iSocialTenantService.updateTenantStatus(suiteId, corpId, false);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleUserAddOrgEvent(String openId, BaseOrgUserContactEvent event) {
        // New version event
        String tenantKey = event.getCorpId();
        String unionId = event.getUnionid();
        if (!event.getErrcode().equals(0)) {
            log.warn("[Normal] - [Incorrect user information]:{}:{}:{}", tenantKey, openId,
                event.getErrmsg());
            handleUserLeaveOrgEvent(openId,
                BeanUtil.toBean(event, SyncHttpUserLeaveOrgEvent.class));
            return;
        }
        if (StrUtil.isBlank(unionId)) {
            log.warn("[Normal] - [The user does not have a union ID]:{}:{}", tenantKey, openId);
            return;
        }
        String suiteId = event.getSuiteId();
        // Get the space ID of the current department
        String spaceId = iSocialTenantBindService.getTenantBindSpaceId(tenantKey, suiteId);
        if (StrUtil.isBlank(spaceId)) {
            log.warn("[Normal] - [User information change event]Tenant「{}」No space bound",
                tenantKey);
            return;
        }
        // Employee inactive change event information will not be processed
        if (!event.getActive()) {
            log.warn(
                "[User information change event] Tenant「{}」User [{}]Not activated, not processed",
                tenantKey, openId);
            return;
        }
        if (!memberVisitable(tenantKey, suiteId, openId)) {
            log.warn(
                "[User information change event]Tenant「{}」User[{}]Not in the visible range, not processed",
                tenantKey, openId);
            return;
        }
        Long memberId = iMemberService.getMemberIdBySpaceIdAndOpenId(spaceId, openId);
        if (ObjectUtil.isNull(memberId)) {
            try {
                Set<Long> needSendMesageSet = new HashSet<>();
                String operateOpenId = event.getOpenId();
                if (ObjectUtil.isNotEmpty(operateOpenId)) {
                    Long operateUserId = iMemberService.getUserIdByOpenId(spaceId, operateOpenId);
                    needSendMesageSet.add(operateUserId);
                }
                // Find the space master admin to check if the seating limit is exceeded
                Long mainAdminUserId = iSpaceService.getSpaceMainAdminUserId(spaceId);
                needSendMesageSet.add(mainAdminUserId);
                // Check if the seat limit is exceeded
                boolean isNotOverLimit = iSpaceService.checkSeatOverLimitAndSendNotify(
                    ListUtil.toList(needSendMesageSet), spaceId, 1l, false, false);
                if (!isNotOverLimit) {
                    // If the seat limit is exceeded, the user will not be synchronized
                    return;
                }
            } catch (Exception e) {
                log.error("Failed to check seat limit", e);
            }
            HashMap<String, String> nickNameMap =
                iDingTalkInternalService.getUserNameByUnionIds(Collections.singletonList(unionId));
            // Create Member
            MemberEntity member =
                createMember(IdWorker.getId(), spaceId, nickNameMap.get(unionId), openId, false);
            // Create Member
            iMemberService.batchCreate(spaceId, Collections.singletonList(member));
            // Create department association and put it directly into the root department
            Long rootTeamId = iTeamService.getRootTeamId(spaceId);
            iTeamMemberRelService.createBatch(
                Collections.singletonList(createTeamMemberRel(rootTeamId, member.getId())));
            // Send start notification
            TaskManager.me().execute(() -> {
                IsvAppProperty app = iDingTalkInternalService.getIsvAppConfig(suiteId);
                iDingTalkInternalService.sendMessageToUserByTemplateId(suiteId, tenantKey,
                    app.getMsgTplId().getWelcome(),
                    DingTalkCardFactory.createIsvEntryCardData(suiteId, tenantKey, app.getAppId()),
                    Collections.singletonList(openId));
            });
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleUserLeaveOrgEvent(String openId, SyncHttpUserLeaveOrgEvent event) {
        String tenantKey = event.getCorpId();
        // Get space ID
        String spaceId =
            iSocialTenantBindService.getTenantBindSpaceId(tenantKey, event.getSuiteId());
        if (StrUtil.isBlank(spaceId)) {
            log.warn("[Normal] - [User resignation event]Tenant「{}」No space bound", tenantKey);
            return;
        }
        MemberEntity member = iMemberService.getBySpaceIdAndOpenId(spaceId, openId);
        if (member != null) {
            if (member.getIsAdmin()) {
                // The resigned member is the master administrator. Set the master administrator of the space station to null
                spaceMapper.updateSpaceOwnerId(spaceId, null, null);
            }
            // Prohibit login with authorization
            String unionId =
                iSocialTenantUserService.getUnionIdByOpenId(event.getSuiteId(), tenantKey, openId);
            if (StrUtil.isNotBlank(unionId)) {
                iUserLinkService.deleteBatchOpenId(Collections.singletonList(openId),
                    LinkType.DINGTALK.getType());
            }
            socialTenantUserMapper.deleteByTenantIdAndOpenId(event.getSuiteId(), tenantKey, openId);
            // Remove member's space
            iMemberService.batchDeleteMemberFromSpace(spaceId,
                Collections.singletonList(member.getId()), false);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String handleMarketOrderEvent(SyncHttpMarketOrderEvent event) {
        String tenantKey = event.getCorpId();
        Integer status =
            iSocialDingTalkOrderService.getStatusByOrderId(event.getCorpId(), event.getSuiteId(),
                event.getOrderId());
        if (SqlHelper.retBool(status)) {
            log.warn("DingTalk order has been processed:{}", event.getOrderId());
            return null;
        }
        // Write DingTalk order
        if (null == status) {
            iSocialDingTalkOrderService.createOrder(event);
        }
        // Get space ID
        String spaceId =
            iSocialTenantBindService.getTenantBindSpaceId(tenantKey, event.getSuiteId());
        if (StrUtil.isBlank(spaceId)) {
            log.warn("The DingTalk enterprise has not received the application activation event:{}",
                event.getCorpId());
            return null;
        }
        String orderId = null;
        try {
            orderId = SocialOrderStrategyFactory.getService(SocialPlatformType.DINGTALK)
                .retrieveOrderPaidEvent(event);
        } catch (Exception e) {
            log.error("Failed to process tenant order, please solve it as soon as possible:{}:{}",
                spaceId, event.getOrderId(), e);
        }
        // Send notification
        TaskManager.me().execute(() -> {
            LocalDateTime expireTime =
                LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getServiceStopTime()),
                    TimeZone.getDefault().toZoneId());
            Long toUserId = iSpaceService.getSpaceOwnerUserId(spaceId);
            SocialNotificationManagement.me()
                .sendSocialSubscribeNotify(spaceId, toUserId, LocalDate.from(expireTime),
                    event.getItemName(), event.getPayFee());
        });
        // Save data to data table
        TaskManager.me().execute(() -> saveDingTalkSubscriptionInfo(spaceId, event));
        return orderId;
    }

    @Override
    public void handleMarketServiceClosedEvent(SyncHttpMarketServiceCloseEvent event) {
        String tenantKey = event.getCorpId();
        // Get space ID
        String spaceId =
            iSocialTenantBindService.getTenantBindSpaceId(tenantKey, event.getSuiteId());
        if (StrUtil.isBlank(spaceId)) {
            log.error("Enterprise not authorized:{}", tenantKey);
            return;
        }
        Integer status =
            iSocialDingTalkRefundService.getStatusByOrderId(event.getCorpId(), event.getSuiteId(),
                event.getOrderId());
        // Already handled
        if (SqlHelper.retBool(status)) {
            return;
        }
        // Refund does not exist. Write Ding Talk refund order
        if (null == status) {
            iSocialDingTalkRefundService.createRefund(event);
        }
        try {
            SocialOrderStrategyFactory.getService(SocialPlatformType.DINGTALK)
                .retrieveOrderRefundEvent(event);
        } catch (Exception e) {
            log.error(
                "Failed to process tenant refund, please solve it as soon as possible:{}:{}:{}",
                spaceId, event.getOrderId(), event.getRefundId(), e);
        }
    }

    private MemberEntity createMember(Long memberId, String spaceId, String memberName,
                                      String openId, boolean isAdmin) {
        MemberEntity member = new MemberEntity();
        member.setId(memberId);
        member.setSpaceId(spaceId);
        memberName = StrUtil.isNotBlank(memberName) ? memberName
            : StrUtil.format("星球居民{}", RandomExtendUtil.randomString(4));
        member.setMemberName(memberName);
        member.setOpenId(openId);
        member.setIsAdmin(isAdmin);
        member.setIsActive(false);
        member.setIsPoint(true);
        member.setNameModified(false);
        member.setIsSocialNameModified(SocialNameModified.NO_SOCIAL.getValue());
        member.setStatus(UserSpaceStatus.INACTIVE.getStatus());
        return member;
    }


    private TeamMemberRelEntity createTeamMemberRel(Long teamId, Long memberId) {
        TeamMemberRelEntity teamMemberRel = new TeamMemberRelEntity();
        teamMemberRel.setId(IdWorker.getId());
        teamMemberRel.setMemberId(memberId);
        teamMemberRel.setTeamId(teamId);
        return teamMemberRel;
    }


    private void handleIsvOrgContactData(ContactMeta contactMeta, SpaceContext spaceContext,
                                         AuthOrgScopes authScopes) {
        // DingTalk third-party applications do not need to restore the address book department structure, and all are synchronized to the root department
        String suiteId = contactMeta.suiteId;
        String authCorpId = contactMeta.corpId;
        // unionId -> DingTalkUserDto
        HashMap<String, DingTalkUserDto> userMap =
            iDingTalkInternalService.getAuthCorpUserDetailMap(suiteId,
                authCorpId, authScopes.getAuthedDept(), authScopes.getAuthedUser());
        // When binding for the first time, there is no primary administrator in the visible range, and the primary administrator is empty
        Long rootTeamId = spaceContext.rootTeamId;
        for (DingTalkUserDto userInfo : userMap.values()) {
            handleMember(contactMeta, userInfo, rootTeamId);
        }
    }

    private void handleMember(ContactMeta contactMeta, DingTalkUserDto userInfo,
                              Long parentTeamId) {
        String openId = userInfo.getOpenId();
        if (contactMeta.openIds.contains(openId)) {
            return;
        }
        DingTalkIsvMemberDto dto = contactMeta.openIdMap.get(openId);
        Long memberId;
        // The member in the database does not exist and has not been synchronized. Users can be bound only when they need to log in
        if (ObjectUtil.isNull(dto)) {
            MemberEntity member =
                createMember(IdWorker.getId(), contactMeta.spaceId, userInfo.getUserName(),
                    openId, contactMeta.authOpenId.equals(openId));
            // Does not exist, does not exist in the map, update member id
            memberId = member.getId();
            contactMeta.memberEntities.add(member);
            // Department binding. Because administrators can edit departments, only new members will be placed in the root department
            contactMeta.teamMemberRelEntities.add(
                OrganizationFactory.createTeamMemberRel(parentTeamId, memberId));
            contactMeta.openIdMap.put(openId,
                DingTalkIsvMemberDto.builder().memberId(memberId).openId(openId).isVisible(true)
                    .build());
        } else {
            // It has been synchronized to determine whether it has been deleted before and recover the data
            memberId = dto.getMemberId();
            if (dto.isDeleted()) {
                contactMeta.recoverMemberIds.add(memberId);
                // re-associate to the root department
                contactMeta.teamMemberRelEntities.add(
                    OrganizationFactory.createTeamMemberRel(parentTeamId, memberId));
                dto.setDeleted(false);
            }
            dto.setVisible(true);
            contactMeta.openIdMap.put(openId, dto);
        }
        // Synchronize third-party users and filter users that have been added. Because the test environment has an application bound to multiple spaces, the management of the Ding Talk address book is not done here.
        // Only synchronize when the address book events occur to prevent the deletion of Ding Talk address book users by mistake
        SocialTenantUserDTO tenantUserDTO = contactMeta.tenantUserMap.get(openId);
        if (null == tenantUserDTO) {
            contactMeta.tenantUserEntities.add(
                SocialFactory.createTenantUser(contactMeta.suiteId, contactMeta.corpId,
                    openId, userInfo.getUnionId()));
            tenantUserDTO =
                SocialTenantUserDTO.builder().openId(openId).isNew(true).isVisible(true).build();
        } else {
            tenantUserDTO.setVisible(true);
        }
        contactMeta.tenantUserMap.put(openId, tenantUserDTO);
        contactMeta.allMemberIds.add(memberId);
        contactMeta.openIds.add(openId);
    }

    private void handleTenantOrders(String tenantKey, String appId) {
        List<String> orderData =
            iSocialDingTalkOrderService.getOrdersByTenantIdAndAppId(tenantKey, appId);
        if (CollUtil.isEmpty(orderData)) {
            log.warn("No pending DingTalk orders:{}:{}", tenantKey, appId);
        }
        orderData.forEach(data -> {
            SyncHttpMarketOrderEvent event = JSONUtil.toBean(data, SyncHttpMarketOrderEvent.class);
            try {
                handleMarketOrderEvent(event);
            } catch (Exception e) {
                log.error("Failed to process Ding Talk list, please solve it quickly:{}:{}",
                    tenantKey, e);
            }
        });
    }

    void saveDingTalkSubscriptionInfo(String spaceId, BaseOrderEvent event) {
        DingTalkSubscriptionInfo info = new DingTalkSubscriptionInfo();
        // Payment scheme for order purchase
        info.setSpaceId(spaceId);
        info.setSpaceName(iSpaceService.getNameBySpaceId(spaceId));
        if (event.getClass().equals(SyncHttpMarketOrderEvent.class)) {
            info.setOrderType(((SyncHttpMarketOrderEvent) event).getOrderType());
        }
        if (event.getClass().equals(SyncHttpMarketServiceCloseEvent.class)) {
            info.setOrderType(DingTalkOrderType.REFUND_CLOSE.getValue());
        }
        info.setGoodsCode(event.getGoodsCode());
        Price price = DingTalkPlanConfigManager.getPriceByItemCodeAndMonth(event.getItemCode());
        if (null == price) {
            Plan plan = BillingConfigManager.getFreePlan(ProductChannel.DINGTALK);
            info.setSubscriptionType(plan.getProduct());
            info.setSeat(plan.getSeats());
        } else {
            info.setSubscriptionType(price.getProduct());
            info.setSeat(price.getSeat());
        }
        info.setServiceStartTime(event.getServiceStartTime());
        info.setServiceStopTime(event.getServiceStopTime());
        info.setData(JSONUtil.toJsonStr(event));
        VikaFactory.saveDingTalkSubscriptionInfo(info);
    }

    /**
     * Check if the user is in the visible range.
     *
     * @param tenantId Enterprise ID
     * @param appId    App ID
     * @param openId   User's open ID
     * @return boolean
     */
    private boolean memberVisitable(String tenantId, String appId, String openId) {
        SocialTenantEntity entity = iSocialTenantService.getByAppIdAndTenantId(appId, tenantId);
        OrgSuiteAuthEvent event =
            JSONUtil.toBean(entity.getAuthInfo(), OrgSuiteAuthEvent.class, true);
        if (event.getAuthScope().getErrcode() != 0) {
            return false;
        }
        AuthOrgScopes scopes = event.getAuthScope().getAuthOrgScopes();
        // Include authorized users
        if (scopes.getAuthedUser().size() > 0 && scopes.getAuthedUser().contains(openId)) {
            return true;
        }
        // Department selected
        if (scopes.getAuthedDept().size() > 0) {
            // All users
            if (scopes.getAuthedDept().contains(ROOT_DEPARTMENT_ID.toString())) {
                return true;
            }
            // Query whether the user is included in the authorized department
            try {
                DingTalkUserDetail userDetail =
                    iDingTalkInternalService.getUserDetailByUserId(appId, tenantId,
                        openId);
                List<String> deptIds = userDetail.getDeptIdList().stream().map(Object::toString)
                    .collect(Collectors.toList());
                if (CollUtil.containsAny(scopes.getAuthedDept(), deptIds)) {
                    return true;
                }
            } catch (Exception e) {
                log.warn("Failed to query the DingTalk ISV user:" + openId, e);
            }
        }
        return false;
    }

    @Data
    @Builder(toBuilder = true)
    static class DingTalkIsvMemberDto {
        private Long memberId;

        private String openId;

        private boolean isDeleted;

        /**
         * Whether it is in the visible range.
         */
        private boolean isVisible;
    }

    @Data
    @Builder(toBuilder = true)
    static class SocialTenantUserDTO {
        private String openId;

        /**
         * New or not, used to send notification messages.
         */
        private boolean isNew;

        /**
         * Whether it is in the visible range.
         */
        private boolean isVisible;
    }

    class SpaceContext {

        String spaceId;

        String spaceName;

        String spaceLogo;

        Long rootTeamId;

        String oldAdminOpenId;

        String rootNodeId;

        private boolean isCreate;

        public SpaceContext(String spaceId, String spaceName, String spaceLogo) {
            this.spaceId = spaceId;
            this.spaceName = spaceName;
            this.spaceLogo = spaceLogo;
        }

        void prepare() {
            if (StrUtil.isBlank(spaceId) || null == spaceMapper.selectBySpaceId(spaceId)) {
                spaceId = IdUtil.createSpaceId();
                spaceName = spaceName + SPACE_NAME_DEFAULT_SUFFIX;
                // Create root department
                rootTeamId = iTeamService.createRootTeam(spaceId, spaceName);
                iUnitService.create(spaceId, UnitType.TEAM, rootTeamId);
                // Create Root Node
                rootNodeId = iNodeService.createChildNode(-1L, CreateNodeDto.builder()
                    .spaceId(spaceId)
                    .newNodeId(IdUtil.createNodeId())
                    .type(NodeType.ROOT.getNodeType()).build());
                isCreate = true;
            } else {
                rootTeamId = iTeamService.getRootTeamId(spaceId);
                oldAdminOpenId = getMainAdminOpenId();
            }
        }

        void after(Long owner) {
            if (isCreate) {
                // owner may be null because the main administrator may not be in the visible range, and the api cannot get data
                SpaceEntity space =
                    SocialFactory.createSocialBindBindSpaceInfo(spaceId, spaceName, spaceLogo,
                        null, owner);
                iSpaceService.save(space);
                // Update app market status
                iAppInstanceService.createInstanceByAppType(spaceId, AppType.DINGTALK_STORE.name());
                // Tagged space is synchronizing the address book of the space station
                iSocialService.setContactSyncing(spaceId, owner.toString());

                // Randomly quote the template of the popular recommended carousel chart in the template center
                String templateNodeId = iTemplateService.getDefaultTemplateNodeId();
                if (StrUtil.isNotBlank(templateNodeId)) {
                    // Transfer node method, including GRPC calls, and place the last
                    iNodeService.copyNodeToSpace(-1L, spaceId, rootNodeId, templateNodeId,
                        NodeCopyOptions.create());
                }
            } else {
                if (owner == null) {
                    // The primary administrator is not in the visible range. Set the primary administrator of the space station to null
                    spaceMapper.updateSpaceOwnerId(spaceId, null, null);
                }
                iSocialService.contactFinished(spaceId);
            }
        }

        String getMainAdminOpenId() {
            Long mainMemberId = iSpaceService.getSpaceMainAdminMemberId(spaceId);
            return memberMapper.selectOpenIdById(mainMemberId);
        }
    }

    class ContactMeta {
        String spaceId;

        String suiteId;

        String corpId;

        String authOpenId;

        Map<String, DingTalkIsvMemberDto> openIdMap;

        // The existing DingTalk address book users are not deleted when the address book is synchronized.
        // They are synchronized through callback events because the same enterprise user ID is the same
        Map<String, SocialTenantUserDTO> tenantUserMap;

        // ID of the member table corresponding to the open ID in the address book range
        List<Long> allMemberIds = CollUtil.newArrayList();

        // The DingTalk user ID of this synchronization, which is used to send the start message
        Set<String> openIds = CollUtil.newHashSet();

        List<SocialTenantUserEntity> tenantUserEntities = new ArrayList<>();

        List<MemberEntity> memberEntities = new ArrayList<>();

        List<Long> recoverMemberIds = new ArrayList<>();

        List<TeamMemberRelEntity> teamMemberRelEntities = new ArrayList<>();

        ContactMeta(String spaceId, String corpId, String suiteId, String authOpenId) {
            this.spaceId = spaceId;
            this.corpId = corpId;
            this.suiteId = suiteId;
            this.authOpenId = authOpenId;
            this.openIdMap = getMemberOpenIdMap();
            this.tenantUserMap = getSocialTenantUserMap();
        }

        void doSaveOrUpdate(String agentId) {
            try {
                Set<Long> needSendMesageSet = new HashSet<>();
                if (ObjectUtil.isNotEmpty(authOpenId)) {
                    Long operateUserId = iMemberService.getUserIdByOpenId(spaceId, authOpenId);
                    needSendMesageSet.add(operateUserId);
                }
                // Find the space master admin to check if the seating limit is exceeded
                Long mainAdminUserId = iSpaceService.getSpaceMainAdminUserId(spaceId);
                needSendMesageSet.add(mainAdminUserId);

                // Check if the seat limit is exceeded
                boolean isNotOverLimit = iSpaceService.checkSeatOverLimitAndSendNotify(
                    ListUtil.toList(needSendMesageSet), spaceId,
                    memberEntities.size() + recoverMemberIds.size(), false, true);
                if (!isNotOverLimit) {
                    // Delete Cache
                    userSpaceCacheService.delete(spaceId);
                    // If the seat limit is exceeded, the user will not be synchronized， and throw an exception
                    if (Objects.nonNull(authOpenId)) {
                        // If the number of users exceeds the limit, the user will not be synchronized，and need send message to ownerAdmin
                        Message cardMessage =
                            DingTalkCardFactory.createSeatsOverLimitCardMsg(agentId);
                        TaskManager.me().execute(
                            () -> dingTalkService.asyncSendCardMessageToUserPrivate(agentId,
                                cardMessage,
                                Collections.singletonList(authOpenId)));
                    }
                    return;
                }
            } catch (Exception e) {
                log.error("Failed to check seat limit and send message card", e);
            }
            iSocialTenantUserService.createBatch(tenantUserEntities);
            iMemberService.batchCreate(spaceId, memberEntities);
            // Restore Member
            if (!recoverMemberIds.isEmpty()) {
                iMemberService.batchRecoveryMemberFromSpace(spaceId, recoverMemberIds);
            }
            iTeamMemberRelService.createBatch(teamMemberRelEntities);
            // Delete Cache
            userSpaceCacheService.delete(spaceId);
        }

        void deleteMembers() {
            // RemoveMemberIds, the previous member, is calculated and is not in this synchronization range
            List<Long> removeMemberIds =
                openIdMap.values().stream().filter(i -> !i.isVisible && !i.isDeleted)
                    .map(DingTalkIsvMemberDto::getMemberId).collect(Collectors.toList());
            // Old Members are the members to be deleted
            iMemberService.batchDeleteMemberFromSpace(spaceId, removeMemberIds, false);
            // Delete social tenant user
            List<String> openIds =
                tenantUserMap.values().stream().filter(i -> !i.isVisible)
                    .map(SocialTenantUserDTO::getOpenId).collect(Collectors.toList());
            if (!openIds.isEmpty()) {
                iSocialTenantUserService.deleteByTenantIdAndOpenIds(suiteId, corpId, openIds);
            }
        }

        Map<String, DingTalkIsvMemberDto> getMemberOpenIdMap() {
            List<MemberEntity> members = iMemberService.getMembersBySpaceId(spaceId, true);
            return members.stream().filter(i -> StrUtil.isNotBlank(i.getOpenId()))
                .collect(Collectors.toMap(MemberEntity::getOpenId,
                    dto -> DingTalkIsvMemberDto.builder().memberId(dto.getId())
                        .openId(dto.getOpenId()).isDeleted(dto.getIsDeleted()).build(),
                    (pre, cur) -> !cur.isDeleted() ? cur : pre));
        }

        Map<String, SocialTenantUserDTO> getSocialTenantUserMap() {
            List<String> openIds = iSocialTenantUserService.getOpenIdsByTenantId(suiteId, corpId);
            return openIds.stream().collect(Collectors.toMap(i -> i,
                i -> SocialTenantUserDTO.builder().openId(i).build(), (pre, cur) -> pre));
        }
    }
}

