package com.apitable.enterprise.airagent.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.apitable.asset.service.IAssetService;
import com.apitable.core.exception.BusinessException;
import com.apitable.enterprise.airagent.entity.AgentUserEntity;
import com.apitable.enterprise.airagent.model.AgentCreateRO;
import com.apitable.enterprise.airagent.service.IAgentAuthService;
import com.apitable.enterprise.airagent.service.IAgentService;
import com.apitable.enterprise.airagent.service.IAgentUserBindService;
import com.apitable.enterprise.airagent.service.IAgentUserService;
import com.apitable.enterprise.auth0.autoconfigure.Auth0Template;
import com.apitable.enterprise.auth0.model.Auth0UserProfile;
import com.apitable.shared.holder.UserHolder;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.mgmt.users.User;
import java.util.List;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * agent auth service impl.
 */
@Service
@Slf4j
public class AgentAuthServiceImpl implements IAgentAuthService {

    @Autowired(required = false)
    private Auth0Template auth0Template;

    @Resource
    private IAssetService iAssetService;

    @Resource
    private IAgentUserService iAgentUserService;

    @Resource
    private IAgentUserBindService iAgentUserBindService;

    @Resource
    private IAgentService iAgentService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUserIfNotExist(Auth0UserProfile userProfile) {
        String primaryUserId = linkUserAccount(userProfile);
        Long userId = iAgentUserBindService.getUserIdByExternalKey(primaryUserId);
        if (userId == null) {
            // create user
            AgentUserEntity userEntity =
                buildUserEntity(userProfile.getNickname(), userProfile.getPicture(),
                    userProfile.getEmail());
            iAgentUserService.save(userEntity);
            // create user bind
            iAgentUserBindService.create(userEntity.getId(), primaryUserId);
            // create subscription
            userId = userEntity.getId();
            // create user default ai agent
            createDefaultAiAgent(userId);
        }
        return userId;
    }

    private String linkUserAccount(Auth0UserProfile userProfile) {
        // call auth0 get users by email endpoint
        String email = userProfile.getEmail();
        List<User> users = auth0Template.usersByEmailSortByCreatedAt(email);
        // sort users by created date, find primary user
        // The user with the earliest time and email verified as true is the primary user
        if (CollectionUtil.isEmpty(users)) {
            // if no user found, return empty user
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
                throw new BusinessException("link primary user error");
            }
        }
        return primaryUserId;
    }

    private AgentUserEntity buildUserEntity(String name, String picture, String email) {
        String avatarRelativeUrl = iAssetService.downloadAndUploadUrl(picture);
        return AgentUserEntity.builder()
            .nickName(name)
            .avatar(avatarRelativeUrl)
            .email(email).build();
    }

    private void createDefaultAiAgent(Long userId) {
        UserHolder.set(userId);
        iAgentService.create(userId, null);
    }
}
