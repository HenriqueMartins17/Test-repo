package com.apitable.enterprise.airagent.mapper;

import com.apitable.enterprise.airagent.entity.AgentEntity;
import com.apitable.enterprise.airagent.model.AgentDTO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * Agent mapper.
 */
public interface AgentMapper extends BaseMapper<AgentEntity> {

    /**
     * get user agents list.
     *
     * @param userId user id
     * @return AgentDTO
     */
    List<AgentDTO> selectByUserId(@Param("userId") Long userId);

    /**
     * query agent by agent id.
     *
     * @param agentId agent id
     * @return AgentDTO
     */
    AgentDTO selectByAgentId(@Param("agentId") String agentId);

    /**
     * query top agent id by user id.
     *
     * @param userId user id
     * @return agent entity
     */
    AgentEntity selectTopAgentByUserId(@Param("userId") Long userId);

    /**
     * get agent name list.
     *
     * @param userId user id
     * @param name   agent name
     * @return string
     */
    String selectAgentNameByUserIdAndName(@Param("userId") Long userId, @Param("name") String name);

    /**
     * logical delete agent.
     *
     * @param userId  user id
     * @param agentId agent id
     * @return affected rows
     */
    int updateIsDeletedByAgentId(@Param("agentId") String agentId,
                                 @Param("userId") Long userId);

    /**
     * query pre agent id.
     *
     * @param agentId agent id
     * @return String
     */
    String selectPreAgentIdByAgentId(@Param("agentId") String agentId);

    /**
     * update pre agent id by id.
     *
     * @param id         id
     * @param preAgentId previous agent id
     * @param userId     user id
     * @return affected rows
     */
    int updatePreAgentIdById(@Param("id") Long id, @Param("preAgentId") String preAgentId,
                             @Param("userId") Long userId);
}
