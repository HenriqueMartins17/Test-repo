package com.apitable.enterprise.stripe.config;

import com.apitable.enterprise.apitablebilling.enums.ProductEnum;
import com.apitable.enterprise.apitablebilling.enums.RecurringInterval;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * product catalog factory.
 */
public class ProductCatalogFactory {

    public static volatile ProductCatalog INSTANCE;

    /**
     * init product catalog feature.
     */
    public static void appendFeature() {
        PlanFeatures features = PlanFeatureLoader.getConfig();
        INSTANCE.getProducts().forEach(product -> {
            ProductEnum productEnum = ProductEnum.of(product.getName());
            if (features.containsKey(productEnum)) {
                product.setFeature(features.get(productEnum));
            }
        });
    }

    /**
     * find product by product id.
     *
     * @param productId product id
     * @return product optional
     */
    public static Optional<Product> findByProductId(String productId) {
        return INSTANCE.findByProductId(productId);
    }

    /**
     * find product by product enum.
     *
     * @param product        product enum
     * @param throwException throw exception if not found
     * @return product or null
     */
    public static Product findProduct(ProductEnum product, boolean throwException) {
        Optional<Product> productOptional = INSTANCE.findByProductName(product.getName());
        if (throwException) {
            return productOptional.orElseThrow(() -> new RuntimeException("product not found"));
        }
        return productOptional.orElse(null);
    }

    /**
     * find price by product and price id.
     *
     * @param productEnum product enum
     * @param priceId     price id
     * @return price optional
     */
    public static boolean findPrice(ProductEnum productEnum, String priceId) {
        List<ProductEnum> ignoredList = Arrays.asList(
            ProductEnum.STARTER,
            ProductEnum.BUSINESS,
            ProductEnum.COMMUNITY,
            ProductEnum.APITABLE_ENTERPRISE,
            ProductEnum.AITABLE_PREMIUM,
            ProductEnum.APPSUMO_TIER1,
            ProductEnum.APPSUMO_TIER2,
            ProductEnum.APPSUMO_TIER3,
            ProductEnum.APPSUMO_TIER4,
            ProductEnum.APPSUMO_TIER5,
            ProductEnum.EXCLUSIVE_LIMITED_TIER1,
            ProductEnum.EXCLUSIVE_LIMITED_TIER2,
            ProductEnum.EXCLUSIVE_LIMITED_TIER3,
            ProductEnum.EXCLUSIVE_LIMITED_TIER4,
            ProductEnum.EXCLUSIVE_LIMITED_TIER5);
        if (ignoredList.contains(productEnum)) {
            return true;
        }
        Product product = findProduct(productEnum, true);
        return product.findPrice(priceId).isPresent();
    }

    /**
     * reverse price id by interval in same product.
     *
     * @param productId product id
     * @param interval  interval
     * @return price id or null
     */
    public static String reverseInterval(String productId, RecurringInterval interval) {
        Optional<Product> product = findByProductId(productId);
        Optional<Price> price = product.flatMap(value -> value.reverseInterval(interval));
        return price.map(Price::getId).orElse(null);
    }

    /**
     * get product feature.
     *
     * @param product product enum
     * @return product feature
     */
    public static PlanFeature getProductFeature(ProductEnum product) {
        return findProduct(product, true).getFeature();
    }
}
