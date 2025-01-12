package com.apitable.enterprise.stripe.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.apitable.enterprise.apitablebilling.enums.ProductEnum;
import com.apitable.enterprise.apitablebilling.enums.RecurringInterval;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class ProductTest {

    @Test
    void testGetPriceWithFree() {
        ProductCatalogFactory.INSTANCE = StripeProductCatalogLoader.getConfig();
        Product product = ProductCatalogFactory.findProduct(ProductEnum.FREE, true);
        Price price = product.getPrice(RecurringInterval.MONTH.name());
        assertThat(price).isNull();
    }

    @Test
    void testGetPriceWithStarter() {
        ProductCatalogFactory.INSTANCE = StripeProductCatalogLoader.getConfig();
        Product product = ProductCatalogFactory.findProduct(ProductEnum.STARTER, true);
        Price price = product.getPrice(RecurringInterval.MONTH.name());
        assertThat(price).isNotNull();
    }

    @Test
    void testFindPriceWithoutExist() {
        ProductCatalogFactory.INSTANCE = StripeProductCatalogLoader.getConfig();
        Product product = ProductCatalogFactory.findProduct(ProductEnum.STARTER, true);
        Optional<Price> price = product.findPrice("not-exist");
        assertThat(price).isNotPresent();
    }

    @Test
    void testFindPriceWithExist() {
        ProductCatalogFactory.INSTANCE = StripeProductCatalogLoader.getConfig();
        Product product = ProductCatalogFactory.findProduct(ProductEnum.STARTER, true);
        Optional<Price> price = product.findPrice("price_1OMQdBH4MhQOilwxAWjBfF56");
        assertThat(price).isPresent();
    }

    @Test
    void testFindPriceWithNullPrice() {
        ProductCatalogFactory.INSTANCE = StripeProductCatalogLoader.getConfig();
        Product product = ProductCatalogFactory.findProduct(ProductEnum.APPSUMO_TIER1, true);
        Optional<Price> price = product.findPrice("not-exist");
        assertThat(price).isEmpty();
    }
    
    @Test
    void testReverseInterval() {
        ProductCatalogFactory.INSTANCE = StripeProductCatalogLoader.getConfig();
        Product product = ProductCatalogFactory.findProduct(ProductEnum.STARTER, true);
        Price beforePrice = product.getPrice(RecurringInterval.MONTH.name());
        assertThat(beforePrice).isNotNull();
        Optional<Price> reverseIntervalPrice =
            product.reverseInterval(RecurringInterval.MONTH);
        assertThat(reverseIntervalPrice).isPresent();
        assertThat(reverseIntervalPrice.get().getId()).isNotBlank()
            .isNotEqualTo(beforePrice.getId());
    }
}
