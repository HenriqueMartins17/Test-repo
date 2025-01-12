/*
 * APITable <https://github.com/apitable/apitable>
 * Copyright (C) 2022 APITable Ltd. <https://apitable.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.apitable.enterprise.elink.service;

import java.util.Collections;
import java.util.List;

import jakarta.annotation.Resource;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.enterprise.elink.model.ElinkUnitDTO;
import com.apitable.enterprise.elink.model.ElinkUserDTO;
import com.apitable.enterprise.social.entity.SocialTenantDepartmentBindEntity;
import com.apitable.enterprise.social.entity.SocialTenantDepartmentEntity;
import com.apitable.enterprise.social.service.ISocialTenantDepartmentBindService;
import com.apitable.enterprise.social.service.ISocialTenantDepartmentService;
import com.apitable.enterprise.social.service.ISocialUserBindService;
import com.apitable.enterprise.vcode.service.IVCodeService;
import com.apitable.organization.entity.MemberEntity;
import com.apitable.organization.entity.TeamEntity;
import com.apitable.organization.service.IMemberService;
import com.apitable.organization.service.ITeamMemberRelService;
import com.apitable.organization.service.ITeamService;
import com.apitable.player.service.IPlayerActivityService;
import com.apitable.user.entity.UserEntity;
import com.apitable.user.service.IUserService;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static com.apitable.enterprise.social.enums.SocialException.USER_NOT_EXIST;
import static com.apitable.organization.enums.OrganizationException.GET_TEAM_ERROR;
import static com.apitable.organization.enums.OrganizationException.TEAM_HAS_SUB;
import static com.apitable.user.enums.UserException.REGISTER_FAIL;

/**
 * <p>
 * Elink connector template
 * </p>
 */
@Slf4j
@Component
public class ElinkService {

    @Resource
    private IUserService iUserService;

    @Resource
    private ISocialUserBindService iSocialUserBindService;

    @Resource
    private ISocialTenantDepartmentService iSocialTenantDepartmentService;

    @Resource
    private ISocialTenantDepartmentBindService iSocialTenantDepartmentBindService;

    @Resource
    private IMemberService iMemberService;

    @Resource
    private ITeamService iTeamService;

    @Resource
    private ITeamMemberRelService iTeamMemberRelService;

    @Resource
    private IPlayerActivityService iPlayerActivityService;

    @Resource
    private IVCodeService ivCodeService;

    private final String TENANT_KEY = "yzj";

    @Transactional
    public void saveOrUpdateUser(String spaceId, ElinkUserDTO yzyUserDTO) {
        String tenantId = String.format("%s-%s", TENANT_KEY, spaceId);
        //Find out if the account is bound by a third party
        Long userId = iSocialUserBindService.getUserIdByUnionId(yzyUserDTO.getOpenId());
        MemberEntity memberEntity = null;
        UserEntity userEntity = null;
        Long memberId = null;
        List<Long> teamIds =
            iSocialTenantDepartmentBindService.getBindSpaceTeamIdsByTenantId(spaceId, tenantId,
                yzyUserDTO.getUnitIds());
        log.info("saveOrUpdateUser spaceId:{},YZYUserDTO:{},teamIds:{}", spaceId, yzyUserDTO,
            teamIds);
        //Has bound a third party
        if (userId != null) {
            //Update the original account information
            userEntity = iUserService.getById(userId);
            userEntity.setNickName(yzyUserDTO.getUserName());
            userEntity.setEmail(yzyUserDTO.getEmail());
            userEntity.setMobilePhone(yzyUserDTO.getMobile());
            userEntity.setRemark(yzyUserDTO.getOpenId());
            iUserService.saveOrUpdate(userEntity);
            log.info(
                "saveOrUpdateUser openId:{} Already bound the account, associated space station members",
                yzyUserDTO.getOpenId());
            //Processing members
            memberId = iMemberService.getMemberIdByUserIdAndSpaceId(userId, spaceId);
            if (memberId != null) {
                memberEntity = iMemberService.getById(memberId);
                memberEntity.setMobile(yzyUserDTO.getMobile());
                memberEntity.setMemberName(yzyUserDTO.getUserName());
                memberEntity.setEmail(yzyUserDTO.getEmail());
                memberEntity.setJobNumber(yzyUserDTO.getJobNumber());
                memberEntity.setOpenId(yzyUserDTO.getOpenId());
                iMemberService.saveOrUpdate(memberEntity);
                if (teamIds.size() != 0) {
                    iTeamMemberRelService.removeByMemberId(memberId);
                    iMemberService.updateMemberByTeamId(spaceId,
                        Collections.singletonList(memberId), teamIds);
                }
            } else {
                //Departments associated to members
                memberId = iMemberService.createMember(userId, spaceId, null);
                if (teamIds.size() > 0) {
                    iTeamMemberRelService.removeByMemberId(memberId);
                    iMemberService.updateMemberByTeamId(spaceId,
                        Collections.singletonList(memberId), teamIds);
                }
            }

        } else {
            log.info(
                "saveOrUpdateUser openId:{} No account has been bound, create or associate account and member",
                yzyUserDTO.getOpenId());
            //Check mobile has bind user.
            if (StrUtil.isNotEmpty(yzyUserDTO.getMobile())) {
                userId = iUserService.getUserIdByMobile(yzyUserDTO.getMobile());
            }
            //Check email has bind user.
            if (userId == null && StrUtil.isNotEmpty(yzyUserDTO.getEmail())) {
                userEntity = iUserService.getByEmail(yzyUserDTO.getEmail());
            }
            if (userEntity != null) {
                userId = userEntity.getId();
            }
            //Create user
            if (userId == null) {
                userEntity = UserEntity.builder()
                    .uuid(IdUtil.fastSimpleUUID())
                    .email(yzyUserDTO.getEmail())
                    .nickName(yzyUserDTO.getUserName())
                    .remark(yzyUserDTO.getOpenId())
                    .build();
                if (StrUtil.isNotEmpty(yzyUserDTO.getMobile())) {
                    userEntity.setMobilePhone(yzyUserDTO.getMobile());
                }
                boolean flag = iUserService.save(userEntity);
                ExceptionUtil.isTrue(flag, REGISTER_FAIL);
                //Create user activity records
                iPlayerActivityService.createUserActivityRecord(userEntity.getId());
                //Create a personal invitation code
                ivCodeService.createPersonalInviteCode(userEntity.getId());
                memberId = iMemberService.createMember(userEntity.getId(), spaceId, null);
                if (teamIds.size() > 0) {
                    iTeamMemberRelService.removeByMemberId(memberId);
                    iMemberService.updateMemberByTeamId(spaceId,
                        Collections.singletonList(memberId), teamIds);
                }
                iSocialUserBindService.create(userEntity.getId(), yzyUserDTO.getOpenId());
            } else {
                //Already have a vika_user account, which is different in this space station
                memberId = iMemberService.getMemberIdByUserIdAndSpaceId(userId, spaceId);
                if (memberId != null) {
                    //This space station exists, just update it
                    memberEntity = MemberEntity.builder()
                        .id(memberId)
                        .mobile(yzyUserDTO.getMobile())
                        .email(yzyUserDTO.getEmail())
                        .memberName(yzyUserDTO.getUserName())
                        .openId(yzyUserDTO.getOpenId())
                        .build();
                    iMemberService.updateById(memberEntity);
                    if (teamIds.size() > 0) {
                        iTeamMemberRelService.removeByMemberId(memberId);
                        iMemberService.updateMemberByTeamId(spaceId,
                            Collections.singletonList(memberId), teamIds);
                    }
                } else {
                    memberId = iMemberService.createMember(userId, spaceId, null);
                    if (teamIds.size() > 0) {
                        iTeamMemberRelService.removeByMemberId(memberId);
                        iMemberService.updateMemberByTeamId(spaceId,
                            Collections.singletonList(memberId), teamIds);
                    }
                }
                iSocialUserBindService.create(userId, yzyUserDTO.getOpenId());
            }
        }

    }

    @Transactional
    public void deleteUser(String spaceId, String openId) {
        log.info("delete linked account :spaceId:{},openId:{}", spaceId, openId);
        Long userId = iSocialUserBindService.getUserIdByUnionId(openId);
        ExceptionUtil.isNotNull(userId, USER_NOT_EXIST);
        Long memberId = iMemberService.getMemberIdByUserIdAndSpaceId(userId, spaceId);
        iMemberService.batchDeleteMemberFromSpace(spaceId, Collections.singletonList(memberId),
            false);

    }

    @Transactional
    public void saveOrUpdateUnit(String spaceId, ElinkUnitDTO elinkUnitDTO) {
        String tenantId = String.format("%s-%s", TENANT_KEY, spaceId);
        SocialTenantDepartmentEntity tenantDepartment =
            iSocialTenantDepartmentService.getByTenantIdAndDepartmentId(spaceId, tenantId,
                elinkUnitDTO.getUnitId());
        if (tenantDepartment != null) {
            tenantDepartment.setDepartmentName(elinkUnitDTO.getName());
            //update department
            log.info(
                "Update external departments: spaceId[{}],unitName:[{}],UnitId:[{}],openParentUnitId:[{}}]",
                spaceId, elinkUnitDTO.getName(), elinkUnitDTO.getUnitId(),
                elinkUnitDTO.getParentUnitId());
            Long parentTeamId =
                iSocialTenantDepartmentBindService.getBindSpaceTeamIdBySpaceId(spaceId, tenantId,
                    elinkUnitDTO.getParentUnitId());
            if (parentTeamId == null) {
                parentTeamId = iTeamService.getRootTeamId(spaceId);
            }
            //update Vika team ID
            Long teamId =
                iSocialTenantDepartmentBindService.getBindSpaceTeamIdBySpaceId(spaceId, tenantId,
                    tenantDepartment.getDepartmentId());
            //Whether the original associated team is deleted
            TeamEntity teamEntity = iTeamService.getById(teamId);
            if (teamEntity == null) {
                iSocialTenantDepartmentBindService.deleteSpaceBindTenantDepartment(spaceId,
                    tenantId, tenantDepartment.getDepartmentId());
                teamId = iTeamService.createSubTeam(spaceId, elinkUnitDTO.getName(), parentTeamId);
                SocialTenantDepartmentBindEntity tenantDepartmentBind =
                    new SocialTenantDepartmentBindEntity().toBuilder()
                        .id(IdWorker.getId())
                        .spaceId(spaceId)
                        .tenantId(tenantId)
                        .teamId(teamId)
                        .tenantDepartmentId(tenantDepartment.getDepartmentId())
                        .build();
                iSocialTenantDepartmentBindService.createBatch(
                    Collections.singletonList(tenantDepartmentBind));
            } else {
                iTeamService.updateTeamParent(teamId, tenantDepartment.getDepartmentName(),
                    parentTeamId);
            }
            //Get new openParentOrgId department <-> vika group new parentId
            if (!StrUtil.isBlank(elinkUnitDTO.getParentUnitId())) {
                tenantDepartment.setParentId(elinkUnitDTO.getParentUnitId());
            }
            //Update social_dep
            iSocialTenantDepartmentService.saveOrUpdate(tenantDepartment);
            return;
        }
        log.info(
            "Create external departments: spaceId[{}],unitName:[{}],UnitId:[{}],openParentUnitId:[{}}]",
            spaceId, elinkUnitDTO.getName(), elinkUnitDTO.getUnitId(),
            elinkUnitDTO.getParentUnitId());
        // Create department
        tenantDepartment = new SocialTenantDepartmentEntity()
            .setId(IdWorker.getId())
            .setTenantId(tenantId)
            .setSpaceId(spaceId)
            .setDepartmentId(elinkUnitDTO.getUnitId())
            .setOpenDepartmentId(elinkUnitDTO.getUnitId())
            .setParentOpenDepartmentId(elinkUnitDTO.getParentUnitId())
            .setParentId(elinkUnitDTO.getParentUnitId())
            .setDepartmentName(elinkUnitDTO.getName());
        iSocialTenantDepartmentService.createBatch(Collections.singletonList(tenantDepartment));
        //Whether the third-party parent department already exists
        Long parenTteamId =
            iSocialTenantDepartmentBindService.getBindSpaceTeamIdBySpaceId(spaceId, tenantId,
                elinkUnitDTO.getParentUnitId());
        //If the parent department does not already exist, it is currently the root department of the third party
        if (parenTteamId == null) {
            parenTteamId = iTeamService.getRootTeamId(spaceId);
        }
        //Create vika team id
        Long teamId = iTeamService.createSubTeam(spaceId, elinkUnitDTO.getName(), parenTteamId);
        //Association and internal group binding relationship
        SocialTenantDepartmentBindEntity tenantDepartmentBind =
            new SocialTenantDepartmentBindEntity();
        tenantDepartmentBind.setId(IdWorker.getId());
        tenantDepartmentBind.setSpaceId(spaceId);
        tenantDepartmentBind.setTeamId(teamId);
        tenantDepartmentBind.setTenantId(tenantId);
        tenantDepartmentBind.setTenantDepartmentId(elinkUnitDTO.getUnitId());
        iSocialTenantDepartmentBindService.createBatch(
            Collections.singletonList(tenantDepartmentBind));
        //todo Create a first-level and third-level department first, and this time when inserting a second-level department, the association will be repaired
        //Returns the ID of the social dep associated with the external department
        iSocialTenantDepartmentService.getIdByDepartmentId(spaceId, tenantId,
            elinkUnitDTO.getUnitId());
    }

    @Transactional
    public void deleteUnit(String spaceId, String unitId) {
        String tenantId = String.format("%s-%s", TENANT_KEY, spaceId);
        Long teamId =
            iSocialTenantDepartmentBindService.getBindSpaceTeamIdBySpaceId(spaceId, tenantId,
                unitId);
        ExceptionUtil.isNotNull(teamId, GET_TEAM_ERROR);
        boolean hasSubUnit = iTeamService.checkHasSubUnitByTeamId(spaceId, teamId);
        ExceptionUtil.isFalse(hasSubUnit, TEAM_HAS_SUB);
        //Delete department
        iSocialTenantDepartmentService.deleteTenantDepartment(spaceId, tenantId, unitId);
    }
}
