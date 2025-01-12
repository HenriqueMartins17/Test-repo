package com.apitable.enterprise.airagent.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Ai Agent create RO.
 */
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Ai Agent create RO")
public class AgentCreateRO {

    @Schema(description = "Ai Agent previous id", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "ag_****")
    private String preAgentId;

    @Schema(description = "Ai Agent name", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "test")
    private String name;
}
