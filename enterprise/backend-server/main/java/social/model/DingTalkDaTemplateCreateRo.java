/*
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up 
 * license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its 
 * subdirectories does not constitute permission to use this code or APITable Enterprise Edition 
 * features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

package com.apitable.enterprise.social.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * DingTalk--Create an app (using a template).
 * </p>
 */
@Schema(description = "DingTalk--Create an app (using a template)")
@Data
public class DingTalkDaTemplateCreateRo {

    @Schema(description = "Enterprise ID using template", required = true)
    private String corpId;

    @Schema(description = "APPLY NAME", required = true)
    private String name;

    @Schema(description = "Creator ID", required = true)
    private String opUserId;

    @Schema(description = "Application template key", required = true)
    private String templateKey;

    @Schema(description = "Whether to keep sample data", required = true)
    private Boolean keepSampleData;

    @Schema(description = "Current timestamp", required = true)
    private String timestamp;

    @Schema(description = "signature", required = true)
    private String signature;

    @Schema(description = "Request ID for easy troubleshooting", required = true)
    private String requestId;
}
