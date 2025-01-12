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

import static com.apitable.enterprise.vikabilling.constants.BillingConstants.GIFT_ADVANCE_CAPACITY;
import static com.apitable.enterprise.vikabilling.constants.BillingConstants.GIFT_BASIC_CAPACITY;
import static com.apitable.shared.constants.NotificationConstants.SPECIFICATION;
import static com.apitable.shared.constants.SpaceConstants.SPACE_NAME_DEFAULT_SUFFIX;
import static com.apitable.space.enums.SpaceException.NOT_SPACE_ADMIN;
import static com.apitable.space.enums.SpaceException.SPACE_ALREADY_CERTIFIED;
import static com.apitable.space.enums.SpaceException.SPACE_NOT_EXIST;
import static com.apitable.user.enums.UserException.REGISTER_EMAIL_ERROR;
import static com.apitable.user.enums.UserException.REGISTER_EMAIL_HAS_EXIST;
import static com.apitable.user.enums.UserException.REGISTER_FAIL;
import static com.apitable.workspace.enums.PermissionException.MEMBER_NOT_IN_SPACE;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.apitable.base.enums.SystemConfigType;
import com.apitable.base.service.ISystemConfigService;
import com.apitable.core.exception.BusinessException;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.core.util.SpringContextHolder;
import com.apitable.enterprise.gm.enums.GmAction;
import com.apitable.enterprise.gm.service.IGmService;
import com.apitable.enterprise.vcode.service.IVCodeService;
import com.apitable.enterprise.vika.core.VikaOperations;
import com.apitable.enterprise.vika.core.model.GmPermissionInfo;
import com.apitable.enterprise.vika.core.model.UserContactInfo;
import com.apitable.organization.service.IMemberService;
import com.apitable.organization.service.IUnitService;
import com.apitable.player.service.IPlayerActivityService;
import com.apitable.shared.component.LanguageManager;
import com.apitable.shared.component.TaskManager;
import com.apitable.shared.component.notification.NotificationManager;
import com.apitable.shared.component.notification.NotificationTemplateId;
import com.apitable.shared.util.ApiHelper;
import com.apitable.space.enums.SpaceCertification;
import com.apitable.space.enums.SpaceResourceGroupCode;
import com.apitable.space.service.ISpaceMemberRoleRelService;
import com.apitable.space.service.ISpaceService;
import com.apitable.space.vo.SpaceGlobalFeature;
import com.apitable.user.entity.DeveloperEntity;
import com.apitable.user.entity.UserEntity;
import com.apitable.user.mapper.DeveloperMapper;
import com.apitable.user.mapper.UserMapper;
import com.apitable.user.service.IUserService;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class GmServiceImpl implements IGmService {

    @Autowired(required = false)
    private VikaOperations vikaOperations;

    @Resource
    private ISystemConfigService iSystemConfigService;

    @Resource
    private IUnitService iUnitService;

    @Resource
    private ISpaceService iSpaceService;

    @Resource
    private IUserService iUserService;

    @Resource
    private IMemberService iMemberService;

    @Resource
    private UserMapper userMapper;

    @Resource
    private ISpaceMemberRoleRelService iSpaceMemberRoleRelService;

    @Resource
    private IVCodeService ivCodeService;

    @Resource
    private IPlayerActivityService iPlayerActivityService;

    @Resource
    private DeveloperMapper developerMapper;

    @Value("${SUPER_ADMINISTRATORS:''}")
    private String superAdministratorsStr;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createUsersByCli() {
        int size = 50;
        String u = "test";
        String p = "13312345";
        for (int i = 0; i < size; i++) {
            if (i < 10) {
                createUserByCli(u + String.format("00%d@vikatest.com", i), "qwer1234",
                    p + String.format("00%d", i));
            } else {
                createUserByCli(u + String.format("0%d@vikatest.com", i), "qwer1234",
                    p + String.format("0%d", i));
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserEntity createUserByCli(String username, String password, String phone) {
        log.info("Create User");
        ExceptionUtil.isTrue(Validator.isEmail(username), REGISTER_EMAIL_ERROR);
        UserEntity user = iUserService.getByEmail(username);
        ExceptionUtil.isNull(user, REGISTER_EMAIL_HAS_EXIST);
        UserEntity newUser = new UserEntity();
        newUser.setUuid(IdUtil.fastSimpleUUID());
        newUser.setEmail(username);
        newUser.setNickName(StrUtil.subBefore(username, '@', true));
        PasswordEncoder passwordEncoder = SpringContextHolder.getBean(PasswordEncoder.class);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setCode("+86");
        newUser.setMobilePhone(phone);
        boolean saveFlag = iUserService.saveUser(newUser);
        ExceptionUtil.isTrue(saveFlag, REGISTER_FAIL);
        String spaceName = newUser.getNickName();
        if (LocaleContextHolder.getLocale().equals(LanguageManager.me().getDefaultLanguage())) {
            spaceName += SPACE_NAME_DEFAULT_SUFFIX;
        }
        iSpaceService.createSpace(newUser, spaceName);
        // Create personal invitation code
        ivCodeService.createPersonalInviteCode(newUser.getId());
        // Create user activity record
        iPlayerActivityService.createUserActivityRecord(newUser.getId());
        DeveloperEntity developer = new DeveloperEntity();
        developer.setId(IdWorker.getId());
        developer.setUserId(newUser.getId());
        developer.setApiKey(ApiHelper.createKey());
        developer.setCreatedBy(0L);
        developer.setUpdatedBy(0L);
        developerMapper.insert(developer);
        return newUser;
    }

    @Override
    public void validPermission(Long userId, GmAction action) {
        log.info("valid user「{}」 「{}」GM permission.", userId, action.name());
        if (StrUtil.isNotBlank(superAdministratorsStr)) {
            if (superAdministratorsStr.contains(userId.toString())) {
                return;
            }
            String uuid = userMapper.selectUuidById(userId);
            if (superAdministratorsStr.contains(uuid)) {
                return;
            }
        }
        this.getGmConfigAfterCheckUserPermission(userId, action);
    }

    @Override
    public void updateGmPermissionConfig(Long userId, String dstId) {
        log.info("「{}」update gm permission config", userId);
        // gets the existing permission configuration
        String config =
            this.getGmConfigAfterCheckUserPermission(userId, GmAction.PERMISSION_CONFIG);
        // gets the updated permission configuration
        List<GmPermissionInfo> gmPermissionInfos =
            vikaOperations.getGmPermissionConfiguration(dstId);
        if (gmPermissionInfos.isEmpty()) {
            throw new BusinessException("NO UPDATES!");
        }
        JSONObject configVal = config != null ? JSONUtil.parseObj(config) : JSONUtil.createObj();
        for (GmPermissionInfo info : gmPermissionInfos) {
            if (info.getUnitIds().isEmpty()) {
                configVal.remove(info.getAction());
            } else {
                configVal.set(info.getAction(), info.getUnitIds());
            }
        }
        iSystemConfigService.saveOrUpdate(userId, SystemConfigType.GM_PERMISSION_CONFIG, null,
            configVal.toString());
    }

    private String getGmConfigAfterCheckUserPermission(Long userId, GmAction action) {
        List<Long> unitIds = new ArrayList<>();
        // Authorized organization unit configured by the system
        String config =
            iSystemConfigService.findConfig(SystemConfigType.GM_PERMISSION_CONFIG, null);
        if (config != null && JSONUtil.parseObj(config).containsKey(action.name())) {
            unitIds.addAll(
                JSONUtil.parseObj(config).getJSONArray(action.name()).toList(Long.class));
        }
        if (unitIds.isEmpty()) {
            throw new BusinessException("PERMISSION CONFIG UNIT IS NULL.");
        }
        // Gets all the user ids associated with the organization unit
        List<Long> userIds = iUnitService.getRelUserIdsByUnitIds(unitIds);
        if (CollUtil.isEmpty(userIds) || !userIds.contains(userId)) {
            throw new BusinessException("INSUFFICIENT PERMISSIONS!");
        }
        return config;
    }

    @Override
    public void spaceCertification(String spaceId, String operatorUserUuid,
                                   SpaceCertification certification) {
        // verify whether the submitter exists
        Long userId = iUserService.getUserIdByUuidWithCheck(operatorUserUuid);
        // verify whether the space exists
        String spaceName = iSpaceService.getNameBySpaceId(spaceId);
        if (StrUtil.isBlank(spaceName)) {
            sendSpaceCertifyFailedNotice(userId, spaceId);
            throw new BusinessException(SPACE_NOT_EXIST);
        }
        // whether the space station can be certified
        iSpaceService.checkCanOperateSpaceUpdate(spaceId);
        // verify that the space has been authenticated
        ExceptionUtil.isFalse(iSpaceService.isCertified(spaceId), SPACE_ALREADY_CERTIFIED);
        // verify the member
        Long memberId = iMemberService.getMemberIdByUserIdAndSpaceId(userId, spaceId);
        if (null == memberId) {
            sendSpaceCertifyFailedNotice(userId, spaceId);
            throw new BusinessException(MEMBER_NOT_IN_SPACE);
        }
        boolean isAdmin = checkMemberIsAdmin(spaceId, memberId);
        if (!isAdmin) {
            sendSpaceCertifyFailedNotice(userId, spaceId);
            throw new BusinessException(NOT_SPACE_ADMIN);
        }
        // open enterprise certification
        SpaceGlobalFeature feature =
            SpaceGlobalFeature.builder().certification(certification.getLevel()).build();
        iSpaceService.switchSpacePros(userId, spaceId, feature);
        // send certificate success notice
        sendSpaceCertifiedNotice(userId, spaceId, certification);
    }

    /**
     * check that the member is not an admin of the space
     *
     * @param spaceId  space id
     * @param memberId member id
     * @return boolean
     */
    private boolean checkMemberIsAdmin(String spaceId, Long memberId) {
        List<String> resourceGroupCodes = SpaceResourceGroupCode.codes();
        List<Long> memberAdminIds =
            iSpaceMemberRoleRelService.getMemberIdListByResourceGroupCodes(spaceId,
                resourceGroupCodes);
        Long adminMemberId = iSpaceService.getSpaceMainAdminMemberId(spaceId);
        if (adminMemberId != null) {
            memberAdminIds.add(adminMemberId);
        }
        return CollUtil.contains(memberAdminIds, memberId);
    }

    private void sendSpaceCertifiedNotice(Long userId, String spaceId,
                                          SpaceCertification certification) {
        // send notification
        TaskManager.me().execute(() -> {
            Dict extra = new Dict();
            if (SpaceCertification.BASIC.equals(certification)) {
                extra.set(SPECIFICATION, GIFT_BASIC_CAPACITY);
            }
            if (SpaceCertification.SENIOR.equals(certification)) {
                extra.set(SPECIFICATION, GIFT_ADVANCE_CAPACITY);
            }
            NotificationManager.me().playerNotify(NotificationTemplateId.SPACE_CERTIFICATION_NOTIFY,
                ListUtil.toList(userId), 0L, spaceId, extra);
        });
    }

    private void sendSpaceCertifyFailedNotice(Long userId, String spaceId) {
        TaskManager.me().execute(() -> NotificationManager.me()
            .playerNotify(NotificationTemplateId.SPACE_CERTIFICATION_FAIL_NOTIFY,
                ListUtil.toList(userId), 0L, spaceId, new Dict()));
    }

    @Override
    public void queryAndWriteBackUserContactInfo(String host, String datasheetId, String viewId,
                                                 String token) {
        // read user's id from datasheet by vika api
        List<UserContactInfo> userContactInfos =
            vikaOperations.getUserIdFromDatasheet(host, datasheetId, viewId, token);
        if (userContactInfos.isEmpty()) {
            throw new BusinessException("There are no records that meet the conditions.");
        }
        // query user's contact information by user's id
        this.getUserPhoneAndEmailByUserId(userContactInfos);
        // write back user's mobile phone and email
        for (UserContactInfo userContactInfo : userContactInfos) {
            vikaOperations.writeBackUserContactInfo(host, token, datasheetId, userContactInfo);
        }
    }

    @Override
    public void getUserPhoneAndEmailByUserId(List<UserContactInfo> userContactInfos) {
        // query user's mobile phone and email by user's id
        List<UserEntity> userEntities = userMapper.selectByUuIds(
            userContactInfos.stream().map(UserContactInfo::getUuid).collect(Collectors.toList()));
        Map<String, UserEntity> uuidToUserMap = userEntities.stream()
            .collect(Collectors.toMap(UserEntity::getUuid, Function.identity()));
        // handle write back information
        for (UserContactInfo info : userContactInfos) {
            if (!uuidToUserMap.containsKey(info.getUuid())) {
                continue;
            }
            BeanUtil.copyProperties(uuidToUserMap.get(info.getUuid()), info);
        }
    }
}
