package com.apitable.enterprise.stripe.config;

import com.apitable.enterprise.apitablebilling.enums.BillingMode;
import java.util.Objects;
import lombok.Data;

/**
 * price object.
 */
@Data
public class Price {

    private String id;

    private String productId;

    private String name;

    private String description;

    private String type;

    private String interval;

    private Long intervalCount;

    private String currency;

    private Long unitAmount;

    public static Price of(com.stripe.model.Price price) {
        Price priceObject = new Price();
        priceObject.setId(price.getId());
        priceObject.setProductId(price.getProduct());
        priceObject.setName(price.getNickname());
        priceObject.setType(price.getType());
        if (Objects.equals(price.getType(), BillingMode.RECURRING.getName())) {
            priceObject.setInterval(price.getRecurring().getInterval());
            priceObject.setIntervalCount(price.getRecurring().getIntervalCount());
        }
        priceObject.setCurrency(price.getCurrency());
        priceObject.setUnitAmount(price.getUnitAmount());
        return priceObject;
    }
}
