/*
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up
 * license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its
 * subdirectories does not constitute permission to use this code or APITable Enterprise Edition
 * features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

package com.apitable.enterprise.social.component;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.apitable.core.util.SpringContextHolder;
import com.apitable.enterprise.social.entity.SocialTenantDepartmentBindEntity;
import com.apitable.enterprise.social.entity.SocialTenantDepartmentEntity;
import com.apitable.enterprise.social.entity.SocialTenantUserEntity;
import com.apitable.enterprise.social.factory.SocialFactory;
import com.apitable.enterprise.social.model.SocialContactDTO;
import com.apitable.enterprise.social.model.SocialContactDTO.SocialDepartment;
import com.apitable.enterprise.social.model.SocialContactOptions;
import com.apitable.enterprise.social.model.TenantDepartmentBindDTO;
import com.apitable.enterprise.social.service.ISocialTenantDepartmentBindService;
import com.apitable.enterprise.social.service.ISocialTenantDepartmentService;
import com.apitable.enterprise.social.service.ISocialTenantUserService;
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
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Social Contact Manager.
 * </p>
 *
 * @author Chambers
 */
@Component
public class SocialContactManager {

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
    private ISocialTenantUserService iSocialTenantUserService;

    @Resource
    private ISocialTenantDepartmentService iSocialTenantDepartmentService;

    @Resource
    private ISocialTenantDepartmentBindService iSocialTenantDepartmentBindService;

    @Resource
    private UserSpaceCacheService userSpaceCacheService;

    public static SocialContactManager me() {
        return SpringContextHolder.getBean(SocialContactManager.class);
    }

    public Set<String> refreshContact(
        String spaceId,
        String tenantId,
        LinkedHashMap<String, SocialContactDTO> contactMap,
        SocialContactOptions options
    ) {
        String openId = options.getOperatorOpenId();
        String socialRootTeamId = options.getSocialRootTeamId();
        // Primary administrator member ID of the space
        Long mainAdminMemberId = iSpaceService.getSpaceMainAdminMemberId(spaceId);
        // Root team id of the space
        Long rootTeamId = iTeamService.getRootTeamId(spaceId);
        ContactMeta contactMeta =
            new ContactMeta(spaceId, tenantId, openId, socialRootTeamId,
                rootTeamId, mainAdminMemberId);

        // Initialize root department information
        OpenDeptToTeam rootDeptToTeam = OpenDeptToTeam.builder()
            .departmentId(socialRootTeamId)
            .teamId(rootTeamId)
            .isNew(false)
            .isCurrentSync(true)
            .op(SyncOperation.KEEP)
            .build();
        contactMeta.openDeptToTeamMap.put(socialRootTeamId, rootDeptToTeam);

        HashSet<Long> unitTeamIds =
            CollUtil.newHashSet(iTeamMemberRelService.getTeamByMemberId(mainAdminMemberId));
        // Initialize master administrator information
        OpenUserToMember mainAdminOpenUserToMember = OpenUserToMember.builder()
            .memberId(mainAdminMemberId)
            .memberName(options.getOperatorSocialMemberName())
            .openId(openId)
            .oldUnitTeamIds(unitTeamIds)
            .isNew(false)
            .isCurrentSync(true)
            .build();
        contactMeta.openUserToMemberMap.put(openId, mainAdminOpenUserToMember);

        // The map of the currently synchronized member open user -> system member
        List<MemberEntity> memberList =
            iMemberService.getMembersBySpaceId(spaceId, true);
        // The same open ID, only the latest member is reserved
        Map<String, OpenUserToMember> memberListByOpenIdToMap = memberList.stream()
            // Since the master management is initialized manually above,
            // filtering is required here
            .filter(dto -> !dto.getId().equals(mainAdminMemberId))
            .collect(Collectors.toMap(MemberEntity::getOpenId, dto -> {
                OpenUserToMember member = OpenUserToMember.builder()
                    .openId(dto.getOpenId())
                    .memberId(dto.getId())
                    .memberName(dto.getMemberName())
                    .isDeleted(dto.getIsDeleted())
                    .build();
                // Query Associated Organization Ids todo Batch query is required here
                HashSet<Long> teamUnitIds = CollUtil.newHashSet(
                    iTeamMemberRelService.getTeamByMemberId(member.getMemberId()));
                member.setOldUnitTeamIds(teamUnitIds);
                return member;
            }, (pre, cur) -> !cur.getIsDeleted() ? cur : pre));
        contactMeta.openUserToMemberMap.putAll(memberListByOpenIdToMap);

        // Social department ID and system department ID,
        // the initial value is the root department ID
        List<TenantDepartmentBindDTO> teamList =
            iSocialTenantDepartmentService.getTenantBindTeamListBySpaceId(spaceId);
        Map<String, OpenDeptToTeam> teamListByDepartmentIdToMap =
            teamList.stream()
                .collect(Collectors.toMap(TenantDepartmentBindDTO::getDepartmentId,
                    dto -> OpenDeptToTeam.builder()
                        .id(dto.getId())
                        .departmentId(dto.getDepartmentId())
                        .departmentName(dto.getDepartmentName())
                        .openDepartmentId(dto.getOpenDepartmentId())
                        .parentDepartmentId(dto.getParentDepartmentId())
                        .parentOpenDepartmentId(dto.getParentOpenDepartmentId())
                        .teamId(dto.getTeamId())
                        .parentTeamId(dto.getParentTeamId())
                        .internalSequence(dto.getInternalSequence())
                        .build()));
        contactMeta.openDeptToTeamMap.putAll(teamListByDepartmentIdToMap);

        // Synchronize contacts
        this.syncContacts(contactMeta, contactMap);

        // Limit on number of inspectors
        // long defaultMaxMemberCount = iSubscriptionService.getPlanSeats(spaceId);
        // ExceptionUtil.isTrue(contactMeta.openIds.size() <= defaultMaxMemberCount,
        //     SubscribeFunctionException.MEMBER_LIMIT);

        // If the synchronization member does not have a master administrator,
        // the master administrator needs to be attached to the root door
        if (!contactMeta.openIds.contains(contactMeta.openId)) {
            TeamMemberRelEntity teamMemberRel =
                OrganizationFactory.createTeamMemberRel(contactMeta.rootTeamId,
                    contactMeta.mainAdminMemberId);
            contactMeta.teamMemberRelEntities.add(teamMemberRel);
        }

        // Initialize address book structure
        contactMeta.doDeleteTeams();
        // Delete the missing member
        contactMeta.deleteMembers();
        // Delete member association
        contactMeta.doDeleteMemberRels();
        // Update master administrator information
        contactMeta.updateMainAdminMember(openId);
        // Store to DB
        contactMeta.doSaveOrUpdate();
        // Delete Cache
        userSpaceCacheService.delete(spaceId);
        return contactMeta.openIds;
    }

    private void syncContacts(ContactMeta contactMeta,
                              LinkedHashMap<String, SocialContactDTO> contactTree) {
        Set<String> deptIds = contactTree.keySet();
        contactTree.forEach((deptId, contact) -> {
            SocialDepartment department = contact.getDepartment();
            // The current visible range has no parent department
            if (!deptIds.contains(department.getParentDeptId())) {
                department.setParentDeptId(contactMeta.socialRootTeamId);
            }
            handleDept(contactMeta, department);
            if (contact.getUserMap() != null) {
                contact.getUserMap().values()
                    .forEach(user -> handleMember(contactMeta, user,
                        contactMeta.getTeamId(deptId)));
            }
        });
    }

    private void handleMember(ContactMeta contactMeta,
                              SocialContactDTO.SocialUser userDetail, Long parentTeamId) {
        // Filter inactive social users
        if (BooleanUtil.isFalse(userDetail.getActive())) {
            return;
        }
        String openId = userDetail.getOpenId();
        OpenUserToMember cahceMember = contactMeta.openUserToMemberMap.get(openId);
        // No synchronization
        if (!contactMeta.openIds.contains(openId)) {
            // The member in the database does not exist and has not been synchronized. Users can
            // be bound only when they need to log in
            if (null == cahceMember) {
                MemberEntity member = SocialFactory.createMember(userDetail)
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
                    .memberName(member.getMemberName()).openId(openId).isNew(true).build();
            } else {
                // ExistCheck whether key information needs to be modified
                if (!cahceMember.getMemberName().equals(userDetail.getName())
                    || cahceMember.getIsDeleted()
                    || !userDetail.getOpenId().equals(cahceMember.getOpenId())) {
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
        // Bind departments. If there is no corresponding department relationship in the cache,
        // directly link to the root department
        cahceMember.getNewUnitTeamIds().add(parentTeamId);
        if (CollUtil.isEmpty(cahceMember.getOldUnitTeamIds())
            || (CollUtil.isNotEmpty(cahceMember.getOldUnitTeamIds())
            && !cahceMember.getOldUnitTeamIds().contains(parentTeamId))) {
            // Member history does not exist under department,
            // add member and department association records
            contactMeta.teamMemberRelEntities.add(
                OrganizationFactory.createTeamMemberRel(parentTeamId, cahceMember.getMemberId()));
        }
        contactMeta.openIds.add(openId);
        // Add social user - system user
        contactMeta.openUserToMemberMap.put(openId, cahceMember);
    }

    private void handleDept(ContactMeta contactMeta, SocialDepartment socialDepartment) {
        // Do not process root department
        if (contactMeta.socialRootTeamId.equals(socialDepartment.getDeptId())) {
            return;
        }
        String parentDeptId = socialDepartment.getParentDeptId();
        String tenantId = contactMeta.tenantId;
        String spaceId = contactMeta.spaceId;
        List<String> subDepIds = contactMeta.openDeptIdMap.containsKey(parentDeptId)
            ? contactMeta.openDeptIdMap.get(parentDeptId) : CollUtil.newArrayList();
        subDepIds.add(socialDepartment.getDeptId());
        contactMeta.openDeptIdMap.put(parentDeptId, subDepIds);
        int sequence = subDepIds.size();

        OpenDeptToTeam openDeptToTeam =
            contactMeta.openDeptToTeamMap.get(socialDepartment.getDeptId());
        Long teamPid = contactMeta.getTeamId(parentDeptId);

        if (null == openDeptToTeam) {
            TeamEntity team = OrganizationFactory.createTeam(spaceId,
                IdWorker.getId(), teamPid, socialDepartment.getDeptName(), sequence);
            contactMeta.teamEntities.add(team);
            SocialTenantDepartmentEntity department =
                SocialFactory.createDepartment(spaceId, tenantId, socialDepartment);
            contactMeta.tenantDepartmentEntities.add(department);
            SocialTenantDepartmentBindEntity tenantDepartmentBind =
                SocialFactory.createTenantDepartmentBind(spaceId, team.getId(),
                    tenantId, socialDepartment.getDeptId());
            contactMeta.tenantDepartmentBindEntities.add(tenantDepartmentBind);
            // Synchronous relation
            openDeptToTeam = OpenDeptToTeam.builder()
                .departmentName(team.getTeamName())
                .departmentId(department.getDepartmentId())
                .openDepartmentId(department.getOpenDepartmentId())
                .parentDepartmentId(department.getParentId())
                .parentOpenDepartmentId(department.getParentOpenDepartmentId())
                .teamId(team.getId())
                .parentTeamId(team.getParentId())
                .internalSequence(team.getSequence())
                .isNew(true)
                .op(SyncOperation.ADD)
                .build();
        } else {
            boolean isUpdate = BooleanUtil.or(
                // Modify Department Level
                BooleanUtil.negate(openDeptToTeam.getParentOpenDepartmentId().equals(parentDeptId)),
                // Modify Name
                BooleanUtil.negate(openDeptToTeam.getDepartmentName()
                    .equals(socialDepartment.getDeptName())),
                // Modify Order
                openDeptToTeam.getInternalSequence() != sequence
            );
            if (isUpdate) {
                // Modify Department Structure
                SocialTenantDepartmentEntity updateTenantDepartment =
                    SocialTenantDepartmentEntity.builder()
                        .id(openDeptToTeam.getId())
                        .departmentName(socialDepartment.getDeptName())
                        .parentId(parentDeptId)
                        .parentOpenDepartmentId(parentDeptId)
                        .departmentOrder(sequence)
                        .build();
                contactMeta.updateTenantDepartmentEntities.add(updateTenantDepartment);

                TeamEntity updateTeam = TeamEntity.builder()
                    .id(openDeptToTeam.getTeamId())
                    .teamName(socialDepartment.getDeptName())
                    .parentId(teamPid)
                    .sequence(sequence)
                    .build();
                contactMeta.updateTeamEntities.add(updateTeam);

                openDeptToTeam.setDepartmentName(updateTenantDepartment.getDepartmentName())
                    .setParentDepartmentId(updateTenantDepartment.getParentId())
                    .setParentOpenDepartmentId(updateTenantDepartment.getParentOpenDepartmentId())
                    .setParentTeamId(updateTeam.getParentId())
                    .setInternalSequence(sequence);
                openDeptToTeam.setOp(SyncOperation.UPDATE);
            } else {
                // No modification
                openDeptToTeam.setOp(SyncOperation.KEEP);
            }
        }

        // Save parent department ID -> team ID
        openDeptToTeam.setIsCurrentSync(true);
        // Mark the department for this synchronization
        contactMeta.openDeptToTeamMap.put(socialDepartment.getDeptId(), openDeptToTeam);
    }

    class ContactMeta {

        String spaceId;

        String tenantId;

        String openId;

        String socialRootTeamId;

        Long rootTeamId;

        Long mainAdminMemberId;

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

        // Social User - system User
        Map<String, OpenUserToMember> openUserToMemberMap = MapUtil.newHashMap(true);

        // Social Department - system Department
        Map<String, OpenDeptToTeam> openDeptToTeamMap = MapUtil.newHashMap(true);

        // The Social user ID of this synchronization, which is used to send the start message
        Set<String> openIds = CollUtil.newHashSet();

        // Store the parent-child department relationship for calculating sequence
        HashMap<String, List<String>> openDeptIdMap = new HashMap<>();

        ContactMeta(String spaceId, String tenantId, String openId,
                    String socialRootTeamId, Long rootTeamId, Long mainAdminMemberId) {
            this.spaceId = spaceId;
            this.tenantId = tenantId;
            this.openId = openId;
            this.socialRootTeamId = socialRootTeamId;
            this.openDeptIdMap.put(socialRootTeamId, CollUtil.newArrayList());
            this.rootTeamId = rootTeamId;
            this.mainAdminMemberId = mainAdminMemberId;
        }

        // Get the cached Team id. No data. Default: root Team id
        Long getTeamId(String deptId) {
            return Optional.ofNullable(this.openDeptToTeamMap.get(deptId))
                .map(OpenDeptToTeam::getTeamId)
                .orElse(rootTeamId);
        }

        void doSaveOrUpdate() {
            iSocialTenantUserService.createBatch(tenantUserEntities);

            iSocialTenantDepartmentService.createBatch(tenantDepartmentEntities);
            iSocialTenantDepartmentService.updateBatchById(updateTenantDepartmentEntities);

            iSocialTenantDepartmentBindService.createBatch(tenantDepartmentBindEntities);

            iMemberService.batchCreate(spaceId, memberEntities);
            // Restore the previous organizational unit to prevent the members in the table from
            // being grayed out
            if (!recoverMemberIds.isEmpty()) {
                iUnitService.batchUpdateIsDeletedBySpaceIdAndRefId(spaceId, recoverMemberIds,
                    UnitType.MEMBER, false);
            }
            iMemberService.batchUpdateNameAndOpenIdAndIsDeletedByIds(updateMemberEntities);

            iTeamService.batchCreateTeam(spaceId, teamEntities);
            iTeamService.updateBatchById(updateTeamEntities);
            iTeamMemberRelService.createBatch(teamMemberRelEntities);
        }

        void doDeleteTeams() {
            // Calculate the groups to be deleted
            List<Long> oldTeamIds = iTeamService.getTeamIdsBySpaceId(spaceId);
            Map<Long, String> newTeams = this.openDeptToTeamMap.values().stream()
                .filter(OpenDeptToTeam::getIsCurrentSync)
                .collect(Collectors.toMap(OpenDeptToTeam::getTeamId,
                    OpenDeptToTeam::getDepartmentId));

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
                        OpenDeptToTeam::getDepartmentId));

                for (Long deleteTeamId : oldTeamIds) {
                    // Delete the Member under the system department.
                    // There are multiple departments for the personnel.
                    // It is necessary to judge whether the synchronized personnel are in the list.
                    // If they exist, they will not be deleted.
                    // Otherwise, they will be deleted
                    List<Long> memberIds = iTeamMemberRelService.getMemberIdsByTeamId(deleteTeamId);
                    memberIds.removeAll(currentSyncMemberUsers);

                    String deleteWeComTeamId = teamToWecomTeamMap.get(deleteTeamId);
                    if (StrUtil.isNotBlank(deleteWeComTeamId)) {
                        // Remove department - delete the third-party department,
                        // delete the binding relationship, and delete the system department
                        iSocialTenantDepartmentService.deleteSpaceTenantDepartment(spaceId,
                            tenantId, deleteWeComTeamId);
                    } else {
                        // It means that there is no binding,
                        // and system department is deleted directly
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
                .collect(Collectors.toMap(OpenUserToMember::getMemberId,
                    OpenUserToMember::getOpenId));

            Set<Long> newMemberIds = new HashSet<>(newMemberUsers.keySet());

            // Calculate intersection, users without changes
            newMemberIds.retainAll(oldMemberIds);
            if (!newMemberIds.isEmpty()) {
                // Users to be deleted when calculating difference sets
                oldMemberIds.removeAll(newMemberIds);
            }

            // The member that is not equal to or needs to be deleted is empty,
            // which means it is not the first synchronization
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
            this.openUserToMemberMap.values().forEach(member -> {
                Set<Long> oldUnitTeamIds = member.getOldUnitTeamIds();
                if (CollUtil.isEmpty(oldUnitTeamIds)) {
                    return;
                }
                oldUnitTeamIds.removeAll(member.getNewUnitTeamIds());
                if (CollUtil.isEmpty(oldUnitTeamIds)) {
                    return;
                }
                iTeamMemberRelService.removeByTeamIdsAndMemberId(member.getMemberId(),
                    new ArrayList<>(oldUnitTeamIds));
            });
        }

        void updateMainAdminMember(String openId) {
            OpenUserToMember adminMember = openUserToMemberMap.get(openId);
            // Update master administrator information
            MemberEntity member = MemberEntity.builder()
                .id(adminMember.getMemberId())
                .openId(adminMember.getOpenId())
                .memberName(adminMember.getMemberName())
                .build();
            iMemberService.updateById(member);
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    private static class OpenUserToMember {

        private Long memberId;

        private String openId;

        private String memberName;

        private Set<Long> oldUnitTeamIds;

        @Builder.Default
        private Set<Long> newUnitTeamIds = new HashSet<>();

        @Builder.Default
        private Boolean isNew = false;

        @Builder.Default
        private Boolean isCurrentSync = false;

        @Builder.Default
        private Boolean isDeleted = false;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    @Builder(toBuilder = true)
    private static class OpenDeptToTeam {

        private Long id;

        private String departmentName;

        private String departmentId;

        private String parentDepartmentId;

        private String openDepartmentId;

        private String parentOpenDepartmentId;

        private Long teamId;

        private Long parentTeamId;

        private Integer internalSequence;

        @Builder.Default
        private Boolean isNew = false;

        @Builder.Default
        private Boolean isCurrentSync = false;

        private SyncOperation op;

    }

    enum SyncOperation {
        ADD, UPDATE, KEEP
    }

}
