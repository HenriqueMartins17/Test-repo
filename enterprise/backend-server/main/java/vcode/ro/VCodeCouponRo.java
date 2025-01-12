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
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * <p>
 * Request parameters of V code coupon template.
 * </p>
 */
@Data
@Schema(description = "Request parameters of V code coupon template")
public class VCodeCouponRo {

    @Schema(description = "Exchange amount", example = "10", required = true)
    @NotNull(message = "The exchange amount cannot be blank")
    private Integer count;

    @Schema(description = "Remarks", example = "Seed user benefit exchange template")
    private String comment;

}
