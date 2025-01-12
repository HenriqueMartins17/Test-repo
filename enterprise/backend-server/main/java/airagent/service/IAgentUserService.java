package com.apitable.enterprise.airagent.service;

import com.apitable.enterprise.airagent.entity.AgentUserEntity;
import com.apitable.enterprise.airagent.model.UserProfile;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * agent user service interface.
 */
public interface IAgentUserService extends IService<AgentUserEntity> {

    /**
     * get user profile.
     *
     * @param userId user id
     * @return user profile
     */
    UserProfile getUserProfile(Long userId);
}
