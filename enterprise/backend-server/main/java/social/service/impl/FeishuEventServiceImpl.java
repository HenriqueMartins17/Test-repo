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

import static com.vikadata.social.feishu.constants.FeishuConstants.FEISHU_ROOT_DEPT_ID;
import static com.vikadata.social.feishu.constants.FeishuErrorCode.GET_TENANT_DENIED;
import static com.vikadata.social.feishu.constants.FeishuErrorCode.NO_DEPT_AUTHORITY_ERROR;
import static com.vikadata.social.feishu.event.bot.BaseMessageEvent.GROUP_CHAT_TYPE;
import static com.vikadata.social.feishu.event.bot.BaseMessageEvent.PRIVATE_CHAT_TYPE;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.apitable.asset.service.IAssetService;
import com.apitable.core.exception.BusinessException;
import com.apitable.core.util.SpringContextHolder;
import com.apitable.enterprise.appstore.enums.AppType;
import com.apitable.enterprise.appstore.service.IAppInstanceService;
import com.apitable.enterprise.social.entity.SocialTenantDepartmentBindEntity;
import com.apitable.enterprise.social.entity.SocialTenantDepartmentEntity;
import com.apitable.enterprise.social.entity.SocialTenantUserEntity;
import com.apitable.enterprise.social.enums.SocialAppType;
import com.apitable.enterprise.social.enums.SocialPlatformType;
import com.apitable.enterprise.social.event.feishu.FeishuCardFactory;
import com.apitable.enterprise.social.factory.SocialFactory;
import com.apitable.enterprise.social.model.FeishuContactAuthScope;
import com.apitable.enterprise.social.notification.SocialNotificationManagement;
import com.apitable.enterprise.social.service.IFeishuEventService;
import com.apitable.enterprise.social.service.IFeishuService;
import com.apitable.enterprise.social.service.IFeishuTenantContactService;
import com.apitable.enterprise.social.service.ISocialFeishuEventLogService;
import com.apitable.enterprise.social.service.ISocialTenantBindService;
import com.apitable.enterprise.social.service.ISocialTenantDepartmentBindService;
import com.apitable.enterprise.social.service.ISocialTenantDepartmentService;
import com.apitable.enterprise.social.service.ISocialTenantService;
import com.apitable.enterprise.social.service.ISocialTenantUserService;
import com.apitable.enterprise.vikabilling.enums.BillingException;
import com.apitable.enterprise.vikabilling.listener.SyncOrderEvent;
import com.apitable.enterprise.vikabilling.service.ISocialFeishuOrderService;
import com.apitable.enterprise.vikabilling.service.ISpaceSubscriptionService;
import com.apitable.enterprise.vikabilling.setting.Price;
import com.apitable.enterprise.vikabilling.strategy.SocialOrderStrategyFactory;
import com.apitable.enterprise.vikabilling.util.LarkPlanConfigManager;
import com.apitable.interfaces.billing.model.SubscriptionInfo;
import com.apitable.organization.entity.MemberEntity;
import com.apitable.organization.entity.TeamEntity;
import com.apitable.organization.entity.TeamMemberRelEntity;
import com.apitable.organization.enums.UnitType;
import com.apitable.organization.enums.UserSpaceStatus;
import com.apitable.organization.factory.OrganizationFactory;
import com.apitable.organization.service.IMemberService;
import com.apitable.organization.service.IRoleMemberService;
import com.apitable.organization.service.ITeamMemberRelService;
import com.apitable.organization.service.ITeamService;
import com.apitable.organization.service.IUnitService;
import com.apitable.shared.component.TaskManager;
import com.apitable.shared.util.IdUtil;
import com.apitable.space.entity.SpaceEntity;
import com.apitable.space.mapper.SpaceMapper;
import com.apitable.space.service.ISpaceService;
import com.apitable.space.vo.SpaceGlobalFeature;
import com.apitable.template.service.ITemplateService;
import com.apitable.workspace.dto.CreateNodeDto;
import com.apitable.workspace.dto.NodeCopyOptions;
import com.apitable.workspace.enums.NodeType;
import com.apitable.workspace.service.INodeService;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.vikadata.social.feishu.card.Message;
import com.vikadata.social.feishu.event.UserInfo;
import com.vikadata.social.feishu.event.app.AppOpenEvent;
import com.vikadata.social.feishu.event.app.AppStatusChangeEvent;
import com.vikadata.social.feishu.event.app.AppUninstalledEvent;
import com.vikadata.social.feishu.event.app.OrderPaidEvent;
import com.vikadata.social.feishu.event.bot.AddBotEvent;
import com.vikadata.social.feishu.event.bot.BaseMessageEvent;
import com.vikadata.social.feishu.event.bot.P2pChatCreateEvent;
import com.vikadata.social.feishu.event.contact.v3.ContactDeptCreateEvent;
import com.vikadata.social.feishu.event.contact.v3.ContactDeptDeleteEvent;
import com.vikadata.social.feishu.event.contact.v3.ContactDeptUpdateEvent;
import com.vikadata.social.feishu.event.contact.v3.ContactScopeUpdateEvent;
import com.vikadata.social.feishu.event.contact.v3.ContactUserDeleteEvent;
import com.vikadata.social.feishu.event.contact.v3.ContactUserUpdateEvent;
import com.vikadata.social.feishu.exception.ContactAccessDeniesException;
import com.vikadata.social.feishu.exception.FeishuApiException;
import com.vikadata.social.feishu.model.FeishuAdminUserList;
import com.vikadata.social.feishu.model.FeishuAdminUserList.Admin;
import com.vikadata.social.feishu.model.FeishuTenantInfo;
import com.vikadata.social.feishu.model.v3.FeishuDeptObject;
import com.vikadata.social.feishu.model.v3.FeishuUserObject;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Lark Event Processing Service Implementation (2.0)
 * Asynchronous processing of Lark event push. The other party requires 200 responses within 1 second. Sometimes the execution time of business code cannot be met
 */
@Service
@Slf4j
public class FeishuEventServiceImpl implements IFeishuEventService {

    @Resource
    private ISocialTenantService iSocialTenantService;

    @Resource
    private ISocialTenantBindService iSocialTenantBindService;

    @Resource
    private ISocialFeishuEventLogService iSocialFeishuEventLogService;

    @Resource
    private IFeishuService iFeishuService;

    @Resource
    private ISpaceService iSpaceService;

    @Resource
    private INodeService iNodeService;

    @Resource
    private ITemplateService iTemplateService;

    @Resource
    private IMemberService iMemberService;

    @Resource
    private ITeamService iTeamService;

    @Resource
    private ISocialTenantUserService iSocialTenantUserService;

    @Resource
    private ISocialTenantDepartmentService iSocialTenantDepartmentService;

    @Resource
    private ISocialTenantDepartmentBindService iSocialTenantDepartmentBindService;

    @Resource
    private ITeamMemberRelService iTeamMemberRelService;

    @Resource
    private IUnitService iUnitService;

    @Resource
    private IAppInstanceService iAppInstanceService;

    @Resource
    private SpaceMapper spaceMapper;

    @Resource
    private IAssetService iAssetService;

    @Resource
    private RedisLockRegistry redisLockRegistry;

    @Resource
    private IFeishuTenantContactService iFeishuTenantContactService;

    @Resource
    private ISpaceSubscriptionService iSpaceSubscriptionService;

    @Resource
    private ISocialFeishuOrderService iSocialFeishuOrderService;

    @Resource
    private IRoleMemberService iRoleMemberService;

    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void handleAppOpenEvent(AppOpenEvent event) {
        // Only store apps will use it, and the application context must have
        String appId = event.getAppId();
        String tenantKey = event.getTenantKey();
        String lockKey = StrUtil.join("-", appId, tenantKey);
        Lock lock = redisLockRegistry.obtain(lockKey);
        try {
            if (lock.tryLock(100, TimeUnit.MILLISECONDS)) {
                try {
                    // To be on the safe side, check
                    boolean isTenantExist = iSocialTenantService.isTenantExist(tenantKey, appId);
                    if (!isTenantExist) {
                        // Create if the tenant does not exist
                        iSocialTenantService.createTenant(SocialPlatformType.FEISHU,
                            SocialAppType.ISV, event.getAppId(), tenantKey,
                            new JSONObject().toString());
                    } else {
                        // Update tenant if it already exists
                        iSocialTenantService.updateTenantStatus(appId, tenantKey, true);
                    }
                    iFeishuService.switchDefaultContext();
                    UserInfo installer = getAppInstaller(event);
                    // Synchronize contacts
                    syncTenantContact(appId, tenantKey, installer);
                    // Processing subscription information of unbound space
                    handleTenantOrders(tenantKey, appId);
                    // Send a notification when synchronization is complete
                    if (installer != null && StrUtil.isNotBlank(installer.getOpenId())) {
                        log.info("[Lark] Installer：{}", installer.getOpenId());
                        // Send Message Card
                        sendEntryCardToUser(appId, event.getTenantKey(),
                            event.getInstaller().getOpenId());
                    }
                    // Event processing completed
                    iSocialFeishuEventLogService.doneEvent(event.getMeta().getUuid());
                } catch (Exception e) {
                    log.error(
                        String.format("Tenant[%s] handling events [%s] fail, resolve it quickly",
                            tenantKey, event.getType()), e);
                    throw e;
                } finally {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            log.error("[" + tenantKey + "] Tenant lock operations are too frequent", e);
            throw new BusinessException(BillingException.ACCOUNT_CREDIT_ALTER_FREQUENTLY);
        }
    }

    private UserInfo getAppInstaller(AppOpenEvent event) {
        if (CollUtil.isNotEmpty(event.getApplicants())) {
            // When ordinary members apply to install apps, they have priority to return to this applicant
            return CollUtil.getFirst(event.getApplicants());
        } else if (event.getInstaller() != null &&
            StrUtil.isNotBlank(event.getInstaller().getOpenId())) {
            // When no applicant installs, the administrator installs, and the installer information is returned first
            return event.getInstaller();
        } else if (event.getInstallerEmployee() != null &&
            StrUtil.isNotBlank(event.getInstallerEmployee().getOpenId())) {
            // This field is not found
            return event.getInstallerEmployee();
        } else {
            log.error("Application activation event[{}]No operator", event.getMeta().getUuid());
            return null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleAppStopEvent(AppStatusChangeEvent event) {
        // Deactivate Do Not Delete Bound Spaces
        iSocialTenantService.stopByTenant(event.getAppId(), event.getTenantKey());
        // Event processing completed
        iSocialFeishuEventLogService.doneEvent(event.getMeta().getUuid());
    }

    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void handleAppRestartEvent(AppStatusChangeEvent event) {
        // Only store apps will use it, and the application context must have
        String appId = event.getAppId();
        String tenantKey = event.getTenantKey();
        String lockKey = StrUtil.join("-", appId, tenantKey);
        Lock lock = redisLockRegistry.obtain(lockKey);
        try {
            // Process the scope of authorization change, synchronize the department first, and then synchronize the employees
            MultiValueMap<FeishuDeptObject, FeishuUserObject> contactMap =
                fetchTenantContactIfFail(tenantKey, event.getOperator());
            if (lock.tryLock(100, TimeUnit.MILLISECONDS)) {
                try {
                    // To be safe, turn it back on. Check it
                    boolean isTenantExist = iSocialTenantService.isTenantExist(tenantKey, appId);
                    if (!isTenantExist) {
                        // Create if the tenant does not exist
                        iSocialTenantService.createTenant(SocialPlatformType.FEISHU,
                            SocialAppType.ISV, event.getAppId(), tenantKey,
                            new JSONObject().toString());
                    } else {
                        // Update tenant if it already exists
                        iSocialTenantService.updateTenantStatus(appId, tenantKey, true);
                    }
                    iFeishuService.switchDefaultContext();
                    String spaceId =
                        iSocialTenantBindService.getTenantDepartmentBindSpaceId(appId, tenantKey);
                    handleTenantContactData(appId, tenantKey, spaceId, contactMap,
                        event.getOperator().getOpenId(), true);
                    if (StrUtil.isNotBlank(event.getOperator().getOpenId())) {
                        // Send Message Card
                        sendEntryCardToUser(appId, event.getTenantKey(),
                            event.getOperator().getOpenId());
                    }
                    // Event processing completed
                    iSocialFeishuEventLogService.doneEvent(event.getMeta().getUuid());
                } catch (Exception e) {
                    log.error(
                        String.format("Tenant [%s] handling events [%s] failure, solve it quickly",
                            tenantKey, event.getType()), e);
                    throw e;
                } finally {
                    // should catch IllegalStateException
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            log.error("[" + tenantKey + "]Tenant lock operations are too frequent", e);
            throw new BusinessException(BillingException.ACCOUNT_CREDIT_ALTER_FREQUENTLY);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleAppUninstalledEvent(AppUninstalledEvent event) {
        // Uninstall also needs to be removed
        String spaceId = iSocialTenantBindService.getTenantDepartmentBindSpaceId(event.getAppId(),
            event.getTenantKey());
        if (StrUtil.isNotBlank(spaceId)) {
            iSocialTenantService.removeTenant(event.getAppId(), event.getTenantKey(), spaceId);
        }
        // Event processing completed
        iSocialFeishuEventLogService.doneEvent(event.getMeta().getUuid());
    }

    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void handleContactScopeChangeEvent(ContactScopeUpdateEvent event) {
        // Change of address book authorization scope
        iFeishuService.switchDefaultContext();
        String tenantKey = event.getHeader().getTenantKey();
        String appId = event.getHeader().getAppId();
        boolean isTenantExist = iSocialTenantService.isTenantExist(tenantKey, appId);
        if (!isTenantExist) {
            // The tenant is created at the time of activation. Only the address book will be refreshed, so skip
            return;
        }
        String lockKey = StrUtil.join("-", appId, tenantKey);
        Lock lock = redisLockRegistry.obtain(lockKey);
        try {
            // Remote access to address book
            MultiValueMap<FeishuDeptObject, FeishuUserObject> contactMap =
                iFeishuTenantContactService.fetchTenantContact(tenantKey);
            if (lock.tryLock(100, TimeUnit.MILLISECONDS)) {
                try {
                    String spaceId =
                        iSocialTenantBindService.getTenantDepartmentBindSpaceId(appId, tenantKey);
                    // Process the scope of authorization change, synchronize the department first, and then synchronize the employees
                    handleTenantContactData(appId, tenantKey, spaceId, contactMap, null, true);
                    // Event processing completed
                    iSocialFeishuEventLogService.doneEvent(event.getHeader().getEventId());
                } catch (Exception e) {
                    log.error(
                        String.format("Tenant [%s] handling events[%s] failure, solve it quickly",
                            tenantKey, event.getHeader().getEventType()), e);
                    throw e;
                } finally {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            log.error("[" + tenantKey + "]Tenant lock operations are too frequent", e);
            throw new BusinessException(BillingException.ACCOUNT_CREDIT_ALTER_FREQUENTLY);
        }
    }

    private MultiValueMap<FeishuDeptObject, FeishuUserObject> fetchTenantContactIfFail(
        String tenantKey,
        UserInfo installer) {
        // If the enterprise address book does not have permission, try to obtain the basic information of the installer and put it under the root door
        try {
            MultiValueMap<FeishuDeptObject, FeishuUserObject> contactMap =
                iFeishuTenantContactService.fetchTenantContact(tenantKey);
            if (contactMap.isEmpty()) {
                if (installer != null) {
                    contactMap.add(FeishuContactAuthScope.createRootDeptObject(),
                        SocialFactory.createDefaultLarkUser(installer));
                }
            }
            return contactMap;
        } catch (ContactAccessDeniesException exception) {
            MultiValueMap<FeishuDeptObject, FeishuUserObject> contactMap =
                new LinkedMultiValueMap<>();
            // No address book permission, return default installer information
            if (installer == null) {
                return contactMap;
            }
            if (StrUtil.isBlank(installer.getOpenId())) {
                return contactMap;
            }
            FeishuUserObject userObject;
            try {
                userObject = iFeishuService.getUser(tenantKey, installer.getOpenId());
            } catch (Exception ex) {
                log.error(
                    "Lark enterprise tenant [{}] no permission for address book, Installer [{}] can't get, return to the default user",
                    tenantKey, installer.getOpenId());
                userObject = SocialFactory.createDefaultLarkUser(installer);
            }
            contactMap.add(FeishuContactAuthScope.createRootDeptObject(), userObject);
            return contactMap;
        }
    }

    /**
     * Synchronize enterprise address book and create space station
     *
     * @param tenantKey Tenant ID
     */
    private SpaceContext syncTenantContact(String appId, String tenantKey, UserInfo installer) {
        // Process the scope of authorization change, synchronize the department first, and then synchronize the employees
        MultiValueMap<FeishuDeptObject, FeishuUserObject> contactMap =
            fetchTenantContactIfFail(tenantKey, installer);
        // Remote access to address book
        String spaceId = iSocialTenantBindService.getTenantDepartmentBindSpaceId(appId, tenantKey);
        return handleTenantContactData(appId, tenantKey, spaceId, contactMap,
            installer.getOpenId(), false);
    }

    @Override
    public SpaceContext handleTenantContactData(String appId, String tenantKey, String spaceId,
                                                MultiValueMap<FeishuDeptObject, FeishuUserObject> contactMap,
                                                String operatorOpenId, boolean checkSeatNum) {
        // Query department and department
        SpaceContext spaceContext = new SpaceContext(spaceId);
        spaceContext.prepare(tenantKey);
        // Root gate
        Long rootTeamId = spaceContext.rootTeamId;
        // Bound department
        List<SocialTenantDepartmentBindEntity> tenantBindTeamList =
            iSocialTenantDepartmentBindService.getBindDepartmentList(tenantKey, spaceId);
        Map<String, Long> bindTeamMap = tenantBindTeamList.stream()
            .collect(Collectors.toMap(SocialTenantDepartmentBindEntity::getTenantDepartmentId,
                SocialTenantDepartmentBindEntity::getTeamId));
        Map<String, Long> deptIdGeneratorMap =
            generateDeptBindTeamIdIfAbsent(contactMap, bindTeamMap, rootTeamId);
        List<String> bindDepartmentIdRecords = new ArrayList<>(bindTeamMap.keySet());
        // Existing members
        Map<String, Long> hasBindMemberMap = spaceContext.getMembers().stream()
            .filter(m -> StrUtil.isNotBlank(m.getOpenId()))
            .collect(Collectors.toMap(MemberEntity::getOpenId, MemberEntity::getId));
        Map<String, Long> tenantUserBindMemberIdMap = new HashMap<>(contactMap.values().size());
        contactMap.values().forEach(users ->
            users.forEach(user ->
                tenantUserBindMemberIdMap.put(user.getOpenId(),
                    hasBindMemberMap.getOrDefault(user.getOpenId(), IdWorker.getId()))));
        List<String> bindOpenIdRecords = new ArrayList<>(hasBindMemberMap.keySet());
        // Members of tenants
        Map<String, List<String>> tenantUserOpenIds =
            iSocialTenantUserService.getOpenIdMapByTenantId(appId, tenantKey);
        // Tenant's department
        List<String> tenantDeptIds =
            iSocialTenantDepartmentService.getDepartmentIdsByTenantId(tenantKey, spaceId);

        ContactMeta meta = new ContactMeta();
        meta.spaceId = spaceContext.spaceId;

        // Primary administrator member ID of the space
        Long oldMainAdminMemberId = spaceContext.getMainAdmin();
        boolean isMainAdminContain = oldMainAdminMemberId != null &&
            tenantUserBindMemberIdMap.containsValue(oldMainAdminMemberId);
        Long mainAdminMemberId = null;
        Long indexMemberId = null;
        int index = 0;
        Map<String, Long> createdOrUpdatedMemberIdMap = new HashMap<>(16);
        // Because employees of Lark enterprises can belong to multiple departments, traversal records are made to prevent multiple updates of members
        List<Long> updatedMemberIds = new ArrayList<>();
        for (Entry<FeishuDeptObject, List<FeishuUserObject>> entry : contactMap.entrySet()) {
            FeishuDeptObject dept = entry.getKey();
            Long teamId = deptIdGeneratorMap.get(dept.getDepartmentId());
            // Processing department
            if (!dept.getDepartmentId().equals(FEISHU_ROOT_DEPT_ID)) {
                Long parentTeamId = dept.getParentDepartmentId().equals(FEISHU_ROOT_DEPT_ID) ?
                    rootTeamId :
                    deptIdGeneratorMap.getOrDefault(dept.getParentDepartmentId(), rootTeamId);
                if (bindTeamMap.containsKey(dept.getDepartmentId())) {
                    // Department is bound
                    Long bindId =
                        iSocialTenantDepartmentService.getIdByDepartmentId(spaceId, tenantKey,
                            dept.getDepartmentId());
                    meta.updateTenantDepartmentEntities.add(SocialTenantDepartmentEntity.builder()
                        .id(bindId).spaceId(meta.spaceId).tenantId(tenantKey)
                        .departmentId(dept.getDepartmentId())
                        .openDepartmentId(dept.getOpenDepartmentId())
                        .parentId(dept.getParentDepartmentId())
                        .departmentName(dept.getName())
                        .departmentOrder(Integer.parseInt(dept.getOrder()))
                        .build());
                    meta.updateTeamEntities.add(TeamEntity.builder()
                        .id(teamId)
                        .parentId(parentTeamId)
                        .teamName(StrUtil.sub(dept.getName(), 0, 99))
                        .sequence(Integer.parseInt(dept.getOrder()))
                        .build());
                    // Delete a member under a department
                    iTeamMemberRelService.removeByTeamId(teamId);
                    // Remove Department Records
                    bindDepartmentIdRecords.remove(dept.getDepartmentId());
                } else {
                    // Department not bound
                    if (!tenantDeptIds.contains(dept.getDepartmentId())) {
                        SocialTenantDepartmentEntity tenantDepartment =
                            SocialFactory.createFeishuTenantDepartment(tenantKey, meta.spaceId,
                                dept);
                        meta.tenantDepartmentEntities.add(tenantDepartment);
                        meta.teamEntities.add(
                            OrganizationFactory.createTeam(meta.spaceId, teamId, parentTeamId,
                                StrUtil.sub(dept.getName(), 0, 99),
                                Integer.parseInt(dept.getOrder())));
                        meta.tenantDepartmentBindEntities.add(
                            SocialFactory.createTenantDepartmentBind(meta.spaceId, teamId,
                                tenantKey, dept.getDepartmentId(), dept.getOpenDepartmentId()));
                    }
                }
            }
            // Members under the processing department
            for (FeishuUserObject user : entry.getValue()) {
                if (!tenantUserOpenIds.containsKey(user.getOpenId())) {
                    meta.tenantUserEntities.add(
                        SocialFactory.createTenantUser(appId, tenantKey, user.getOpenId(),
                            user.getUnionId()));
                    // Employees can have multiple departments to prevent duplicate employee addition
                    tenantUserOpenIds.put(user.getOpenId(), ListUtil.toList(user.getUnionId()));
                } else {
                    List<String> unionIds = tenantUserOpenIds.get(user.getOpenId());
                    if (!unionIds.contains(user.getUnionId())) {
                        unionIds.add(user.getUnionId());
                        meta.tenantUserEntities.add(
                            SocialFactory.createTenantUser(appId, tenantKey, user.getOpenId(),
                                user.getUnionId()));
                        // Employees can have multiple departments to prevent duplicate employee addition
                        tenantUserOpenIds.put(user.getOpenId(), unionIds);
                    }
                }
                Long memberId = tenantUserBindMemberIdMap.get(user.getOpenId());
                if (index == 0) {
                    indexMemberId = memberId;
                }
                if (!isMainAdminContain) {
                    // There is no primary administrator or it is not included in the authorization scope of the address book before. Use the default administrator assignment rule
                    if (mainAdminMemberId == null) {
                        if (user.isTenantManager()) {
                            mainAdminMemberId = memberId;
                        } else if (index == contactMap.size() - 1) {
                            mainAdminMemberId = indexMemberId;
                        }
                    }
                } else {
                    mainAdminMemberId = oldMainAdminMemberId;
                }
                if (hasBindMemberMap.containsKey(user.getOpenId())) {
                    // Modify Members
                    if (!updatedMemberIds.contains(memberId)) {
                        meta.updateMemberEntities.add(MemberEntity.builder()
                            .id(memberId)
                            .memberName(user.getName())
                            .mobile(StrUtil.subSuf(user.getMobile(), 3))
                            .email(user.getEmail())
                            .position(user.getJobTitle())
                            .openId(user.getOpenId())
                            .nameModified(true)
                            .isPoint(false)
                            .isActive(true)
                            .isAdmin(
                                mainAdminMemberId != null && mainAdminMemberId.equals(memberId))
                            .status(UserSpaceStatus.ACTIVE.getStatus())
                            .isDeleted(false)
                            .build());
                        updatedMemberIds.add(memberId);
                    }
                    bindOpenIdRecords.remove(user.getOpenId());
                } else {
                    if (!createdOrUpdatedMemberIdMap.containsKey(user.getOpenId())) {
                        // Add Members
                        meta.memberEntities.add(
                            SocialFactory.createFeishuMember(memberId, spaceContext.spaceId,
                                user.getName(), user.getMobile(), user.getEmail(),
                                user.getJobTitle(),
                                user.getOpenId(),
                                mainAdminMemberId != null && mainAdminMemberId.equals(memberId)));
                        createdOrUpdatedMemberIdMap.put(user.getOpenId(), memberId);
                    }
                }
                meta.teamMemberRelEntities.add(
                    SocialFactory.createFeishuTeamMemberRel(teamId, memberId));
            }
            index++;
        }
        // Space member deletion
        List<Long> removeMemberIds = new ArrayList<>();
        bindOpenIdRecords.forEach(openId -> removeMemberIds.add(hasBindMemberMap.get(openId)));
        iMemberService.batchDeleteMemberFromSpace(spaceId, removeMemberIds, false);
        // Deleting Department and Employee Records under Tenant
        iSocialTenantUserService.deleteByFeishuOpenIds(appId, tenantKey,
            new ArrayList<>(bindOpenIdRecords));
        iSocialTenantDepartmentService.deleteBatchByDepartmentId(spaceId, tenantKey,
            bindDepartmentIdRecords);
        // Save Changes
        meta.doSaveOrUpdate(appId, tenantKey, operatorOpenId, checkSeatNum);
        // The new space randomly references the template method, including GRPC calls, and places the last
        spaceContext.after(tenantKey, mainAdminMemberId);
        return spaceContext;
    }

    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void handleUserLeaveEvent(ContactUserDeleteEvent event) {
        // Switch application configuration context
        iFeishuService.switchDefaultContext();
        String appId = event.getHeader().getAppId();
        String tenantKey = event.getHeader().getTenantKey();
        String lockKey = StrUtil.join("-", appId, tenantKey);
        Lock lock = redisLockRegistry.obtain(lockKey);
        try {
            if (lock.tryLock(100, TimeUnit.MILLISECONDS)) {
                try {
                    // New version event
                    handleUserLeave(appId, tenantKey, event.getEvent().getUser().getOpenId());
                    // Event processing completed
                    iSocialFeishuEventLogService.doneEvent(event.getHeader().getEventId());
                } catch (Exception e) {
                    log.error(
                        String.format("Tenant [%s] handling events [%s] fail, resolve it quickly",
                            tenantKey, event.getHeader().getEventType()), e);
                    throw e;
                } finally {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            log.error("[" + tenantKey + "]Tenant lock operations are too frequent", e);
            throw new BusinessException(BillingException.ACCOUNT_CREDIT_ALTER_FREQUENTLY);
        }
    }

    private void handleUserLeave(String appId, String tenantKey, String userOpenId) {
        // Get space ID
        String spaceId = iSocialTenantBindService.getTenantDepartmentBindSpaceId(appId, tenantKey);
        if (StrUtil.isBlank(spaceId)) {
            log.error("[User resignation event]Tenant「{}」No space bound", tenantKey);
            return;
        }
        MemberEntity member = iMemberService.getBySpaceIdAndOpenId(spaceId, userOpenId);
        if (member != null) {
            if (member.getIsAdmin()) {
                handleChangeMainAdminIfLeaveOrSuspend(tenantKey, userOpenId, spaceId,
                    member.getId());
            }
            // Do not log in with Lark authorization
            iSocialTenantUserService.deleteByTenantIdAndOpenId(appId, tenantKey, userOpenId);
            // Remove member's space
            iMemberService.batchDeleteMemberFromSpace(spaceId,
                Collections.singletonList(member.getId()), false);
        }
    }

    private void handleChangeMainAdminIfLeaveOrSuspend(String tenantKey, String mainAdminOpenId,
                                                       String spaceId, Long mainAdminMemberId) {
        // If the resigned member is the primary administrator, modify other employees as the primary administrator in sequence
        FeishuAdminUserList adminList = iFeishuService.getAdminList(tenantKey);
        List<String> adminOpenIds =
            adminList.getUserList().stream().map(Admin::getOpenId).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(adminOpenIds)) {
            adminOpenIds.removeIf(openId -> openId.equals(mainAdminOpenId));
            boolean change = false;
            for (String openId : adminOpenIds) {
                Long memberId = iMemberService.getMemberIdBySpaceIdAndOpenId(spaceId, openId);
                if (memberId != null) {
                    MemberEntity updateMember = new MemberEntity();
                    updateMember.setId(memberId);
                    updateMember.setIsAdmin(true);
                    iMemberService.updateById(updateMember);
                    spaceMapper.updateSpaceOwnerId(spaceId, memberId, null);
                    change = true;
                    break;
                }
            }

            if (!change) {
                // Find new members as administrators according to the creation time
                Long randomMemberId = iMemberService.getRandomMemberId(spaceId, mainAdminMemberId);
                if (randomMemberId != null) {
                    MemberEntity updateMember = new MemberEntity();
                    updateMember.setId(randomMemberId);
                    updateMember.setIsAdmin(true);
                    iMemberService.updateById(updateMember);
                    spaceMapper.updateSpaceOwnerId(spaceId, randomMemberId, null);
                }
            }
        } else {
            // Find new members as administrators according to the creation time
            Long randomMemberId = iMemberService.getRandomMemberId(spaceId, mainAdminMemberId);
            if (randomMemberId != null) {
                MemberEntity updateMember = new MemberEntity();
                updateMember.setId(randomMemberId);
                updateMember.setIsAdmin(true);
                iMemberService.updateById(updateMember);
                spaceMapper.updateSpaceOwnerId(spaceId, randomMemberId, null);
            }
        }
    }

    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void handleUserUpdateEvent(ContactUserUpdateEvent event) {
        // switchApplicationConfigurationContext
        iFeishuService.switchDefaultContext();
        // New version event
        String appId = event.getHeader().getAppId();
        String tenantKey = event.getHeader().getTenantKey();
        String lockKey = StrUtil.join("-", appId, tenantKey);
        Lock lock = redisLockRegistry.obtain(lockKey);
        try {
            if (lock.tryLock(100, TimeUnit.MILLISECONDS)) {
                try {
                    handleUserUpdate(event);
                } catch (Exception e) {
                    log.error(
                        String.format("Tenant [%s] handling events [%s] fail, resolve it quickly",
                            tenantKey, event.getHeader().getEventType()), e);
                    throw e;
                } finally {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            log.error("[" + tenantKey + "]Tenant lock operations are too frequent", e);
            throw new BusinessException(BillingException.ACCOUNT_CREDIT_ALTER_FREQUENTLY);
        }
    }

    private void handleUserUpdate(ContactUserUpdateEvent event) {
        String appId = event.getHeader().getAppId();
        String tenantKey = event.getHeader().getTenantKey();
        // Get the space ID of the current department
        String spaceId = iSocialTenantBindService.getTenantDepartmentBindSpaceId(appId, tenantKey);
        if (StrUtil.isBlank(spaceId)) {
            log.error(" [User information change event] Tenant「{}」not bound any space", tenantKey);
            return;
        }
        // Changed employee information
        FeishuUserObject userObject = event.getEvent().getUser();
        // Employee information before change
        FeishuUserObject changeProperty = event.getEvent().getChangeProperty();
        if (changeProperty.getStatus() != null &&
            !changeProperty.getStatus().equals(userObject.getStatus())) {
            // The employee status changes in three ways: employee activates the enterprise, employee account is suspended, and employee account is restored
            if (!changeProperty.getStatus().isActivated() && userObject.getStatus().isActivated()) {
                // All other statuses must be false to be active
                if (!userObject.getStatus().isFrozen() && !userObject.getStatus().isResigned()) {
                    log.info("[User status change] - [Employee activates enterprise]");
                    // Add the corresponding space station and modify other properties
                    handleUserActive(event.getHeader().getAppId(), tenantKey, spaceId, userObject);
                }
            }
            if (!changeProperty.getStatus().isFrozen() && userObject.getStatus().isFrozen()) {
                log.info("[User status change] - [Suspend employee account]");
                // Kick out of the space station
                handleUserSuspend(tenantKey, spaceId, userObject.getOpenId());
            }
            if (changeProperty.getStatus().isFrozen() && !userObject.getStatus().isFrozen()) {
                log.info("[User status change] - [Restore employee account]");
                // Restore the member and account corresponding to the employee
                handleUserRestore(tenantKey, spaceId, userObject);
            }
        } else {
            Long memberId =
                iMemberService.getMemberIdBySpaceIdAndOpenId(spaceId, userObject.getOpenId());
            if (memberId != null) {
                // Name change
                if (StrUtil.isNotBlank(changeProperty.getName())) {
                    // Update member information
                    MemberEntity member = new MemberEntity();
                    member.setId(memberId);
                    member.setMemberName(userObject.getName());
                    iMemberService.updateById(member);
                }

                // Department change
                if (CollUtil.isNotEmpty(changeProperty.getDepartmentIds())) {
                    // First delete the original department association
                    iTeamMemberRelService.removeByMemberId(memberId);
                    // Reassociate
                    handleUserDepartmentRelate(tenantKey, spaceId, memberId,
                        userObject.getDepartmentIds());
                }
            } else if (userObject.getStatus().isActivated() &&
                !userObject.getStatus().isResigned() && !userObject.getStatus().isFrozen()) {
                // Users join the space station
                log.info("[User modification information] - [Employee activates enterprise]");
                handleUserActive(event.getHeader().getAppId(), tenantKey, spaceId, userObject);
            }
        }
        // Event processing completed
        iSocialFeishuEventLogService.doneEvent(event.getHeader().getEventId());
    }

    private Map<String, Long> generateDeptBindTeamIdIfAbsent(
        MultiValueMap<FeishuDeptObject, FeishuUserObject> contactMap, Map<String, Long> bindTeamMap,
        Long rootTeamId) {
        Map<String, Long> deptIdMap = new HashMap<>(contactMap.size());
        for (FeishuDeptObject deptObject : contactMap.keySet()) {
            deptIdMap.computeIfAbsent(deptObject.getDepartmentId(), v -> {
                if (deptObject.getDepartmentId().equals(FEISHU_ROOT_DEPT_ID)) {
                    return rootTeamId;
                } else {
                    return bindTeamMap.getOrDefault(deptObject.getDepartmentId(), IdWorker.getId());
                }
            });
        }
        return deptIdMap;
    }

    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void handleDeptDeleteEvent(ContactDeptDeleteEvent event) {
        // Switch application configuration context
        iFeishuService.switchDefaultContext();
        String appId = event.getHeader().getAppId();
        String tenantKey = event.getHeader().getTenantKey();
        // Get the space ID of the current department
        String spaceId = iSocialTenantBindService.getTenantDepartmentBindSpaceId(appId, tenantKey);
        if (StrUtil.isBlank(spaceId)) {
            log.error("[Department deletion event] Tenant「{}」No space bound", tenantKey);
            return;
        }
        String lockKey = StrUtil.join("-", appId, tenantKey);
        Lock lock = redisLockRegistry.obtain(lockKey);
        try {
            if (lock.tryLock(100, TimeUnit.MILLISECONDS)) {
                try {
                    // Ignore the members under the department. The precondition for Lark department deletion is that the members under the department must be deleted
                    // There must be no sub department under the deleted department in Lark, so we don't need to pay attention to the change of the sub department
                    // Delete Tenant Department Record
                    FeishuDeptObject deptObject = event.getEvent().getDepartment();
                    iSocialTenantDepartmentService.deleteTenantDepartment(spaceId, tenantKey,
                        deptObject.getDepartmentId());
                    // Event processing completed
                    iSocialFeishuEventLogService.doneEvent(event.getHeader().getEventId());
                } catch (Exception e) {
                    log.error(
                        String.format("Tenant [%s] handling events [%s] fail, resolve it quickly",
                            tenantKey, event.getHeader().getEventType()), e);
                    throw e;
                } finally {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            log.error("[" + tenantKey + "]Tenant lock operations are too frequent", e);
            throw new BusinessException(BillingException.ACCOUNT_CREDIT_ALTER_FREQUENTLY);
        }
    }

    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void handleDeptUpdateEvent(ContactDeptUpdateEvent event) {
        // Switch application configuration context
        iFeishuService.switchDefaultContext();
        String appId = event.getHeader().getAppId();
        String tenantKey = event.getHeader().getTenantKey();
        // Preconditions: Name Modification | Department Level Adjustment (the following employees will be brought in automatically when adjusting.
        // Note that the department adjustment sorting is slightly complicated)
        // Get the space ID of the current department
        String spaceId = iSocialTenantBindService.getTenantDepartmentBindSpaceId(appId, tenantKey);
        if (StrUtil.isBlank(spaceId)) {
            log.error("[Department information update event] Tenant「{}」not bound any space",
                tenantKey);
            return;
        }
        String lockKey = StrUtil.join("-", appId, tenantKey);
        Lock lock = redisLockRegistry.obtain(lockKey);
        try {
            if (lock.tryLock(1000, TimeUnit.MILLISECONDS)) {
                try {
                    FeishuDeptObject deptObject = event.getEvent().getDepartment();
                    handleUpdateDept(spaceId, tenantKey, deptObject);
                    // Event processing completed
                    iSocialFeishuEventLogService.doneEvent(event.getHeader().getEventId());
                } catch (Exception e) {
                    log.error(
                        String.format("Tenant [%s] handling events [%s] fail, resolve it quickly",
                            tenantKey, event.getHeader().getEventType()), e);
                    throw e;
                } finally {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            log.error("[" + tenantKey + "]Tenant lock operations are too frequent", e);
            throw new BusinessException(BillingException.ACCOUNT_CREDIT_ALTER_FREQUENTLY);
        }
    }

    private void handleUpdateDept(String spaceId, String tenantKey, FeishuDeptObject deptObject) {
        // Bound department ID
        Long teamId = iSocialTenantDepartmentBindService.getBindSpaceTeamId(spaceId, tenantKey,
            deptObject.getDepartmentId());
        if (teamId == null) {
            // It does not exist. It may not be authorized by all employees, but partially. Then move in. First, query whether the parent department exists
            FeishuDeptObject parentDeptObject =
                getDeptOrNull(tenantKey, deptObject.getParentDepartmentId());
            if (parentDeptObject == null) {
                log.warn(
                    "[Department information update event] Tenant「{}」department「{}」's superior department [{}] no access",
                    tenantKey, deptObject.getDepartmentId(), deptObject.getParentDepartmentId());
                return;
            }
            Long bindParentTeamId =
                iSocialTenantDepartmentBindService.getBindSpaceTeamId(spaceId, tenantKey,
                    parentDeptObject.getDepartmentId());
            if (bindParentTeamId == null) {
                log.warn(
                    "[Department information update event] Tenant「{}」department「{}」's superior department[{}] not bound",
                    tenantKey, deptObject.getDepartmentId(), deptObject.getParentDepartmentId());
                return;
            }
            // New Create Department
            TeamEntity team = new TeamEntity();
            team.setId(IdWorker.getId());
            team.setSpaceId(spaceId);
            team.setTeamName(StrUtil.sub(deptObject.getName(), 0, 99));
            Long sequence = Long.parseLong(deptObject.getOrder());
            team.setSequence(sequence.intValue());
            team.setParentId(bindParentTeamId);
            iTeamService.batchCreateTeam(spaceId, Collections.singletonList(team));

            // Add Lark department record
            SocialTenantDepartmentEntity tenantDepartment = new SocialTenantDepartmentEntity();
            tenantDepartment.setId(IdWorker.getId());
            tenantDepartment.setTenantId(tenantKey);
            tenantDepartment.setSpaceId(spaceId);
            tenantDepartment.setDepartmentId(deptObject.getDepartmentId());
            tenantDepartment.setOpenDepartmentId(deptObject.getOpenDepartmentId());
            tenantDepartment.setParentId(parentDeptObject.getDepartmentId());
            tenantDepartment.setParentOpenDepartmentId(parentDeptObject.getOpenDepartmentId());
            tenantDepartment.setDepartmentName(deptObject.getName());
            iSocialTenantDepartmentService.createBatch(Collections.singletonList(tenantDepartment));
            // New Binding
            SocialTenantDepartmentBindEntity tenantDepartmentBind =
                new SocialTenantDepartmentBindEntity();
            tenantDepartmentBind.setId(IdWorker.getId());
            tenantDepartmentBind.setSpaceId(spaceId);
            tenantDepartmentBind.setTeamId(team.getId());
            tenantDepartmentBind.setTenantId(tenantKey);
            tenantDepartmentBind.setTenantDepartmentId(deptObject.getDepartmentId());
            tenantDepartmentBind.setTenantOpenDepartmentId(deptObject.getOpenDepartmentId());
            iSocialTenantDepartmentBindService.createBatch(
                Collections.singletonList(tenantDepartmentBind));
            return;
        }
        TeamEntity team = new TeamEntity();
        team.setId(teamId);
        team.setTeamName(StrUtil.sub(deptObject.getName(), 0, 99));
        Long sequence = Long.parseLong(deptObject.getOrder());
        team.setSequence(sequence.intValue());        // Process parent department
        SocialTenantDepartmentEntity tenantDepartment =
            iSocialTenantDepartmentService.getByDepartmentId(spaceId, tenantKey,
                deptObject.getDepartmentId());
        if (null == tenantDepartment) {
            log.info("Tenant department not exits:{}:{}:{}", tenantKey, spaceId,
                deptObject.getDepartmentId());
            return;
        }
        String parentDepartmentId = deptObject.getParentDepartmentId();
        if (StrUtil.isNotBlank(parentDepartmentId) &&
            !parentDepartmentId.equals(FEISHU_ROOT_DEPT_ID)) {
            // The root door is 0, and the updated superior department is not empty or root department
            // If the parent department id returns the format of open department id, retrieve and overwrite it
            if (parentDepartmentId.startsWith("od-")) {
                FeishuDeptObject parentDept = getDeptOrNull(tenantKey, parentDepartmentId);
                parentDepartmentId =
                    parentDept != null ? parentDept.getDepartmentId() : FEISHU_ROOT_DEPT_ID;
            }
        }
        Long parentTeamId = getParentTeamId(tenantKey, tenantDepartment, parentDepartmentId);
        team.setParentId(parentTeamId);
        iTeamService.updateById(team);

        // Override value of Lark department
        SocialTenantDepartmentEntity updateTenantDepartment = new SocialTenantDepartmentEntity();
        updateTenantDepartment.setId(tenantDepartment.getId());
        updateTenantDepartment.setDepartmentName(deptObject.getName());
        updateTenantDepartment.setParentId(parentDepartmentId);
        iSocialTenantDepartmentService.updateById(updateTenantDepartment);
    }

    @Override
    public void handleP2pChatCreateEvent(P2pChatCreateEvent event) {
        // When the user clicks the robot to enter, he/she sends a <Start Using> message card
        sendEntryCardToUser(event.getAppId(), event.getTenantKey(), event.getUser().getOpenId());
        // Event processing completed
        iSocialFeishuEventLogService.doneEvent(event.getMeta().getUuid());
    }

    @Override
    public void handleAddBotEvent(AddBotEvent event) {
        // Send a message to the group
        sendEntryCardToChatGroup(event.getAppId(), event.getTenantKey(), event.getOpenChatId());
        // Event processing completed
        iSocialFeishuEventLogService.doneEvent(event.getMeta().getUuid());
    }

    @Override
    public <E extends BaseMessageEvent> void handleMessageEvent(E event) {
        if (event.isMention()) {
            // Mention robots
            if (event.getChatType().equals(PRIVATE_CHAT_TYPE)) {
                // Chat privately and send it to users to see for themselves without sending robots
                sendEntryCardToUser(event.getAppId(), event.getTenantKey(), event.getOpenId());
            } else if (event.getChatType().equals(GROUP_CHAT_TYPE)) {
                // Group chat @ robot, directly sent to the group
                sendEntryCardToChatGroup(event.getAppId(), event.getTenantKey(),
                    event.getOpenChatId());
            }
        } else {
            //  Didn't mention it. Only private chat will reply
            if (event.getChatType().equals(PRIVATE_CHAT_TYPE)) {
                // Chat privately and send it to users to see for themselves without sending robots
                sendEntryCardToUser(event.getAppId(), event.getTenantKey(), event.getOpenId());
            }
        }
        // Event processing completed
        iSocialFeishuEventLogService.doneEvent(event.getMeta().getUuid());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleOrderPaidEvent(OrderPaidEvent event) {
        Integer status =
            iSocialFeishuOrderService.getStatusByOrderId(event.getTenantKey(), event.getAppId(),
                event.getOrderId());
        if (SqlHelper.retBool(status)) {
            log.warn("Lark order has been processed:{}", event.getOrderId());
            return;
        }
        // Write Lark Order
        if (null == status) {
            iSocialFeishuOrderService.createOrder(event);
        }
        String spaceId = iSocialTenantBindService.getTenantDepartmentBindSpaceId(event.getAppId(),
            event.getTenantKey());
        if (StrUtil.isBlank(spaceId)) {
            log.warn("Lark enterprise has not received the event of opening the application:{}",
                event.getTenantKey());
            return;
        }
        try {
            String orderId = SocialOrderStrategyFactory.getService(SocialPlatformType.FEISHU)
                .retrieveOrderPaidEvent(event);
            // Event processing completed
            iSocialFeishuEventLogService.doneEvent(event.getMeta().getUuid());
            // Synchronize order events
            SpringContextHolder.getApplicationContext()
                .publishEvent(new SyncOrderEvent(this, orderId));
        } catch (Exception e) {
            log.error("Failed to process tenant order, please solve it as soon as possible:{}:{}",
                spaceId, event.getOrderId(), e);
        }
        // Send notification
        SubscriptionInfo planInfo = iSpaceSubscriptionService.getPlanInfoBySpaceId(spaceId);
        TaskManager.me().execute(() -> {
            Long toUserId = iSpaceService.getSpaceOwnerUserId(spaceId);
            Price price = LarkPlanConfigManager.getPriceByLarkPlanId(event.getPricePlanId());
            if (price != null) {
                SocialNotificationManagement.me()
                    .sendSocialSubscribeNotify(spaceId, toUserId, planInfo.getEndDate(),
                        price.getGoodChTitle(), event.getOrderPayPrice());
            }
        });
    }

    private Long createTeamIfNotExist(String tenantKey, String spaceId, String parentDepartmentId,
                                      String newTeamName, String order) {
        // First level department or not
        if (parentDepartmentId.equals(FEISHU_ROOT_DEPT_ID)) {
            // First level department
            Long rootTeamId = iTeamService.getRootTeamId(spaceId);
            // New Create Department
            TeamEntity team = new TeamEntity();
            team.setId(IdWorker.getId());
            team.setParentId(rootTeamId);
            team.setSpaceId(spaceId);
            team.setTeamName(newTeamName);
            Long sequence = Long.parseLong(order);
            team.setSequence(sequence.intValue());
            iTeamService.batchCreateTeam(spaceId, Collections.singletonList(team));
            return team.getId();
        } else {
            // Non primary departments
            // Get the group ID of the parent department ID bound to the space station
            Long parentTeamId =
                iSocialTenantDepartmentBindService.getBindSpaceTeamId(spaceId, tenantKey,
                    parentDepartmentId);
            if (parentTeamId == null) {
                // Superior department does not exist
                parentTeamId = iTeamService.getRootTeamId(spaceId);
            }
            // Create space station team
            TeamEntity team = new TeamEntity();
            team.setId(IdWorker.getId());
            team.setParentId(parentTeamId);
            team.setSpaceId(spaceId);
            team.setTeamName(newTeamName);
            Long sequence = Long.parseLong(order);
            team.setSequence(sequence.intValue());
            iTeamService.batchCreateTeam(spaceId, Collections.singletonList(team));
            return team.getId();
        }
    }

    private Long getParentTeamId(String tenantKey, SocialTenantDepartmentEntity tenantDepartment,
                                 String parentDepartmentId) {
        // Department level adjustment
        if (!ObjectUtil.equal(tenantDepartment.getParentId(), parentDepartmentId)) {
            if (tenantDepartment.getParentId().equals(FEISHU_ROOT_DEPT_ID)) {
                // It was the lower layer of the root gate previously
                return iSocialTenantDepartmentBindService.getBindSpaceTeamId(
                    tenantDepartment.getSpaceId(), tenantKey, parentDepartmentId);
            }

            if (parentDepartmentId.equals(FEISHU_ROOT_DEPT_ID)) {
                // Now it's the lower layer of the root gate
                return iTeamService.getRootTeamId(tenantDepartment.getSpaceId());
            }
        }
        return null;
    }

    private FeishuDeptObject getDeptOrNull(String tenantKey, String openDepartmentId) {
        try {
            return iFeishuService.getDept(tenantKey, null, openDepartmentId);
        } catch (FeishuApiException e) {
            if (e.getCode() != NO_DEPT_AUTHORITY_ERROR) {
                throw e;
            }
            return null;
        }
    }

    private void handleUserActive(String appId, String tenantKey, String spaceId,
                                  FeishuUserObject userObject) {
        // After the employee is employed, click the corresponding enterprise on the client to activate it
        // Tenant user records can only be created when the address book is synchronized. After the employee activates, there should be no space station
        // Create Tenant User Records
        boolean isExistTenantUser =
            iSocialTenantUserService.isTenantUserUnionIdExist(appId, tenantKey,
                userObject.getOpenId(), userObject.getUnionId());
        if (!isExistTenantUser) {
            iSocialTenantUserService.create(appId, tenantKey, userObject.getOpenId(),
                userObject.getUnionId());
        }
        // Query whether there is this member in the space. The deleted member should also be found out
        Long memberId =
            iMemberService.getMemberIdByOpenIdIgnoreDelete(spaceId, userObject.getOpenId());
        if (memberId == null) {
            // Create Member
            MemberEntity member =
                SocialFactory.createFeishuMember(IdWorker.getId(), spaceId, userObject.getName(),
                    userObject.getMobile(), userObject.getEmail(), userObject.getJobTitle(),
                    userObject.getOpenId(), false);
            iMemberService.batchCreate(spaceId, Collections.singletonList(member));
            memberId = member.getId();
        } else {
            // Restore member information
            MemberEntity member = iMemberService.getByIdIgnoreDelete(memberId);
            if (member.getIsDeleted()) {
                // Deleted recovery members
                member.setMemberName(userObject.getName());
                member.setIsActive(true);
                member.setStatus(UserSpaceStatus.INACTIVE.getStatus());
                member.setIsPoint(true);
                // Member names in the space do not synchronize user names
                member.setNameModified(true);
                // Recover member data
                iMemberService.restoreMember(member);
                // Restore Organization Unit Data
                iUnitService.restoreMemberUnit(spaceId, Collections.singletonList(memberId));
            } else {
                // Update member information
                MemberEntity updateMember = new MemberEntity();
                updateMember.setId(memberId);
                updateMember.setMemberName(userObject.getName());
                iMemberService.updateById(updateMember);
            }
        }
        // Create Department Association
        handleUserDepartmentRelate(tenantKey, spaceId, memberId, userObject.getDepartmentIds());
    }

    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void handleDeptCreateEvent(ContactDeptCreateEvent event) {
        // Switch application configuration context
        iFeishuService.switchDefaultContext();
        String appId = event.getHeader().getAppId();
        String tenantKey = event.getHeader().getTenantKey();
        String lockKey = StrUtil.join("-", appId, tenantKey);
        Lock lock = redisLockRegistry.obtain(lockKey);
        try {
            if (lock.tryLock(100, TimeUnit.MILLISECONDS)) {
                try {
                    // Get the space ID of the current department
                    String spaceId =
                        iSocialTenantBindService.getTenantDepartmentBindSpaceId(appId, tenantKey);
                    if (StrUtil.isBlank(spaceId)) {
                        return;
                    }
                    handleCreateDept(tenantKey, spaceId, event.getEvent().getDepartment());
                    // Event completion
                    iSocialFeishuEventLogService.doneEvent(event.getHeader().getEventId());
                } catch (Exception e) {
                    log.error(
                        String.format("Tenant [%s] handling events [%s] fail, resolve it quickly",
                            tenantKey, event.getHeader().getEventType()), e);
                    throw e;
                } finally {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            log.error("[" + tenantKey + "]Tenant lock operations are too frequent", e);
            throw new BusinessException(BillingException.ACCOUNT_CREDIT_ALTER_FREQUENTLY);
        }
    }

    private void handleCreateDept(String tenantKey, String spaceId, FeishuDeptObject deptObject) {
        FeishuDeptObject parentDeptObject = FeishuContactAuthScope.createRootDeptObject();
        if (StrUtil.isNotBlank(deptObject.getParentDepartmentId()) &&
            !deptObject.getParentDepartmentId().equals(FEISHU_ROOT_DEPT_ID)) {
            if (deptObject.getParentDepartmentId().startsWith("od-")) {
                parentDeptObject = getDeptOrNull(tenantKey, deptObject.getParentDepartmentId());
                if (parentDeptObject == null) {
                    parentDeptObject = FeishuContactAuthScope.createRootDeptObject();
                }
            }
        }
        // The newly added department does not need to be queried in the system, just know what the parent department of the department is in the enterprise tenant
        Long toCreateTeamId =
            createTeamIfNotExist(tenantKey, spaceId, parentDeptObject.getDepartmentId(),
                StrUtil.sub(deptObject.getName(), 0, 99), deptObject.getOrder());

        // Add Lark department record
        SocialTenantDepartmentEntity tenantDepartment = new SocialTenantDepartmentEntity();
        tenantDepartment.setId(IdWorker.getId());
        tenantDepartment.setTenantId(tenantKey);
        tenantDepartment.setSpaceId(spaceId);
        tenantDepartment.setDepartmentId(deptObject.getDepartmentId());
        tenantDepartment.setOpenDepartmentId(deptObject.getOpenDepartmentId());
        tenantDepartment.setParentId(parentDeptObject.getDepartmentId());
        tenantDepartment.setParentOpenDepartmentId(parentDeptObject.getOpenDepartmentId());
        tenantDepartment.setDepartmentName(deptObject.getName());
        iSocialTenantDepartmentService.createBatch(Collections.singletonList(tenantDepartment));
        // New Binding
        SocialTenantDepartmentBindEntity tenantDepartmentBind =
            new SocialTenantDepartmentBindEntity();
        tenantDepartmentBind.setId(IdWorker.getId());
        tenantDepartmentBind.setSpaceId(spaceId);
        tenantDepartmentBind.setTeamId(toCreateTeamId);
        tenantDepartmentBind.setTenantId(tenantKey);
        tenantDepartmentBind.setTenantDepartmentId(deptObject.getDepartmentId());
        tenantDepartmentBind.setTenantOpenDepartmentId(deptObject.getOpenDepartmentId());
        iSocialTenantDepartmentBindService.createBatch(
            Collections.singletonList(tenantDepartmentBind));
    }

    private void handleUserDepartmentRelate(String tenantKey, String spaceId, Long memberId,
                                            List<String> userDepartmentIds) {
        List<TeamMemberRelEntity> entities = new ArrayList<>();
        for (String departmentId : userDepartmentIds) {
            TeamMemberRelEntity dmr = new TeamMemberRelEntity();
            dmr.setId(IdWorker.getId());
            dmr.setMemberId(memberId);
            if (departmentId.equals(FEISHU_ROOT_DEPT_ID)) {
                Long rootTeamId = iTeamService.getRootTeamId(spaceId);
                dmr.setTeamId(rootTeamId);
                entities.add(dmr);
            } else {
                Long bindTeamId = compatibleFeishuDept(spaceId, tenantKey, departmentId);
                if (bindTeamId != null) {
                    dmr.setTeamId(bindTeamId);
                    entities.add(dmr);
                }
            }
        }
        iTeamMemberRelService.createBatch(entities);
    }

    private Long compatibleFeishuDept(String spaceId, String tenantKey, String departmentId) {
        if (departmentId.startsWith("od-")) {
            FeishuDeptObject deptObject = getDeptOrNull(tenantKey, departmentId);
            if (deptObject == null) {
                return null;
            }
            return iSocialTenantDepartmentBindService.getBindSpaceTeamId(spaceId, tenantKey,
                deptObject.getDepartmentId());
        }
        return iSocialTenantDepartmentBindService.getBindSpaceTeamId(spaceId, tenantKey,
            departmentId);
    }

    private void handleUserSuspend(String tenantKey, String spaceId, String userOpenId) {
        // The administrator suspends the use of this employee account in the enterprise in the background
        // After the suspension, members cannot log in to the enterprise, so it is irrelevant to prohibit logging in
        MemberEntity member = iMemberService.getBySpaceIdAndOpenId(spaceId, userOpenId);
        if (member == null) {
            log.warn("Lark enterprise [{}] suspended members [{}] not exist", tenantKey,
                userOpenId);
            return;
        }
        if (member.getIsAdmin()) {
            // If the primary administrator is suspended, other administrators should be replaced dynamically
            handleChangeMainAdminIfLeaveOrSuspend(tenantKey, userOpenId, spaceId, member.getId());
        }
        List<Long> memberIds = Collections.singletonList(member.getId());
        // Delete a member's associated department
        iTeamMemberRelService.removeByMemberIds(memberIds);
        // delete the associated role
        iRoleMemberService.removeByRoleMemberIds(memberIds);
        // Delete Member
        iMemberService.removeByMemberIds(memberIds);
    }

    private void handleUserRestore(String tenantKey, String spaceId, FeishuUserObject userObject) {
        // The administrator restores this employee account in the background for use in the enterprise
        Long memberId =
            iMemberService.getMemberIdByOpenIdIgnoreDelete(spaceId, userObject.getOpenId());
        if (memberId == null) {
            log.warn("Lark enterprise [{}] recovered members [{}] not exist", tenantKey,
                userObject.getOpenId());
            return;
        }
        MemberEntity member = iMemberService.getByIdIgnoreDelete(memberId);
        member.setIsActive(true);
        member.setStatus(UserSpaceStatus.INACTIVE.getStatus());
        member.setIsPoint(true);
        // Member names in the space do not synchronize user names
        member.setNameModified(true);
        // Recover member data
        iMemberService.restoreMember(member);
        // Restore Organization Unit Data
        iUnitService.restoreMemberUnit(spaceId, Collections.singletonList(memberId));
        // Relate departments
        handleUserDepartmentRelate(tenantKey, spaceId, memberId, userObject.getDepartmentIds());
    }

    public void sendEntryCardToUser(String appId, String tenantKey, String userOpenId) {
        try {
            // Send the <Start Using> message card to the user
            Message cardMessage = FeishuCardFactory.createV2EntryCardMsg(appId);
            String messageId =
                iFeishuService.sendCardMessageToUserPrivate(tenantKey, userOpenId, cardMessage);
            log.info("Message delivery ID：{}", messageId);
        } catch (FeishuApiException exception) {
            // Don't pay attention if you can't send it out. Call the police to find out the reason
            log.error("Failed to send message card to application", exception);
        }
    }

    public void sendSeatsOverLimitCardToUser(String appId, String tenantKey, String userOpenId) {
        try {
            // Send the <Start Using> message card to the user
            Message cardMessage = FeishuCardFactory.createSeatsOverLimitCardMsg(appId);
            String messageId =
                iFeishuService.sendCardMessageToUserPrivate(tenantKey, userOpenId, cardMessage);
            log.info("Message delivery ID：{}", messageId);
        } catch (FeishuApiException exception) {
            // Don't pay attention if you can't send it out. Call the police to find out the reason
            log.error("Failed to send message card to application", exception);
        }
    }

    private void sendEntryCardToChatGroup(String appId, String tenantKey, String chatId) {
        try {
            // Send <Start using> message cards to the group
            Message cardMessage = FeishuCardFactory.createV2EntryCardMsg(appId);
            String messageId =
                iFeishuService.sendCardMessageToChatGroup(tenantKey, chatId, cardMessage);
            log.info("Message delivery ID：{}", messageId);
        } catch (FeishuApiException exception) {
            // Don't pay attention if you can't send it out. Call the police to find out the reason
            log.error("Failed to send message card to group", exception);
        }
    }

    @Override
    public void handleTenantOrders(String tenantKey, String appId) {
        List<String> orderData =
            iSocialFeishuOrderService.getOrdersByTenantIdAndAppId(tenantKey, appId);
        orderData.forEach(data -> {
            OrderPaidEvent event = JSONUtil.toBean(data, OrderPaidEvent.class);
            try {
                handleOrderPaidEvent(event);
            } catch (Exception e) {
                log.error(
                    "Failed to process tenant order, please solve it as soon as possible:{}:{}",
                    tenantKey, event.getOrderId(), e);
            }
        });
    }

    class ContactMeta {

        String spaceId;

        List<SocialTenantDepartmentEntity> tenantDepartmentEntities = new ArrayList<>();

        List<SocialTenantDepartmentEntity> updateTenantDepartmentEntities = new ArrayList<>();

        List<SocialTenantDepartmentBindEntity> tenantDepartmentBindEntities = new ArrayList<>();

        List<SocialTenantUserEntity> tenantUserEntities = new ArrayList<>();

        List<TeamEntity> teamEntities = new ArrayList<>();

        List<TeamEntity> updateTeamEntities = new ArrayList<>();

        List<MemberEntity> memberEntities = new ArrayList<>();

        List<MemberEntity> updateMemberEntities = new ArrayList<>();

        List<TeamMemberRelEntity> teamMemberRelEntities = new ArrayList<>();

        void doSaveOrUpdate(String appId, String tenantKey, String userOpenId,
                            boolean checkSeatNum) {
            iSocialTenantUserService.createBatch(tenantUserEntities);

            iSocialTenantDepartmentService.createBatch(tenantDepartmentEntities);
            iSocialTenantDepartmentService.updateBatchById(updateTenantDepartmentEntities);

            iSocialTenantDepartmentBindService.createBatch(tenantDepartmentBindEntities);


            // Check if the seat limit is exceeded
            try {
                Set<Long> needSendMesageSet = new HashSet<>();
                // Find the space master admin to check if the seating limit is exceeded
                Long mainAdminUserId = iSpaceService.getSpaceMainAdminUserId(spaceId);
                needSendMesageSet.add(mainAdminUserId);
                if (ObjectUtil.isNotEmpty(userOpenId)) {
                    Long operateUserId = iMemberService.getUserIdByOpenId(spaceId, userOpenId);
                    needSendMesageSet.add(operateUserId);
                }
                boolean isNotOverLimit =
                    !checkSeatNum || iSpaceService.checkSeatOverLimitAndSendNotify(
                        ListUtil.toList(needSendMesageSet), spaceId, memberEntities.size(), false,
                        true);
                if (isNotOverLimit) {
                    iMemberService.batchCreate(spaceId, memberEntities);
                } else {
                    if (StrUtil.isNotBlank(userOpenId)) {
                        // Send Message Card
                        sendSeatsOverLimitCardToUser(appId, tenantKey, userOpenId);
                    }
                }
            } catch (Exception e) {
                log.error("Failed to check seat limit and send message card", e);
            }
            iMemberService.updatePartPropertyBatchByMemberId(spaceId, updateMemberEntities);

            iTeamService.batchCreateTeam(spaceId, teamEntities);
            iTeamService.updateBatchById(updateTeamEntities);
            iTeamMemberRelService.createBatch(teamMemberRelEntities);
        }
    }

    public class SpaceContext {

        String spaceId;

        String spaceName;

        String spaceLogo;

        Long rootTeamId;

        String rootNodeId;

        private boolean isCreate;

        public SpaceContext(String spaceId) {
            this.spaceId = spaceId;
        }

        void prepare(String tenantId) {
            if (StrUtil.isBlank(spaceId)) {
                spaceId = IdUtil.createSpaceId();
                FeishuTenantInfo tenantInfo = null;
                try {
                    tenantInfo = iFeishuService.getFeishuTenantInfo(tenantId);
                } catch (FeishuApiException exception) {
                    if (exception.getCode() == GET_TENANT_DENIED) {
                        // This application has not been used by the enterprise in 180 days, so it is not an error, just return NULL
                        log.warn(
                            "Lark enterprise information cannot be obtained, and the other party has not used this application for more than 180 days");
                    }
                }
                spaceName = getTenantNameIfAbsent(tenantInfo);
                spaceLogo = getTenantAvatarIfAbsent(tenantInfo);
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
            }
        }

        void after(String tenantId, Long owner) {
            if (isCreate) {
                String props = JSONUtil.parseObj(SpaceGlobalFeature.builder().nodeExportable(true)
                    .invitable(false).joinable(false).mobileShowable(false).build()).toString();
                SpaceEntity space = SpaceEntity.builder()
                    .spaceId(spaceId)
                    .name(spaceName)
                    .logo(spaceLogo)
                    .props(props)
                    .owner(owner)
                    .build();
                iSpaceService.save(space);
                // Bind the tenant and this space
                iSocialTenantBindService.addTenantBind(iFeishuService.getIsvAppId(), tenantId,
                    spaceId);
                // Update app market status
                iAppInstanceService.createInstanceByAppType(spaceId, AppType.LARK_STORE.name());

                // Randomly quote the template of the popular recommended carousel chart in the template center
                String templateNodeId = iTemplateService.getDefaultTemplateNodeId();
                if (StrUtil.isNotBlank(templateNodeId)) {
                    // Transfer node method, including GRPC calls, and place the last
                    iNodeService.copyNodeToSpace(-1L, spaceId, rootNodeId, templateNodeId,
                        NodeCopyOptions.create());
                }
            } else {
                spaceMapper.updateSpaceOwnerId(spaceId, owner, null);
            }
        }

        private String getTenantNameIfAbsent(FeishuTenantInfo tenantInfo) {
            if (tenantInfo != null) {
                return tenantInfo.getName();
            }
            return "工作空间站";
        }

        private String getTenantAvatarIfAbsent(FeishuTenantInfo tenantInfo) {
            if (tenantInfo != null) {
                return iAssetService.downloadAndUploadUrl(tenantInfo.getAvatar().getAvatarOrigin());
            }
            return null;
        }

        Long getMainAdmin() {
            if (isCreate) {
                return null;
            } else {
                return iSpaceService.getSpaceMainAdminMemberId(spaceId);
            }
        }

        List<MemberEntity> getMembers() {
            if (isCreate) {
                return new ArrayList<>();
            } else {
                return iMemberService.getMembersBySpaceId(spaceId, true);
            }
        }
    }
}
