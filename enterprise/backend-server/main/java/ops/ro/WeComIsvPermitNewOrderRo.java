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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * WeCom Isv Permit New Order Ro.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "WeCom Isv Permit New Order Ro")
public class WeComIsvPermitNewOrderRo extends OpsAuthRo {

    @Schema(description = "license space to activate")
    @NotBlank
    private String spaceId;

    @Schema(description =
        "the number of months to purchase the account. take 31 days as a month, max"
            + " 36 months")
    @NotNull
    @Min(1)
    @Max(36)
    private Integer durationMonths;

}
