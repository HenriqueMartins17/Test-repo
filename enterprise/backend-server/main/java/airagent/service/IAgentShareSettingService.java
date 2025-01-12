package com.apitable.enterprise.airagent.service;

import com.apitable.enterprise.airagent.entity.AgentShareSettingEntity;
import com.apitable.enterprise.airagent.model.ShareVO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * agent share setting service interface.
 *
 * @author Shawn Deng
 */
public interface IAgentShareSettingService extends IService<AgentShareSettingEntity> {
    /**
     * publish agent share links.
     *
     * @param agentId agent id / ai id
     * @return share id
     */
    String publishSharing(String agentId);

    /**
     * close agent sharing link.
     *
     * @param agentId agent id
     */
    void closeSharing(String agentId);

    /**
     * get sharing info.
     *
     * @param shareId agent share id
     * @return agentId
     */
    String getAgentIdByShareId(String shareId);

    /**
     * get share info by agent id.
     *
     * @param agentId agent id
     * @return ShareVO
     */
    ShareVO getShareSettingByAgentId(String agentId);
}
