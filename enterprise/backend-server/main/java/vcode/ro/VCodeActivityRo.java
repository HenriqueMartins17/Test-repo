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

package com.apitable.enterprise.vcode.ro;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * <p>
 * Code V activity request parameters.
 * </p>
 */
@Data
@Schema(description = "Code V activity request parameters")
public class VCodeActivityRo {

    @Schema(description = "Activity Name", example = "XX Channel promotion", required = true)
    @NotBlank(message = "Name cannot be empty")
    private String name;

    @Schema(description = "Scene Values", example = "XX_channel_popularize", required = true)
    @NotBlank(message = "The scene value cannot be empty")
    private String scene;

}
