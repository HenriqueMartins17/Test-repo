package com.apitable.enterprise.stripe.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class StripeLiveProductCatalogLoaderTest {

    @Test
    void testLoadJsonFile() {
        ProductCatalog productCatalog = StripeLiveProductCatalogLoader.getConfig();
        assertThat(productCatalog).isNotNull();
        assertThat(productCatalog.getProducts()).isNotEmpty();
    }
}
