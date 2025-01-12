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

package com.apitable.enterprise.vikabilling.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DryRunOrderArgs.
 */
@Data
@Schema(description = "Trial order parameters")
public class DryRunOrderArgs {

    @Schema(description = "run type", example = "SUBSCRIPTION_ACTION", hidden = true)
    private String dryRunType = "SUBSCRIPTION_ACTION";

    @Schema(description = "run type action", example = "UPGRADE")
    private String action;

    @NotBlank(message = "Space id is not allowed to be empty")
    @Schema(description = "space id", example = "spc2123s")
    private String spaceId;

    @Schema(description = "production type", example = "SILVER")
    private String product;

    @Schema(description = "seat", example = "10")
    private Integer seat;

    @Schema(description = "month", example = "6")
    private Integer month;
}
