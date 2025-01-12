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

import com.apitable.enterprise.vikabilling.util.OrderUtil;
import com.apitable.enterprise.vikabilling.util.model.BillingPlanPrice;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.Locale;
import lombok.Data;

/**
 * <p>
 * Product Price View.
 * </p>
 */
@Data
@Schema(description = "Product Price View")
public class ProductPriceVo {

    @Schema(description = "production type", example = "SILVER")
    private String product;

    @Schema(description = "price id", example = "price_dasx1212cas")
    private String priceId;

    @Schema(description = "seat", example = "10")
    private Integer seat;

    @Schema(description = "seat description i18n key", example = "silver_pl")
    private String seatDescI18nName;

    @Schema(description = "month", example = "6")
    private Integer month;

    @Schema(description = "discount amount (unit: yuan)", example = "999.99")
    private BigDecimal priceDiscount;

    @Schema(description = "original price (unit: yuan)", example = "19998.11")
    private BigDecimal priceOrigin;

    @Schema(description = "p`ayment amount (unit: yuan)", example = "18998.11")
    private BigDecimal pricePaid;

    /**
     * fromPrice.
     */
    public static ProductPriceVo fromPrice(BillingPlanPrice planPrice) {
        ProductPriceVo vo = new ProductPriceVo();
        vo.setProduct(planPrice.getProduct().toUpperCase(Locale.ROOT));
        vo.setPriceId(planPrice.getPriceId());
        vo.setSeat(planPrice.getSeat());
        vo.setSeatDescI18nName(planPrice.getSeatDesc());
        vo.setMonth(planPrice.getMonth());
        vo.setPriceOrigin(OrderUtil.toCurrencyUnit(planPrice.getOrigin()));
        vo.setPriceDiscount(OrderUtil.toCurrencyUnit(planPrice.getDiscount()));
        vo.setPricePaid(OrderUtil.toCurrencyUnit(planPrice.getActual()));
        return vo;
    }
}
