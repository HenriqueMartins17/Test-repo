package com.apitable.enterprise.airagent.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent info view.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentVO {

    @Schema(description = "Ai id", type = "java.lang.String", example = "***")
    private String aiId;

    @Schema(description = "Agent name", type = "java.lang.String", example = "***")
    private String agentName;

    @Schema(description = "Agent id", type = "java.lang.String", example = "***")
    private String agentId;

    @Schema(description = "Agent pre id", type = "java.lang.String", example = "***")
    private String preAgentId;

    @Schema(description = "Agent icon", type = "java.lang.String", example = "***")
    private String icon;

    /**
     * Convert to AgentVO.
     *
     * @param aiAgent sorted agent
     * @return AgentVO
     */
    public static AgentVO of(AiAgent aiAgent) {
        AgentVO agentVO = new AgentVO();
        agentVO.setAiId(aiAgent.getAgentId());
        agentVO.setAgentId(aiAgent.getAgentId());
        agentVO.setPreAgentId(aiAgent.getPreAgentId());
        agentVO.setAgentName(aiAgent.getAgentName());
        agentVO.setIcon(aiAgent.getIcon());
        return agentVO;
    }
}
