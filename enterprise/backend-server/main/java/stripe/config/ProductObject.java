package com.apitable.enterprise.stripe.config;

import java.util.List;
import lombok.Data;

/**
 * product.
 */
@Data
@Deprecated(since = "1.7.0", forRemoval = true)
public class ProductObject {

    private String id;

    private String name;

    private String defaultPrice;

    private boolean mostPopular;

    private List<PriceObject> prices;
}
