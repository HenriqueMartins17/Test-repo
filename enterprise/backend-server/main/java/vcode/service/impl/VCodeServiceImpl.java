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

package com.apitable.enterprise.vcode.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.Resource;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import lombok.extern.slf4j.Slf4j;

import com.apitable.base.enums.DatabaseException;
import com.apitable.enterprise.integral.enums.IntegralAlterType;
import com.apitable.enterprise.integral.service.IIntegralService;
import com.apitable.enterprise.vcode.dto.VCodeDTO;
import com.apitable.enterprise.vcode.entity.CodeEntity;
import com.apitable.enterprise.vcode.enums.VCodeException;
import com.apitable.enterprise.vcode.enums.VCodeType;
import com.apitable.enterprise.vcode.enums.VCodeUsageType;
import com.apitable.enterprise.vcode.mapper.VCodeMapper;
import com.apitable.enterprise.vcode.mapper.VCodeUsageMapper;
import com.apitable.enterprise.vcode.ro.VCodeCreateRo;
import com.apitable.enterprise.vcode.ro.VCodeUpdateRo;
import com.apitable.enterprise.vcode.service.IVCodeActivityService;
import com.apitable.enterprise.vcode.service.IVCodeCouponService;
import com.apitable.enterprise.vcode.service.IVCodeService;
import com.apitable.enterprise.vcode.service.IVCodeUsageService;
import com.apitable.enterprise.vcode.vo.VCodePageVo;
import com.apitable.enterprise.wechat.dto.ThirdPartyMemberInfo;
import com.apitable.enterprise.wechat.mapper.ThirdPartyMemberMapper;
import com.apitable.shared.config.properties.ConstProperties;
import com.apitable.shared.constants.IntegralActionCodeConstants;
import com.apitable.shared.util.RandomExtendUtil;
import com.apitable.user.entity.UserEntity;
import com.apitable.user.enums.ThirdPartyMemberType;
import com.apitable.user.mapper.UserMapper;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.core.util.SqlTool;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.apitable.enterprise.vcode.enums.VCodeException.ACCOUNT_NOT_REGISTER;
import static com.apitable.enterprise.vcode.enums.VCodeException.CANNOT_ZERO;
import static com.apitable.enterprise.vcode.enums.VCodeException.CODE_NOT_EXIST;
import static com.apitable.enterprise.vcode.enums.VCodeException.EXPIRE_TIME_INCORRECT;
import static com.apitable.enterprise.vcode.enums.VCodeException.INVITE_CODE_EXPIRE;
import static com.apitable.enterprise.vcode.enums.VCodeException.INVITE_CODE_NOT_EXIST;
import static com.apitable.enterprise.vcode.enums.VCodeException.INVITE_CODE_NOT_VALID;
import static com.apitable.enterprise.vcode.enums.VCodeException.INVITE_CODE_USED;
import static com.apitable.enterprise.vcode.enums.VCodeException.REDEMPTION_CODE_EXPIRE;
import static com.apitable.enterprise.vcode.enums.VCodeException.REDEMPTION_CODE_NOT_EXIST;
import static com.apitable.enterprise.vcode.enums.VCodeException.REDEMPTION_CODE_NOT_VALID;
import static com.apitable.enterprise.vcode.enums.VCodeException.REDEMPTION_CODE_USED;
import static com.apitable.enterprise.vcode.enums.VCodeException.TEMPLATE_EMPTY;
import static com.apitable.enterprise.vcode.enums.VCodeException.TYPE_ERROR;
import static com.apitable.enterprise.vcode.enums.VCodeException.TYPE_INFO_ERROR;
import static com.apitable.shared.constants.IntegralActionCodeConstants.REDEMPTION_CODE;

/**
 * <p>
 * VCode Service Implement Class
 * </p>
 */
@Slf4j
@Service
public class VCodeServiceImpl implements IVCodeService {

    @Resource
    private ThirdPartyMemberMapper thirdPartyMemberMapper;

    @Resource
    private VCodeMapper vCodeMapper;

    @Resource
    private VCodeUsageMapper vCodeUsageMapper;

    @Resource
    private IVCodeUsageService ivCodeUsageService;

    @Resource
    private IVCodeActivityService ivCodeActivityService;

    @Resource
    private IVCodeCouponService ivCodeCouponService;

    @Resource
    private UserMapper userMapper;

    @Resource
    private ConstProperties constProperties;

    @Resource
    private IIntegralService iIntegralService;

    @Override
    public String getUserInviteCode(Long userId) {
        return vCodeMapper.selectCodeByTypeAndRefId(VCodeType.PERSONAL_INVITATION_CODE.getType(),
            userId);
    }

    @Override
    public Long getRefIdByCodeAndType(String code, Integer type) {
        return vCodeMapper.selectRefIdByCodeAndType(code, type);
    }

    @Override
    public IPage<VCodePageVo> getVCodePageVo(Page<VCodePageVo> page, Integer type,
                                             Long activityId) {
        return vCodeMapper.selectDetailInfo(page, type, activityId);
    }

    @Override
    public String getOfficialInvitationCode(String appId, String unionId) {
        log.info("Get the official invitation code. UnionId：{}", unionId);
        // Query whether the WeChat user has obtained an invitation code, if so, there is no need to regenerate
        ThirdPartyMemberInfo info = thirdPartyMemberMapper.selectInfo(appId, unionId,
            ThirdPartyMemberType.WECHAT_PUBLIC_ACCOUNT.getType());
        String code =
            vCodeMapper.selectCodeByTypeAndRefId(VCodeType.OFFICIAL_INVITATION_CODE.getType(),
                info.getId());
        if (code == null) {
            code = this.getUniqueCodeBatch(VCodeType.OFFICIAL_INVITATION_CODE.getType());
            CodeEntity entity = CodeEntity.builder()
                .type(VCodeType.OFFICIAL_INVITATION_CODE.getType())
                .refId(info.getId())
                .code(code)
                .availableTimes(1)
                .remainTimes(1)
                .limitTimes(1)
                .build();
            boolean flag = SqlHelper.retBool(vCodeMapper.insert(entity));
            ExceptionUtil.isTrue(flag, DatabaseException.INSERT_ERROR);
            // Save pick up log
            ivCodeUsageService.createUsageRecord(info.getId(), info.getNickName(),
                VCodeUsageType.ACQUIRE.getType(), code);
        }
        return code;
    }

    @Override
    public String getActivityCode(Long activityId, String appId, String unionId) {
        log.info("Get active VCode，activityId：{}，unionId：{}", activityId, unionId);
        //Query whether the activity has a corresponding VCode
        int count = SqlTool.retCount(vCodeMapper.countByActivityId(activityId));
        if (count == 0) {
            return null;
        }
        // Query whether the operator has received, if yes, return directly
        ThirdPartyMemberInfo info = thirdPartyMemberMapper.selectInfo(appId, unionId,
            ThirdPartyMemberType.WECHAT_PUBLIC_ACCOUNT.getType());
        String code = vCodeMapper.getAcquiredCode(activityId, info.getId());
        if (code != null) {
            return code;
        }
        // Get the VCode available for the specified activity
        List<String> availableCodes = vCodeMapper.getAvailableCode(activityId);
        if (CollUtil.isEmpty(availableCodes)) {
            return "finished";
        }
        code = availableCodes.get(RandomUtil.randomInt(0, availableCodes.size()));
        // Save pick up log
        ivCodeUsageService.createUsageRecord(info.getId(), info.getNickName(),
            VCodeUsageType.ACQUIRE.getType(), code);
        return code;
    }

    @Override
    public List<String> create(Long userId, VCodeCreateRo ro) {
        // Check expiration time
        ExceptionUtil.isTrue(ObjectUtil.isNull(ro.getExpireTime()) ||
            ro.getExpireTime().isAfter(LocalDateTime.now()), EXPIRE_TIME_INCORRECT);
        Long templateId = null;
        if (ro.getType().equals(VCodeType.REDEMPTION_CODE.getType())) {
            // The type is redemption code, and a redemption template must be selected
            ExceptionUtil.isNotNull(ro.getTemplateId(), TEMPLATE_EMPTY);
            // Check if the coupon template exists
            ivCodeCouponService.checkCouponIfExist(ro.getTemplateId());
            templateId = ro.getTemplateId();
        } else {
            // Type must be redemption code or official invitation code
            ExceptionUtil.isTrue(ro.getType().equals(VCodeType.OFFICIAL_INVITATION_CODE.getType()),
                TYPE_ERROR);
        }
        // Check if activity exists
        ivCodeActivityService.checkActivityIfExist(ro.getActivityId());
        // Check if the specified user exists
        Long assignUserId = null;
        if (StrUtil.isNotBlank(ro.getMobile())) {
            assignUserId = userMapper.selectIdByMobile(ro.getMobile());
            ExceptionUtil.isNotNull(assignUserId, ACCOUNT_NOT_REGISTER);
        }
        // Check the total number of VCode that can be used, and limit the number of uses by a single person
        ExceptionUtil.isTrue(ro.getAvailableTimes() != 0 && ro.getLimitTimes() != 0, CANNOT_ZERO);
        Integer remainTimes = ro.getAvailableTimes() > 0 ? ro.getAvailableTimes() : null;
        List<CodeEntity> entities = new ArrayList<>(ro.getCount());
        List<String> codes = new ArrayList<>(ro.getCount());
        List<String> allCode = new ArrayList<>();
        for (int i = 0; i < ro.getCount(); i++) {
            String code = this.getUniqueCodeBatch(allCode, ro.getType());
            CodeEntity entity = CodeEntity.builder()
                .id(IdWorker.getId())
                .type(ro.getType())
                .activityId(ro.getActivityId())
                .refId(templateId)
                .code(code)
                .availableTimes(ro.getAvailableTimes())
                .remainTimes(remainTimes)
                .limitTimes(ro.getLimitTimes())
                .expiredAt(ro.getExpireTime())
                .assignUserId(assignUserId)
                .createdBy(userId)
                .updatedBy(userId)
                .build();
            entities.add(entity);
            codes.add(code);
        }
        boolean flag = SqlHelper.retBool(vCodeMapper.insertList(entities));
        ExceptionUtil.isTrue(flag, DatabaseException.INSERT_ERROR);
        return codes;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void edit(Long userId, String code, VCodeUpdateRo ro) {
        log.info("Edit VCode setting. UserId:{}. VCode:{}. RO:{}", userId, code, ro);
        // Verify that VCode exists and is not a personal invitation code type
        Integer type = vCodeMapper.selectTypeByCode(code);
        ExceptionUtil.isNotNull(type, CODE_NOT_EXIST);
        ExceptionUtil.isFalse(VCodeType.PERSONAL_INVITATION_CODE.getType() == type, TYPE_ERROR);
        // Modify redemption template ID
        if (ObjectUtil.isNotNull(ro.getTemplateId())) {
            // Check if VCode is a voucher
            ExceptionUtil.isTrue(VCodeType.REDEMPTION_CODE.getType() == type, TYPE_INFO_ERROR);
            // Check if the coupon template exists
            ivCodeCouponService.checkCouponIfExist(ro.getTemplateId());
            boolean flag =
                SqlHelper.retBool(vCodeMapper.updateRefIdByCode(userId, code, ro.getTemplateId()));
            ExceptionUtil.isTrue(flag, DatabaseException.EDIT_ERROR);
        }
        // Modify the total available
        if (ObjectUtil.isNotNull(ro.getAvailableTimes())) {
            ExceptionUtil.isTrue(ro.getAvailableTimes() != 0, CANNOT_ZERO);
            Integer remainTimes = null;
            // If it is not set to infinite times, count the times used, and calculate the remaining times
            if (ro.getAvailableTimes() != -1) {
                int usageTimes = SqlTool.retCount(
                    vCodeUsageMapper.countByCodeAndType(code, VCodeUsageType.USE.getType(), null));
                remainTimes = ro.getAvailableTimes() - usageTimes;
            }
            boolean flag = SqlHelper.retBool(
                vCodeMapper.updateAvailableTimesByCode(userId, code, ro.getAvailableTimes(),
                    remainTimes));
            ExceptionUtil.isTrue(flag, DatabaseException.EDIT_ERROR);
        }
        // Modify the number of uses for a single person
        if (ObjectUtil.isNotNull(ro.getLimitTimes())) {
            ExceptionUtil.isTrue(ro.getLimitTimes() != 0, CANNOT_ZERO);
            boolean flag = SqlHelper.retBool(
                vCodeMapper.updateLimitTimesByCode(userId, code, ro.getLimitTimes()));
            ExceptionUtil.isTrue(flag, DatabaseException.EDIT_ERROR);
        }
        // Modify expiration time
        if (ObjectUtil.isNotNull(ro.getExpireTime())) {
            boolean flag = SqlHelper.retBool(
                vCodeMapper.updateExpiredAtByCode(userId, code, ro.getExpireTime()));
            ExceptionUtil.isTrue(flag, DatabaseException.EDIT_ERROR);
        }
    }

    @Override
    public void delete(Long userId, String code) {
        boolean flag = SqlHelper.retBool(vCodeMapper.removeByCode(userId, code));
        ExceptionUtil.isTrue(flag, DatabaseException.DELETE_ERROR);
    }

    @Override
    public void checkInviteCode(String inviteCode) {
        ExceptionUtil.isNotBlank(inviteCode, INVITE_CODE_NOT_EXIST);
        CodeEntity entity = vCodeMapper.selectByCode(inviteCode);
        // Check the validity of the invitation code
        ExceptionUtil.isNotNull(entity, INVITE_CODE_NOT_VALID);
        ExceptionUtil.isNull(entity.getAssignUserId(), INVITE_CODE_NOT_VALID);
        ExceptionUtil.isFalse(entity.getType().equals(VCodeType.REDEMPTION_CODE.getType()),
            INVITE_CODE_NOT_VALID);
        ExceptionUtil.isTrue(entity.getExpiredAt() == null ||
            entity.getExpiredAt().isAfter(LocalDateTime.now()), INVITE_CODE_EXPIRE);
        // Satisfy the total number of available use is unlimited, or the remaining number of times is enough
        ExceptionUtil.isTrue(entity.getAvailableTimes() < 0 || entity.getRemainTimes() > 0,
            INVITE_CODE_USED);
        // Official invitation code
        if (entity.getType().equals(VCodeType.OFFICIAL_INVITATION_CODE.getType())) {
            return;
        }
        // Cancellation cooling-off period / Invitation code for cancelled accounts is unavailable
        UserEntity user = userMapper.selectById(entity.getCreatedBy());
        ExceptionUtil.isNotNull(user, INVITE_CODE_NOT_VALID);
        ExceptionUtil.isFalse(user.getIsDeleted() || user.getIsPaused(), INVITE_CODE_NOT_VALID);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void useInviteCode(Long useUserId, String useUserName, String inviteCode) {
        // Update remaining availability
        this.updateRemainTimes(inviteCode, INVITE_CODE_NOT_VALID);
        // Save usage logs
        ivCodeUsageService.createUsageRecord(useUserId, useUserName, VCodeUsageType.USE.getType(),
            inviteCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createPersonalInviteCode(Long userId) {
        String code = this.getUniqueCodeBatch(VCodeType.PERSONAL_INVITATION_CODE.getType());
        CodeEntity entity = CodeEntity.builder()
            .id(IdWorker.getId())
            .type(VCodeType.PERSONAL_INVITATION_CODE.getType())
            .refId(userId)
            .code(code)
            .availableTimes(-1)
            .remainTimes(1)
            .limitTimes(1)
            .createdBy(userId)
            .updatedBy(userId)
            .build();
        boolean flag = SqlHelper.retBool(vCodeMapper.insert(entity));
        ExceptionUtil.isTrue(flag, DatabaseException.INSERT_ERROR);
    }

    @Override
    public void checkRedemptionCode(Long userId, String redemptionCode) {
        log.info("Check redemption code，userId:{}，redemption code:{}", userId, redemptionCode);
        ExceptionUtil.isNotBlank(redemptionCode, REDEMPTION_CODE_NOT_EXIST);
        // Verify the validity of the redemption code
        CodeEntity entity = vCodeMapper.selectByCode(redemptionCode);
        ExceptionUtil.isNotNull(entity, REDEMPTION_CODE_NOT_VALID);
        ExceptionUtil.isTrue(entity.getType().equals(VCodeType.REDEMPTION_CODE.getType()),
            REDEMPTION_CODE_NOT_VALID);
        // Verify that the redemption code has no designated user, or is being used by the designated user
        ExceptionUtil.isTrue(entity.getAssignUserId() == null ||
            entity.getAssignUserId().equals(userId), REDEMPTION_CODE_NOT_VALID);
        // Check valid time
        ExceptionUtil.isTrue(entity.getExpiredAt() == null ||
            entity.getExpiredAt().isAfter(LocalDateTime.now()), REDEMPTION_CODE_EXPIRE);
        // Satisfy the total number of available use is unlimited, or the remaining number of times is enough
        ExceptionUtil.isTrue(entity.getAvailableTimes() < 0 || entity.getRemainTimes() > 0,
            INVITE_CODE_USED);
        // When the number of times of limited use by a single person is limited,
        // determine whether the total number of redemption codes used by the current user does not exceed the number of times limited by a single person
        if (entity.getLimitTimes() > 0) {
            int useCount = SqlTool.retCount(
                vCodeUsageMapper.countByCodeAndType(redemptionCode, VCodeUsageType.USE.getType(),
                    userId));
            ExceptionUtil.isTrue(entity.getLimitTimes() > useCount, REDEMPTION_CODE_USED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer useRedemptionCode(Long userId, String redemptionCode) {
        log.info("Use redemption code. userId:{}. redemption code:{}", userId, redemptionCode);
        // Update remaining availability
        this.updateRemainTimes(redemptionCode, REDEMPTION_CODE_NOT_VALID);
        // Save usage logs
        ivCodeUsageService.createUsageRecord(userId, userMapper.selectNickNameById(userId),
            VCodeUsageType.USE.getType(), redemptionCode);
        // Query the number of V coins exchanged for the exchange code
        Integer integral = vCodeMapper.selectIntegral(redemptionCode);
        ExceptionUtil.isNotNull(integral, REDEMPTION_CODE_NOT_VALID);
        // Exchange VCode
        iIntegralService.alterIntegral(REDEMPTION_CODE, IntegralAlterType.INCOME, integral, userId,
            JSONUtil.createObj());
        return integral;
    }

    private void updateRemainTimes(String code, VCodeException exception) {
        Integer times = vCodeMapper.selectAvailableTimesByCode(code);
        ExceptionUtil.isNotNull(times, exception);
        // The total number of uses is unlimited, and the remaining times do not need to be changed
        if (times < 0) {
            return;
        }
        vCodeMapper.subRemainTimes(code);
    }

    private String getUniqueCodeBatch(List<String> codes, Integer type) {
        String code;
        do {
            code = this.getUniqueCodeBatch(type);
        } while (codes.contains(code));
        codes.add(code);
        return code;
    }

    /**
     * Get unique VCode
     */
    private String getUniqueCodeBatch(Integer type) {
        // Determine whether it is a redemption code. If it is, use uppercase and lowercase plus a digital random code,
        // otherwise use a pure digital random code.
        boolean isRedemptionCode = type.equals(VCodeType.REDEMPTION_CODE.getType());
        String code;
        boolean exit;
        int length = 8;
        do {
            code = isRedemptionCode ? RandomExtendUtil.randomStringLowerCase(length) :
                RandomExtendUtil.randomNumbers(length);
            exit = SqlTool.retCount(vCodeMapper.countByCode(code)) > 0;
        } while (exit);
        return code;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rewardForUserUpdateMobile(Long userId, String nickName) {
        // Get the invitation code when registering
        VCodeDTO vCodeDTO = ivCodeUsageService.getInvitorUserId(userId);
        if (vCodeDTO == null) {
            return;
        }
        // Judge the invitation code type
        boolean isPersonal =
            vCodeDTO.getType().equals(VCodeType.PERSONAL_INVITATION_CODE.getType());
        String actionCode = isPersonal ? IntegralActionCodeConstants.BE_INVITED_TO_REWARD
            : IntegralActionCodeConstants.OFFICIAL_INVITATION_REWARD;
        // Each user can only enjoy one point reward
        int historyNum = iIntegralService.getCountByUserIdAndActionCode(userId, actionCode);
        if (historyNum >= 1) {
            return;
        }
        // Personal invitation code reward
        if (isPersonal) {
            iIntegralService.personalInvitedReward(userId, nickName, vCodeDTO.getUserId());
            return;
        }
        // Official invitation code award
        iIntegralService.officialInvitedReward(userId);
    }
}
