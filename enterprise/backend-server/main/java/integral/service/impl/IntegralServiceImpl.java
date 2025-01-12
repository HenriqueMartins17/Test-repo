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

package com.apitable.enterprise.integral.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

import jakarta.annotation.Resource;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import com.apitable.core.exception.BusinessException;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.core.util.SqlTool;
import com.apitable.enterprise.vikabilling.enums.BillingException;
import com.apitable.enterprise.integral.entity.IntegralHistoryEntity;
import com.apitable.enterprise.integral.enums.IntegralAlterType;
import com.apitable.enterprise.integral.mapper.IntegralHistoryMapper;
import com.apitable.enterprise.integral.service.IIntegralService;
import com.apitable.enterprise.integral.setting.IntegralConfigLoader;
import com.apitable.enterprise.integral.setting.IntegralRule;
import com.apitable.enterprise.vcode.enums.VCodeException;
import com.apitable.enterprise.vcode.enums.VCodeType;
import com.apitable.enterprise.vcode.service.IVCodeService;
import com.apitable.enterprise.vika.core.VikaOperations;
import com.apitable.enterprise.vika.core.model.IntegralRewardInfo;
import com.apitable.shared.cache.bean.LoginUserDto;
import com.apitable.shared.cache.service.LoginUserCacheService;
import com.apitable.shared.component.TaskManager;
import com.apitable.shared.component.notification.NotificationManager;
import com.apitable.shared.component.notification.NotificationTemplateId;
import com.apitable.shared.constants.IntegralActionCodeConstants;
import com.apitable.shared.sysconfig.wizard.PlayerConfigLoader;
import com.apitable.shared.sysconfig.wizard.Wizard;
import com.apitable.user.entity.UserEntity;
import com.apitable.user.service.IUserService;
import com.apitable.user.vo.IntegralRecordVO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.apitable.core.constants.RedisConstants.GENERAL_LOCKED;
import static com.apitable.enterprise.integral.cache.RedisKey.getInviteHistoryKey;
import static com.apitable.shared.constants.IntegralActionCodeConstants.WALLET_ACTIVITY_REWARD;
import static com.apitable.shared.constants.NotificationConstants.ACTION_NAME;
import static com.apitable.shared.constants.NotificationConstants.ACTIVITY_NAME;
import static com.apitable.shared.constants.NotificationConstants.COUNT;

@Service
@Slf4j
public class IntegralServiceImpl implements IIntegralService {

    @Resource
    private IntegralHistoryMapper integralHistoryMapper;

    @Resource
    private IUserService iUserService;

    @Resource
    private IVCodeService ivCodeService;

    @Autowired(required = false)
    private VikaOperations vikaOperations;

    @Resource
    private RedisLockRegistry redisLockRegistry;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private LoginUserCacheService loginUserCacheService;

    @Override
    public int getTotalIntegralValueByUserId(Long userId) {
        return SqlTool.retCount(integralHistoryMapper.selectTotalIntegralValueByUserId(userId));
    }

    @Override
    public IPage<IntegralRecordVO> getIntegralRecordPageByUserId(Page<IntegralRecordVO> page,
                                                                 Long userId) {
        return integralHistoryMapper.selectPageByUserId(page, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void trigger(String action, IntegralAlterType alterType, Long by, JSONObject parameter) {
        log.info("trigger integral operation：{}", action);
        log.info("============ init user integral reward lock ============");
        Lock lock = redisLockRegistry.obtain(by.toString());
        try {
            log.info("============ try get user integral reward lock ============");
            // get lock return true. if failure, waiting 2 seconds. if timeout, return false.
            if (lock.tryLock(100, TimeUnit.MILLISECONDS)) {
                try {
                    log.info("============ lock in successfully ============");
                    // find the original integral value of the trigger
                    int beforeTotalIntegralValue = getTotalIntegralValueByUserId(by);
                    // find integral rule
                    IntegralRule rule = IntegralConfigLoader.getConfig().getRule().get(action);
                    // change integral value
                    int alterIntegralValue = rule.getIntegralValue();
                    // record integral value
                    this.createHistory(by, action, alterType, beforeTotalIntegralValue,
                        alterIntegralValue, parameter);
                    if (rule.isNotify()) {
                        TaskManager.me().execute(() -> NotificationManager.me()
                            .playerNotify(NotificationTemplateId.INTEGRAL_INCOME_NOTIFY,
                                Collections.singletonList(by),
                                0L, null, Dict.create().set(COUNT, rule.getIntegralValue())
                                    .set(ACTION_NAME, rule.getActionName())));
                    }
                } catch (Exception e) {
                    log.error("failed to reward the user integral", e);
                    throw e;
                } finally {
                    log.info("============ unlock ============");
                    lock.unlock();
                }
            } else {
                log.error("the user operates integral are too frequent");
                throw new BusinessException(BillingException.ACCOUNT_CREDIT_ALTER_FREQUENTLY);
            }
        } catch (InterruptedException e) {
            log.error("users operates integral are too frequent", e);
            // acquire lock was interrupted
            throw new BusinessException(BillingException.ACCOUNT_CREDIT_ALTER_FREQUENTLY);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void alterIntegral(String actionCode, IntegralAlterType alterType, int alterIntegral,
                              Long by, JSONObject parameter) {
        log.info("Change integral");
        log.info("============ init user integral lock ============");
        Lock lock = redisLockRegistry.obtain(by.toString());
        try {
            // get lock return true. if failure, waiting 2 seconds. if timeout, return false.
            log.info("============ try get user integral lock ============");
            if (lock.tryLock(100, TimeUnit.MILLISECONDS)) {
                try {
                    log.info("============ get user integral lock ============");
                    // find the original integral value of the trigger
                    int beforeTotalIntegralValue = getTotalIntegralValueByUserId(by);
                    // record integral value
                    this.createHistory(by, actionCode, alterType, beforeTotalIntegralValue,
                        alterIntegral, parameter);
                } catch (Exception e) {
                    log.error("failed to reward the user「{}」 integral", by, e);
                    throw e;
                } finally {
                    log.info("============ unlocking ============");
                    lock.unlock();
                }
            } else {
                log.error("User「{}」operate integral too frequently", by);
                throw new BusinessException(BillingException.ACCOUNT_CREDIT_ALTER_FREQUENTLY);
            }
        } catch (InterruptedException e) {
            log.error("User「{}」operate integral too frequently", by, e);
            // acquire lock was interrupted
            throw new BusinessException(BillingException.ACCOUNT_CREDIT_ALTER_FREQUENTLY);
        }
    }

    @Override
    public Long createHistory(Long userId, String actionCode, IntegralAlterType alterType,
                              Integer oldIntegralValue, Integer alterIntegralValue,
                              JSONObject parameter) {
        // create integral, stands for calculating user integral.
        IntegralHistoryEntity historyEntity = new IntegralHistoryEntity();
        historyEntity.setUserId(userId);
        historyEntity.setActionCode(actionCode);
        historyEntity.setAlterType(alterType.getState());
        historyEntity.setOriginIntegral(oldIntegralValue);
        historyEntity.setAlterIntegral(alterIntegralValue);
        historyEntity.setTotalIntegral(
            determineIntegralValue(alterType, oldIntegralValue, alterIntegralValue));
        historyEntity.setParameter(parameter.toString());
        historyEntity.setCreatedBy(userId);
        historyEntity.setUpdatedBy(userId);
        integralHistoryMapper.insert(historyEntity);
        return historyEntity.getId();
    }

    @Override
    public int getCountByUserIdAndActionCode(Long userId, String actionCode) {
        return SqlTool.retCount(
            integralHistoryMapper.selectCountByUserIdAndActionCode(userId, actionCode));
    }

    @Override
    public boolean checkByUserIdAndActionCodes(Long userId, Collection<String> actionCodes) {
        return SqlTool.retCount(
            integralHistoryMapper.selectCountByUserIdAndActionCodes(userId, actionCodes)) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void activityReward(String processor) {
        String reward =
            "https://integration.vika.ltd,usk8qo1Dk9PbecBlaqFIvbb,dst9qAYY0ud1E3Av4f,viwPFbZIozOUs";
        String[] split = reward.split(",");
        List<IntegralRewardInfo> rewardInfos =
            vikaOperations.getIntegralRewardInfo(split[0], split[1], split[2], split[3]);
        if (rewardInfos.isEmpty()) {
            throw new BusinessException("There are no records that meet the conditions.");
        }
        // collect different types of method targets
        Map<String, IntegralRewardInfo> emailTargetMap = new HashMap<>();
        Map<String, List<IntegralRewardInfo>> areaCodeToInfosMap = new HashMap<>();
        for (IntegralRewardInfo info : rewardInfos) {
            // target is email address
            if (Validator.isEmail(info.getTarget())) {
                emailTargetMap.put(info.getTarget(), info);
                continue;
            }
            // target is phone number. if area code is null, info is lack.
            if (info.getAreaCode() == null) {
                vikaOperations.updateIntegralRewardResult(split[0], split[1], split[2],
                    info.getRecordId(), "Incorrect information filled in", processor);
                continue;
            }
            // group by area code.
            if (areaCodeToInfosMap.containsKey(info.getAreaCode())) {
                areaCodeToInfosMap.get(info.getAreaCode()).add(info);
            } else {
                List<IntegralRewardInfo> targets = new ArrayList<>();
                targets.add(info);
                areaCodeToInfosMap.put(info.getAreaCode(), targets);
            }
        }
        // deliver integral
        if (!emailTargetMap.isEmpty()) {
            List<UserEntity> userEntities = iUserService.getByEmails(emailTargetMap.keySet());
            Map<String, Long> emailToUserIdMap = userEntities.stream()
                .collect(Collectors.toMap(UserEntity::getEmail, UserEntity::getId));
            for (Entry<String, IntegralRewardInfo> entry : emailTargetMap.entrySet()) {
                // deliver integral
                this.deliver(entry.getValue(), emailToUserIdMap, processor, split);
            }
        }
        for (Entry<String, List<IntegralRewardInfo>> entry : areaCodeToInfosMap.entrySet()) {
            List<String> mobilePhones = entry.getValue().stream().map(IntegralRewardInfo::getTarget)
                .collect(Collectors.toList());
            List<UserEntity> userEntities =
                iUserService.getByCodeAndMobilePhones(entry.getKey(), mobilePhones);
            Map<String, Long> mobileToUserIdMap = userEntities.stream()
                .collect(Collectors.toMap(UserEntity::getMobilePhone, UserEntity::getId));
            for (IntegralRewardInfo info : entry.getValue()) {
                // deliver integral
                this.deliver(info, mobileToUserIdMap, processor, split);
            }
        }
    }

    private void deliver(IntegralRewardInfo info, Map<String, Long> targetToUserIdMap,
                         String processor, String[] split) {
        // the email user does not exist
        if (!targetToUserIdMap.containsKey(info.getTarget())) {
            vikaOperations.updateIntegralRewardResult(split[0], split[1], split[2],
                info.getRecordId(), "not to deliver，because the account is not registered.",
                processor);
            return;
        }
        Long userId = targetToUserIdMap.get(info.getTarget());
        this.alterIntegral(WALLET_ACTIVITY_REWARD, IntegralAlterType.INCOME, info.getCount(),
            userId, JSONUtil.createObj().set("name", info.getActivityName()));
        try {
            vikaOperations.updateIntegralRewardResult(split[0], split[1], split[2],
                info.getRecordId(), "delivered", processor);
        } catch (Exception e) {
            throw new BusinessException(
                StrUtil.format("「{}」integral was delivered，rewrite datasheet failure. msg:{}",
                    info.getTarget(), e.getMessage()));
        }
        // send notification
        TaskManager.me().execute(() -> NotificationManager.me()
            .playerNotify(NotificationTemplateId.ACTIVITY_INTEGRAL_INCOME_NOTIFY,
                Collections.singletonList(userId), 0L, null,
                Dict.create().set(COUNT, info.getCount())
                    .set(ACTIVITY_NAME, info.getActivityName())));
    }

    /**
     * calculate integral by alter type.
     *
     * @param alterType          alterType（INCOME、EXPENSES）
     * @param oldIntegralValue   oldIntegralValue
     * @param alterIntegralValue alterIntegralValue
     * @return the calculated integral value
     */
    private Integer determineIntegralValue(IntegralAlterType alterType, Integer oldIntegralValue,
                                           Integer alterIntegralValue) {
        if (alterType == IntegralAlterType.INCOME) {
            // type income，increase the integral
            return oldIntegralValue + alterIntegralValue;
        }
        if (alterType == IntegralAlterType.EXPENSES) {
            // type expense，deducting the integral
            int integral = oldIntegralValue - alterIntegralValue;
            if (integral < 0) {
                throw new BusinessException("Not enough integral.");
            }
            return integral;
        }
        throw new IllegalArgumentException(
            "Unknown Integral Alter Type, Please Check Your Code....");
    }

    @Override
    public void officialInvitedReward(Long registerUserId) {
        String officialRewardActionCode = IntegralActionCodeConstants.OFFICIAL_INVITATION_REWARD;
        IntegralRule beInvitedRewardIntegralRule =
            IntegralConfigLoader.getConfig().getRule().get(officialRewardActionCode);
        int beInvitedRewardIntegralValue = beInvitedRewardIntegralRule.getIntegralValue();
        int beInvitorBeforeIntegralValue = getTotalIntegralValueByUserId(registerUserId);
        // new user create points record
        createHistory(registerUserId, officialRewardActionCode, IntegralAlterType.INCOME,
            beInvitorBeforeIntegralValue, beInvitedRewardIntegralValue, JSONUtil.createObj());
        // send notification
        if (beInvitedRewardIntegralRule.isNotify()) {
            TaskManager.me().execute(() -> NotificationManager.me()
                .playerNotify(NotificationTemplateId.INTEGRAL_INCOME_NOTIFY,
                    Collections.singletonList(registerUserId), 0L, null,
                    Dict.create().set(COUNT, beInvitedRewardIntegralRule.getIntegralValue())
                        .set(ACTION_NAME, beInvitedRewardIntegralRule.getActionName())));
        }
    }

    @Override
    public void personalInvitedReward(Long registerUserId, String registerUserName,
                                      Long inviteUserId) {
        // To apply bonus points to the inviter, first lock the current user points changes
        Lock lock = redisLockRegistry.obtain(inviteUserId.toString());
        try {
            if (lock.tryLock(100, TimeUnit.MILLISECONDS)) {
                try {
                    // ============ Invited to register reward point value ================
                    IntegralRule beInvitedRewardIntegralRule =
                        IntegralConfigLoader.getConfig().getRule()
                            .get(IntegralActionCodeConstants.BE_INVITED_TO_REWARD);
                    int beInvitedRewardIntegralValue =
                        beInvitedRewardIntegralRule.getIntegralValue();
                    // new user create points record
                    String invitorName = iUserService.getNicknameByUserId(inviteUserId);
                    int beInvitorBeforeIntegralValue =
                        getTotalIntegralValueByUserId(registerUserId);
                    createHistory(registerUserId, IntegralActionCodeConstants.BE_INVITED_TO_REWARD,
                        IntegralAlterType.INCOME,
                        beInvitorBeforeIntegralValue, beInvitedRewardIntegralValue,
                        JSONUtil.createObj().putOnce("name", invitorName));
                    // send notification
                    if (beInvitedRewardIntegralRule.isNotify()) {
                        TaskManager.me().execute(() -> NotificationManager.me()
                            .playerNotify(NotificationTemplateId.INTEGRAL_INCOME_NOTIFY,
                                Collections.singletonList(registerUserId), 0L, null,
                                Dict.create()
                                    .set(COUNT, beInvitedRewardIntegralRule.getIntegralValue())
                                    .set(ACTION_NAME,
                                        beInvitedRewardIntegralRule.getActionName())));
                    }

                    // ============ Reward code owner =============
                    IntegralRule inviteRewardIntegralRule =
                        IntegralConfigLoader.getConfig().getRule()
                            .get(IntegralActionCodeConstants.INVITATION_REWARD);
                    // mobile phone registration to get reward multiples
                    int inviteRewardIntegralValue = inviteRewardIntegralRule.getIntegralValue();
                    int invitorBeforeIntegralValue = getTotalIntegralValueByUserId(inviteUserId);
                    // create points record
                    String inviteUserName = registerUserName != null ? registerUserName :
                        iUserService.getNicknameByUserId(registerUserId);
                    Long recordId =
                        createHistory(inviteUserId, IntegralActionCodeConstants.INVITATION_REWARD,
                            IntegralAlterType.INCOME,
                            invitorBeforeIntegralValue, inviteRewardIntegralValue,
                            JSONUtil.createObj().putOnce("userId", registerUserId)
                                .putOnce("name", inviteUserName));
                    // send notification
                    if (inviteRewardIntegralRule.isNotify()) {
                        TaskManager.me().execute(() -> NotificationManager.me()
                            .playerNotify(NotificationTemplateId.INTEGRAL_INCOME_NOTIFY,
                                Collections.singletonList(inviteUserId), 0L, null,
                                Dict.create().set(COUNT, inviteRewardIntegralValue)
                                    .set(ACTION_NAME, inviteRewardIntegralRule.getActionName())));
                    }
                    // Temporarily record the invited new user, and change the name across connections
                    String key = getInviteHistoryKey(registerUserId.toString());
                    redisTemplate.opsForValue().set(key, recordId, 1, TimeUnit.HOURS);
                } catch (Exception e) {
                    // if the business fails throw an exception directly
                    log.error("Invitation code registration reward failed", e);
                    throw new BusinessException(VCodeException.INVITE_CODE_REWARD_ERROR);
                } finally {
                    // Unlock the points lock of the user who owns the invitation code
                    lock.unlock();
                }
            } else {
                // The registration operation is too frequent, please try again later
                log.error("The registration operation is too frequent, please try again later");
                throw new BusinessException(VCodeException.INVITE_CODE_FREQUENTLY);
            }
        } catch (InterruptedException e) {
            // Interrupted, return failure message
            log.error("Invitation code registration reward failed", e);
            throw new BusinessException(VCodeException.INVITE_CODE_REWARD_ERROR);
        }
    }

    @Override
    public void useInviteCodeReward(Long userId, String inviteCode) {
        // Query the user's invitation code, and determine that your own invitation code is not available
        String userInviteCode = ivCodeService.getUserInviteCode(userId);
        ExceptionUtil.isFalse(inviteCode.equals(userInviteCode),
            VCodeException.INVITE_CODE_NOT_VALID);
        // Users have not used invitation rewards
        boolean usedInviteReward = checkByUserIdAndActionCodes(userId,
            CollectionUtil.newArrayList(IntegralActionCodeConstants.BE_INVITED_TO_REWARD,
                IntegralActionCodeConstants.OFFICIAL_INVITATION_REWARD));
        ExceptionUtil.isFalse(usedInviteReward, VCodeException.INVITE_CODE_REWARD_ERROR);
        // Load user information
        LoginUserDto userDto = loginUserCacheService.getLoginUser(userId);
        // Save the use record of invitation code
        ivCodeService.useInviteCode(userId, userDto.getNickName(), inviteCode);
        // Query the user of the invitation code. If it does not exist, it represents the official invitation code
        Long inviteUserId = ivCodeService.getRefIdByCodeAndType(inviteCode,
            VCodeType.PERSONAL_INVITATION_CODE.getType());
        if (inviteUserId == null) {
            // Non personal invitation code, official invitation code
            officialInvitedReward(userId);
            return;
        }
        personalInvitedReward(userId, userDto.getNickName(), inviteUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rewardWizard(Long userId, String wizardId) {
        // Determine whether to trigger the novice guide reward
        Wizard wizard = PlayerConfigLoader.getConfig().getWizard().get(wizardId);
        if (wizard == null || wizard.getIntegralAction() == null) {
            return;
        }
        // Avoid concurrent requests causing multiple rewards
        String lockKey = StrUtil.format(GENERAL_LOCKED, "user:wizard:award",
            StrUtil.format("{}-{}", userId, wizardId));
        BoundValueOperations<String, Object> ops = redisTemplate.boundValueOps(lockKey);
        Boolean result = ops.setIfAbsent("", 10, TimeUnit.SECONDS);
        if (BooleanUtil.isFalse(result)) {
            return;
        }
        // Send rewards only after the first trigger
        String key = "wizardId";
        int count = SqlTool.retCount(
            integralHistoryMapper.selectCountByUserIdAndKeyValue(userId, key, wizardId));
        if (count > 0) {
            return;
        }
        trigger(wizard.getIntegralAction(), IntegralAlterType.INCOME, userId,
            JSONUtil.createObj().putOnce(key, wizardId));
    }

    @Override
    public void updateInvitationUserNickNameInParams(Long userId, String nickName) {
        // If it is an invitation to reward, modify the user's name
        String key = getInviteHistoryKey(userId.toString());
        if (BooleanUtil.isTrue(redisTemplate.hasKey(key))) {
            Long recordId = Long.parseLong(StrUtil.toString(redisTemplate.opsForValue().get(key)));
            integralHistoryMapper.updateParameterById(recordId,
                JSONUtil.createObj().putOnce("userId", userId).putOnce("name", nickName)
                    .toString());
            redisTemplate.delete(key);
        }
    }
}
