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

package com.apitable.enterprise.user.ro;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * <p>
 * DingTalk Association Request Parameters.
 * </p>
 */
@Data
@Schema(description = "DingTalk Association Request Parameters")
public class DingTalkBindOpRo {

    @Schema(description = "Area code", example = "+86", required = true)
    private String areaCode;

    @Schema(description = "Phone number", example = "133...", required = true)
    @NotBlank(message = "Mobile number cannot be empty")
    private String phone;

    @Schema(description = "Unique identification within open applications", example = "liSii8KC",
        required = true)
    @NotBlank(message = "open Id cannot be empty")
    private String openId;

    @Schema(description = "Unique ID in the developer enterprise", example =
        "PiiiPyQqBNBii0HnCJ3zljcuAiEiE", required = true)
    @NotBlank(message = "The union ID cannot be empty")
    private String unionId;
}
