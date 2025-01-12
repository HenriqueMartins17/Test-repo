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
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * verifying lock unlocking ro.
 */
@Data
@Schema(description = "verifying lock unlocking ro")
public class UnlockRo {

    @NotBlank(message = "the target value cannot be null")
    @Schema(description = "Target: mobile phone or email address", required = true, example =
        "13800138000")
    private String target;

    @Schema(description = "type: 0、log in frequently（only phone）；1、verification code is "
        + "repeatedly obtained within one minute；2、verification code is frequently obtained "
        + "within 20 minutes（default）", example = "0")
    private Integer type = 2;
}
