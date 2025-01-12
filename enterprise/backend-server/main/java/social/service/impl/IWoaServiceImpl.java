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

package com.apitable.enterprise.social.service.impl;

import static com.apitable.base.enums.DatabaseException.INSERT_ERROR;
import static com.apitable.enterprise.social.enums.SocialException.APP_HAS_BIND_SPACE;
import static com.apitable.enterprise.social.enums.SocialException.SPACE_HAS_BOUND_TENANT;
import static com.apitable.enterprise.social.enums.SocialException.SPACE_UNBOUND_TENANT;
import static com.apitable.enterprise.social.enums.SocialException.TENANT_NOT_BIND_SPACE;
import static com.apitable.enterprise.social.enums.SocialException.TENANT_NOT_EXIST;
import static com.apitable.enterprise.social.enums.SocialException.USER_NOT_AUTH;
import static com.apitable.enterprise.social.enums.SocialException.USER_NOT_EXIST_WECOM;
import static com.apitable.user.enums.UserException.USER_NOT_EXIST;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.apitable.core.exception.BusinessException;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.enterprise.social.component.SocialContactManager;
import com.apitable.enterprise.social.entity.SocialTenantBindEntity;
import com.apitable.enterprise.social.entity.SocialTenantEntity;
import com.apitable.enterprise.social.enums.SocialAppType;
import com.apitable.enterprise.social.enums.SocialPlatformType;
import com.apitable.enterprise.social.model.SocialContactDTO;
import com.apitable.enterprise.social.model.SocialContactDTO.SocialDepartment;
import com.apitable.enterprise.social.model.SocialContactDTO.SocialUser;
import com.apitable.enterprise.social.model.SocialContactOptions;
import com.apitable.enterprise.social.service.ISocialTenantBindService;
import com.apitable.enterprise.social.service.ISocialTenantService;
import com.apitable.enterprise.social.service.ISocialUserBindService;
import com.apitable.enterprise.social.infrastructure.WoaTemplate;
import com.apitable.enterprise.social.infrastructure.model.WoaAppVisibleRangeResponse;
import com.apitable.enterprise.social.infrastructure.model.WoaAppVisibleRangeResponse.Department;
import com.apitable.enterprise.social.infrastructure.model.WoaAppVisibleRangeResponse.User;
import com.apitable.enterprise.social.infrastructure.model.WoaCompanyUserResponse.CompanyUser;
import com.apitable.enterprise.social.infrastructure.model.WoaDepartmentResponse.Dept;
import com.apitable.enterprise.social.infrastructure.model.WoaUserInfoResponse;
import com.apitable.enterprise.social.model.WoaBindConfigDTO;
import com.apitable.enterprise.social.model.WoaUserLoginVo;
import com.apitable.enterprise.social.service.IWoaService;
import com.apitable.organization.entity.MemberEntity;
import com.apitable.organization.service.IMemberService;
import com.apitable.space.enums.SpaceException;
import com.apitable.space.service.ISpaceRoleService;
import com.apitable.space.service.ISpaceService;
import com.apitable.space.vo.SpaceGlobalFeature;
import com.apitable.user.entity.UserEntity;
import com.apitable.user.service.IUserService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
public class IWoaServiceImpl implements IWoaService {

    @Autowired(required = false)
    private WoaTemplate woaTemplate;

    @Resource
    private IUserService iUserService;

    @Resource
    private ISpaceService iSpaceService;

    @Resource
    private ISpaceRoleService iSpaceRoleService;

    @Resource
    private IMemberService iMemberService;

    @Resource
    private ISocialTenantService iSocialTenantService;

    @Resource
    private ISocialTenantBindService iSocialTenantBindService;

    @Resource
    private ISocialUserBindService iSocialUserBindService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WoaUserLoginVo userLoginByOAuth2Code(String appId, String code) {
        // Get application secret key
        String tenantId = woaTemplate.getDefaultTenantId();
        String secretKey = iSocialTenantService.getPermanentCodeByAppIdAndTenantId(appId, tenantId);
        ExceptionUtil.isNotNull(secretKey, TENANT_NOT_EXIST);
        // Get user authorization access token
        String accessToken =
            woaTemplate.getUserAccessTokenByCode(appId, secretKey, code);
        ExceptionUtil.isNotNull(accessToken, USER_NOT_AUTH);
        WoaUserInfoResponse.User woaUser =
            woaTemplate.getUserInfo(appId, secretKey, accessToken);

        // Check the status of application binding space
        String spaceId = iSocialTenantBindService.getTenantBindSpaceId(tenantId, appId);
        ExceptionUtil.isNotBlank(spaceId, TENANT_NOT_BIND_SPACE);

        // Member information of tenant space
        MemberEntity member =
            iMemberService.getBySpaceIdAndOpenId(spaceId, woaUser.getCompanyUid());
        ExceptionUtil.isNotNull(member, USER_NOT_EXIST_WECOM);
        Long userId = iSocialUserBindService.getUserIdByUnionId(woaUser.getCompanyUid());
        if (userId == null) {
            // Create user entity
            userId = iUserService.createByExternalSystem(woaUser.getCompanyUid(),
                woaUser.getNickname(), null, null, null);
        }
        if (member.getUserId() == null) {
            member.setUserId(userId);
            member.setIsActive(true);
            iMemberService.updateById(member);
        }
        return WoaUserLoginVo.builder().userId(userId).bindSpaceId(spaceId).build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refreshContact(Long operatingUserId, String spaceId) {
        if (woaTemplate == null) {
            throw new BusinessException("Woa is not enabled");
        }
        log.info("Space「{}」refresh address book, operating user:{}", spaceId, operatingUserId);

        WoaBindConfigDTO configDTO = this.getTenantBindConfig(spaceId);

        Long mainAdminMemberId = iSpaceService.getSpaceMainAdminMemberId(spaceId);
        String mainAdminOpenId = iMemberService.getOpenIdByMemberId(mainAdminMemberId);
        // Check if woa member exists
        String companyToken = woaTemplate.getCompanyToken(configDTO.getAppId(),
            configDTO.getSecretKey());
        List<CompanyUser> woaUsers = woaTemplate.getCompanyUser(configDTO.getAppId(),
            configDTO.getSecretKey(), companyToken, mainAdminOpenId);
        ExceptionUtil.isNotEmpty(woaUsers, USER_NOT_EXIST);
        CompanyUser companyUser = woaUsers.stream().findFirst().orElse(new CompanyUser());
        // Refresh contact
        this.refreshContact(spaceId, configDTO.getTenantId(), configDTO.getAppId(),
            configDTO.getSecretKey(), companyUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindSpace(Long bindUserId, String spaceId, String appId, String secretKey) {
        if (woaTemplate == null) {
            throw new BusinessException("Woa is not enabled");
        }
        log.info("Space「{}」bind Woa with appId「{}」, operating user:{} ", spaceId, appId,
            bindUserId);
        // Primary administrator user ID of the space
        Long mainAdminUserId = iSpaceService.getSpaceMainAdminUserId(spaceId);
        // Check whether the space master administrator
        ExceptionUtil.isTrue(mainAdminUserId.equals(bindUserId),
            SpaceException.NOT_SPACE_MAIN_ADMIN);
        // Check woa member relation
        CompanyUser woaUser = this.getWoaUserAfterCheckRelation(bindUserId, appId, secretKey);

        // Check whether the space has been bound to other platform tenants
        boolean spaceBindStatus = iSocialTenantBindService.getSpaceBindStatus(spaceId);
        ExceptionUtil.isFalse(spaceBindStatus, SPACE_HAS_BOUND_TENANT);
        // Check whether the application has been bound to other space stations
        String tenantId = woaTemplate.getDefaultTenantId();
        boolean appBindStatus =
            iSocialTenantBindService.getWeComTenantBindStatus(tenantId, appId);
        ExceptionUtil.isFalse(appBindStatus, APP_HAS_BIND_SPACE);

        boolean isTenantExist = iSocialTenantService.isTenantExist(tenantId, appId);
        if (!isTenantExist) {
            // If the tenant does not exist, create it
            SocialTenantEntity tenant = new SocialTenantEntity();
            tenant.setAppId(appId);
            tenant.setAppType(SocialAppType.INTERNAL.getType());
            tenant.setTenantId(tenantId);
            tenant.setContactAuthScope(JSONUtil.createObj().toString());
            tenant.setPermanentCode(secretKey);
            tenant.setPlatform(SocialPlatformType.WOA.getValue());
            boolean flag = iSocialTenantService.save(tenant);
            ExceptionUtil.isTrue(flag, INSERT_ERROR);
        }
        // Delete all sub administrators
        iSpaceRoleService.deleteBySpaceId(spaceId);
        // Change the global status of the space (application and invitation are prohibited)
        SpaceGlobalFeature feature = SpaceGlobalFeature.builder()
            .joinable(false)
            .invitable(false)
            .build();
        iSpaceService.switchSpacePros(bindUserId, spaceId, feature);
        // Refresh contact
        this.refreshContact(spaceId, tenantId, appId, secretKey, woaUser);
        // When the space is bound for the first time, increase the binding
        iSocialTenantBindService.addTenantBind(String.valueOf(appId), tenantId, spaceId);
        // Third party platform integration - user bind
        boolean isExist = iSocialUserBindService.isUnionIdBind(bindUserId, woaUser.getCompanyUid());
        if (!isExist) {
            iSocialUserBindService.create(bindUserId, woaUser.getCompanyUid());
        }
    }

    private CompanyUser getWoaUserAfterCheckRelation(Long userId, String appId, String secretKey) {
        UserEntity user = iUserService.getById(userId);
        ExceptionUtil.isNotNull(user, USER_NOT_EXIST);
        boolean emailBlank = StrUtil.isBlank(user.getEmail());
        boolean phoneBlank = StrUtil.isBlank(user.getMobilePhone());
        if (emailBlank && phoneBlank) {
            throw new BusinessException(
                "The user's credential does not match the woa organization.");
        }
        String companyToken = woaTemplate.getCompanyToken(appId, secretKey);
        int index = 0;
        boolean condition = true;
        while (condition) {
            List<CompanyUser> companyUser =
                woaTemplate.getCompanyUser(appId, secretKey, companyToken, index);
            // The user's mobile phone account or email
            // must be able to associate with the members of woa
            Optional<CompanyUser> first = companyUser.stream()
                .filter(i -> (!emailBlank && user.getEmail().equals(i.getEmail()))
                    || (!phoneBlank && user.getMobilePhone().equals(i.getPhone())))
                .findFirst();
            if (first.isPresent()) {
                return first.get();
            }
            index++;
            condition = companyUser.size() == WoaTemplate.PAGE_QUERY_SIZE;
        }
        throw new BusinessException("The user's credential does not match the woa organization.");
    }

    private WoaBindConfigDTO getTenantBindConfig(String spaceId) {
        // Space unbound application
        SocialTenantBindEntity bind = iSocialTenantBindService.getBySpaceId(spaceId);
        ExceptionUtil.isNotNull(bind, SPACE_UNBOUND_TENANT);
        // Tenant does not exist
        SocialTenantEntity tenant = iSocialTenantService
            .getByAppIdAndTenantId(bind.getAppId(), bind.getTenantId());
        ExceptionUtil.isNotNull(tenant, TENANT_NOT_EXIST);
        return WoaBindConfigDTO.builder()
            .tenantId(bind.getTenantId())
            .appId(bind.getAppId())
            .secretKey(tenant.getPermanentCode())
            .build();
    }

    private void refreshContact(String spaceId, String tenantId, String appId,
                                String secretKey, CompanyUser woaUser) {
        LinkedHashMap<String, SocialContactDTO> contactMap =
            this.getContactTreeMap(appId, secretKey);
        SocialContactOptions options = SocialContactOptions.builder()
            .socialRootTeamId(WoaTemplate.ROOT_DEPARTMENT_ID)
            .operatorOpenId(woaUser.getCompanyUid())
            .operatorSocialMemberName(woaUser.getName())
            .build();
        SocialContactManager.me().refreshContact(spaceId, tenantId, contactMap, options);
    }

    private LinkedHashMap<String, SocialContactDTO> getContactTreeMap(String appId,
                                                                      String secretKey) {
        String companyToken = woaTemplate.getCompanyToken(appId, secretKey);
        // Application visible range
        WoaAppVisibleRangeResponse visibleRange =
            this.getAppVisibleRange(appId, secretKey, companyToken);
        // Visibility across the enterprise
        if (visibleRange.getCompany() != null) {
            Collection<Dept> departments = this.getSubDeptMap(appId, secretKey, companyToken,
                Collections.singletonList(WoaTemplate.ROOT_DEPARTMENT_ID)).values();
            return this.getContactTreeMap(appId, secretKey, companyToken, departments);
        }

        // Initialize root department
        LinkedHashMap<String, SocialContactDTO> contactMap = new LinkedHashMap<>();
        SocialContactDTO contact = new SocialContactDTO();
        SocialDepartment rootDept = SocialDepartment.builder()
            .deptId(WoaTemplate.ROOT_DEPARTMENT_ID)
            .build();
        contact.setDepartment(rootDept);
        contactMap.put(WoaTemplate.ROOT_DEPARTMENT_ID, contact);
        // Department ID in visible range
        if (CollUtil.isNotEmpty(visibleRange.getDepartments())) {
            List<String> deptIds = visibleRange.getDepartments().stream()
                .map(Department::getDeptId)
                .collect(Collectors.toList());
            contactMap.putAll(this.getContactTreeMap(appId, secretKey,
                companyToken, deptIds));
        }
        if (CollUtil.isNotEmpty(visibleRange.getUsers())) {
            Set<String> existedSocialUserIds = contactMap.values().stream()
                .map(dto -> dto.getUserMap().keySet())
                .reduce(new HashSet<>(), (result, ids) -> {
                    result.addAll(ids);
                    return result;
                });
            List<String> visibleUserIds = visibleRange.getUsers().stream()
                .map(User::getCompanyUid).collect(Collectors.toList());
            Collection<String> subtract = CollUtil.subtract(visibleUserIds, existedSocialUserIds);
            if (subtract.isEmpty()) {
                return contactMap;
            }
            Map<String, SocialContactDTO.SocialUser> userMap =
                contactMap.get(WoaTemplate.ROOT_DEPARTMENT_ID).getUserMap();
            List<List<String>> split = CollUtil.split(subtract,
                WoaTemplate.SPECIFIED_QUERY_MAX_NUMBER);
            for (List<String> socialUserIds : split) {
                List<CompanyUser> woaUsers = woaTemplate.getCompanyUser(appId, secretKey,
                    companyToken, ArrayUtil.join(socialUserIds.toArray(), StrUtil.COMMA));
                userMap.putAll(woaUsers.stream()
                    .collect(Collectors.toMap(CompanyUser::getCompanyUid, this::formatSocialUser)));
            }
        }
        return contactMap;
    }

    private LinkedHashMap<String, SocialContactDTO> getContactTreeMap(String appId,
                                                                      String secretKey,
                                                                      String companyToken,
                                                                      List<String> deptIds) {
        List<Dept> departments = new ArrayList<>();
        // Recursive load social department
        List<List<String>> split =
            CollUtil.split(deptIds, WoaTemplate.SPECIFIED_QUERY_MAX_NUMBER);
        for (List<String> socialDeptIds : split) {
            List<Dept> deptList = woaTemplate.getDepartments(appId, secretKey,
                companyToken, ArrayUtil.join(socialDeptIds.toArray(), StrUtil.COMMA));
            departments.addAll(deptList);
        }
        departments.addAll(this.getSubDeptMap(appId, secretKey, companyToken, deptIds).values());
        return this.getContactTreeMap(appId, secretKey, companyToken, departments);
    }

    private LinkedHashMap<String, SocialContactDTO> getContactTreeMap(String appId,
                                                                      String secretKey,
                                                                      String companyToken,
                                                                      Collection<Dept> departments) {
        LinkedHashMap<String, SocialContactDTO> contactMap = new LinkedHashMap<>();
        for (Dept dept : departments) {
            List<CompanyUser> woaUsers = new ArrayList<>();
            int index = 0;
            boolean condition = true;
            // Recursive load social user
            while (condition) {
                List<CompanyUser> companyUsers =
                    woaTemplate.getDeptUsers(appId, secretKey, companyToken,
                        dept.getDeptId(), index);
                woaUsers.addAll(companyUsers);
                index++;
                condition = companyUsers.size() == WoaTemplate.PAGE_QUERY_SIZE;
            }
            SocialDepartment department = this.formatSocialDepartment(dept);
            Map<String, SocialUser> userMap = woaUsers.stream()
                .collect(Collectors.toMap(CompanyUser::getCompanyUid, this::formatSocialUser));
            SocialContactDTO socialContactDTO = new SocialContactDTO(department, userMap);
            contactMap.put(dept.getDeptId(), socialContactDTO);
        }
        return contactMap;
    }

    private LinkedHashMap<String, Dept> getSubDeptMap(String appId,
                                                      String secretKey, String companyToken,
                                                      List<String> deptIds) {
        LinkedHashMap<String, Dept> woaDepartmentMap = new LinkedHashMap<>();
        // Recursive load social department
        for (String deptId : deptIds) {
            if (woaDepartmentMap.containsKey(deptId)) {
                continue;
            }
            List<Dept> departments = new ArrayList<>();
            int index = 0;
            boolean condition = true;
            while (condition) {
                List<Dept> deptList =
                    woaTemplate.getDepartments(appId, secretKey, companyToken,
                        deptId, index);
                index++;
                condition = deptList.size() == WoaTemplate.PAGE_QUERY_SIZE;
                departments.addAll(deptList);
            }
            departments.stream().sorted(Comparator.comparing(Dept::getOrder).reversed())
                .forEach(dept -> woaDepartmentMap.put(dept.getDeptId(), dept));
            List<String> subDeptIds =
                departments.stream().map(Dept::getDeptId).collect(Collectors.toList());
            woaDepartmentMap.putAll(this.getSubDeptMap(appId, secretKey, companyToken, subDeptIds));
        }
        return woaDepartmentMap;
    }

    private WoaAppVisibleRangeResponse getAppVisibleRange(String appId,
                                                          String secretKey, String companyToken) {
        return woaTemplate.getAppVisibleRange(appId, secretKey, companyToken);
    }

    private SocialContactDTO.SocialDepartment formatSocialDepartment(Dept dept) {
        return SocialDepartment.builder()
            .deptId(dept.getDeptId())
            .parentDeptId(dept.getDeptPid())
            .deptName(dept.getName())
            .build();
    }

    private SocialContactDTO.SocialUser formatSocialUser(CompanyUser user) {
        return SocialUser.builder()
            .openId(user.getCompanyUid())
            .unionId(user.getCompanyUid())
            .name(user.getName())
            .email(StrUtil.emptyToNull(user.getEmail()))
            .mobile(StrUtil.emptyToNull(user.getPhone()))
            .active(true)
            .build();
    }
}
