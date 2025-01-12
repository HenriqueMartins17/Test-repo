package com.apitable.enterprise.airagent.model;

import lombok.Data;

/**
 * Sorted agent info.
 */
@Data
public class AiAgent {

    private String agentId;

    private String preAgentId;

    private String agentName;

    private String icon;

    public AiAgent(String agentId, String preAgentId) {
        this.agentId = agentId;
        this.preAgentId = preAgentId;
    }

    /**
     * Constructor.
     *
     * @param agentId    agent id
     * @param preAgentId pre agent id
     * @param agentName  agent name
     */
    public AiAgent(String agentId, String preAgentId, String agentName) {
        this.agentId = agentId;
        this.preAgentId = preAgentId;
        this.agentName = agentName;
    }

    /**
     * Constructor.
     *
     * @param agentDTO agentDTO
     */
    public AiAgent(AgentDTO agentDTO) {
        this.agentId = agentDTO.getAgentId();
        this.preAgentId = agentDTO.getPreAgentId();
        this.agentName = agentDTO.getAgentName();
        this.icon = agentDTO.getIcon();
    }
}
