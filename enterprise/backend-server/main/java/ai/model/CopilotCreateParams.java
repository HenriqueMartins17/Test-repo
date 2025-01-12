package com.apitable.enterprise.ai.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * copilot create params.
 */
@Data
@Schema(description = "copilot create params")
public class CopilotCreateParams {

    @Schema(description = "The space id of user", requiredMode = Schema.RequiredMode.REQUIRED)
    private String spaceId;
}
