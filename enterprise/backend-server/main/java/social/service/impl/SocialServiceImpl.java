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
import static com.apitable.enterprise.social.enums.SocialException.USER_NOT_VISIBLE_WECOM;
import static com.apitable.enterprise.social.redis.RedisKey.getSocialContactLockKey;
import static com.apitable.space.enums.SpaceException.NO_ALLOW_OPERATE;
import static com.apitable.workspace.enums.PermissionException.SET_MAIN_ADMIN_FAIL;
import static com.vikadata.social.feishu.constants.FeishuErrorCode.GET_TENANT_DENIED;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.apitable.asset.service.IAssetService;
import com.apitable.core.exception.BusinessException;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.core.util.SqlTool;
import com.apitable.enterprise.social.entity.SocialTenantEntity;
import com.apitable.enterprise.social.enums.SocialAppType;
import com.apitable.enterprise.social.enums.SocialException;
import com.apitable.enterprise.social.enums.SocialPlatformType;
import com.apitable.enterprise.social.enums.SocialTenantAuthMode;
import com.apitable.enterprise.social.model.FeishuTenantDetailVO;
import com.apitable.enterprise.social.model.FeishuTenantDetailVO.Space;
import com.apitable.enterprise.social.model.SocialUser;
import com.apitable.enterprise.social.model.SpaceBindTenantInfoDTO;
import com.apitable.enterprise.social.model.TenantBaseInfoDto;
import com.apitable.enterprise.social.model.TenantBindDTO;
import com.apitable.enterprise.social.model.TenantDetailVO;
import com.apitable.enterprise.social.model.User;
import com.apitable.enterprise.social.service.IFeishuService;
import com.apitable.enterprise.social.service.ISocialCpIsvService;
import com.apitable.enterprise.social.service.ISocialCpTenantUserService;
import com.apitable.enterprise.social.service.ISocialCpUserBindService;
import com.apitable.enterprise.social.service.ISocialService;
import com.apitable.enterprise.social.service.ISocialTenantBindService;
import com.apitable.enterprise.social.service.ISocialTenantService;
import com.apitable.enterprise.social.service.ISocialUserBindService;
import com.apitable.enterprise.social.service.IWeComService;
import com.apitable.enterprise.social.strategey.CreateSocialUserSimpleFactory;
import com.apitable.enterprise.social.strategey.CreateSocialUserStrategey;
import com.apitable.enterprise.user.service.IUserLinkService;
import com.apitable.enterprise.vcode.service.IVCodeService;
import com.apitable.enterprise.vikabilling.service.ISpaceSubscriptionService;
import com.apitable.interfaces.billing.model.SubscriptionInfo;
import com.apitable.interfaces.social.enums.SocialNameModified;
import com.apitable.organization.entity.MemberEntity;
import com.apitable.organization.mapper.MemberMapper;
import com.apitable.organization.service.IMemberService;
import com.apitable.player.service.IPlayerActivityService;
import com.apitable.shared.cache.service.UserSpaceCacheService;
import com.apitable.shared.util.CollectionUtil;
import com.apitable.space.entity.SpaceEntity;
import com.apitable.space.enums.SpaceUpdateOperate;
import com.apitable.space.mapper.SpaceMapper;
import com.apitable.space.mapper.SpaceMemberRoleRelMapper;
import com.apitable.space.service.ISpaceRoleService;
import com.apitable.space.service.ISpaceService;
import com.apitable.user.entity.UserEntity;
import com.apitable.user.enums.LinkType;
import com.apitable.user.service.IUserService;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.vikadata.social.feishu.exception.FeishuApiException;
import com.vikadata.social.feishu.model.FeishuTenantInfo;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.cp.bean.WxCpTpAuthInfo.Agent;
import me.chanjar.weixin.cp.bean.WxCpTpAuthInfo.Privilege;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Third party integration service interface implementation
 */
@Service
@Slf4j
public class SocialServiceImpl implements ISocialService {

    @Resource
    private ISocialTenantService iSocialTenantService;

    @Resource
    private ISocialUserBindService iSocialUserBindService;

    @Resource
    private ISocialTenantBindService iSocialTenantBindService;

    @Resource
    private IFeishuService iFeishuService;

    @Resource
    private IMemberService iMemberService;

    @Resource
    private ISpaceService iSpaceService;

    @Resource
    private IUserService iUserService;

    @Resource
    private ISpaceRoleService iSpaceRoleService;

    @Resource
    private ISpaceSubscriptionService iSpaceSubscriptionService;

    @Resource
    private MemberMapper memberMapper;

    @Resource
    private SpaceMapper spaceMapper;

    @Resource
    private SpaceMemberRoleRelMapper spaceMemberRoleRelMapper;

    @Resource
    private UserSpaceCacheService userSpaceCacheService;

    @Resource
    private ISocialCpIsvService iSocialCpIsvService;

    @Resource
    private IWeComService iWeComService;

    @Resource
    private CreateSocialUserSimpleFactory createSocialUserSimpleFactory;

    @Resource
    private IAssetService iAssetService;

    @Resource
    private IPlayerActivityService iPlayerActivityService;

    @Resource
    private IVCodeService ivCodeService;

    @Resource
    private IUserLinkService iUserLinkService;

    @Resource
    private ISocialCpTenantUserService iSocialCpTenantUserService;

    @Resource
    private ISocialCpUserBindService iSocialCpUserBindService;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUser(SocialUser user) {
        String avatar = iAssetService.downloadAndUploadUrl(user.getAvatar());
        UserEntity entity = UserEntity.builder()
            .uuid(IdUtil.fastSimpleUUID())
            .nickName(user.getNickName())
            .avatar(avatar)
            .lastLoginTime(LocalDateTime.now())
            .build();
        iUserService.saveUser(entity);
        // Create user activity record
        iPlayerActivityService.createUserActivityRecord(entity.getId());
        // Create personal invitation code
        ivCodeService.createPersonalInviteCode(entity.getId());
        // Create Associated User
        iSocialUserBindService.create(entity.getId(), user.getUnionId());
        boolean isLink = iUserLinkService.isUserLink(user.getUnionId(), LinkType.FEISHU.getType());
        if (!isLink) {
            iUserLinkService.createThirdPartyLink(entity.getId(), user.getOpenId(),
                user.getUnionId(),
                user.getNickName(), LinkType.FEISHU.getType());
        }
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSocialUser(User user) {
        log.info("Create Tenant Associated User");
        // Obtain policies according to different tenant types to create users
        CreateSocialUserStrategey strategy =
            createSocialUserSimpleFactory.getStrategy(user.getSocialPlatformType().getValue());
        return strategy.createSocialUser(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createWeComUser(SocialUser user) {
        String avatar = iAssetService.downloadAndUploadUrl(user.getAvatar());
        UserEntity entity = UserEntity.builder()
            .uuid(IdUtil.fastSimpleUUID())
            .code(user.getAreaCode())
            .mobilePhone(user.getTelephoneNumber())
            .nickName(user.getNickName())
            .avatar(avatar)
            .email(user.getEmailAddress())
            .lastLoginTime(LocalDateTime.now())
            .isSocialNameModified(user.getSocialAppType() == SocialAppType.ISV ?
                SocialNameModified.NO.getValue() : SocialNameModified.NO_SOCIAL.getValue())
            .build();
        iUserService.saveUser(entity);
        // Create user activity record
        iPlayerActivityService.createUserActivityRecord(entity.getId());
        // Create personal invitation code
        ivCodeService.createPersonalInviteCode(entity.getId());
        // Create Associated User
        Long cpTenantUserId =
            iSocialCpTenantUserService.getCpTenantUserId(user.getTenantId(), user.getAppId(),
                user.getOpenId());
        if (Objects.isNull(cpTenantUserId)) {
            cpTenantUserId = iSocialCpTenantUserService.create(user.getTenantId(), user.getAppId(),
                user.getOpenId(), user.getUnionId());
        }
        boolean isBind =
            iSocialCpUserBindService.isCpTenantUserIdBind(entity.getId(), cpTenantUserId);
        if (!isBind) {
            iSocialCpUserBindService.create(entity.getId(), cpTenantUserId);
        }
        return entity.getId();
    }

    @Override
    public void activeTenantSpace(Long userId, String spaceId, String openId) {
        // Activate the member of the tenant space where the association is located
        MemberEntity member = iMemberService.getBySpaceIdAndOpenId(spaceId, openId);
        if (member != null && member.getUserId() == null) {
            MemberEntity updatedMember = new MemberEntity();
            updatedMember.setId(member.getId());
            updatedMember.setUserId(userId);
            updatedMember.setIsActive(true);
            iMemberService.updateById(updatedMember);
        }
    }

    @Override
    public Long activeSpaceByMobile(Long userId, String spaceId, String openId, String mobile) {
        MemberEntity member = iMemberService.getBySpaceIdAndOpenId(spaceId, openId);
        if (member != null && StrUtil.isNotBlank(member.getMobile()) &&
            member.getMobile().equals(mobile)) {
            MemberEntity updateMember = new MemberEntity();
            updateMember.setId(member.getId());
            updateMember.setUserId(userId);
            iMemberService.updateById(updateMember);
            return member.getId();
        }
        return null;
    }

    @Override
    public void checkUserIfInTenant(Long userId, String appId, String tenantKey) {
        // Check whether the user is in the tenant and an administrator
        String openId =
            iSocialUserBindService.getOpenIdByTenantIdAndUserId(appId, tenantKey, userId);
        if (StrUtil.isBlank(openId)) {
            throw new BusinessException(SocialException.USER_NOT_EXIST);
        }
        boolean isTenantAdmin = iFeishuService.checkUserIsAdmin(tenantKey, openId);
        if (!isTenantAdmin) {
            throw new BusinessException(SocialException.ONLY_TENANT_ADMIN_BOUND_ERROR);
        }
    }

    @Override
    public FeishuTenantDetailVO getFeishuTenantInfo(String appId, String tenantKey) {
        FeishuTenantDetailVO infoVO = new FeishuTenantDetailVO();
        infoVO.setTenantKey(tenantKey);
        String spaceId = iSocialTenantBindService.getTenantDepartmentBindSpaceId(appId, tenantKey);
        if (StrUtil.isNotBlank(spaceId)) {
            SpaceEntity spaceEntity = iSpaceService.getBySpaceId(spaceId);
            if (spaceEntity != null) {
                Space space = new Space();
                space.setSpaceId(spaceEntity.getSpaceId());
                space.setSpaceName(spaceEntity.getName());
                space.setSpaceLogo(spaceEntity.getLogo());
                SubscriptionInfo subscribePlanInfo =
                    iSpaceSubscriptionService.getPlanInfoBySpaceId(spaceEntity.getSpaceId());
                space.setProduct(subscribePlanInfo.getProduct());
                space.setDeadline(subscribePlanInfo.getEndDate());
                if (spaceEntity.getOwner() != null) {
                    MemberEntity member = memberMapper.selectById(spaceEntity.getOwner());
                    space.setMainAdminUserId(spaceEntity.getOwner());
                    space.setMainAdminUserName(member.getMemberName());
                    if (member.getUserId() != null) {
                        UserEntity user = iUserService.getById(member.getUserId());
                        space.setMainAdminUserAvatar(user.getAvatar());
                    }
                }
                infoVO.setSpaces(Collections.singletonList(space));
            }
        }
        try {
            FeishuTenantInfo tenantInfo = iFeishuService.getFeishuTenantInfo(tenantKey);
            if (tenantInfo != null) {
                infoVO.setTenantName(tenantInfo.getName());
                infoVO.setAvatar(tenantInfo.getAvatar().getAvatarOrigin());
            }
        } catch (FeishuApiException exception) {
            if (exception.getCode() != GET_TENANT_DENIED) {
                // This application has not been used by the enterprise in 180 days, so it is not an error, just return NULL
                throw exception;
            }
        }
        return infoVO;
    }

    @Override
    public TenantDetailVO getTenantInfo(String tenantKey, String appId) {
        TenantDetailVO infoVO = new TenantDetailVO();
        infoVO.setTenantKey(tenantKey);
        infoVO.setSpaces(getTenantBindSpaceInfo(tenantKey, appId));
        TenantBaseInfoDto tenantBaseInfo = iSocialTenantService.getTenantBaseInfo(tenantKey, appId);
        infoVO.setTenantName(tenantBaseInfo.getTenantName());
        infoVO.setAvatar(tenantBaseInfo.getAvatar());
        return infoVO;
    }

    @Override
    public List<TenantDetailVO.Space> getTenantBindSpaceInfo(String tenantKey, String appId) {
        List<String> bindSpaceIds =
            iSocialTenantBindService.getSpaceIdsByTenantIdAndAppId(tenantKey, appId);
        List<TenantDetailVO.Space> spaces = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(bindSpaceIds)) {
            List<SpaceEntity> spaceEntities = iSpaceService.getBySpaceIds(bindSpaceIds);
            spaceEntities.forEach(spaceEntity -> {
                TenantDetailVO.Space space = new TenantDetailVO.Space();
                space.setSpaceId(spaceEntity.getSpaceId());
                space.setSpaceName(spaceEntity.getName());
                space.setSpaceLogo(spaceEntity.getLogo());
                // DingTalk Subscription
                SubscriptionInfo subscribePlanInfo =
                    iSpaceSubscriptionService.getPlanInfoBySpaceId(spaceEntity.getSpaceId());
                space.setProduct(subscribePlanInfo.getProduct());
                space.setDeadline(subscribePlanInfo.getEndDate());
                if (spaceEntity.getOwner() != null) {
                    MemberEntity member = memberMapper.selectById(spaceEntity.getOwner());
                    if (member != null) {
                        space.setMainAdminUserId(spaceEntity.getOwner());
                        space.setMainAdminUserName(member.getMemberName());
                        if (member.getUserId() != null) {
                            UserEntity user = iUserService.getById(member.getUserId());
                            space.setMainAdminUserAvatar(user.getAvatar());
                        }
                    }
                }
                spaces.add(space);
            });
        }
        return spaces;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeMainAdmin(String spaceId, Long memberId) {
        log.info("Replace the master administrator");
        // Update space and member information
        Long beforeOwnerMemberId = iSpaceService.getSpaceMainAdminMemberId(spaceId);
        MemberEntity beforeMember = iMemberService.getById(beforeOwnerMemberId);
        if (beforeMember != null && beforeOwnerMemberId.equals(memberId)) {
            log.warn("Like the master administrator, no change is required");
            return;
        }
        if (beforeMember != null) {
            iMemberService.cancelMemberMainAdmin(beforeOwnerMemberId);
            if (beforeMember.getUserId() != null) {
                userSpaceCacheService.delete(beforeMember.getUserId(), spaceId);
            }
        }
        boolean flag = SqlHelper.retBool(spaceMapper.updateSpaceOwnerId(spaceId, memberId, null));
        ExceptionUtil.isTrue(flag, SET_MAIN_ADMIN_FAIL);
        iMemberService.setMemberMainAdmin(memberId);
        MemberEntity newAdminMember = iMemberService.getById(memberId);
        if (newAdminMember.getUserId() != null) {
            userSpaceCacheService.delete(newAdminMember.getUserId(), spaceId);
        }
        // If the new administrator was originally a sub administrator, delete the original permission
        int count = SqlTool.retCount(
            spaceMemberRoleRelMapper.selectCountBySpaceIdAndMemberId(spaceId, memberId));
        if (count > 0) {
            iSpaceRoleService.deleteRole(spaceId, memberId);
        }

    }

    @Override
    public List<String> getSocialDisableRoleGroupCode(String spaceId) {
        TenantBindDTO bindInfo = iSocialTenantBindService.getTenantBindInfoBySpaceId(spaceId);
        // Filtering Ding Talk third-party integration
        if (bindInfo != null && bindInfo.getAppId() != null) {
            SocialTenantEntity entity =
                iSocialTenantService.getByAppIdAndTenantId(bindInfo.getAppId(),
                    bindInfo.getTenantId());
            boolean isDingTalkIsv =
                SocialPlatformType.DINGTALK.getValue().equals(entity.getPlatform()) &&
                    SocialAppType.ISV.equals(SocialAppType.of(entity.getAppType()));
            boolean isWeComIsv = SocialPlatformType.WECOM.getValue().equals(entity.getPlatform()) &&
                SocialAppType.ISV.getType() == entity.getAppType();
            boolean isLarkIsv = SocialPlatformType.FEISHU.getValue().equals(entity.getPlatform()) &&
                SocialAppType.ISV.getType() == entity.getAppType();
            if (isDingTalkIsv || isWeComIsv) {
                return new ArrayList<>();
            }
            if (isLarkIsv) {
                return Arrays.asList("MANAGE_NORMAL_MEMBER", "MANAGE_TEAM");
            }
        }
        return bindInfo != null ?
            Arrays.asList("MANAGE_NORMAL_MEMBER", "MANAGE_TEAM", "MANAGE_MEMBER") :
            new ArrayList<>();
    }

    @Override
    public void checkCanOperateSpaceUpdate(String spaceId, SpaceUpdateOperate spaceUpdateOperate) {
        TenantBindDTO bindInfo = iSocialTenantBindService.getTenantBindInfoBySpaceId(spaceId);
        if (spaceUpdateOperate == null) {
            ExceptionUtil.isTrue(bindInfo == null, NO_ALLOW_OPERATE);
        }
        // filter dingtalk
        if (bindInfo != null && bindInfo.getAppId() != null) {
            SocialTenantEntity entity =
                iSocialTenantService.getByAppIdAndTenantId(bindInfo.getAppId(),
                    bindInfo.getTenantId());
            if (SocialPlatformType.DINGTALK.getValue().equals(entity.getPlatform()) &&
                SocialAppType.ISV.equals(SocialAppType.of(entity.getAppType()))) {
                if (SpaceUpdateOperate.dingTalkIsvCanOperated(spaceUpdateOperate)) {
                    return;
                }
            } else if (SocialPlatformType.WECOM.getValue().equals(entity.getPlatform()) &&
                SocialAppType.ISV.getType() == entity.getAppType() &&
                SpaceUpdateOperate.weComIsvCanOperated(spaceUpdateOperate)) {
                return;
            } else if (SocialPlatformType.FEISHU.getValue().equals(entity.getPlatform()) &&
                SocialAppType.ISV.getType() == entity.getAppType() &&
                SpaceUpdateOperate.larIsvCanOperated(spaceUpdateOperate)) {
                return;
            }
            if (spaceUpdateOperate == SpaceUpdateOperate.DELETE_SPACE) {
                // feishu space cannot be deleted
                ExceptionUtil.isFalse(
                    SocialPlatformType.FEISHU.getValue().equals(entity.getPlatform())
                        && SocialAppType.ISV.equals(SocialAppType.of(entity.getAppType())),
                    NO_ALLOW_OPERATE);
                // dingtalk cannot be deleted
                ExceptionUtil.isFalse(
                    SocialPlatformType.DINGTALK.getValue().equals(entity.getPlatform())
                        && SocialAppType.ISV.equals(SocialAppType.of(entity.getAppType())),
                    NO_ALLOW_OPERATE);
                ExceptionUtil.isFalse(entity.getStatus(), NO_ALLOW_OPERATE);
                return;
            }
        }
        ExceptionUtil.isTrue(bindInfo == null, NO_ALLOW_OPERATE);
    }

    @Override
    public void checkCanOperateSpaceUpdate(String spaceId, Long opMemberId, Long acceptMemberId,
                                           List<SpaceUpdateOperate> spaceUpdateOperates) {
        log.info("check that user can operate space updates，space：{}，operation：{}", spaceId,
            spaceUpdateOperates);
        SpaceBindTenantInfoDTO spaceBindTenant =
            iSocialTenantBindService.getSpaceBindTenantInfoByPlatform(spaceId, null, null);
        if (null == spaceBindTenant) {
            return;
        }
        ExceptionUtil.isFalse(ArrayUtil.isEmpty(spaceUpdateOperates), NO_ALLOW_OPERATE);
        ExceptionUtil.isTrue(spaceBindTenant.getStatus(), NO_ALLOW_OPERATE);
        // Dingtalk and feishu all allow to modify the master administrator
        if (SocialPlatformType.DINGTALK.getValue().equals(spaceBindTenant.getPlatform())
            || SocialPlatformType.FEISHU.getValue().equals(spaceBindTenant.getPlatform())) {
            return;
        }
        if (SocialPlatformType.WECOM.getValue().equals(spaceBindTenant.getPlatform()) &&
            CollUtil.contains(spaceUpdateOperates, SpaceUpdateOperate.UPDATE_MAIN_ADMIN)) {
            // Check whether the operation member has a visible area of wecom
            String opOpenId = iMemberService.getOpenIdByMemberId(opMemberId);
            String acceptOpenId = iMemberService.getOpenIdByMemberId(acceptMemberId);
            log.info(
                "verify binding wecpm space「{}」，replace main administrator operation，original administrator：{}，change administrator：{}",
                spaceId, opMemberId, acceptMemberId);
            try {
                if (SocialAppType.ISV.getType() == spaceBindTenant.getAppType()) {
                    // The administrator authorization mode of the wecom  provider is only judged.
                    // because there is no visible scope for member authorization.
                    if (SocialTenantAuthMode.ADMIN.getValue() == spaceBindTenant.getAuthMode()) {
                        SocialTenantEntity socialTenantEntity = iSocialTenantService
                            .getByAppIdAndTenantId(spaceBindTenant.getAppId(),
                                spaceBindTenant.getTenantId());
                        // If necessary, refresh access_token first
                        iSocialCpIsvService.refreshAccessToken(spaceBindTenant.getAppId(),
                            spaceBindTenant.getTenantId(), socialTenantEntity.getPermanentCode());
                        Agent agent =
                            JSONUtil.toBean(socialTenantEntity.getContactAuthScope(), Agent.class);
                        Privilege privilege = agent.getPrivilege();
                        boolean isOpViewable =
                            iSocialCpIsvService.judgeViewable(socialTenantEntity.getTenantId(),
                                opOpenId, socialTenantEntity.getAppId(),
                                privilege.getAllowUsers(), privilege.getAllowParties(),
                                privilege.getAllowTags());
                        if (!isOpViewable) {
                            throw new BusinessException(USER_NOT_VISIBLE_WECOM);
                        }
                        boolean isAcceptViewable =
                            iSocialCpIsvService.judgeViewable(socialTenantEntity.getTenantId(),
                                acceptOpenId, socialTenantEntity.getAppId(),
                                privilege.getAllowUsers(), privilege.getAllowParties(),
                                privilege.getAllowTags());
                        if (!isAcceptViewable) {
                            throw new BusinessException(USER_NOT_VISIBLE_WECOM);
                        }
                    }
                } else {
                    iWeComService.getWeComUserByWeComUserId(spaceBindTenant.getTenantId(),
                        Integer.valueOf(spaceBindTenant.getAppId()), opOpenId);
                    iWeComService.getWeComUserByWeComUserId(spaceBindTenant.getTenantId(),
                        Integer.valueOf(spaceBindTenant.getAppId()), acceptOpenId);
                }
            } catch (BusinessException e) {
                if (e.getCode().equals(USER_NOT_EXIST.getCode())) {
                    throw new BusinessException(USER_NOT_VISIBLE_WECOM);
                }
                throw e;
            } catch (WxErrorException e) {
                throw new IllegalArgumentException(e);
            }
        } else {
            throw new BusinessException(NO_ALLOW_OPERATE);
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder(toBuilder = true)
    protected static class OpenUserToMember {

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
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true)
    @Builder(toBuilder = true)
    protected static class OpenDeptToTeam {
        private Long id;

        private String departmentName;

        private Long departmentId;

        private Long parentDepartmentId;

        private Long openDepartmentId;

        private Long parentOpenDepartmentId;

        private Long teamId;

        private Long parentTeamId;

        private Integer internalSequence;

        @Builder.Default
        private Boolean isNew = false;

        @Builder.Default
        private Boolean isCurrentSync = false;

        private SyncOperation op;

        enum SyncOperation {
            ADD, UPDATE, KEEP
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSocialUserBind(Long userId) {
        iSocialUserBindService.deleteByUserId(userId);
        iSocialCpUserBindService.deleteByUserId(userId);
        iUserLinkService.deleteByUserId(userId);
    }

    @Override
    public Boolean isContactSyncing(String spaceId) {
        return StrUtil.isNotBlank(spaceId) &&
            Boolean.TRUE.equals(redisTemplate.hasKey(getSocialContactLockKey(spaceId)));
    }

    @Override
    public void setContactSyncing(String spaceId, String value) {
        // the label space is synchronizing the contact
        redisTemplate.opsForValue()
            .set(getSocialContactLockKey(spaceId), value, 3600, TimeUnit.SECONDS);
    }

    @Override
    public void contactFinished(String spaceId) {
        String contactLockKey = getSocialContactLockKey(spaceId);
        // Contact processing completed, delete the previous lock
        if (Boolean.TRUE.equals(redisTemplate.hasKey(contactLockKey))) {
            redisTemplate.delete(contactLockKey);
        }
    }
}
