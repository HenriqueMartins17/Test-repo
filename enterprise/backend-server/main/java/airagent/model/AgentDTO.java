package com.apitable.enterprise.airagent.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent DTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentDTO {

    private String agentName;

    private String agentId;

    private String preAgentId;

    private String icon;
}
