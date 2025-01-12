package com.apitable.enterprise.airagent.service.impl;

import com.apitable.enterprise.airagent.entity.AgentUserBindEntity;
import com.apitable.enterprise.airagent.mapper.AgentUserBindMapper;
import com.apitable.enterprise.airagent.service.IAgentUserBindService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * agent user bind service implementation.
 */
@Service
public class AgentUserBindServiceImpl
    extends ServiceImpl<AgentUserBindMapper, AgentUserBindEntity>
    implements IAgentUserBindService {

    @Override
    public AgentUserBindEntity getByExternalKey(String externalKey) {
        QueryWrapper<AgentUserBindEntity> queryWrapper = new QueryWrapper<AgentUserBindEntity>()
            .eq("external_key", externalKey);
        return getOne(queryWrapper, false);
    }

    @Override
    public Long getUserIdByExternalKey(String externalKey) {
        AgentUserBindEntity userBindEntity = getByExternalKey(externalKey);
        if (userBindEntity == null) {
            return null;
        }
        return userBindEntity.getUserId();
    }

    @Override
    public void create(Long userId, String externalKey) {
        AgentUserBindEntity userBindEntity = new AgentUserBindEntity();
        userBindEntity.setUserId(userId);
        userBindEntity.setExternalKey(externalKey);
        save(userBindEntity);
    }
}
