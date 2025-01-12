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

package com.apitable.enterprise.integral.ro;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Integral Deduct Ro.
 */
@Data
@Schema(description = "Integral Deduct Ro")
public class IntegralDeductRo {

    @Schema(description = "userId", example = "12511")
    private Long userId;

    @Schema(description = "areaCode", example = "+86")
    private String areaCode;

    @Schema(description = "account credential（mobile phone or email）", example = "xx@gmail.com")
    private String credential;

    @Schema(description = "the value of the deduction", example = "100", required = true)
    private Integer credit;
}
