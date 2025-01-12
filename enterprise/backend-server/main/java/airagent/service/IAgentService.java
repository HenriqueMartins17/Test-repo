package com.apitable.enterprise.airagent.service;

import com.apitable.enterprise.airagent.entity.AgentEntity;
import com.apitable.enterprise.airagent.model.AgentCreateRO;
import com.apitable.enterprise.airagent.model.AgentUpdateParams;
import com.apitable.enterprise.airagent.model.AiAgent;
import com.apitable.enterprise.airagent.model.SortedAgents;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * agent service interface.
 *
 * @author Shawn Deng
 */
public interface IAgentService extends IService<AgentEntity> {

    /**
     * get user ai agent list.
     *
     * @param userId user id
     * @return SortedAgent List
     */
    SortedAgents getUserAgents(Long userId);

    /**
     * get ai agent by agent id.
     *
     * @param agentId agent id
     * @return SortedAgent
     */
    AiAgent getAgent(String agentId);

    /**
     * check ai agent exist.
     *
     * @param agentId agent id
     */
    void checkAgent(String agentId);

    /**
     * get agent entity by agent id.
     *
     * @param agentId agent id
     * @return agent entity
     */
    AgentEntity getEntityByAgentId(String agentId);

    /**
     * get agent entity by pre agent id.
     * @param preAgentId pre agent id
     * @return agent entity
     */
    AgentEntity getEntityByPreAgentId(String preAgentId);

    /**
     * get top agent id.
     *
     * @param userId user id
     * @return agent entity
     */
    AgentEntity getTopAgent(Long userId);

    /**
     * create ai agent.
     *
     * @param param  param
     * @param userId user id
     * @return aiId
     */
    String create(Long userId, AgentCreateRO param);

    /**
     * update ai agent order.
     *
     * @param id         id
     * @param preAgentId pre agent id
     */
    void updatePreAgentIdById(Long id, String preAgentId);

    /**
     * update ai agent.
     *
     * @param agentId      agent id
     * @param updateParams update params
     */
    void update(String agentId, AgentUpdateParams updateParams);

    /**
     * delete ai agent.
     *
     * @param userId  user id
     * @param agentId ai agent id
     */
    void delete(Long userId, String agentId);
}
