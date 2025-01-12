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
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * <p>
 * Woa application user login request object.
 * </p>
 *
 * @author Chambers
 */
@Data
@Schema(description = "Woa application user login request object")
public class WoaUserLoginRo {

    @NotBlank
    @Schema(description = "Application ID", requiredMode = RequiredMode.REQUIRED)
    private String appId;

    @NotBlank
    @Schema(description = "Authorization Code", requiredMode = RequiredMode.REQUIRED)
    private String code;

}
