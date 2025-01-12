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

import com.apitable.enterprise.vikabilling.enums.OrderType;
import com.apitable.shared.support.serializer.OrderAmountSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Data;

/**
 * <p>
 * Order Preview.
 * </p>
 */
@Data
public class OrderPreview {

    @Schema(description = "space id", example = "spc2123s")
    private String spaceId;

    @Schema(description = "order type", example = "BUY")
    private OrderType orderType;

    @Schema(description = "order amount (Unit: Yuan)", example = "19998.21")
    @JsonSerialize(using = OrderAmountSerializer.class)
    private BigDecimal priceOrigin;

    @Schema(description = "discount amount (unit: yuan)", example = "19998.21")
    @JsonSerialize(using = OrderAmountSerializer.class)
    private BigDecimal priceDiscount;

    @Schema(description = "unused amount of the original plan (unit: yuan)", example = "19998.21")
    @JsonSerialize(using = OrderAmountSerializer.class)
    private BigDecimal priceUnusedCalculated;

    @Schema(description = "payment amount (unit: yuan)", example = "18998.11")
    @JsonSerialize(using = OrderAmountSerializer.class)
    private BigDecimal pricePaid;

    @Schema(description = "currency code", example = "CNY")
    private String currency;
}
