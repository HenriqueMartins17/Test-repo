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
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Placing Business Order Ro.
 */
@Data
@Schema(description = "Placing Business Order Ro")
public class CreateBusinessOrderRo {

    @NotBlank(message = "The space ID cannot be blank")
    @Schema(description = "Space Id", required = true, example = "spcNrqN2iH0qK")
    private String spaceId;

    @NotBlank(message = "The order type cannot be blank")
    @Schema(description = "Order type", required = true, example = "BUY")
    private String type;

    @Schema(description = "Product", example = "Enterprise")
    private String product;

    @Schema(description = "Seat", example = "price_s2sf3f232skad")
    private Integer seat;

    @Schema(description = "Privilege start date. If null, it means today start, such as renew "
        + "subscription", example = "2021-10-20")
    private String startDate;

    @Min(1)
    @Schema(description = "The subscription length(unit: month)", example = "1")
    private int months = 1;

    @Schema(description = "Remark", example = "Optionally, it can describe the specificities of "
        + "this order")
    private String remark;
}
