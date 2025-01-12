package com.apitable.enterprise.stripe.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.apitable.enterprise.apitablebilling.enums.ProductEnum;
import com.apitable.enterprise.apitablebilling.enums.RecurringInterval;
import org.junit.jupiter.api.Test;

public class ProductCatalogFactoryTest {

    @Test
    void testReverseInterval() {
        ProductCatalogFactory.INSTANCE = StripeProductCatalogLoader.getConfig();
        Product product = ProductCatalogFactory.findProduct(ProductEnum.STARTER, true);
        Price beforePrice = product.getPrice(RecurringInterval.MONTH.name());
        assertThat(beforePrice).isNotNull();
        String reverseIntervalPriceId =
            ProductCatalogFactory.reverseInterval(product.getId(), RecurringInterval.MONTH);
        assertThat(reverseIntervalPriceId).isNotBlank().isNotEqualTo(beforePrice.getId());
    }

    @Test
    void testFindPriceWithIgnore() {
        ProductCatalogFactory.INSTANCE = StripeProductCatalogLoader.getConfig();
        boolean found = ProductCatalogFactory.findPrice(ProductEnum.STARTER, "no-exist");
        assertThat(found).isTrue();
    }

    @Test
    void testFindPriceWithoutExist() {
        ProductCatalogFactory.INSTANCE = StripeProductCatalogLoader.getConfig();
        boolean found = ProductCatalogFactory.findPrice(ProductEnum.PRO, "no-exist");
        assertThat(found).isFalse();
    }

    @Test
    void testFindPriceWithExist() {
        ProductCatalogFactory.INSTANCE = StripeProductCatalogLoader.getConfig();
        boolean found =
            ProductCatalogFactory.findPrice(ProductEnum.STARTER, "price_1OMQdBH4MhQOilwxAWjBfF56");
        assertThat(found).isTrue();
    }
}
