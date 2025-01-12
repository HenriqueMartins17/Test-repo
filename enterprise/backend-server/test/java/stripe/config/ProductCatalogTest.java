package com.apitable.enterprise.stripe.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.apitable.enterprise.apitablebilling.enums.ProductEnum;
import org.junit.jupiter.api.Test;

public class ProductCatalogTest {

    @Test
    void testFullProductCatalog() {
        ProductCatalog productCatalog = StripeProductCatalogLoader.getConfig();
        assertThat(productCatalog).isNotNull();
        assertThat(productCatalog.getProducts()).isNotEmpty()
            .map(product -> ProductEnum.of(product.getName()))
            .containsOnly(
                ProductEnum.FREE,
                ProductEnum.STARTER,
                ProductEnum.PLUS,
                ProductEnum.PRO,
                ProductEnum.BUSINESS,
                ProductEnum.ENTERPRISE,
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
                ProductEnum.EXCLUSIVE_LIMITED_TIER5
            );
    }

    @Test
    void testFilterHidden() {
        ProductCatalog productCatalog = StripeProductCatalogLoader.getConfig();
        ProductCatalog filteredProductCatalog = productCatalog.filterHidden();
        assertThat(filteredProductCatalog).isNotNull();
        assertThat(filteredProductCatalog.getProducts()).isNotEmpty()
            .map(product -> ProductEnum.of(product.getName()))
            .containsOnly(
                ProductEnum.FREE,
                ProductEnum.STARTER,
                ProductEnum.PLUS,
                ProductEnum.PRO,
                ProductEnum.BUSINESS,
                ProductEnum.ENTERPRISE,
                ProductEnum.COMMUNITY,
                ProductEnum.APITABLE_ENTERPRISE,
                ProductEnum.AITABLE_PREMIUM
            );
    }
}
