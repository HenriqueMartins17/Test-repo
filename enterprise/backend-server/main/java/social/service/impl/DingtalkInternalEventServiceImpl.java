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

import static com.apitable.enterprise.social.enums.SocialException.USER_NOT_EXIST;
import static com.vikadata.social.dingtalk.constants.DingTalkConst.ROOT_DEPARTMENT_ID;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.enterprise.social.entity.SocialTenantDepartmentBindEntity;
import com.apitable.enterprise.social.entity.SocialTenantDepartmentEntity;
import com.apitable.enterprise.social.entity.SocialTenantEntity;
import com.apitable.enterprise.social.entity.SocialTenantUserEntity;
import com.apitable.enterprise.social.enums.SocialAppType;
import com.apitable.enterprise.social.enums.SocialPlatformType;
import com.apitable.enterprise.social.factory.SocialFactory;
import com.apitable.enterprise.social.model.DingTalkContactDTO;
import com.apitable.enterprise.social.model.DingTalkContactDTO.DingTalkDepartmentDTO;
import com.apitable.enterprise.social.model.TenantDepartmentBindDTO;
import com.apitable.enterprise.social.service.IDingTalkService;
import com.apitable.enterprise.social.service.IDingtalkInternalEventService;
import com.apitable.enterprise.social.service.ISocialTenantBindService;
import com.apitable.enterprise.social.service.ISocialTenantDepartmentBindService;
import com.apitable.enterprise.social.service.ISocialTenantDepartmentService;
import com.apitable.enterprise.social.service.ISocialTenantService;
import com.apitable.enterprise.social.service.ISocialTenantUserService;
import com.apitable.enterprise.social.service.impl.SocialServiceImpl.OpenDeptToTeam;
import com.apitable.enterprise.social.service.impl.SocialServiceImpl.OpenDeptToTeam.SyncOperation;
import com.apitable.enterprise.social.service.impl.SocialServiceImpl.OpenUserToMember;
import com.apitable.interfaces.social.enums.SocialNameModified;
import com.apitable.organization.entity.MemberEntity;
import com.apitable.organization.entity.TeamEntity;
import com.apitable.organization.entity.TeamMemberRelEntity;
import com.apitable.organization.enums.UnitType;
import com.apitable.organization.enums.UserSpaceStatus;
import com.apitable.organization.factory.OrganizationFactory;
import com.apitable.organization.service.IMemberService;
import com.apitable.organization.service.ITeamMemberRelService;
import com.apitable.organization.service.ITeamService;
import com.apitable.organization.service.IUnitService;
import com.apitable.shared.cache.service.UserSpaceCacheService;
import com.apitable.space.service.ISpaceService;
import com.apitable.space.vo.SpaceGlobalFeature;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.vikadata.social.dingtalk.DingtalkConfig.AgentApp;
import com.vikadata.social.dingtalk.enums.DingTalkEventTag;
import com.vikadata.social.dingtalk.model.DingTalkAppVisibleScopeResponse;
import com.vikadata.social.dingtalk.model.DingTalkServerAuthInfoResponse;
import com.vikadata.social.dingtalk.model.DingTalkUserDetail;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DingtalkInternalEventServiceImpl implements IDingtalkInternalEventService {

    @Resource
    private IDingTalkService iDingTalkService;

    @Resource
    private ISocialTenantUserService iSocialTenantUserService;

    @Resource
    private ISocialTenantService iSocialTenantService;

    @Resource
    private ISocialTenantBindService iSocialTenantBindService;

    @Resource
    private ISocialTenantDepartmentService iSocialTenantDepartmentService;

    @Resource
    private ISocialTenantDepartmentBindService iSocialTenantDepartmentBindService;

    @Resource
    private ISpaceService iSpaceService;

    @Resource
    private IUnitService iUnitService;

    @Resource
    private ITeamService iTeamService;

    @Resource
    private IMemberService iMemberService;

    @Resource
    private ITeamMemberRelService iTeamMemberRelService;

    @Resource
    private UserSpaceCacheService userSpaceCacheService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Set<String> dingTalkAppBindSpace(String agentId, String spaceId, Long bindUserId,
                                            LinkedHashMap<Long, DingTalkContactDTO> contactMap) {
        AgentApp agentApp = iDingTalkService.getAgentAppById(agentId);
        String corpId = agentApp.getCorpId();
        String appId = agentApp.getCustomKey();
        // The union id bound by the current user
        String openId =
            iSocialTenantUserService.getOpenIdByTenantIdAndUserId(appId, corpId, bindUserId);
        // The third party does not have this user
        ExceptionUtil.isFalse(StrUtil.isBlank(openId), USER_NOT_EXIST);
        // When the space is bound for the first time, increase the binding
        iSocialTenantBindService.addTenantBind(agentApp.getCustomKey(), corpId, spaceId);
        // To bind users, you need to prevent users from being bound when a callback is registered before registering a callback
        Set<String> tenantUserIds =
            connectDingTalkAgentAppContact(spaceId, agentId, openId, contactMap);
        // There is no enterprise application information and it needs to be created. If it has been deactivated, it needs to be updated
        SocialTenantEntity entity = iSocialTenantService.getByAppIdAndTenantId(appId, corpId);
        if (entity == null || !entity.getStatus()) {
            DingTalkServerAuthInfoResponse serverAuthInfo =
                iDingTalkService.getServerAuthInfo(agentId);
            String url = iDingTalkService.getDingTalkEventCallbackUrl(agentId);
            List<String> registerEvents = DingTalkEventTag.baseEvent();
            String authInfo =
                SocialFactory.createDingTalkAuthInfo(serverAuthInfo, agentId, url, registerEvents);
            // Save or update tenant information Application visibility
            DingTalkAppVisibleScopeResponse visibleScope =
                iDingTalkService.getAppVisibleScopes(agentId);
            iSocialTenantService.createOrUpdateWithScope(SocialPlatformType.DINGTALK,
                SocialAppType.INTERNAL,
                appId, corpId, JSONUtil.toJsonStr(visibleScope), authInfo);
            // Register the application callback event url
            iDingTalkService.registerCallbackUrl(agentId, url, registerEvents);
        }
        // Change the global status of the space (application and invitation are prohibited)
        SpaceGlobalFeature feature =
            SpaceGlobalFeature.builder().joinable(false).invitable(false).build();
        iSpaceService.switchSpacePros(bindUserId, spaceId, feature);
        return tenantUserIds;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Set<String> dingTalkRefreshContact(String spaceId, String agentId, String operatorOpenId,
                                              LinkedHashMap<Long, DingTalkContactDTO> contactMap) {
        AgentApp agentApp = iDingTalkService.getAgentAppById(agentId);
        Set<String> openIds =
            connectDingTalkAgentAppContact(spaceId, agentId, operatorOpenId, contactMap);
        // Save or update tenant information
        DingTalkServerAuthInfoResponse serverAuthInfo = iDingTalkService.getServerAuthInfo(agentId);
        String url = iDingTalkService.getDingTalkEventCallbackUrl(agentId);
        List<String> registerEvents = DingTalkEventTag.baseEvent();
        String authInfo =
            SocialFactory.createDingTalkAuthInfo(serverAuthInfo, agentId, url, registerEvents);
        // Application visible range
        DingTalkAppVisibleScopeResponse visibleScope =
            iDingTalkService.getAppVisibleScopes(agentId);
        iSocialTenantService.createOrUpdateWithScope(SocialPlatformType.DINGTALK,
            SocialAppType.INTERNAL,
            agentApp.getCustomKey(), agentApp.getCorpId(), JSONUtil.toJsonStr(visibleScope),
            authInfo);
        return openIds;
    }

    /**
     * TODO replace use SocialContactManager
     *
     * @see com.apitable.enterprise.social.component.SocialContactManager
     */
    public Set<String> connectDingTalkAgentAppContact(String spaceId, String agentId,
                                                      String operatorOpenId,
                                                      LinkedHashMap<Long, DingTalkContactDTO> contactMap) {
        if (contactMap.isEmpty()) {
            return new HashSet<>();
        }
        // Self built authorization application. It is not necessary to obtain the address book authorization permission of DingTalk
        AgentApp agentApp = iDingTalkService.getAgentAppById(agentId);
        String tenantId = agentApp.getCorpId();
        // Root organization ID of the space
        Long rootTeamId = iTeamService.getRootTeamId(spaceId);
        // DingTalk user information of the main administrator of the space
        DingTalkUserDetail operatorOpenUser =
            iDingTalkService.getUserDetailByUserId(agentId, operatorOpenId);
        // Primary administrator member ID of the space
        Long mainAdminMemberId = iSpaceService.getSpaceMainAdminMemberId(spaceId);
        // Initialize master administrator information
        OpenUserToMember mainAdminOpenUserToMember =
            OpenUserToMember.builder().memberId(mainAdminMemberId)
                .memberName(operatorOpenUser.getName())
                .openId(operatorOpenId)
                .oldUnitTeamIds(
                    CollUtil.newHashSet(iTeamMemberRelService.getTeamByMemberId(mainAdminMemberId)))
                .isNew(false).isCurrentSync(true).build();
        DingTalkContactMeta contactMeta =
            new DingTalkContactMeta(spaceId, tenantId, agentId, rootTeamId);
        contactMeta.openUserToMemberMap.put(operatorOpenId, mainAdminOpenUserToMember);
        // Initialize root department information
        OpenDeptToTeam rootDeptToTeam =
            OpenDeptToTeam.builder().departmentId(ROOT_DEPARTMENT_ID).teamId(rootTeamId)
                .isNew(false).isCurrentSync(true).op(SyncOperation.KEEP)
                .build();
        contactMeta.openDeptToTeamMap.put(ROOT_DEPARTMENT_ID, rootDeptToTeam);
        // The map of the currently synchronized member open User -> vika Member
        List<MemberEntity> memberList = iMemberService.getMembersBySpaceId(spaceId, true);
        // The same open ID, only the latest member is reserved
        Map<String, OpenUserToMember> memberListByOpenIdToMap = memberList.stream()
            // Since the master management is initialized manually above, filtering is required here
            .filter(dto -> !dto.getId().equals(mainAdminMemberId))
            .collect(Collectors.toMap(MemberEntity::getOpenId, dto -> {
                OpenUserToMember cahceData =
                    OpenUserToMember.builder().openId(dto.getOpenId()).memberId(dto.getId())
                        .memberName(dto.getMemberName()).isDeleted(dto.getIsDeleted()).build();
                // Query Associated Organization Ids todo Batch query is required here
                cahceData.setOldUnitTeamIds(CollUtil.newHashSet(
                    iTeamMemberRelService.getTeamByMemberId(cahceData.getMemberId())));
                return cahceData;
            }, (pre, cur) -> !cur.getIsDeleted() ? cur : pre));
        contactMeta.openUserToMemberMap.putAll(memberListByOpenIdToMap);

        // DingTalk department ID and vika system department ID, the initial value is the root department ID
        List<TenantDepartmentBindDTO> teamList =
            iSocialTenantDepartmentService.getTenantBindTeamListBySpaceId(spaceId);
        Map<Long, OpenDeptToTeam> teamListByDepartmentIdToMap = teamList.stream().collect(
            Collectors.toMap(keyDto -> Long.valueOf(keyDto.getDepartmentId()),
                dto -> OpenDeptToTeam.builder()
                    .id(dto.getId()).departmentName(dto.getDepartmentName())
                    .departmentId(Long.valueOf(dto.getDepartmentId()))
                    .openDepartmentId(Long.valueOf(dto.getOpenDepartmentId()))
                    .parentDepartmentId(Long.valueOf(dto.getParentDepartmentId()))
                    .parentOpenDepartmentId(Long.valueOf(dto.getParentOpenDepartmentId()))
                    .teamId(dto.getTeamId()).parentTeamId(dto.getParentTeamId())
                    .internalSequence(dto.getInternalSequence())
                    .build()));
        contactMeta.openDeptToTeamMap.putAll(teamListByDepartmentIdToMap);
        // Synchronize contacts
        syncDingTalkContacts(contactMeta, contactMap);

        // Limit on number of inspectors
        // long defaultMaxMemberCount = iSubscriptionService.getPlanSeats(spaceId);
        // ExceptionUtil.isTrue(contactMeta.openIds.size() <= defaultMaxMemberCount, SubscribeFunctionException.MEMBER_LIMIT);
        // If the synchronization member does not have a master administrator, the master administrator needs to be attached to the root door
        if (!contactMeta.openIds.contains(operatorOpenId)) {
            contactMeta.teamMemberRelEntities.add(
                OrganizationFactory.createTeamMemberRel(rootTeamId, mainAdminMemberId));
        }
        // Initialize address book structure
        contactMeta.doDeleteTeams();
        // Delete the missing member
        contactMeta.deleteMembers();
        // Delete member association
        contactMeta.doDeleteMemberRels();
        // Update master administrator information
        contactMeta.updateMainAdminMember(operatorOpenId);
        // Store to DB
        contactMeta.doSaveOrUpdate();
        // Delete Cache
        userSpaceCacheService.delete(spaceId);
        return contactMeta.openIds;
    }

    private void syncDingTalkContacts(DingTalkContactMeta contactMeta,
                                      LinkedHashMap<Long, DingTalkContactDTO> contactTree) {
        Set<Long> deptIds = contactTree.keySet();
        contactTree.forEach((deptId, contact) -> {
            DingTalkDepartmentDTO dingTalkDepartmentDTO = contact.getDepartment();
            // The current visible range has no parent department
            if (!deptIds.contains(dingTalkDepartmentDTO.getParentDeptId())) {
                dingTalkDepartmentDTO.setParentDeptId(ROOT_DEPARTMENT_ID);
            }
            handleDingTalkDept(contactMeta, dingTalkDepartmentDTO);
            if (contact.getUserMap() != null) {
                contact.getUserMap().values()
                    .forEach(user -> handleDingTalkMember(contactMeta, user,
                        contactMeta.getTeamId(deptId)));
            }
        });
    }

    private void handleDingTalkMember(DingTalkContactMeta contactMeta,
                                      DingTalkContactDTO.DingTalkUserDTO userDetail,
                                      Long parentTeamId) {
        // Filter inactive DingTalk users
        if (BooleanUtil.isFalse(userDetail.getActive())) {
            return;
        }
        String dingTalkUserid = userDetail.getOpenId();
        OpenUserToMember cahceMember = contactMeta.openUserToMemberMap.get(dingTalkUserid);
        // No synchronization
        if (!contactMeta.openIds.contains(dingTalkUserid)) {
            // The member in the database does not exist and has not been synchronized. Users can be bound only when they need to log in
            if (null == cahceMember) {
                MemberEntity member = SocialFactory.createDingTalkMember(userDetail)
                    .setId(IdWorker.getId())
                    .setSpaceId(contactMeta.spaceId)
                    .setIsActive(false)
                    .setIsPoint(true)
                    .setStatus(UserSpaceStatus.INACTIVE.getStatus())
                    .setNameModified(false)
                    .setIsSocialNameModified(SocialNameModified.NO_SOCIAL.getValue())
                    .setIsAdmin(false);
                contactMeta.memberEntities.add(member);
                cahceMember = OpenUserToMember.builder().memberId(member.getId())
                    .memberName(member.getMemberName()).openId(dingTalkUserid).isNew(true).build();
            } else {
                // ExistCheck whether key information needs to be modified
                if (!cahceMember.getMemberName().equals(userDetail.getName()) ||
                    cahceMember.getIsDeleted() ||
                    !userDetail.getOpenId().equals(cahceMember.getOpenId())) {
                    MemberEntity updateMember =
                        MemberEntity.builder().id(cahceMember.getMemberId())
                            .memberName(userDetail.getName()).openId(userDetail.getOpenId())
                            .isDeleted(false).spaceId(contactMeta.spaceId).build();
                    // Members to be recovered
                    if (cahceMember.getIsDeleted()) {
                        contactMeta.recoverMemberIds.add(cahceMember.getMemberId());
                    }
                    // Update Cache
                    cahceMember.setMemberName(userDetail.getName());
                    cahceMember.setOpenId(userDetail.getOpenId());
                    cahceMember.setIsDeleted(false);
                    contactMeta.openUserToMemberMap.put(cahceMember.getOpenId(), cahceMember);
                    contactMeta.updateMemberEntities.add(updateMember);
                }
            }
            // Mark users for this synchronization
            cahceMember.setIsCurrentSync(true);
        }
        // Bind departments. If there is no corresponding department relationship in the cache, directly link to the root department
        cahceMember.getNewUnitTeamIds().add(parentTeamId);
        if (CollUtil.isEmpty(cahceMember.getOldUnitTeamIds()) ||
            (CollUtil.isNotEmpty(cahceMember.getOldUnitTeamIds()) &&
                !cahceMember.getOldUnitTeamIds().contains(parentTeamId))) {
            // Member history does not exist under department, add member and department association records
            contactMeta.teamMemberRelEntities.add(
                OrganizationFactory.createTeamMemberRel(parentTeamId, cahceMember.getMemberId()));
        }
        contactMeta.openIds.add(dingTalkUserid);
        // Add DingTalk user - vika user
        contactMeta.openUserToMemberMap.put(dingTalkUserid, cahceMember);
    }

    private void handleDingTalkDept(DingTalkContactMeta contactMeta,
                                    DingTalkDepartmentDTO deptBaseInfo) {
        if (ROOT_DEPARTMENT_ID.equals(deptBaseInfo.getDeptId())) {
            // Do not process root department
            return;
        }
        Long parentDeptId = deptBaseInfo.getParentDeptId();
        String tenantId = contactMeta.tenantId;
        String spaceId = contactMeta.spaceId;
        List<Long> subDepIds = contactMeta.openDeptIdMap.containsKey(parentDeptId) ?
            contactMeta.openDeptIdMap.get(parentDeptId) : CollUtil.newArrayList();
        subDepIds.add(deptBaseInfo.getDeptId());
        contactMeta.openDeptIdMap.put(parentDeptId, subDepIds);
        int sequence = subDepIds.size();

        OpenDeptToTeam openDeptToTeam = contactMeta.openDeptToTeamMap.get(deptBaseInfo.getDeptId());
        Long teamPid = contactMeta.getTeamId(parentDeptId);

        if (null == openDeptToTeam) {
            TeamEntity team = OrganizationFactory.createTeam(spaceId, IdWorker.getId(), teamPid,
                deptBaseInfo.getDeptName(),
                sequence);

            contactMeta.teamEntities.add(team);
            SocialTenantDepartmentEntity dingTalkDepartment =
                SocialFactory.createDingTalkDepartment(spaceId, tenantId, deptBaseInfo);
            contactMeta.tenantDepartmentEntities.add(dingTalkDepartment);
            contactMeta.tenantDepartmentBindEntities.add(
                SocialFactory.createTenantDepartmentBind(spaceId, team.getId(), tenantId,
                    deptBaseInfo.getDeptId().toString()));
            // Synchronous relation
            openDeptToTeam = OpenDeptToTeam.builder()
                .departmentName(team.getTeamName())
                .departmentId(Long.valueOf(dingTalkDepartment.getDepartmentId()))
                .openDepartmentId(Long.valueOf(dingTalkDepartment.getOpenDepartmentId()))
                .parentDepartmentId(Long.valueOf(dingTalkDepartment.getParentId()))
                .parentOpenDepartmentId(
                    Long.valueOf(dingTalkDepartment.getParentOpenDepartmentId()))
                .teamId(team.getId()).parentTeamId(team.getParentId())
                .internalSequence(team.getSequence())
                .isNew(true)
                .op(SyncOperation.ADD)
                .build();
        } else {
            boolean isUpdate = BooleanUtil.or(
                // Modify Department Level
                BooleanUtil.negate(openDeptToTeam.getParentOpenDepartmentId().equals(parentDeptId)),
                // Modify Name
                BooleanUtil.negate(
                    openDeptToTeam.getDepartmentName().equals(deptBaseInfo.getDeptName())),
                // Modify Order
                openDeptToTeam.getInternalSequence() != sequence
            );
            if (isUpdate) {
                // Modify Department Structure
                SocialTenantDepartmentEntity updateTenantDepartment =
                    SocialTenantDepartmentEntity.builder()
                        .id(openDeptToTeam.getId())
                        .departmentName(deptBaseInfo.getDeptName())
                        .parentId(parentDeptId.toString())
                        .parentOpenDepartmentId(parentDeptId.toString())
                        .departmentOrder(sequence)
                        .build();
                contactMeta.updateTenantDepartmentEntities.add(updateTenantDepartment);

                TeamEntity updateTeam = TeamEntity.builder()
                    .id(openDeptToTeam.getTeamId())
                    .teamName(deptBaseInfo.getDeptName())
                    .parentId(teamPid)
                    .sequence(sequence)
                    .build();
                contactMeta.updateTeamEntities.add(updateTeam);

                openDeptToTeam.setDepartmentName(updateTenantDepartment.getDepartmentName())
                    .setParentDepartmentId(Long.valueOf(updateTenantDepartment.getParentId()))
                    .setParentOpenDepartmentId(
                        Long.valueOf(updateTenantDepartment.getParentOpenDepartmentId()))
                    .setParentTeamId(updateTeam.getParentId())
                    .setInternalSequence(sequence);
                openDeptToTeam.setOp(SyncOperation.UPDATE);
            } else {
                // No modification
                openDeptToTeam.setOp(SyncOperation.KEEP);
            }
        }

        // Save parent department ID -> team ID
        openDeptToTeam.setIsCurrentSync(true); // Mark the department for this synchronization
        contactMeta.openDeptToTeamMap.put(deptBaseInfo.getDeptId(), openDeptToTeam);
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

        List<Long> recoverMemberIds = new ArrayList<>();

        void doSaveOrUpdate() {
            iSocialTenantUserService.createBatch(tenantUserEntities);

            iSocialTenantDepartmentService.createBatch(tenantDepartmentEntities);
            iSocialTenantDepartmentService.updateBatchById(updateTenantDepartmentEntities);

            iSocialTenantDepartmentBindService.createBatch(tenantDepartmentBindEntities);

            iMemberService.batchCreate(spaceId, memberEntities);
            // Restore the previous organizational unit to prevent the members in the table from being grayed out
            if (!recoverMemberIds.isEmpty()) {
                iUnitService.batchUpdateIsDeletedBySpaceIdAndRefId(spaceId, recoverMemberIds,
                    UnitType.MEMBER, false);
            }
            iMemberService.batchUpdateNameAndOpenIdAndIsDeletedByIds(updateMemberEntities);

            iTeamService.batchCreateTeam(spaceId, teamEntities);
            iTeamService.updateBatchById(updateTeamEntities);
            iTeamMemberRelService.createBatch(teamMemberRelEntities);
        }
    }

    class DingTalkContactMeta extends ContactMeta {
        String agentId;

        String tenantId;

        Long rootTeamId;

        // DingTalk User - vika User
        Map<String, OpenUserToMember> openUserToMemberMap = MapUtil.newHashMap(true);

        // DingTalk Department - vika Department
        Map<Long, OpenDeptToTeam> openDeptToTeamMap = MapUtil.newHashMap(true);

        // The DingTalk user ID of this synchronization, which is used to send the start message
        Set<String> openIds = CollUtil.newHashSet();

        // Store the parent-child department relationship for calculating sequence
        HashMap<Long, List<Long>> openDeptIdMap = new HashMap<>();

        DingTalkContactMeta(String spaceId, String tenantId, String agentId, Long rootTeamId) {
            this.spaceId = spaceId;
            this.tenantId = tenantId;
            this.agentId = agentId;
            this.rootTeamId = rootTeamId;
            this.openDeptIdMap.put(ROOT_DEPARTMENT_ID, CollUtil.newArrayList());
        }

        // Get the cached Team Id. No data. Default: root Team Id
        Long getTeamId(Long dingTalkDeptId) {
            return Optional.ofNullable(this.openDeptToTeamMap.get(dingTalkDeptId))
                .map(OpenDeptToTeam::getTeamId)
                .orElse(rootTeamId);
        }

        void doDeleteTeams() {
            // Calculate the groups to be deleted
            List<Long> oldTeamIds = iTeamService.getTeamIdsBySpaceId(spaceId);
            Map<Long, Long> newTeams = this.openDeptToTeamMap.values().stream()
                .filter(OpenDeptToTeam::getIsCurrentSync)
                .collect(
                    Collectors.toMap(OpenDeptToTeam::getTeamId, OpenDeptToTeam::getDepartmentId));

            Set<Long> newTeamIds = new HashSet<>(newTeams.keySet());

            // Calculate intersection, department without change
            newTeamIds.retainAll(oldTeamIds);
            if (!newTeamIds.isEmpty()) {
                // Calculate the difference set and the department to be deleted
                oldTeamIds.removeAll(newTeamIds);
            }

            if (CollUtil.isNotEmpty(oldTeamIds)) {
                List<Long> currentSyncMemberUsers = this.openUserToMemberMap.values().stream()
                    .filter(OpenUserToMember::getIsCurrentSync)
                    .map(OpenUserToMember::getMemberId).collect(Collectors.toList());

                Map<Long, String> teamToWecomTeamMap = this.openDeptToTeamMap.values().stream()
                    .collect(Collectors.toMap(OpenDeptToTeam::getTeamId,
                        dto -> dto.getDepartmentId().toString()));

                for (Long deleteTeamId : oldTeamIds) {
                    // Delete the Member under the vika department. There are multiple departments for the personnel. It is necessary to judge whether the synchronized personnel are in the list.
                    // If they exist, they will not be deleted. Otherwise, they will be deleted
                    List<Long> memberIds = iTeamMemberRelService.getMemberIdsByTeamId(deleteTeamId);
                    memberIds.removeAll(currentSyncMemberUsers);

                    String deleteWeComTeamId = teamToWecomTeamMap.get(deleteTeamId);
                    if (StrUtil.isNotBlank(deleteWeComTeamId)) {
                        // Remove department - delete the third-party department, delete the binding relationship, and delete the Vika department
                        iSocialTenantDepartmentService.deleteSpaceTenantDepartment(spaceId,
                            tenantId, deleteWeComTeamId);
                    } else {
                        // It means that there is no binding, and vika department is deleted directly
                        iTeamService.deleteTeam(deleteTeamId);
                    }
                    // Remove Members
                    iMemberService.batchDeleteMemberFromSpace(spaceId, memberIds, false);
                }
            }
        }

        void deleteMembers() {
            List<Long> oldMemberIds = iMemberService.getMemberIdsBySpaceId(spaceId);
            Map<Long, String> newMemberUsers = this.openUserToMemberMap.values().stream()
                .filter(OpenUserToMember::getIsCurrentSync)
                .collect(
                    Collectors.toMap(OpenUserToMember::getMemberId, OpenUserToMember::getOpenId));

            Set<Long> newMemberIds = new HashSet<>(newMemberUsers.keySet());

            // Calculate intersection, users without changes
            newMemberIds.retainAll(oldMemberIds);
            if (!newMemberIds.isEmpty()) {
                // Users to be deleted when calculating difference sets
                oldMemberIds.removeAll(newMemberIds);
            }

            // The member that is not equal to or needs to be deleted is empty, which means it is not the first synchronization
            Set<String> newWeComUserIds = this.openUserToMemberMap.values().stream()
                .filter(OpenUserToMember::getIsNew)
                .map(OpenUserToMember::getOpenId)
                .collect(Collectors.toSet());
            if (newMemberUsers.size() != newWeComUserIds.size() || oldMemberIds.isEmpty()) {
                // Recalculate the new users
                openIds.retainAll(newWeComUserIds);
            }

            // Remove Members
            iMemberService.batchDeleteMemberFromSpace(spaceId, oldMemberIds, false);
        }

        void doDeleteMemberRels() {
            this.openUserToMemberMap.values().forEach(cahceData -> {
                Set<Long> oldUnitTeamIds = cahceData.getOldUnitTeamIds();
                if (CollUtil.isNotEmpty(oldUnitTeamIds)) {
                    oldUnitTeamIds.removeAll(cahceData.getNewUnitTeamIds());
                    if (CollUtil.isNotEmpty(oldUnitTeamIds)) {
                        iTeamMemberRelService.removeByTeamIdsAndMemberId(cahceData.getMemberId(),
                            new ArrayList<>(oldUnitTeamIds));
                    }
                }
            });
        }

        void updateMainAdminMember(String openId) {
            OpenUserToMember adminMember = openUserToMemberMap.get(openId);
            // Update master administrator information
            iMemberService.updateById(MemberEntity.builder().memberName(adminMember.getMemberName())
                .id(adminMember.getMemberId()).openId(adminMember.getOpenId()).build());
        }
    }
}
