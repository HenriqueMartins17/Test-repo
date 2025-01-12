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
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Lark Bind user request parameters.
 */
@Data
@Schema(description = "Lark Bind user request parameters")
public class FeishuBindUserDTO {

    @Schema(description = "Area code", example = "+86")
    @NotBlank(message = "The region of the mobile phone number cannot be empty")
    private String areaCode;

    @Schema(description = "Phone number", example = "13800000000")
    @NotBlank(message = "Mobile number cannot be empty")
    private String mobile;

    @Schema(description = "Mobile phone verification code", example = "123456")
    @NotBlank(message = "Mobile phone verification code cannot be empty")
    private String code;

    @Schema(description = "Lark The user's unique ID in the application", example =
        "ou_6364101b36f45b594e8aa55edafe52de")
    @NotBlank(message = "Enterprise user ID cannot be empty")
    private String openId;

    @Schema(description = "Lark The user's unique ID in the application", example =
        "ou_6364101b36f45b594e8aa55edafe52de")
    @NotBlank(message = "Enterprise ID cannot be empty")
    private String tenantKey;
}
