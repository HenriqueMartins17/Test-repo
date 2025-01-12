package com.apitable.enterprise.stripe.config;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * price config util.
 *
 * @author Shawn Deng
 */
@Deprecated(since = "1.7.0", forRemoval = true)
public class PriceConfigUtil {

    public static volatile StripePriceConfig priceConfig;

    /**
     * find product object through by product id.
     *
     * @return Product
     */
    public static Optional<ProductObject> findProductByProductId(String productId) {
        return priceConfig.values().stream()
            .filter(product -> product.getId().equals(productId))
            .findFirst();
    }

    /**
     * find trial price through by price id.
     *
     * @return Price Optional
     */
    public static Optional<PriceObject> findTrialPrice() {
        List<PriceObject> priceObjects =
            priceConfig.values().stream()
                .flatMap(product -> product.getPrices().stream())
                .collect(Collectors.toList());
        return priceObjects.stream()
            .filter(PriceObject::isTrial)
            .findFirst();
    }
}
