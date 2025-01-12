package com.apitable.enterprise.airagent.mapper;

import com.apitable.enterprise.airagent.entity.AgentShareSettingEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * Agent share setting mapper.
 */
public interface AgentShareSettingMapper extends BaseMapper<AgentShareSettingEntity> {
    /**
     * get share id.
     *
     * @param agentId agent id
     * @return share info
     */
    AgentShareSettingEntity selectShareIdAndIsEnabledByAgentId(@Param("agentId") String agentId);

    /**
     * get share info.
     *
     * @param shareId share id
     * @return AgentShareSettingEntity
     */
    AgentShareSettingEntity selectAgentIdAndIsEnabledByShareId(@Param("shareId") String shareId);

}
