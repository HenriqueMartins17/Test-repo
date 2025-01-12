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
 * Lark Application event configuration request parameters.
 */
@Schema(description = "Lark Application event configuration request parameters")
@Data
public class FeishuAppEventConfigRo {

    @Schema(description = "Event Encryption Key", type = "String", example = "asdj123jl1")
    private String eventEncryptKey;

    @NotBlank
    @Schema(description = "Event verification token", type = "String", example = "12h3khkjhass")
    private String eventVerificationToken;
}
