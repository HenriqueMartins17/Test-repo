/*
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its subdirectories does not constitute permission to use this code or APITable Enterprise Edition features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

package com.apitable.enterprise.vikabilling.core;

import java.math.BigDecimal;

/**
 * Order Price
 */
public class OrderPrice {

    private final BigDecimal priceOrigin;

    private final BigDecimal priceDiscount;

    private final BigDecimal priceUnusedCalculated;

    private BigDecimal pricePaid;

    public OrderPrice(BigDecimal priceOrigin, BigDecimal priceDiscount, BigDecimal priceUnusedCalculated, BigDecimal pricePaid) {
        this.priceOrigin = priceOrigin;
        this.priceDiscount = priceDiscount;
        this.priceUnusedCalculated = priceUnusedCalculated;
        this.pricePaid = pricePaid;
    }

    public BigDecimal getPriceOrigin() {
        return priceOrigin;
    }

    public BigDecimal getPriceDiscount() {
        return priceDiscount;
    }

    public BigDecimal getPriceUnusedCalculated() {
        return priceUnusedCalculated;
    }

    public void setPricePaid(BigDecimal pricePaid) {
        this.pricePaid = pricePaid;
    }

    public BigDecimal getPricePaid() {
        return pricePaid;
    }
}
