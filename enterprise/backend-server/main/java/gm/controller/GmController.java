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

package com.apitable.enterprise.gm.controller;

import static com.apitable.base.enums.DatabaseException.EDIT_ERROR;
import static com.apitable.core.constants.RedisConstants.ERROR_PWD_NUM_DIR;
import static com.apitable.shared.constants.IntegralActionCodeConstants.OFFICIAL_ADJUSTMENT;
import static com.apitable.shared.constants.NotificationConstants.BODY_EXTRAS;
import static com.apitable.shared.constants.NotificationConstants.EXPIRE_AT;
import static com.apitable.space.enums.SpaceException.UPDATE_SPACE_INFO_FAIL;
import static com.apitable.user.enums.UserException.USER_NOT_EXIST;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.apitable.base.enums.EmailCodeType;
import com.apitable.base.enums.ParameterException;
import com.apitable.base.enums.SmsCodeType;
import com.apitable.core.constants.RedisConstants;
import com.apitable.core.exception.BusinessException;
import com.apitable.core.support.ResponseData;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.enterprise.gm.enums.GmAction;
import com.apitable.enterprise.gm.ro.ConfigDatasheetRo;
import com.apitable.enterprise.gm.ro.HqAddUserRo;
import com.apitable.enterprise.gm.ro.QueryUserInfoRo;
import com.apitable.enterprise.gm.ro.SpaceCertificationRo;
import com.apitable.enterprise.gm.ro.UnlockRo;
import com.apitable.enterprise.gm.ro.UserActivityAssignRo;
import com.apitable.enterprise.gm.ro.UserActivityRo;
import com.apitable.enterprise.gm.service.IGmService;
import com.apitable.enterprise.gm.vo.HqAddUserVo;
import com.apitable.enterprise.idaas.model.IdaasAppBindRo;
import com.apitable.enterprise.idaas.model.IdaasAppBindVo;
import com.apitable.enterprise.idaas.model.IdaasTenantCreateRo;
import com.apitable.enterprise.idaas.model.IdaasTenantCreateVo;
import com.apitable.enterprise.idaas.service.IIdaasAppBindService;
import com.apitable.enterprise.idaas.service.IIdaasTenantService;
import com.apitable.enterprise.integral.enums.IntegralAlterType;
import com.apitable.enterprise.integral.ro.IntegralDeductRo;
import com.apitable.enterprise.integral.service.IIntegralService;
import com.apitable.player.mapper.PlayerActivityMapper;
import com.apitable.player.ro.NotificationCreateRo;
import com.apitable.player.ro.NotificationRevokeRo;
import com.apitable.player.service.IPlayerNotificationService;
import com.apitable.shared.captcha.CodeValidateScope;
import com.apitable.shared.captcha.ValidateCode;
import com.apitable.shared.captcha.ValidateCodeRepository;
import com.apitable.shared.captcha.ValidateCodeType;
import com.apitable.shared.captcha.ValidateTarget;
import com.apitable.shared.component.notification.INotificationFactory;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.GetResource;
import com.apitable.shared.component.scanner.annotation.PostResource;
import com.apitable.shared.context.LoginContext;
import com.apitable.shared.context.SessionContext;
import com.apitable.shared.sysconfig.notification.NotificationTemplate;
import com.apitable.space.enums.SpaceCertification;
import com.apitable.user.mapper.UserMapper;
import com.apitable.user.service.IUserService;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * Used for GM command in vika-cli command line tool.
 * </p>
 */
@RestController
@Tag(name = "Cli Office GM API")
@ApiResource
@Slf4j
public class GmController {

    @Resource
    private IGmService iGmService;

    @Resource
    private UserMapper userMapper;

    @Resource
    private PlayerActivityMapper playerActivityMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private IPlayerNotificationService playerNotificationService;

    @Resource
    private ValidateCodeRepository validateCodeRepository;

    @Resource
    private INotificationFactory notificationFactory;

    @Resource
    private IIdaasTenantService idaasTenantService;

    @Resource
    private IIdaasAppBindService idaasAppBindService;

    @Resource
    private IUserService iUserService;

    @Resource
    private IIntegralService iIntegralService;

    private final String testMobilePre = "1340000";

    /**
     * Update GM permission config.
     */
    @PostResource(path = "/gm/permission/update", requiredPermission = false)
    @Operation(summary = "Update GM permission config")
    public ResponseData<Void> updatePermission(@RequestBody ConfigDatasheetRo ro) {
        iGmService.updateGmPermissionConfig(SessionContext.getUserId(), ro.getDatasheetId());
        return ResponseData.success();
    }

    /**
     * Create user.
     */
    @PostResource(path = "/gm/new/user", requiredPermission = false)
    @Operation(summary = "Create user(Irregular vest number, used for testing)", description =
        "create a user by username and password.")
    public ResponseData<HqAddUserVo> createUser(@RequestBody @Valid HqAddUserRo ro) {
        // Limit mailbox range.
        if (!ro.getUsername().endsWith("@vikadata.com") || !ro.getUsername().startsWith("test")) {
            throw new BusinessException(
                "Please use the [@vikadata.com] test email starting with [test]! Such as: "
                    + "test001@vikadata.com");
        }
        // Limit phone number range.
        if (StrUtil.isNotBlank(ro.getPhone()) && !ro.getPhone().startsWith(testMobilePre)) {
            throw new BusinessException("Test phone number please begin with " + testMobilePre);
        }
        // Create a user.
        iGmService.createUserByCli(ro.getUsername(), ro.getPassword(), ro.getPhone());
        HqAddUserVo vo = new HqAddUserVo();
        vo.setUsername(ro.getUsername());
        vo.setPassword(ro.getPassword());
        vo.setPhone(ro.getPhone());
        return ResponseData.success(vo);
    }

    /**
     * Batch Create user.
     */
    @PostResource(path = "/gm/new/users", requiredLogin = false)
    @Operation(summary = "Batch Create user(Irregular vest number, used for testing)")
    public ResponseData<Void> createUsers() {
        // Create a user
        iGmService.createUsersByCli();
        return ResponseData.success();
    }

    /**
     * Lock verification.
     */
    @PostResource(path = "/gm/lock", requiredPermission = false)
    @Operation(summary = "Lock verification")
    public ResponseData<Void> lock(@RequestBody @Valid UnlockRo ro) {
        log.info("The operator [{}] lock type [{}] verification for target [{}].",
            SessionContext.getUserId(), ro.getTarget(), ro.getType());
        // Verify permissions
        iGmService.validPermission(SessionContext.getUserId(), GmAction.VALIDATION_LOCK);
        Integer type = ro.getType();
        String lockedKey = this.getLockedKey(ro.getTarget(), type);
        redisTemplate.opsForValue().set(lockedKey, 5, type == 1 ? 1 : 20, TimeUnit.MINUTES);
        return ResponseData.success();
    }

    /**
     * Unlock verification.
     */
    @PostResource(path = "/gm/unlock", requiredPermission = false)
    @Operation(summary = "Unlock verification")
    public ResponseData<Void> unlock(@RequestBody @Valid UnlockRo ro) {
        log.info("The operator [{}] unlock type [{}] verification for target [{}]",
            SessionContext.getUserId(), ro.getTarget(), ro.getType());
        // Verify permissions
        iGmService.validPermission(SessionContext.getUserId(), GmAction.VALIDATION_UNLOCK);
        String lockedKey = this.getLockedKey(ro.getTarget(), ro.getType());
        redisTemplate.delete(lockedKey);
        return ResponseData.success();
    }

    private String getLockedKey(String target, Integer type) {
        String lockedKey;
        switch (type) {
            case 0:
                Long userId = userMapper.selectIdByMobile(target);
                ExceptionUtil.isNotNull(userId, USER_NOT_EXIST);
                lockedKey = ERROR_PWD_NUM_DIR + userId;
                break;
            case 1:
                lockedKey = RedisConstants.getSendCaptchaRateKey(
                    ValidateTarget.create(target).getIntactTarget());
                break;
            default:
                lockedKey =
                    RedisConstants.getLockedKey(ValidateTarget.create(target).getIntactTarget());
                break;
        }
        return lockedKey;
    }

    /**
     * Reset the active state of the user.
     */
    @PostResource(path = "/gm/reset/activity", requiredPermission = false)
    @Operation(summary = "Reset the active state of the user")
    public ResponseData<Void> resetActivity(@RequestBody(required = false) UserActivityRo ro) {
        Long userId = SessionContext.getUserId();
        if (ro != null && ro.getWizardId() != null) {
            // Deletes the specified active state value
            String key = StrUtil.format("\"{}\"", ro.getWizardId());
            playerActivityMapper.updateActionsRemoveByUserId(userId, key);
        } else {
            // Reset all active state records
            playerActivityMapper.updateActionsByUserId(userId, new JSONObject().toString());
        }
        return ResponseData.success();
    }

    /**
     * Specifies the active state of the user.
     */
    @PostResource(path = "/gm/assign/activity", requiredPermission = false)
    @Operation(summary = "Specifies the active state of the user")
    public ResponseData<Void> assignActivity(@RequestBody UserActivityAssignRo ro) {
        log.info("The operator「{}」specifies the active state of the user [{}/{}].",
            SessionContext.getUserId(), ro.getTestMobile(), ro.getUserIds());
        // Verify permissions.
        iGmService.validPermission(SessionContext.getUserId(), GmAction.USER_ACTIVITY_ASSIGN);
        // Verify parameters.
        ExceptionUtil.isTrue(ro.getWizardId() != null && ro.getValue() != null,
            ParameterException.INCORRECT_ARG);
        String key = StrUtil.format("\"{}\"", ro.getWizardId());
        // The test phone number is preferred.
        if (ro.getTestMobile() != null) {
            Long userId = userMapper.selectIdByMobile(ro.getTestMobile());
            ExceptionUtil.isNotNull(userId, USER_NOT_EXIST);
            boolean flag = SqlHelper.retBool(
                playerActivityMapper.updateActionsByJsonSet(Collections.singletonList(userId), key,
                    ro.getValue()));
            ExceptionUtil.isTrue(flag, EDIT_ERROR);
        } else {
            ExceptionUtil.isNotEmpty(ro.getUserIds(), ParameterException.INCORRECT_ARG);
            // Partial insert
            List<List<Long>> split = CollUtil.split(ro.getUserIds(), 100);
            for (List<Long> userIds : split) {
                playerActivityMapper.updateActionsByJsonSet(userIds, key, ro.getValue());
            }
        }
        return ResponseData.success();
    }

    /**
     * Create a player notification.
     */
    @PostResource(path = "/gm/new/player/notify", requiredPermission = false)
    @Operation(summary = "Create a player notification", description = "Adding system "
        + "notification.")
    public ResponseData<Void> addPlayerNotify(@RequestBody @Valid NotificationCreateRo ro) {
        log.info("The operator「{}」issue a system notification ", SessionContext.getUserId());
        // Verify permissions.
        iGmService.validPermission(SessionContext.getUserId(),
            GmAction.SYSTEM_NOTIFICATION_PUBLISH);
        NotificationTemplate template =
            notificationFactory.getTemplateById(ro.getTemplateId());
        if (ObjectUtil.isNull(template)) {
            throw new BusinessException("The template id does not exist");
        }
        // Currently, only system notifications can be revoked
        if (!"system".equals(template.getNotificationsType())) {
            throw new BusinessException("Adding non-system messages is not currently supported");
        }
        String lockedKey = RedisConstants.getNotificationLockedKey(ro.getTemplateId(), "");
        Object extras = ro.getBody().get(BODY_EXTRAS);
        if (ObjectUtil.isNotNull(ro.getExpireAt())) {
            lockedKey =
                RedisConstants.getNotificationLockedKey(ro.getTemplateId(), ro.getExpireAt());
            extras = JSONUtil.parseObj(JSONUtil.getByPath(ro.getBody(), BODY_EXTRAS))
                .putOnce(EXPIRE_AT, ro.getExpireAt());
        }
        Boolean lock = redisTemplate.opsForValue().setIfAbsent(lockedKey, 1);
        if (BooleanUtil.isFalse(lock)) {
            throw new BusinessException("Multiple messages are not allowed to be published");
        }
        try {
            ro.setBody(JSONUtil.createObj().putOnce(BODY_EXTRAS, extras));
            boolean result = playerNotificationService.batchCreateNotify(ListUtil.toList(ro));
            if (result) {
                return ResponseData.success();
            }
        } catch (Exception e) {
            log.error("Sending a message failed.", e);
        }
        redisTemplate.delete(lockedKey);
        throw new BusinessException("Sending a message failed.");
    }

    /**
     * Cancel a player notification.
     */
    @PostResource(path = "/gm/revoke/player/notify", requiredPermission = false)
    @Operation(summary = "Cancel a player notification", description = "Cancel a player "
        + "notification, deleted from the notification center")
    public ResponseData<Void> revokePlayerNotify(@RequestBody @Valid NotificationRevokeRo ro) {
        log.info("The operator「{}」cancels a player notification", SessionContext.getUserId());
        // Verify permission.
        iGmService.validPermission(SessionContext.getUserId(), GmAction.SYSTEM_NOTIFICATION_REVOKE);
        NotificationTemplate template =
            notificationFactory.getTemplateById(ro.getTemplateId());
        if (ObjectUtil.isNull(template)) {
            throw new BusinessException("The template id does not exist");
        }
        // Currently, only system notifications can be revoked
        if (!"system".equals(template.getNotificationsType())) {
            throw new BusinessException("Undoing non-system messages is not currently supported.");
        }
        String lockedKey = RedisConstants.getNotificationLockedKey(ro.getTemplateId(), "");
        if (StrUtil.isNotBlank(ro.getVersion())) {
            lockedKey = RedisConstants.getNotificationLockedKey(ro.getTemplateId(),
                ro.getVersion().replace(".", "_"));
        }
        if (ObjectUtil.isNotNull(ro.getExpireAt())) {
            lockedKey =
                RedisConstants.getNotificationLockedKey(ro.getTemplateId(), ro.getExpireAt());
        }
        if (BooleanUtil.isFalse(redisTemplate.hasKey(lockedKey))) {
            throw new BusinessException("The message does not exist. Undoing is not supported.");
        }
        if (BooleanUtil.isTrue(redisTemplate.hasKey(lockedKey))
            && redisTemplate.opsForValue().get(lockedKey) != null && Objects.equals(
            redisTemplate.opsForValue().get(lockedKey), ro.getRevokeType())) {
            throw new BusinessException(
                "The message has been revoked. Please do not undo it again.");
        }
        boolean result = playerNotificationService.revokeNotification(ro);
        if (result) {
            redisTemplate.opsForValue().set(lockedKey, ro.getRevokeType());
            return ResponseData.success();
        }
        throw new BusinessException("Failed to undo the message.");
    }

    /**
     * Get captcha.
     */
    @GetResource(path = "/gm/getCaptcha/{target}", requiredPermission = false)
    @Operation(summary = "Get captcha", hidden = true)
    public ResponseData<String> getCaptcha(@PathVariable("target") String target,
                                           @RequestParam(name = "type", required = false, defaultValue = "2")
                                           Integer type) {
        log.info("The operator「{}」get type [{}]captcha for target [{}]", SessionContext.getUserId(),
            target, type);
        // Verify permission.
        iGmService.validPermission(SessionContext.getUserId(), GmAction.TEST_CAPTCHA);
        ValidateCodeType codeType;
        CodeValidateScope scope;
        if (Validator.isMobile(target)) {
            // Obtain the mobile phone verification code
            if (!target.startsWith(testMobilePre)) {
                throw new BusinessException(
                    "Please the test mobile phone number begin [" + testMobilePre + "]");
            }
            codeType = ValidateCodeType.SMS;
            scope = CodeValidateScope.fromName(SmsCodeType.fromName(type).name());
            target = StrUtil.addPrefixIfNot(target, "+86");
        } else if (Validator.isEmail(target)) {
            // Obtain the email verification code
            if (!target.endsWith("@vikadata.com") || !target.startsWith("test")) {
                throw new BusinessException(
                    "Please use the [@vikadata.com] test email starting with [test]!such as: "
                        + "test001@vikadata.com");
            }
            codeType = ValidateCodeType.EMAIL;
            scope = CodeValidateScope.fromName(EmailCodeType.fromName(type).name());
        } else {
            throw new BusinessException(
                "Please output the specified format of mobile phone number or email!");
        }
        String randomCode = RandomUtil.randomNumbers(6);
        ValidateCode validateCode = new ValidateCode(randomCode, scope.name().toLowerCase(), 600);
        // storage verification code.
        validateCodeRepository.save(codeType.toString().toLowerCase(), validateCode, target, 600);
        // storage verification code service type.
        String scopeKey =
            RedisConstants.getCaptchaScopeKey(codeType.toString().toLowerCase(), target);
        redisTemplate.opsForValue().set(scopeKey, scope.name().toLowerCase(), 10, TimeUnit.MINUTES);
        return ResponseData.success(randomCode);
    }

    /**
     * Authenticate space.
     */
    @PostResource(path = "/gm/space/certification", requiredPermission = false)
    @Operation(summary = "Authenticate space", hidden = true)
    public ResponseData<Void> spaceCertification(@RequestBody SpaceCertificationRo ro) {
        log.info("Operator [{}] authenticates the space [{}]", SessionContext.getUserId(),
            ro.getSpaceId());
        SpaceCertification certification = SpaceCertification.toEnum(ro.getCertification());
        ExceptionUtil.isTrue(certification != null, UPDATE_SPACE_INFO_FAIL);
        iGmService.validPermission(SessionContext.getUserId(), GmAction.SPACE_CERTIFY);
        iGmService.spaceCertification(ro.getSpaceId(), ro.getUuid(), certification);
        return ResponseData.success();
    }

    /**
     * IDaaS privatization deployment create tenant.
     */
    @PostResource(path = "/gm/idaas/tenant/create", requiredLogin = false)
    @Operation(summary = "IDaaS privatization deployment create tenant", hidden = true)
    public ResponseData<IdaasTenantCreateVo> idaasTenantCreate(
        @RequestBody IdaasTenantCreateRo request) {
        log.info("IDaaS privatization deployment create tenant:" + JSONUtil.toJsonStr(request));
        IdaasTenantCreateVo idaasTenantCreateVo = idaasTenantService.createTenant(request);
        return ResponseData.success(idaasTenantCreateVo);
    }

    /**
     * IDaaS privatization deployment bind app.
     */
    @PostResource(path = "/gm/idaas/app/bind", requiredLogin = false)
    @Operation(summary = "IDaaS privatization deployment bind app", hidden = true)
    public ResponseData<IdaasAppBindVo> idaasAppBind(@RequestBody IdaasAppBindRo request) {
        log.info("IDaaS privatization deployment bind app: " + JSONUtil.toJsonStr(request));
        IdaasAppBindVo idaasAppBindVo = idaasAppBindService.bindTenantApp(request);
        return ResponseData.success(idaasAppBindVo);
    }

    /**
     * query user's mobile phone and email by user's id.
     */
    @PostResource(path = "/gm/user/writeContactInfo", requiredPermission = false)
    @Operation(summary = "query user's mobile phone and email by user's id")
    public ResponseData<Void> userContactInfoQuery(@RequestBody QueryUserInfoRo ro) {
        log.info("Operator 「{}」 query user mobile phone and email", SessionContext.getUserId());
        // check permission
        iGmService.validPermission(SessionContext.getUserId(), GmAction.CONTACT_INFO_QUERY);
        // query and write back user's mobile phone and email
        iGmService.queryAndWriteBackUserContactInfo(ro.getHost(), ro.getDatasheetId(),
            ro.getViewId(), ro.getToken());
        return ResponseData.success();
    }

    /**
     * Activity Integral Reward.
     */
    @PostResource(path = "/activity/reward", requiredPermission = false)
    @Operation(summary = "Activity Integral Reward")
    public ResponseData<Void> activityReward() {
        // valid permission
        iGmService.validPermission(SessionContext.getUserId(), GmAction.INTEGRAL_REWARD);
        iIntegralService.activityReward(LoginContext.me().getLoginUser().getNickName());
        return ResponseData.success();
    }

    /**
     * Query User Integral.
     */
    @GetResource(path = "/integral/get", requiredPermission = false)
    @Operation(summary = "Query User Integral")
    @Parameters({
        @Parameter(name = "userId", description = "User ID", schema = @Schema(type = "long"), in
            = ParameterIn.QUERY, example = "12511"),
        @Parameter(name = "areaCode", description = "Area Code", schema = @Schema(type = "integer"),
            in = ParameterIn.QUERY, example = "+1"),
        @Parameter(name = "credential", description = "Account Credential（mobile or email）",
            schema = @Schema(type = "string"), in = ParameterIn.QUERY, example = "xx@gmail.com")
    })
    public ResponseData<Integer> get(@RequestParam(value = "userId", required = false) Long userId,
                                     @RequestParam(value = "areaCode", required = false)
                                     String areaCode,
                                     @RequestParam(value = "credential", required = false)
                                     String credential) {
        // valid permission
        iGmService.validPermission(SessionContext.getUserId(), GmAction.INTEGRAL_QUERY);
        Long id =
            userId != null ? userId : (iUserService.getByUsername(areaCode, credential)).getId();
        return ResponseData.success(iIntegralService.getTotalIntegralValueByUserId(id));
    }

    /**
     * Deduct User Integral.
     */
    @PostResource(path = "/integral/deduct", requiredPermission = false)
    @Operation(summary = "Deduct User Integral")
    public ResponseData<Void> deduct(@RequestBody IntegralDeductRo ro) {
        // valid permission
        iGmService.validPermission(SessionContext.getUserId(), GmAction.INTEGRAL_SUBTRACT);
        // get user id
        Long userId = ro.getUserId() != null ? ro.getUserId()
            : (iUserService.getByUsername(ro.getAreaCode(), ro.getCredential())).getId();
        // deduct user integral
        iIntegralService.alterIntegral(OFFICIAL_ADJUSTMENT, IntegralAlterType.EXPENSES,
            ro.getCredit(), userId, JSONUtil.createObj());
        return ResponseData.success();
    }
}
