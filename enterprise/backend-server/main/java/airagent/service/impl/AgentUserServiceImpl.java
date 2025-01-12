package com.apitable.enterprise.airagent.service.impl;

import com.apitable.core.exception.BusinessException;
import com.apitable.enterprise.airagent.entity.AgentUserEntity;
import com.apitable.enterprise.airagent.enums.AirAgentException;
import com.apitable.enterprise.airagent.mapper.AgentUserMapper;
import com.apitable.enterprise.airagent.model.UserProfile;
import com.apitable.enterprise.airagent.service.IAgentUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * agent user service implementation.
 */
@Service
public class AgentUserServiceImpl extends ServiceImpl<AgentUserMapper, AgentUserEntity>
    implements IAgentUserService {

    @Override
    public UserProfile getUserProfile(Long userId) {
        AgentUserEntity userEntity = getById(userId);
        if (userEntity == null) {
            throw new BusinessException(AirAgentException.USER_NOT_FOUND);
        }
        UserProfile userProfile = new UserProfile();
        userProfile.setNickName(userEntity.getNickName());
        userProfile.setAvatar(userEntity.getAvatar());
        userProfile.setEmail(userEntity.getEmail());
        return userProfile;
    }
}
