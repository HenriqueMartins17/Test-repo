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

package com.apitable.enterprise.gm.ro;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Data;

/**
 * ChatBot Enable RO.
 */
@Data
@Schema(description = "ChatBot Enable RO")
public class ChatbotEnableRo {

    @Schema(description = "Space ID", requiredMode = RequiredMode.REQUIRED, example = "spc11")
    private String spaceId;

    @Schema(description = "Auth Token", requiredMode = RequiredMode.REQUIRED, example = "aqq")
    private String token;

    @Schema(description = "Valid Date", requiredMode = RequiredMode.NOT_REQUIRED, example = "30")
    private Integer days;

    @Schema(description = "Whether off", requiredMode = RequiredMode.NOT_REQUIRED, example = "true")
    private Boolean isOff;
}
