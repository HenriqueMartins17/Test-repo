package com.apitable.enterprise.airagent.service.impl;

import com.apitable.core.util.ExceptionUtil;
import com.apitable.enterprise.airagent.entity.AgentShareSettingEntity;
import com.apitable.enterprise.airagent.exception.AgentShareSettingException;
import com.apitable.enterprise.airagent.mapper.AgentShareSettingMapper;
import com.apitable.enterprise.airagent.model.ShareVO;
import com.apitable.enterprise.airagent.service.IAgentShareSettingService;
import com.apitable.shared.util.IdUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * agent share setting service implements.
 */
@Service
@Slf4j
public class AgentShareSettingServiceImpl extends ServiceImpl<AgentShareSettingMapper, AgentShareSettingEntity>
    implements IAgentShareSettingService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String publishSharing(String agentId) {
        // check agent whether you have a share link
        AgentShareSettingEntity entity = baseMapper.selectShareIdAndIsEnabledByAgentId(agentId);
        if (null == entity) {
            entity = AgentShareSettingEntity.builder()
                .id(IdWorker.getId())
                .agentId(agentId)
                .shareId(IdUtil.createShareId())
                .isEnabled(true)
                .build();
        }
        if (!entity.getIsEnabled()) {
            entity.setIsEnabled(true);
        }
        saveOrUpdate(entity);
        return entity.getShareId();
    }

    @Override
    public void closeSharing(String agentId) {
        AgentShareSettingEntity entity = baseMapper.selectShareIdAndIsEnabledByAgentId(agentId);
        if (null == entity) {
            return;
        }
        entity.setIsEnabled(false);
        updateById(entity);
    }

    @Override
    public String getAgentIdByShareId(String agentId) {
        AgentShareSettingEntity entity = baseMapper.selectAgentIdAndIsEnabledByShareId(agentId);
        ExceptionUtil.isFalse(null == entity, AgentShareSettingException.AGENT_NOT_SHARED);
        ExceptionUtil.isTrue(entity.getIsEnabled(), AgentShareSettingException.AGENT_SHARING_DISABLED);
        return entity.getAgentId();
    }

    @Override
    public ShareVO getShareSettingByAgentId(String agentId) {
        ShareVO vo = new ShareVO();
        AgentShareSettingEntity entity = baseMapper.selectShareIdAndIsEnabledByAgentId(agentId);
        if (null != entity) {
            vo.setShareId(entity.getShareId());
            vo.setIsEnabled(entity.getIsEnabled());
        }
        return vo;
    }
}
