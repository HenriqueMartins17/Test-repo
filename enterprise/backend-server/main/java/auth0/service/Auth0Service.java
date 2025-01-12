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

package com.apitable.enterprise.auth0.service;

import static com.apitable.user.enums.UserException.EMAIL_NO_EXIST;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.apitable.asset.service.IAssetService;
import com.apitable.core.exception.BusinessException;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.enterprise.apitablebilling.appsumo.service.IAppsumoService;
import com.apitable.enterprise.auth0.autoconfigure.Auth0Template;
import com.apitable.enterprise.auth0.model.Auth0User;
import com.apitable.enterprise.auth0.model.Auth0UserProfile;
import com.apitable.enterprise.auth0.model.UserSpaceDTO;
import com.apitable.enterprise.auth0.util.QaExampleLoader;
import com.apitable.organization.dto.MemberDTO;
import com.apitable.organization.service.IMemberService;
import com.apitable.player.service.IPlayerActivityService;
import com.apitable.shared.config.properties.ConstProperties;
import com.apitable.space.model.Space;
import com.apitable.space.service.ISpaceService;
import com.apitable.template.service.ITemplateService;
import com.apitable.user.entity.UserEntity;
import com.apitable.user.enums.UserException;
import com.apitable.user.service.IUserService;
import com.apitable.workspace.dto.NodeCopyOptions;
import com.apitable.workspace.enums.NodeType;
import com.apitable.workspace.model.Datasheet;
import com.apitable.workspace.model.DatasheetCreateObject;
import com.apitable.workspace.model.DatasheetObject;
import com.apitable.workspace.service.INodeService;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.mgmt.EmailVerificationIdentity;
import com.auth0.json.mgmt.users.Identity;
import com.auth0.json.mgmt.users.User;
import jakarta.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Auth0 Service.
 */
@Component
@Slf4j
public class Auth0Service {

    @Autowired(required = false)
    private Auth0Template auth0Template;

    @Resource
    private IUserService iUserService;

    @Resource
    private IMemberService iMemberService;

    @Resource
    private IAssetService iAssetService;

    @Resource
    private IUserBindService iUserBindService;

    @Resource
    private IPlayerActivityService iPlayerActivityService;

    @Resource
    private ISpaceService iSpaceService;

    @Resource
    private ConstProperties constProperties;

    @Resource
    private ServerProperties serverProperties;

    @Resource
    private ITemplateService iTemplateService;

    @Resource
    private INodeService iNodeService;

    @Resource
    private IAppsumoService iAppsumoService;

    public boolean isOpen() {
        return auth0Template != null;
    }

    /**
     * logout.
     *
     * @return String
     */
    public String logout() {
        String logoutUrl = auth0Template.buildLogoutUrl(
            constProperties.getServerDomain());
        if (log.isDebugEnabled()) {
            log.debug("logout redirect url: {}", logoutUrl);
        }
        return logoutUrl;
    }

    /**
     * create user invitation url.
     *
     * @param email     email
     * @param returnUrl url
     * @return String
     */
    public String createUserInvitationLink(String email, String returnUrl) {
        try {
            String userId;
            User user = auth0Template.usersByEmail(email);
            if (user == null) {
                userId = auth0Template.createUser(email, false);
            } else {
                userId = user.getId();
            }
            String ticket = auth0Template.createPasswordResetTicket(userId,
                returnUrl);
            return ticket + "type=invite";
        } catch (Auth0Exception e) {
            log.error("can't create user with this email", e);
            throw new BusinessException("can't send with this email");
        }
    }

    public User createUserWithoutEmailVerification(String email, String password)
        throws Auth0Exception {
        return auth0Template.createUser(email, password, false);
    }

    private void initializeTemplate(String template, Long userId, String userUuid,
                                    String spaceId, String rootNodeId) {
        if (template != null && template.equals("ai_onboarding")) {
            initializeAiExampleDatasheet(userId, userUuid, spaceId, rootNodeId);
            return;
        }
        List<String> templateIds = template == null
            ? StrUtil.splitTrim(constProperties.getRegisterQuoteTemplates(), StrUtil.C_COMMA)
            : Collections.singletonList(template);
        List<String> templateNodeIds =
            iTemplateService.getTemplateNodeIds(constProperties.getTemplateSpace(), templateIds);
        for (String templateNodeId : templateNodeIds) {
            iNodeService.copyNodeToSpace(userId, spaceId, rootNodeId,
                templateNodeId, NodeCopyOptions.create());
        }
    }

    private void initializeAiExampleDatasheet(Long userId, String userUuid, String spaceId,
                                              String parentId) {
        Datasheet datasheet = QaExampleLoader.get();
        datasheet.getRecords().forEach(record -> record.initFieldUpdateInfo(userUuid,
            Instant.now(Clock.system(ZoneOffset.UTC)).toEpochMilli()));
        DatasheetCreateObject datasheetCreateObject = DatasheetCreateObject.builder()
            .name("AI ChatBot Q&A")
            .type(NodeType.DATASHEET)
            .parentId(parentId)
            .datasheetObject(
                DatasheetObject.builder()
                    .meta(datasheet.getMeta())
                    .records(datasheet.getRecords())
                    .build()
            )
            .build();
        iNodeService.createDatasheetNode(userId, spaceId, datasheetCreateObject);
    }

    /**
     * create user by auth0 user profile.
     *
     * @param userProfile      auth0 user profile
     * @param externalProperty external property
     * @return auth0 user
     */
    @Transactional(rollbackFor = Exception.class)
    public Auth0User createUserByAuth0IfNotExist(Auth0UserProfile userProfile,
                                                 Map<String, String> externalProperty) {
        // call auth0 get users by email endpoint
        String email = userProfile.getEmail();
        List<User> users = auth0Template.usersByEmailSortByCreatedAt(email);
        // sort users by created date, find primary user
        // The user with the earliest time and email verified as true is the primary user
        if (CollectionUtil.isEmpty(users)) {
            // if no user found, return empty user
            log.info("No user found with email: {}", email);
            throw new BusinessException("No user found with this email");
        }
        User primaryUser = users.get(0);
        String primaryUserId = primaryUser.getId();
        String currentUserUserId = userProfile.getSub();
        if (!currentUserUserId.equals(primaryUserId)) {
            // The returned unique id format: google-oauth2|102841242677504184784, need to be cut when linking
            String provider = currentUserUserId.substring(0, currentUserUserId.indexOf('|'));
            String newUserId = currentUserUserId.substring(currentUserUserId.indexOf('|') + 1);
            // call auth0 link account endpoint to link primary user and now user
            try {
                auth0Template.linkIdentity(primaryUserId, newUserId, provider, null);
            } catch (Auth0Exception e) {
                log.error("link primary user error", e);
                throw new BusinessException("link primary user error");
            }
        }

        Auth0User user = new Auth0User();
        Long userId = iUserBindService.getUserIdByExternalKey(primaryUserId);
        if (userId == null) {
            // create user bind
            UserEntity userEntity = buildUserEntity(primaryUser.getPicture(),
                primaryUser.getNickname(), primaryUser.getEmail());
            iUserService.saveUser(userEntity);
            // Create user activity record
            iPlayerActivityService.createUserActivityRecord(userEntity.getId());
            // create user bind
            iUserBindService.create(userEntity.getId(), primaryUserId);
            // init one space for user
            String spaceName = String.format("%s's Space", userEntity.getNickName());
            Space space = iSpaceService.createSpace(userEntity, spaceName);
            // if user from ai onboarding page, init datasheet for it
            Map<String, Object> appMetadata = auth0Template.getUserAppMetadata(primaryUserId);
            Object templateObj = appMetadata.get("template");
            String template = templateObj != null ? templateObj.toString() : null;
            initializeTemplate(template, userEntity.getId(), userEntity.getUuid(),
                space.getId(), space.getRootNodeId());
            if (template != null) {
                user.addQueryString("template=" + template);
            }
            if (appMetadata.containsKey("src")) {
                Object srcObj = appMetadata.get("src");
                String src = srcObj != null ? srcObj.toString() : null;
                if (StrUtil.isNotBlank(src)) {
                    user.addQueryString("src=" + src);
                }
            }
            if (appMetadata.containsKey("via")) {
                Object viaObj = appMetadata.get("via");
                String via = viaObj != null ? viaObj.toString() : null;
                if (StrUtil.isNotBlank(via)) {
                    user.addQueryString("via=" + via);
                }
            }
            if (appMetadata.containsKey("referral")) {
                Object obj = appMetadata.get("referral");
                String value = obj != null ? obj.toString() : null;
                if (StrUtil.isNotBlank(value)) {
                    externalProperty.put("referral", value);
                }
            }

            if (appMetadata.containsKey("coupon")) {
                Object obj = appMetadata.get("coupon");
                String value = obj != null ? obj.toString() : null;
                if (StrUtil.isNotBlank(value)) {
                    externalProperty.put("coupon", value);
                }
            }
            userId = userEntity.getId();
        }
        List<MemberDTO> inactiveMembers =
            iMemberService.getInactiveMemberByEmail(userProfile.getEmail());
        List<Long> memberIds = inactiveMembers.stream().map(MemberDTO::getId)
            .collect(Collectors.toList());
        iMemberService.activeIfExistInvitationSpace(userId, memberIds);
        user.setUserId(userId);
        extractQueryString(externalProperty, user);
        return user;
    }

    /**
     * create user use auth0 information.
     *
     * @param user user
     * @return user id
     */
    @Transactional(rollbackFor = Exception.class)
    public UserSpaceDTO createUserByAuth0IfNotExist(User user) {
        UserSpaceDTO dto = new UserSpaceDTO();
        Long userId = iUserBindService.getUserIdByExternalKey(user.getId());
        if (null == userId) {
            // create user bind
            UserEntity userEntity = buildUserEntity(user.getPicture(),
                user.getNickname(), user.getEmail());
            iUserService.saveUser(userEntity);
            // Create user activity record
            iPlayerActivityService.createUserActivityRecord(userEntity.getId());
            // create user bind
            iUserBindService.create(userEntity.getId(), user.getId());
            // init one space for user
            String spaceName = String.format("%s's Space", userEntity.getNickName());
            Space space = iSpaceService.createSpace(userEntity, spaceName);
            initializeTemplate(null, userEntity.getId(), userEntity.getUuid(),
                space.getId(), space.getRootNodeId());
            userId = userEntity.getId();
            dto.setSpaceId(space.getId());
        }
        dto.setUserId(userId);
        List<MemberDTO> inactiveMembers =
            iMemberService.getInactiveMemberByEmail(user.getEmail());
        List<Long> memberIds = inactiveMembers.stream().map(MemberDTO::getId)
            .collect(Collectors.toList());
        iMemberService.activeIfExistInvitationSpace(userId, memberIds);
        return dto;
    }

    private void extractQueryString(Map<String, String> externalProperty, Auth0User user) {
        if (externalProperty.containsKey("via") &&
            StrUtil.isNotBlank(externalProperty.get("via"))) {
            user.addQueryString("via=" + externalProperty.get("via"));
        }
        if (externalProperty.containsKey("referral") &&
            StrUtil.isNotBlank(externalProperty.get("referral"))) {
            user.addQueryString("referral=" + externalProperty.get("referral"));
        }
        if (externalProperty.containsKey("coupon") &&
            StrUtil.isNotBlank(externalProperty.get("coupon"))) {
            user.addQueryString("coupon=" + externalProperty.get("coupon"));
        }
        if (externalProperty.containsKey("choosePlan") &&
            StrUtil.isNotBlank(externalProperty.get("choosePlan"))) {
            user.addQueryString("choosePlan=" + externalProperty.get("choosePlan"));
        }
        if (externalProperty.containsKey("reference") &&
            StrUtil.isNotBlank(externalProperty.get("reference"))) {
            Map<String, String> paramMap =
                HttpUtil.decodeParamMap(externalProperty.get("reference"), StandardCharsets.UTF_8);
            if (StrUtil.isNotBlank(paramMap.get("spaceId"))) {
                user.addQueryString("spaceId=" + paramMap.get("spaceId"));
            }
        }
    }

    /**
     * reset password.
     *
     * @param userId user id
     * @return boolean
     */
    public boolean resetPassword(Long userId) {
        if (!isOpen()) {
            return false;
        }
        String email = iUserService.getEmailByUserId(userId);
        try {
            User user = auth0Template.usersByEmail(email);
            if (!CollectionUtil.contains(user.getIdentities(),
                o -> ObjectUtil.equal(o.getConnection(),
                    auth0Template.getDbConnectionName()))) {
                // create new user with auth0 db
                String newUserId = auth0Template.createUser(email, false);
                User newUser = auth0Template.usersByEmail(email);
                Identity identity = CollectionUtil.findOne(newUser.getIdentities(),
                    o -> ObjectUtil.equal(o.getConnection(), auth0Template.getDbConnectionName()));
                // link new user to original user
                auth0Template.linkIdentity(user.getId(), newUserId, identity.getProvider(),
                    null);
            }
            // send reset password email
            auth0Template.resetPassword(email);
        } catch (Auth0Exception e) {
            log.error("RestPasswordError", e);
            return false;
        }
        return true;
    }

    private UserEntity buildUserEntity(String picture, String nickname,
                                       String email) {
        String avatar = iAssetService.downloadAndUploadUrl(picture);
        return UserEntity.builder().uuid(IdUtil.fastSimpleUUID())
            .nickName(nickname).avatar(avatar).email(email).build();
    }

    /**
     * send invitation email.
     *
     * @param spaceId      space id
     * @param inviteUserId invite user id
     * @param email        email
     */
    public void sendInvitationEmail(Long inviteUserId, String spaceId,
                                    String email) {
        final String defaultLang = LocaleContextHolder.getLocale()
            .toLanguageTag();
        final String lang = iUserService.getLangByEmail(defaultLang, email);
        Long memberId = iMemberService.getMemberIdByUserIdAndSpaceId(
            inviteUserId, spaceId);
        UserEntity user = iUserService.getByEmail(email);
        if (user == null) {
            String returnUrl = constProperties.getServerDomain()
                + serverProperties.getServlet().getContextPath()
                + "/invitation/callback";
            String link = createUserInvitationLink(email, returnUrl);
            iMemberService.sendUserInvitationEmail(lang, spaceId, memberId,
                link, email);
        } else {
            String link = String.format("%s/workbench?spaceId=%s",
                constProperties.getServerDomain(), spaceId);
            iMemberService.sendUserInvitationEmail(lang, spaceId, memberId,
                link, email);
        }
    }

    /**
     * send invitation email.
     *
     * @param spaceId      space id
     * @param inviteUserId user's id who will be invited to the specific space
     * @param emails       emails
     */
    public void sendInvitationEmail(Long inviteUserId, String spaceId,
                                    List<String> emails) {
        List<UserEntity> userEntities = iUserService.getByEmails(emails);
        Map<String, Long> emailUserMap = userEntities.stream()
            .collect(Collectors.toMap(UserEntity::getEmail, UserEntity::getId));
        // collect emails whether it can send invitation
        List<String> shouldSendInvitationEmails = new ArrayList<>();
        List<String> shouldSendInvitationForSignupEmail = new ArrayList<>();
        emails.forEach(inviteEmail -> {
            // check email user if existed
            if (emailUserMap.containsKey(inviteEmail)) {
                // remember email should send invitation email
                shouldSendInvitationEmails.add(inviteEmail);
            } else {
                shouldSendInvitationForSignupEmail.add(inviteEmail);
            }
        });
        // inviter member id in space
        Long memberId = iMemberService.getMemberIdByUserIdAndSpaceId(
            inviteUserId, spaceId);
        final String defaultLang = LocaleContextHolder.getLocale()
            .toLanguageTag();
        // create space workbench link
        shouldSendInvitationEmails.forEach(email -> {
            String link = String.format("%s/workbench?spaceId=%s",
                constProperties.getServerDomain(), spaceId);
            if (log.isDebugEnabled()) {
                log.debug("link to send: {}", link);
            }
            final String locale = iUserService.getLangByEmail(defaultLang,
                email);
            iMemberService.sendUserInvitationEmail(locale, spaceId, memberId,
                link, email);
        });
        // create invitation link for sign up
        String returnUrl =
            constProperties.getServerDomain() + serverProperties.getServlet()
                .getContextPath() + "/invitation/callback";
        shouldSendInvitationForSignupEmail.forEach(email -> {
            String link = createUserInvitationLink(email, returnUrl);
            if (log.isDebugEnabled()) {
                log.debug("link to send: {}", link);
            }
            final String locale = iUserService.getLangByEmail(defaultLang,
                email);
            iMemberService.sendUserInvitationEmail(locale, spaceId, memberId,
                link, email);
        });
    }

    /**
     * send verification email.
     *
     * @param email email
     */
    public void sendVerificationEmail(String email) throws Auth0Exception {
        User user = auth0Template.usersByEmail(email);
        ExceptionUtil.isNotNull(user, EMAIL_NO_EXIST);
        ExceptionUtil.isFalse(user.isEmailVerified(), UserException.EMAIL_HAS_BIND);
        Identity userIdentity = user.getIdentities().stream()
            .filter(o -> o.getConnection().equals(auth0Template.getDbConnectionName()))
            .findAny()
            .orElse(null);
        if (null != userIdentity) {
            EmailVerificationIdentity identity =
                new EmailVerificationIdentity(userIdentity.getProvider(), userIdentity.getUserId());
            auth0Template.sendVerificationEmail(user.getId(), identity);
        }
    }


    /**
     * update user nick name.
     *
     * @param userId internal system user id
     * @param user   user info
     * @return {@link User}
     */
    public User updateUser(Long userId, User user) throws Auth0Exception {
        String externalUserId = iUserBindService.getExternalKeyByUserId(userId);
        return auth0Template.updateUser(externalUserId, user);
    }

    /**
     * update appsumo user email
     *
     * @param email new email
     * @param oldEmail old email
     */
    public void updateAppsumoUser(String email, String oldEmail) {
        iAppsumoService.linkAppsumoActivationEmail(email, oldEmail);
    }

    /**
     * delete a user.
     *
     * @param userId user id
     */
    public void deleteUser(Long userId) {
        String externalUserId = iUserBindService.getExternalKeyByUserId(userId);
        if (null == externalUserId) {
            log.error("notFoundUserInAuth0:{}", userId);
            return;
        }
        auth0Template.deleteUser(externalUserId);
    }

    /**
     * get user by email.
     *
     * @param email email
     * @return User
     * @throws Auth0Exception auth0 exception
     */
    public User userByEmail(String email) throws Auth0Exception {
        return auth0Template.usersByEmail(email);
    }

    /**
     * get db connection name.
     * @return connection name
     */
    public String getDbConnectionName() {
        return auth0Template.getDbConnectionName();
    }
}
