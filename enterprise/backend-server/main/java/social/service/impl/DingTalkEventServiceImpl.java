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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.apitable.core.exception.BusinessException;
import com.apitable.enterprise.social.entity.SocialTenantDepartmentBindEntity;
import com.apitable.enterprise.social.entity.SocialTenantDepartmentEntity;
import com.apitable.enterprise.social.entity.SocialTenantUserEntity;
import com.apitable.enterprise.social.enums.SocialException;
import com.apitable.enterprise.social.factory.SocialFactory;
import com.apitable.enterprise.social.model.DingTalkContactDTO;
import com.apitable.enterprise.social.model.DingTalkContactDTO.DingTalkUserDTO;
import com.apitable.enterprise.social.service.IDingTalkEventService;
import com.apitable.enterprise.social.service.IDingTalkService;
import com.apitable.enterprise.social.service.ISocialTenantBindService;
import com.apitable.enterprise.social.service.ISocialTenantDepartmentBindService;
import com.apitable.enterprise.social.service.ISocialTenantDepartmentService;
import com.apitable.enterprise.social.service.ISocialTenantUserService;
import com.apitable.interfaces.social.enums.SocialNameModified;
import com.apitable.organization.entity.MemberEntity;
import com.apitable.organization.entity.TeamEntity;
import com.apitable.organization.enums.UserSpaceStatus;
import com.apitable.organization.mapper.TeamMemberRelMapper;
import com.apitable.organization.service.IMemberService;
import com.apitable.organization.service.ITeamMemberRelService;
import com.apitable.organization.service.ITeamService;
import com.apitable.space.service.ISpaceService;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.vikadata.social.dingtalk.DingtalkConfig.AgentApp;
import com.vikadata.social.dingtalk.constants.DingTalkConst;
import com.vikadata.social.dingtalk.model.DingTalkAppVisibleScopeResponse;
import com.vikadata.social.dingtalk.model.DingTalkDepartmentDetailResponse.DingTalkDeptDetail;
import com.vikadata.social.dingtalk.model.DingTalkUserDetail;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of third-party platform tenant address book service interface
 */
@Service
@Slf4j
public class DingTalkEventServiceImpl implements IDingTalkEventService {

    @Resource
    private ISpaceService iSpaceService;

    @Resource
    private ITeamService iTeamService;

    @Resource
    private IMemberService iMemberService;

    @Resource
    private ITeamMemberRelService iTeamMemberRelService;

    @Resource
    private TeamMemberRelMapper teamMemberRelMapper;

    @Resource
    private ISocialTenantBindService iSocialTenantBindService;

    @Resource
    private ISocialTenantDepartmentService iSocialTenantDepartmentService;

    @Resource
    private ISocialTenantDepartmentBindService iSocialTenantDepartmentBindService;

    @Resource
    private ISocialTenantUserService iSocialTenantUserService;

    @Resource
    private IDingTalkService dingTalkService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleUserActiveOrg(String agentId, String tenantKey, String userOpenId) {
        // Activate after the employee clicks the enterprise
        AgentApp agentApp = dingTalkService.getAgentAppById(agentId);
        String spaceId =
            iSocialTenantBindService.getTenantBindSpaceId(tenantKey, agentApp.getCustomKey());
        // No space is bound and success is returned
        if (StrUtil.isBlank(spaceId)) {
            return;
        }
        // User Details
        DingTalkUserDetail userDetail;
        try {
            userDetail = dingTalkService.getUserDetailByUserId(agentId, userOpenId);
        } catch (Exception e) {
            log.warn("Incorrect DingTalk user information:{}:{}", tenantKey, userOpenId, e);
            // When it is not available, the user does not have access rights configured in the enterprise
            return;
        }
        if (userDetail == null) {
            // When it is not available, the user does not have access rights configured in the enterprise
            return;
        }
        // Employee not activated
        if (BooleanUtil.isFalse(userDetail.getActive())) {
            return;
        }
        // Tenant user records can only be created when the address book is synchronized. After the employee activates, there should be no space
        // Multi application enterprises, employees may have synchronized
        boolean isExist =
            iSocialTenantUserService.isTenantUserOpenIdExist(agentId, tenantKey, userOpenId);
        if (!isExist) {
            // Create Tenant User Records
            SocialTenantUserEntity tenantUser =
                SocialFactory.createTenantUser(agentId, tenantKey, userDetail.getUserid(),
                    userDetail.getUnionid());
            iSocialTenantUserService.save(tenantUser);
        }
        // Check whether the member exists. If it does not exist, create it. It may also be a new user. It is created when logging in
        Long memberId = iMemberService.getMemberIdBySpaceIdAndOpenId(spaceId, userOpenId);
        if (memberId == null) {
            MemberEntity member = new MemberEntity();
            member.setId(IdWorker.getId());
            member.setSpaceId(spaceId);
            member.setMemberName(userDetail.getName());
            // Member names in the space do not synchronize user names
            member.setNameModified(true);
            member.setIsSocialNameModified(SocialNameModified.NO_SOCIAL.getValue());
            member.setOpenId(userDetail.getUserid());
            member.setIsActive(false);
            member.setIsPoint(true);
            member.setMobile(userDetail.getMobile());
            member.setPosition(userDetail.getTitle());
            member.setEmail(userDetail.getEmail());
            member.setStatus(UserSpaceStatus.INACTIVE.getStatus());
            member.setIsAdmin(false);
            iMemberService.batchCreate(spaceId, Collections.singletonList(member));
            // Department
            handleDingTalkMemberDeptRel(spaceId, tenantKey, member.getId(),
                userDetail.getDeptIdList());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handUserLeaveOrg(String agentId, String tenantKey, String userOpenId) {
        // Get the space ID of the current department
        AgentApp agentApp = dingTalkService.getAgentAppById(agentId);
        String spaceId =
            iSocialTenantBindService.getTenantBindSpaceId(tenantKey, agentApp.getCustomKey());
        if (StrUtil.isBlank(spaceId)) {
            return;
        }
        Long memberId = iMemberService.getMemberIdBySpaceIdAndOpenId(spaceId, userOpenId);
        if (memberId == null) {
            return;
        }
        // Disable login using DingTalk authorization
        iSocialTenantUserService.deleteByTenantIdAndOpenId(agentId, tenantKey, userOpenId);
        // The main administrator of the space is not allowed to delete
        Long mainAdminMemberId = iSpaceService.getSpaceMainAdminUserId(spaceId);
        if (mainAdminMemberId.equals(memberId)) {
            return;
        }
        // Remove member's space
        iMemberService.batchDeleteMemberFromSpace(spaceId, Collections.singletonList(memberId),
            true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleUserModifyOrg(String agentId, String tenantKey, String userOpenId) {
        // Edit employee information (modify data, change department)
        // Get the space ID of the current department
        AgentApp agentApp = dingTalkService.getAgentAppById(agentId);
        String spaceId =
            iSocialTenantBindService.getTenantBindSpaceId(tenantKey, agentApp.getCustomKey());
        if (StrUtil.isBlank(spaceId)) {
            return;
        }
        // Query member information of the space
        Long memberId = iMemberService.getMemberIdBySpaceIdAndOpenId(spaceId, userOpenId);
        if (memberId == null) {
            return;
        }
        // Query user information
        Map<Long, DingTalkContactDTO> contactMap =
            dingTalkService.getContactTreeMapByOpenIds(agentId,
                Collections.singletonList(userOpenId), null);
        List<DingTalkUserDTO> userList =
            contactMap.values().stream().filter(i -> i.getUserMap() != null)
                .map(i -> i.getUserMap().get(userOpenId)).collect(Collectors.toList());
        if (userList.isEmpty()) {
            throw new BusinessException(SocialException.USER_NOT_EXIST);
        }
        // Update member information
        MemberEntity member = SocialFactory.createDingTalkMember(userList.get(0));
        member.setId(memberId);
        iMemberService.updateById(member);
        if (dingTalkService.syncContactWithTree()) {
            // Update department association
            handleDingTalkMemberDeptRel(spaceId, tenantKey, memberId,
                ListUtil.toList(contactMap.keySet()));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleOrgDeptCreate(String agentId, String tenantKey, Long openDepartmentId) {
        // Get the space ID of the current department
        AgentApp agentApp = dingTalkService.getAgentAppById(agentId);
        String spaceId =
            iSocialTenantBindService.getTenantBindSpaceId(tenantKey, agentApp.getCustomKey());
        if (StrUtil.isBlank(spaceId)) {
            return;
        }
        // Root organization ID of the space
        Long rootTeamId = iTeamService.getRootTeamId(spaceId);
        // Get DingTalk enterprise department details
        DingTalkDeptDetail departmentDetail =
            dingTalkService.getDeptDetail(agentId, openDepartmentId);
        // The newly added department does not need to be queried in the system, just know what the parent department of the department is in the enterprise tenant
        Long parentDeptId = departmentDetail.getParentId();
        DingTalkAppVisibleScopeResponse visibleScope = dingTalkService.getAppVisibleScopes(agentId);
        List<Long> parentIds = dingTalkService.getDeptParentIdList(agentId, openDepartmentId);
        // Not in visible range, no synchronization required
        if (!CollUtil.containsAny(visibleScope.getDeptVisibleScopes(), parentIds)) {
            return;
        }
        // Get the group ID of the parent department ID bound to the space
        Long parentTeamId =
            iSocialTenantDepartmentBindService.getBindSpaceTeamIdBySpaceId(spaceId, tenantKey,
                parentDeptId.toString());
        if (parentTeamId == null) {
            // The superior department does not exist, which proves to be a level 1 node
            parentTeamId = rootTeamId;
        }
        // Create space team
        Long toCreateTeamId =
            iTeamService.createSubTeam(spaceId, departmentDetail.getName(), parentTeamId);
        // New tenant department record
        SocialTenantDepartmentEntity tenantDepartment =
            SocialFactory.createDingTalkDepartment(spaceId, tenantKey,
                dingTalkService.formatDingTalkDepartmentDto(departmentDetail));
        iSocialTenantDepartmentService.createBatch(Collections.singletonList(tenantDepartment));
        // New Binding
        SocialTenantDepartmentBindEntity tenantDepartmentBind =
            SocialFactory.createTenantDepartmentBind(spaceId,
                toCreateTeamId, tenantKey, departmentDetail.getDeptId().toString());
        iSocialTenantDepartmentBindService.createBatch(
            Collections.singletonList(tenantDepartmentBind));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleOrgDeptModify(String agentId, String tenantKey, Long departmentId) {
        // Preconditions: Name Modification | Department Level Adjustment (the following employees will be brought in automatically when adjusting. Note that the department adjustment sorting is slightly complicated)
        // Get the space ID of the current department
        AgentApp agentApp = dingTalkService.getAgentAppById(agentId);
        String spaceId =
            iSocialTenantBindService.getTenantBindSpaceId(tenantKey, agentApp.getCustomKey());
        if (StrUtil.isBlank(spaceId)) {
            return;
        }
        // Bound department ID
        Long teamId =
            iSocialTenantDepartmentBindService.getBindSpaceTeamIdBySpaceId(spaceId, tenantKey,
                departmentId.toString());
        if (teamId == null) {
            return;
        }
        // Get enterprise department details
        DingTalkDeptDetail departmentDetail = dingTalkService.getDeptDetail(agentId, departmentId);
        SocialTenantDepartmentEntity tenantDepartment =
            iSocialTenantDepartmentService.getByTenantIdAndDepartmentId(spaceId, tenantKey,
                departmentId.toString());
        // Department level adjustment
        if (!tenantDepartment.getParentId().equals(departmentDetail.getParentId().toString())) {
            Long dingTalkRootDeptId = DingTalkConst.ROOT_DEPARTMENT_ID;
            // Modified parent department
            Long newParentDeptId = departmentDetail.getParentId();
            Long parentTeamId;
            Integer sequence = null;
            if (!tenantDepartment.getParentId().equals(dingTalkRootDeptId.toString()) &&
                newParentDeptId.equals(dingTalkRootDeptId)) {
                // Sub department -> first level department
                parentTeamId = iTeamService.getRootTeamId(spaceId);
            } else {
                // Sub department -> other sub departments
                parentTeamId =
                    iSocialTenantDepartmentBindService.getBindSpaceTeamIdBySpaceId(spaceId,
                        tenantKey, newParentDeptId.toString());
            }
            // Parent department is not synchronized
            if (ObjectUtil.isEmpty(parentTeamId)) {
                Long rootTeamId = iTeamService.getRootTeamId(spaceId);
                parentTeamId =
                    handleDingTalkDeptParentCreate(spaceId, tenantKey, agentId, departmentId,
                        rootTeamId);
                sequence = 1;
            }
            TeamEntity team = new TeamEntity();
            team.setId(teamId);
            team.setTeamName(departmentDetail.getName());
            team.setParentId(parentTeamId);
            team.setSequence(
                sequence != null ? sequence : iTeamService.getMaxSequenceByParentId(parentTeamId));
            iTeamService.updateById(team);
        } else {
            TeamEntity team = new TeamEntity();
            team.setId(teamId);
            team.setTeamName(departmentDetail.getName());
            iTeamService.updateById(team);
        }
        // Override the value of DingTalk department
        SocialTenantDepartmentEntity updateTenantDepartment = new SocialTenantDepartmentEntity();
        updateTenantDepartment.setId(tenantDepartment.getId());
        updateTenantDepartment.setDepartmentName(departmentDetail.getName());
        updateTenantDepartment.setParentId(departmentDetail.getParentId().toString());
        updateTenantDepartment.setParentOpenDepartmentId(departmentDetail.getParentId().toString());
        iSocialTenantDepartmentService.updateById(updateTenantDepartment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleOrgDeptRemove(String agentId, String tenantKey, Long departmentId) {
        // Get the space ID of the current department
        AgentApp agentApp = dingTalkService.getAgentAppById(agentId);
        String spaceId =
            iSocialTenantBindService.getTenantBindSpaceId(tenantKey, agentApp.getCustomKey());
        if (StrUtil.isBlank(spaceId)) {
            return;
        }
        // Ignore the members under the department. The precondition for deleting a department is that the members under the department must be deleted
        // There must be no sub department under the deleted department, so we don't need to pay attention to the change of the sub department
        // Delete Tenant Department Record
        iSocialTenantDepartmentService.deleteSpaceTenantDepartment(spaceId, tenantKey,
            departmentId.toString());
    }

    private void handleDingTalkMemberDeptRel(String spaceId, String tenantKey, Long memberId,
                                             List<Long> userDepartmentIds) {
        // Employee's old enterprise department
        List<Long> beforeTeamIds = teamMemberRelMapper.selectTeamIdsByMemberId(memberId);
        // Employee's new enterprise department
        List<String> userDepartmentIdsByStr =
            userDepartmentIds.stream().map(Convert::toStr).collect(Collectors.toList());
        List<Long> afterTeamIds =
            iSocialTenantDepartmentBindService.getBindSpaceTeamIdsByTenantId(spaceId, tenantKey,
                userDepartmentIdsByStr);
        if (userDepartmentIds.contains(DingTalkConst.ROOT_DEPARTMENT_ID) ||
            afterTeamIds.isEmpty()) {
            // Because the root department is not synchronized with the third-party synchronization, if the person belongs to the root department, add a record manually
            // When the person's department changes to a non visible area, a root department record is also manually inserted
            afterTeamIds.add(iTeamService.getRootTeamId(spaceId));
        }
        // First deal with the department that needs to delete the associated relationship
        List<Long> removeTeamIds = CollUtil.subtractToList(beforeTeamIds, afterTeamIds);
        if (CollUtil.isNotEmpty(removeTeamIds)) {
            // Delete Department Association
            teamMemberRelMapper.deleteByTeamIdsAndMemberId(memberId,
                CollUtil.distinct(removeTeamIds));
        }
        // When dealing with the department that needs to add an association relationship
        List<Long> addTeamIds = CollUtil.subtractToList(afterTeamIds, beforeTeamIds);
        if (CollUtil.isNotEmpty(addTeamIds)) {
            // Add department association
            iTeamMemberRelService.addMemberTeams(Collections.singletonList(memberId),
                CollUtil.distinct(addTeamIds));
        }
    }

    private Long handleDingTalkDeptParentCreate(String spaceId, String tenantKey, String agentId,
                                                Long deptId, Long
                                                    rootTeamId) {
        Long teamId = rootTeamId;
        List<Long> parentIds = dingTalkService.getDeptParentIdList(agentId, deptId);
        // Remove Yourself
        parentIds.remove(deptId);
        parentIds.remove(DingTalkConst.ROOT_DEPARTMENT_ID);
        Map<Long, Long> openDeptIdTeamIdMap = new HashMap<>();
        openDeptIdTeamIdMap.put(DingTalkConst.ROOT_DEPARTMENT_ID, rootTeamId);
        List<SocialTenantDepartmentEntity> createTenantDept = CollUtil.newArrayList();
        List<SocialTenantDepartmentBindEntity> createTenantDeptBind = CollUtil.newArrayList();
        // Create a new parent department in reverse order
        for (int i = parentIds.size() - 1; i >= 0; i--) {
            Long parentDeptId = parentIds.get(i);
            DingTalkDeptDetail parentDeptDetail =
                dingTalkService.getDeptDetail(agentId, parentDeptId);
            Long parentTeamId =
                iSocialTenantDepartmentBindService.getBindSpaceTeamIdBySpaceId(spaceId, tenantKey,
                    parentDeptId.toString());
            // New required
            if (ObjectUtil.isEmpty(parentTeamId)) {
                Long toCreateTeamId =
                    iTeamService.createSubTeam(spaceId, parentDeptDetail.getName(),
                        openDeptIdTeamIdMap.get(parentDeptDetail.getParentId()));
                openDeptIdTeamIdMap.put(parentDeptDetail.getDeptId(), toCreateTeamId);
                // New tenant department record
                createTenantDept.add(SocialFactory.createDingTalkDepartment(spaceId, tenantKey,
                    dingTalkService.formatDingTalkDepartmentDto(parentDeptDetail)));
                // New Binding
                createTenantDeptBind.add(
                    SocialFactory.createTenantDepartmentBind(spaceId, toCreateTeamId, tenantKey,
                        parentDeptDetail.getDeptId().toString()));
                parentTeamId = toCreateTeamId;
            } else {
                openDeptIdTeamIdMap.put(parentDeptId, parentTeamId);
            }
            teamId = parentTeamId;
        }
        if (!createTenantDept.isEmpty()) {
            iSocialTenantDepartmentService.createBatch(createTenantDept);
        }
        if (!createTenantDeptBind.isEmpty()) {
            iSocialTenantDepartmentBindService.createBatch(createTenantDeptBind);
        }
        return teamId;
    }
}
