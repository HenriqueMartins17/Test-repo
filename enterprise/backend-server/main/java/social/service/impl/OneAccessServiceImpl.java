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


import static com.apitable.enterprise.social.enums.SocialException.GET_USER_INFO_ERROR;
import static com.apitable.enterprise.social.enums.SocialException.ONLY_TENANT_ADMIN_BOUND_ERROR;
import static com.apitable.organization.enums.OrganizationException.CREATE_TEAM_ERROR;
import static com.apitable.organization.enums.OrganizationException.DELETE_SPACE_ADMIN_ERROR;
import static com.apitable.organization.enums.OrganizationException.GET_TEAM_ERROR;
import static com.apitable.organization.enums.OrganizationException.TEAM_HAS_SUB;
import static com.apitable.organization.enums.OrganizationException.UPDATE_TEAM_ERROR;
import static com.apitable.user.enums.DeveloperException.GENERATE_API_KEY_ERROR;
import static com.apitable.user.enums.UserException.REGISTER_FAIL;
import static com.apitable.user.enums.UserException.UPDATE_USER_INFO_FAIL;
import static com.apitable.user.enums.UserException.USER_NOT_EXIST;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONUtil;
import cn.vika.core.utils.StringUtil;
import com.apitable.core.exception.BusinessException;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.enterprise.social.entity.SocialTenantDepartmentBindEntity;
import com.apitable.enterprise.social.entity.SocialTenantDepartmentEntity;
import com.apitable.enterprise.social.model.OneAccessCopyInfoRo.MemberRo;
import com.apitable.enterprise.social.model.OneAccessOrgVo;
import com.apitable.enterprise.social.model.OneAccessTokenResponse;
import com.apitable.enterprise.social.model.OneAccessUserInfoResponse;
import com.apitable.enterprise.social.model.OneAccessUserQueryVo;
import com.apitable.enterprise.social.properties.OneAccessProperties;
import com.apitable.enterprise.social.service.IOneAccessService;
import com.apitable.enterprise.social.service.ISocialTenantDepartmentBindService;
import com.apitable.enterprise.social.service.ISocialTenantDepartmentService;
import com.apitable.enterprise.social.service.ISocialUserBindService;
import com.apitable.enterprise.social.util.AESCipher;
import com.apitable.enterprise.vcode.service.IVCodeService;
import com.apitable.organization.entity.MemberEntity;
import com.apitable.organization.entity.TeamEntity;
import com.apitable.organization.service.IMemberService;
import com.apitable.organization.service.ITeamMemberRelService;
import com.apitable.organization.service.ITeamService;
import com.apitable.player.service.IPlayerActivityService;
import com.apitable.shared.cache.bean.UserSpaceDto;
import com.apitable.shared.cache.service.UserSpaceCacheService;
import com.apitable.shared.util.ApiHelper;
import com.apitable.user.entity.DeveloperEntity;
import com.apitable.user.entity.UserEntity;
import com.apitable.user.mapper.DeveloperMapper;
import com.apitable.user.service.IUserService;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
public class OneAccessServiceImpl implements IOneAccessService {

    @Resource
    private OneAccessProperties oneAccessProperties;

    @Resource
    private IUserService userService;

    @Resource
    private DeveloperMapper developerMapper;

    @Resource
    private IMemberService iMemberService;

    @Resource
    private UserSpaceCacheService userSpaceCacheService;

    @Resource
    private IUserService iUserService;

    @Resource
    private ISocialUserBindService iSocialUserBindService;

    @Resource
    private IPlayerActivityService iPlayerActivityService;

    @Resource
    private IVCodeService ivCodeService;

    @Resource
    private ITeamMemberRelService iTeamMemberRelService;

    @Resource
    private ISocialTenantDepartmentService iSocialTenantDepartmentService;

    @Resource
    private ISocialTenantDepartmentBindService iSocialTenantDepartmentBindService;

    @Resource
    private ITeamService iTeamService;


    /**
     * return request body content
     *
     * @param request HttpServletRequest
     * @return requestBody to string
     */
    public static String getRequestBody(HttpServletRequest request) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String str;
        try {
            br = request.getReader();
            while ((str = br.readLine()) != null) {
                sb.append(str);
            }
            br.close();
        } catch (IOException e) {
            log.error("get request body error", e);
            if (br != null) {
                try {
                    br.close();
                } catch (IOException eo) {
                    log.error("io exception error", eo);
                }
            }
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    log.error("close BufferedReader error", e);
                }
            }
        }
        return sb.toString();
    }

    @Override
    public String getSpaceIdByBimAccount(String bimRemoteUser, String bimRemotePwd) {
        // Only space administrators can operate with permissions
        Long userId = developerMapper.selectUserIdByApiKey(bimRemotePwd);
        UserSpaceDto userSpaceDto = userSpaceCacheService.getUserSpace(userId, bimRemoteUser);
        ExceptionUtil.isTrue(userSpaceDto.isMainAdmin(), ONLY_TENANT_ADMIN_BOUND_ERROR);
        return userSpaceDto.getSpaceId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUser(String spaceId, String mobile, String loginName, String nickName,
                           String email, String openOrgId, String oneId) {
        log.debug("Create user with login name, mobile number and email");
        // Convert loginName to uuid
        String uuid = SecureUtil.md5(loginName);
        Wrapper<UserEntity> queryWrapper = new QueryWrapper<UserEntity>()
            .eq("uuid", uuid);
        UserEntity userEntity = userService.getOne(queryWrapper);
        // Associate an existing phone
        if (userEntity == null && !StrUtil.isEmpty(mobile)) {
            userEntity = userService.getByCodeAndMobilePhone("", mobile);
        }

        if (userEntity == null) {
            // create user userEntity
            userEntity = UserEntity.builder()
                .uuid(uuid)
                .nickName(nickName)
                .mobilePhone(mobile)
                .email(email)
                .color(RandomUtil.randomInt(0, 10))
                .remark(loginName)
                .lastLoginTime(LocalDateTime.now())
                .build();
            boolean flag = userService.save(userEntity);
            ExceptionUtil.isTrue(flag, REGISTER_FAIL);
            // Create user activity records
            iPlayerActivityService.createUserActivityRecord(userEntity.getId());
            // Create a personal invitation code
            ivCodeService.createPersonalInviteCode(userEntity.getId());

        } else {
            userEntity.setEmail(email).setNickName(nickName);
            userService.updateById(userEntity);
        }
        // record OneId
        if (!StrUtil.isEmpty(oneId)) {
            iSocialUserBindService.deleteBatchByUnionId(Collections.singletonList(oneId));
            iSocialUserBindService.create(userEntity.getId(), oneId);
        }
        Long memberId = iMemberService.getMemberIdByUserIdAndSpaceId(userEntity.getId(), spaceId);
        // Add when this member does not exist on the space station, and associate the department
        if (memberId == null) {
            Long teamId = getTeamIdByTenantDepartment(openOrgId);
            // Create member record
            iMemberService.createMember(userEntity.getId(), spaceId, teamId);
        }
        //Create Api Token
        DeveloperEntity developer = developerMapper.selectByUserId(userEntity.getId());
        if (developer == null) {
            log.info("user [{}] loginName [{}] create api key", userEntity.getId(), loginName);
            String apiKey = ApiHelper.createKey();
            developer = new DeveloperEntity();
            developer.setUserId(userEntity.getId());
            developer.setApiKey(apiKey);
            developer.setCreatedBy(userEntity.getId());
            developer.setUpdatedBy(userEntity.getId());
            boolean flag = SqlHelper.retBool(developerMapper.insert(developer));
            ExceptionUtil.isTrue(flag, GENERATE_API_KEY_ERROR);
        }

        return userEntity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(String spaceId, Long userId, String mobile, String loginName,
                              String nickName, String email, String openOrgId) {
        Long memberId = iMemberService.getMemberIdByUserIdAndSpaceId(userId, spaceId);
        if (memberId == null) {
            return false;
        }
        // Update member information
        MemberEntity memberEntity = MemberEntity.builder()
            .id(memberId)
            .build();
        UserEntity userEntity = UserEntity.builder()
            .id(userId)
            .build();
        memberEntity.setUpdatedAt(LocalDateTime.now());
        userEntity.setUpdatedAt(LocalDateTime.now());
        // set update properties
        if (!StrUtil.isBlank(mobile)) {
            memberEntity.setMobile(mobile);
            userEntity.setMobilePhone(mobile);
        }
        if (!StrUtil.isBlank(email)) {
            memberEntity.setEmail(email);
            userEntity.setEmail(email);
        }
        if (!StrUtil.isBlank(nickName)) {
            memberEntity.setMemberName(nickName);
            userEntity.setNickName(nickName);
        }
        boolean flag = iMemberService.updateById(memberEntity);
        ExceptionUtil.isTrue(flag, UPDATE_USER_INFO_FAIL);
        // Update user department information
        Long teamId = getTeamIdByTenantDepartment(openOrgId);
        if (teamId != null) {
            iTeamMemberRelService.removeByMemberId(memberId);
            iMemberService.updateMemberByTeamId(spaceId, Collections.singletonList(memberId),
                Collections.singletonList(teamId));
        }
        // Update user information
        return userService.updateById(userEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(String spaceId, Long userId) {
        // delete from space
        UserSpaceDto userSpaceDto = userSpaceCacheService.getUserSpace(userId, spaceId);
        // Check that the admin cannot be removed
        ExceptionUtil.isFalse(userSpaceDto.isMainAdmin(), DELETE_SPACE_ADMIN_ERROR);
        // Unlink third-party accounts
        List<String> unionIds = iSocialUserBindService.getUnionIdsByUserId(userId);
        if (unionIds != null) {
            iSocialUserBindService.deleteBatchByUnionId(unionIds);
        }
        // remove members of the space station
        iMemberService.batchDeleteMemberFromSpace(spaceId,
            Collections.singletonList(userSpaceDto.getMemberId()), true);
    }

    @Override
    public OneAccessUserQueryVo getUserById(String spaceId, Long userId, String oneId) {

        OneAccessUserQueryVo oneAccessUserVo = new OneAccessUserQueryVo("");
        //Get user info.
        if (!StringUtil.isEmpty(oneId)) {
            userId = iSocialUserBindService.getUserIdByUnionId(oneId);
        }
        MemberEntity memberEntity = iMemberService.getByUserIdAndSpaceId(userId, spaceId);
        ExceptionUtil.isNotNull(memberEntity, USER_NOT_EXIST);
        UserEntity userEntity = iUserService.getById(userId);
        //Get org info from socialTenantDepartment.
        String orgId = "";
        String tenantId = String.format("one-%s", spaceId);
        List<Long> teamIds = iTeamMemberRelService.getTeamByMemberId(memberEntity.getId());
        if (!teamIds.isEmpty()) {
            Wrapper<SocialTenantDepartmentBindEntity> queryWrapper =
                new QueryWrapper<SocialTenantDepartmentBindEntity>()
                    .eq("space_id", spaceId).in(true, "team_id", teamIds);
            SocialTenantDepartmentBindEntity socialTenantDepartmentBindEntity =
                iSocialTenantDepartmentBindService.getOne(queryWrapper);
            if (socialTenantDepartmentBindEntity != null) {
                Long departmentId =
                    iSocialTenantDepartmentService.getIdByDepartmentId(spaceId, tenantId,
                        socialTenantDepartmentBindEntity.getTenantDepartmentId());
                orgId = String.valueOf(departmentId);
            }
        }
        // Build userVo
        oneAccessUserVo.setAccount(OneAccessUserQueryVo.Account.builder()
            .fullName(memberEntity.getMemberName())
            .loginName(userEntity.getRemark())
            .orgId(orgId)
            .uid(String.valueOf(memberEntity.getUserId()))
            .build());

        return oneAccessUserVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long getUserIdByCode(String code) {
        // get uid from OneAccess via code
        OneAccessTokenResponse response = this.getToken(code);
        // Get username-related information, and log in and bind
        OneAccessUserInfoResponse userInfoResponse = getUserInfo(response.getAccess_token());
        // Get associated user by uid
        Long userId = iSocialUserBindService.getUserIdByUnionId(userInfoResponse.getUid());
        if (userId == null) {
            // not bind user
            UserEntity user = getUserByLoginName(userInfoResponse.getLoginName());
            if (user != null) {
                // bind user id
                iSocialUserBindService.create(user.getId(), userInfoResponse.getUid());
                userId = user.getId();
            }
            // new User , create user and join to collaborationSpace
            if (!oneAccessProperties.getCollaborationSpaceId().isEmpty()) {
                userId = createUser(oneAccessProperties.getCollaborationSpaceId(), null,
                    userInfoResponse.getLoginName(),
                    userInfoResponse.getNickName(), null, "", response.getUid());
            }
        }
        //update AvatarUrl and openId
        if (userId != null && StrUtil.isNotBlank(userInfoResponse.getAvatarUrl())) {

            iUserService.updateById(UserEntity.builder()
                .id(userId)
                .avatar(userInfoResponse.getAvatarUrl())
                .build());
            //set member open_id and member_name
            List<MemberEntity> memberEntityList = iMemberService.getByUserId(userId);
            memberEntityList.forEach(m -> {
                m.setOpenId(userInfoResponse.getLoginName());
                m.setMemberName(userInfoResponse.getNickName());
            });
            iMemberService.updateBatchById(memberEntityList);
        }
        return userId;
    }

    @Override
    public Long getUserIdByLoginNameAndUnionId(String loginName, String unionId) {
        // Associate login  uuid <-> loginName
        Long userId = iSocialUserBindService.getUserIdByUnionId(unionId);
        if (userId != null) {
            return userId;
        }
        // Find associated users by user
        UserEntity user = getUserByLoginName(loginName);
        ExceptionUtil.isTrue(user != null, GET_USER_INFO_ERROR);
        // bind user id
        iSocialUserBindService.create(user.getId(), unionId);
        return user.getId();
    }

    /**
     * Get user information by username
     *
     * @param loginName login account name
     * @return UserId
     */
    private UserEntity getUserByLoginName(String loginName) {
        // Associate login  uuid <-> loginName
        String uuid = SecureUtil.md5(loginName);
        Wrapper<UserEntity> queryWrapper = new QueryWrapper<UserEntity>()
            .eq("uuid", uuid);
        iUserService.getBaseMapper().selectOne(queryWrapper);
        return iUserService.getBaseMapper().selectOne(queryWrapper);
    }

    @Override
    public <T> T getRequestObject(HttpServletRequest request, Class<T> requestType) {
        String requestBody = getRequestBody(request);
        String plaintext = AESCipher.decrypt(requestBody, oneAccessProperties.getEncryptKey());
        log.info("requestBody=>{},plaintext=>{}", requestBody, plaintext);
        return JSONUtil.toBean(plaintext, requestType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrg(String spaceId, String orgName, String openOrgId,
                          String openParentOrgId) {
        ExceptionUtil.isFalse(StrUtil.isBlank(orgName) || StrUtil.isBlank(openOrgId),
            CREATE_TEAM_ERROR);
        // Check if it already exists, return directly
        String tenantId = String.format("one-%s", spaceId);
        SocialTenantDepartmentEntity tenantDepartment =
            iSocialTenantDepartmentService.getByTenantIdAndDepartmentId(spaceId, tenantId,
                openOrgId);
        if (tenantDepartment != null) {
            return tenantDepartment.getId();
        }
        log.info(
            "Create an external department : spaceId[{}],orgName:[{}],openOrgId:[{}],openParentOrgId:[{}}]",
            spaceId, orgName, openOrgId, openParentOrgId);
        // Create a department
        tenantDepartment = new SocialTenantDepartmentEntity()
            .setId(IdWorker.getId())
            .setTenantId(tenantId)
            .setSpaceId(spaceId)
            .setDepartmentId(openOrgId)
            .setOpenDepartmentId(openOrgId)
            .setParentOpenDepartmentId(openParentOrgId)
            .setParentId(openParentOrgId)
            .setDepartmentName(orgName);
        iSocialTenantDepartmentService.createBatch(Collections.singletonList(tenantDepartment));
        // Whether the third-party parent department already exists
        Long parenTteamId =
            iSocialTenantDepartmentBindService.getBindSpaceTeamIdBySpaceId(spaceId, tenantId,
                openParentOrgId);
        // If the parent department does not already exist, it is currently the root department of the third party
        if (parenTteamId == null) {
            parenTteamId = iTeamService.getRootTeamId(spaceId);
        }
        // Create group internal id
        Long teamId = iTeamService.createSubTeam(spaceId, orgName, parenTteamId);
        // Association and internal team binding relationship
        SocialTenantDepartmentBindEntity tenantDepartmentBind =
            new SocialTenantDepartmentBindEntity();
        tenantDepartmentBind.setId(IdWorker.getId());
        tenantDepartmentBind.setSpaceId(spaceId);
        tenantDepartmentBind.setTeamId(teamId);
        tenantDepartmentBind.setTenantId(tenantId);
        tenantDepartmentBind.setTenantDepartmentId(openOrgId);
        iSocialTenantDepartmentBindService.createBatch(
            Collections.singletonList(tenantDepartmentBind));
        // Returns the social_dep ID which associated with the external department
        return iSocialTenantDepartmentService.getIdByDepartmentId(spaceId, tenantId, openOrgId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrg(String uid, String spaceId, String openOrgId, String orgName,
                          String openParentOrgId, boolean enable) {
        ExceptionUtil.isFalse(StrUtil.isBlank(uid), UPDATE_TEAM_ERROR);
        // Update department information
        String tenantId = String.format("one-%s", spaceId);
        SocialTenantDepartmentEntity tenantDepartment = iSocialTenantDepartmentService.getById(uid);
        ExceptionUtil.isFalse(tenantDepartment == null, GET_TEAM_ERROR);
        log.info(
            "Update external departments: uid:[{}],spaceId[{}],orgName:[{}],openOrgId:[{}],openParentOrgId:[{}}]",
            uid, spaceId, orgName, openOrgId, openParentOrgId);
        // Update department
        Long parentTeamId =
            iSocialTenantDepartmentBindService.getBindSpaceTeamIdBySpaceId(spaceId, tenantId,
                openParentOrgId);
        if (parentTeamId == null) {
            parentTeamId = iTeamService.getRootTeamId(spaceId);
        }
        // Find the team id for the internal team after the update is found
        Long teamId =
            iSocialTenantDepartmentBindService.getBindSpaceTeamIdBySpaceId(spaceId, tenantId,
                tenantDepartment.getDepartmentId());
        // Get new openParentOrgId department <-> internal team new parentId
        if (!StrUtil.isBlank(openParentOrgId)) {
            iTeamService.updateTeamParent(teamId, tenantDepartment.getDepartmentName(),
                parentTeamId);
            tenantDepartment.setParentId(openParentOrgId);
        }
        if (!StrUtil.isBlank(orgName)) {
            iTeamService.updateTeamName(teamId, orgName);
            tenantDepartment.setDepartmentName(orgName);
        }
        if (!enable) {
            String disableOrgName = String.format("%s(禁用)", tenantDepartment.getDepartmentName());
            iTeamService.updateTeamName(teamId, disableOrgName);
        } else if (StrUtil.isBlank(orgName)) {
            // Rename after re-enable
            iTeamService.updateTeamName(teamId, tenantDepartment.getDepartmentName());
        }
        // update social_dep
        iSocialTenantDepartmentService.saveOrUpdate(tenantDepartment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOrg(String spaceId, String uid) {
        // Convert uid to real team Id
        SocialTenantDepartmentEntity tenantDepartment = iSocialTenantDepartmentService.getById(uid);
        ExceptionUtil.isFalse(tenantDepartment == null, GET_TEAM_ERROR);
        // Check if there are still people in the department
        Long teamId = iSocialTenantDepartmentBindService.getBindSpaceTeamId(spaceId,
            tenantDepartment.getTenantId(), tenantDepartment.getDepartmentId());
        boolean hasSubUnit = iTeamService.checkHasSubUnitByTeamId(spaceId, teamId);
        ExceptionUtil.isFalse(hasSubUnit, TEAM_HAS_SUB);
        // Delete department
        iSocialTenantDepartmentService.deleteTenantDepartment(spaceId,
            tenantDepartment.getTenantId(), tenantDepartment.getDepartmentId());
    }

    @Override
    public OneAccessOrgVo getOrgById(String spaceId, String uid, String openOrgId) {
        // Update department information
        String tenantId = String.format("one-%s", spaceId);
        // Convert uid to real team Id
        SocialTenantDepartmentEntity tenantDepartment = iSocialTenantDepartmentService.getById(uid);
        if (tenantDepartment == null) {
            tenantDepartment =
                iSocialTenantDepartmentService.getByDepartmentId(spaceId, tenantId, openOrgId);
        }
        ExceptionUtil.isNotNull(tenantDepartment, GET_TEAM_ERROR);
        OneAccessOrgVo oneAccessOrgVo = new OneAccessOrgVo("");
        String parentOrgId = "";
        // Get parentOrg info
        SocialTenantDepartmentEntity parentTenantDepartment =
            iSocialTenantDepartmentService.getByDepartmentId(spaceId, tenantId,
                tenantDepartment.getParentOpenDepartmentId());
        if (parentTenantDepartment != null) {
            parentOrgId = String.valueOf(parentTenantDepartment.getId());
        }
        oneAccessOrgVo.setOrganization(OneAccessOrgVo.Organization.builder()
            .orgName(tenantDepartment.getDepartmentName())
            .orgId(String.valueOf(tenantDepartment.getId()))
            .parentOrgId(parentOrgId)
            .build());
        return oneAccessOrgVo;
    }

    /**
     * Copy departments and personnel to the finger space station
     * Synchronizing group sub-department personnel is not supported at this time
     *
     * @param spaceId      space Id
     * @param destSpaceId  target space station Id
     * @param teamIds      team ids list
     * @param memberRoList list of member ids
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void copyTeamAndMembers(String spaceId, String destSpaceId, List<String> teamIds,
                                   List<MemberRo> memberRoList) {
        // There is an organizational structure in the subspace and there is no organizational structure
        Long rootTeamId = iTeamService.getRootTeamId(destSpaceId);
        String tenantId = String.format("vika-%s", spaceId);
        // 1.Synchronize organization and people under organization (excluding sub-departments)
        // Does not handle department update name and organization functions
        for (String deptId : teamIds) {
            TeamEntity teamEntity = iTeamService.getById(deptId);
            // If the current space station has already synced the team, perform an update or fix the current team information
            // Long localTeamId = iSocialTenantDepartmentBindService.getBindSpaceTeamId(tenantId,String.format("%s",teamEntity.getId()));
            Long localTeamId =
                iSocialTenantDepartmentBindService.getBindSpaceTeamIdBySpaceId(destSpaceId,
                    tenantId, String.format("%s", teamEntity.getId()));
            if (localTeamId != null && iTeamService.getById(localTeamId) != null) {
                iTeamService.updateTeamName(localTeamId, teamEntity.getTeamName());
            } else {
                // Get the source space station and the unsynchronized group, and add it to the Honma station
                Stack<TeamEntity> teamEntityStack =
                    buildDeptTree(destSpaceId, tenantId, teamEntity);
                while (!teamEntityStack.empty()) {
                    TeamEntity teamEntity1 = teamEntityStack.pop();
                    // Create a department from the top
                    String departmentId = String.format("%s", teamEntity1.getId());
                    SocialTenantDepartmentEntity tenantDepartment =
                        iSocialTenantDepartmentService.getByTenantIdAndDepartmentId(destSpaceId,
                            tenantId, departmentId);
                    if (tenantDepartment == null) {
                        tenantDepartment = new SocialTenantDepartmentEntity()
                            .setId(IdWorker.getId())
                            .setTenantId(tenantId)
                            .setSpaceId(destSpaceId)
                            .setDepartmentId(String.format("%s", departmentId))
                            .setParentId(String.format("%s", teamEntity1.getParentId()))
                            .setDepartmentName(teamEntity1.getTeamName());
                        // Create a third-party integration department
                        iSocialTenantDepartmentService.createBatch(
                            Collections.singletonList(tenantDepartment));
                    }
                    // Clear invalid third-party department binding data
                    iSocialTenantDepartmentBindService.deleteSpaceBindTenantDepartment(destSpaceId,
                        tenantId, tenantDepartment.getDepartmentId());
                    // Create Internal team
                    Long parentTeamId =
                        iSocialTenantDepartmentBindService.getBindSpaceTeamIdBySpaceId(destSpaceId,
                            tenantId, tenantDepartment.getParentId());
                    Long subTeamId;
                    if (parentTeamId == null) {
                        subTeamId =
                            iTeamService.createSubTeam(destSpaceId, teamEntity1.getTeamName(),
                                rootTeamId);
                    } else {
                        subTeamId =
                            iTeamService.createSubTeam(destSpaceId, teamEntity1.getTeamName(),
                                parentTeamId);
                    }
                    // Create binding
                    SocialTenantDepartmentBindEntity tenantDepartmentBind =
                        new SocialTenantDepartmentBindEntity().toBuilder()
                            .id(IdWorker.getId())
                            .spaceId(destSpaceId)
                            .tenantId(tenantId)
                            .teamId(subTeamId)
                            .tenantDepartmentId(tenantDepartment.getDepartmentId())
                            .build();
                    iSocialTenantDepartmentBindService.createBatch(
                        Collections.singletonList(tenantDepartmentBind));
                }
            }
        }

        // 2. Sync selected member
        Map<String, List<MemberRo>> memberGroupMap =
            memberRoList.stream().collect(Collectors.groupingBy(MemberRo::getTeamId));

        log.info("memberGroupMap => {}", memberGroupMap);
        // Execute sequentially to avoid inserting duplicate member
        memberGroupMap.forEach((teamId, memberRos) -> {
            List<MemberEntity> memberEntityList = memberRos.stream()
                .map(m -> iMemberService.getById(m.getMemberId()))
                .filter(Objects::nonNull)
                .filter(
                    m -> iMemberService.getMemberIdByUserIdAndSpaceId(m.getUserId(), destSpaceId) ==
                        null)  //排除已同步过的帐号
                .peek(m -> {
                    m.setId(IdWorker.getId());
                    m.setSpaceId(destSpaceId);
                    m.setIsAdmin(false);
                    m.setStatus(1);
                    m.setIsActive(true);
                })
                .collect(Collectors.toList());

            log.info("teamId={} , memberEntityList => {}", teamId, memberEntityList);
            // Sync individually selected members
            if (!memberEntityList.isEmpty()) {
                iMemberService.batchCreate(destSpaceId, memberEntityList);
                // Join the team associated with the space station
                List<Long> teamMermberIds =
                    memberEntityList.stream().map(MemberEntity::getId).collect(Collectors.toList());
                Long targetTeamId =
                    iSocialTenantDepartmentBindService.getBindSpaceTeamIdBySpaceId(destSpaceId,
                        tenantId, teamId);
                // The queried teamId has been deleted
                targetTeamId =
                    iTeamService.getById(targetTeamId) != null ? targetTeamId : rootTeamId;
                iTeamMemberRelService.addMemberTeams(teamMermberIds,
                    Collections.singletonList(targetTeamId));
            }
        });
    }

    /**
     * Get internal TeamId
     *
     * @param id vika_social_tenant_department
     * @return teamId
     */
    private Long getTeamIdByTenantDepartment(String id) {
        SocialTenantDepartmentEntity tenantDepartment = iSocialTenantDepartmentService.getById(id);
        if (tenantDepartment == null) {
            return null;
        }
        return iSocialTenantDepartmentBindService.getBindSpaceTeamId(tenantDepartment.getSpaceId(),
            tenantDepartment.getTenantId(), tenantDepartment.getDepartmentId());
    }

    /**
     * Get token information from oneaccess
     *
     * @return OneAccessTokenResponse
     */
    private OneAccessTokenResponse getToken(String code) {
        String url = oneAccessProperties.getIamHost() + "/idp/oauth2/getToken";
        RestClient restClient = RestClient.builder()
            .requestFactory(new HttpComponentsClientHttpRequestFactory())
            .build();
        HttpHeaders header = new HttpHeaders();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", oneAccessProperties.getClientId());
        params.add("client_secret", oneAccessProperties.getClientSecret());
        params.add("code", code);
        params.add("grant_type", "authorization_code");
        // post request needs to be set contentType
        header.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        ResponseEntity<OneAccessTokenResponse> responseEntity;
        try {
            responseEntity = restClient
                .post()
                .uri(URI.create(url))
                .headers(headers -> headers.addAll(header))
                .body(params)
                .retrieve().toEntity(OneAccessTokenResponse.class);
            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                log.info(
                    "connect to OneAccess server url => {} , Authentication failed, return request content:{}",
                    url, responseEntity.getBody());
                throw new BusinessException(GET_USER_INFO_ERROR);
            }
            if ("3001".equals(responseEntity.getBody().getError_code())) {
                log.info(
                    "connect to OneAccess server url => {},The query user information is empty, and the request content is returned:{}",
                    url, responseEntity.getBody());
                throw new BusinessException(USER_NOT_EXIST);
            }
            log.info("Get Token information:{}", responseEntity.getBody());
        } catch (Exception e) {
            log.info(
                "connect to OneAccess server url => {} ,Please check if the network is normal ,err:{}",
                url, e.getMessage());
            throw new BusinessException(GET_USER_INFO_ERROR);
        }
        return responseEntity.getBody();
    }

    /**
     * Get user identity information
     *
     * @param token certified token
     * @return user identity information
     */
    private OneAccessUserInfoResponse getUserInfo(String token) {
        String url = StrUtil.format(oneAccessProperties.getIamHost() +
                "/idp/oauth2/getUserInfo?client_id={}&access_token={}",
            oneAccessProperties.getClientId(), token);
        RestClient restClient = RestClient.builder()
            .requestFactory(new HttpComponentsClientHttpRequestFactory())
            .build();
        ResponseEntity<OneAccessUserInfoResponse> responseEntity;
        try {
            responseEntity = restClient
                .get()
                .uri(url)
                .retrieve()
                .toEntity(OneAccessUserInfoResponse.class);
            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                log.info(
                    "connect to OneAccess server url => {} , Authentication failed, return request content:{}",
                    url, responseEntity.getBody());
                throw new BusinessException(GET_USER_INFO_ERROR);
            }
            log.info("Get identity information:{}", responseEntity.getBody());
        } catch (Exception e) {
            log.info(
                "connect to OneAccess server url => {} ,Please check if the network is normal ,err:{}",
                url, e.getMessage());
            throw new BusinessException(GET_USER_INFO_ERROR);
        }
        return responseEntity.getBody();
    }

    /**
     * Build a group structure stack, stop looking up if the top of the stack is inserted
     *
     * @param tenantId   tenant id
     * @param teamEntity Team to be synced
     */
    private Stack<TeamEntity> buildDeptTree(String spaceId, String tenantId,
                                            TeamEntity teamEntity) {
        // Create a stack for generating the bottom-level departments from the root
        Stack<TeamEntity> teamEntityStack = new Stack<>();
        while (teamEntity != null && teamEntity.getParentId() != 0) {
            // If the current organization third party already exists, stop the upward query
            String departmentId = String.format("%s", teamEntity.getId());
            Long teamId =
                iSocialTenantDepartmentBindService.getBindSpaceTeamIdBySpaceId(spaceId, tenantId,
                    departmentId);
            if (teamId != null && iTeamService.getById(teamId) != null) {
                break;
            }
            teamEntityStack.push(teamEntity);
            // Get the reverse-checked space station department
            teamEntity = iTeamService.getById(teamEntity.getParentId());
        }
        return teamEntityStack;
    }

}
