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

package com.apitable.enterprise.ops.ro;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * sync dingtalk app ro.
 */
@Data
@Schema(description = "sync dingtalk app ro")
public class SyncSocialDingTalkAppRo {

    @NotBlank
    @Schema(description = "suiteId/customKey", example = "suite***",
        requiredMode = RequiredMode.REQUIRED)
    private String suiteId;

    @NotBlank
    @Schema(description = "suiteSecret", example = "***",
        requiredMode = RequiredMode.REQUIRED)
    private String suiteSecret;

    @NotBlank
    @Schema(description = "agentId", example = "1248***",
        requiredMode = RequiredMode.REQUIRED)
    private String agentId;

    @NotBlank
    @Schema(description = "token", example = "***",
        requiredMode = RequiredMode.REQUIRED)
    private String token;

    @NotBlank
    @Schema(description = "aesKey", example = "***",
        requiredMode = RequiredMode.REQUIRED)
    private String aesKey;

    @Schema(description = "1: self 2: isv, default is 1", example = "1")
    private Integer appType = 1;

    @Schema(description = "authCorpId, required by self app", example = "ding***")
    private String authCorpId;

    @Schema(description = "suiteTicket, required by self app", example = "***")
    private String suiteTicket;
}
