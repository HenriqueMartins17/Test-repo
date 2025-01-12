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

package com.apitable.enterprise.vikabilling.util.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.apitable.enterprise.vikabilling.setting.Event;
import com.apitable.enterprise.vikabilling.setting.Price;
import com.apitable.enterprise.vikabilling.setting.PriceList;

import static com.apitable.enterprise.vikabilling.util.BillingConfigManager.getByEventId;
import static com.apitable.enterprise.vikabilling.util.BillingConfigManager.getByPriceListId;

public class BillingPlanPrice {

    private final Price price;

    private final BigDecimal discount;

    private final BigDecimal actual;

    public BillingPlanPrice(Price price, LocalDate now) {
        this.price = price;
        this.discount = calculateDiscount(price, now);
        this.actual = price.getOriginPrice() != null && price.getOriginPrice().compareTo(BigDecimal.ZERO) > 0 ?
                price.getOriginPrice().subtract(this.discount) : BigDecimal.ZERO;
    }

    private BigDecimal calculateDiscount(Price price, LocalDate now) {
        if (price.getPriceListId() == null) {
            return BigDecimal.ZERO;
        }
        PriceList priceList = getByPriceListId(price.getPriceListId());
        if (priceList == null) {
            return BigDecimal.ZERO;
        }
        Event event = getByEventId(priceList.getEvent());
        if (event == null) {
            return BigDecimal.ZERO;
        }
        if (event.getStartDate() != null && event.getStartDate().compareTo(now) > 0) {
            return BigDecimal.ZERO;
        }
        if (event.getEndDate() != null && event.getEndDate().compareTo(now) < 0) {
            return BigDecimal.ZERO;
        }
        if (priceList.getDiscountAmount() == null) {
            return BigDecimal.ZERO;
        }
        return priceList.getDiscountAmount();
    }

    public static BillingPlanPrice of(Price price, LocalDate now) {
        return new BillingPlanPrice(price, now);
    }

    public String getPriceId() {
        return price.getId();
    }

    public String getGoodEnTitle() {
        return price.getGoodEnTitle();
    }

    public String getGoodChTitle() {
        return price.getGoodChTitle();
    }

    public String getPlanId() {
        return price.getPlanId();
    }

    public Integer getMonth() {
        return price.getMonth();
    }

    public String getProduct() {
        return price.getProduct();
    }

    public Integer getSeat() {
        return price.getSeat();
    }

    public String getSeatDesc() {
        return price.getSeatDesc();
    }

    public boolean isOnline() {
        return price.isOnline();
    }

    public BigDecimal getOrigin() {
        return price.getOriginPrice();
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public BigDecimal getActual() {
        return actual;
    }
}
