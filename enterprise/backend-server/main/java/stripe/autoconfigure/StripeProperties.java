package com.apitable.enterprise.stripe.autoconfigure;

import com.apitable.enterprise.stripe.config.PriceConfigUtil;
import com.apitable.enterprise.stripe.config.ProductCatalogFactory;
import com.apitable.enterprise.stripe.config.StripeLiveProductCatalogLoader;
import com.apitable.enterprise.stripe.config.StripePriceConfigLoader;
import com.apitable.enterprise.stripe.config.StripePriceLiveConfigLoader;
import com.apitable.enterprise.stripe.config.StripeProductCatalogLoader;
import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * stripe properties.
 *
 * @author Shawn Deng
 */
@Data
@ConfigurationProperties("stripe")
public class StripeProperties implements InitializingBean {

    private boolean enabled;

    private String apiKey;

    private String signatureSecret;

    public boolean isRunProdMode() {
        return apiKey != null && apiKey.startsWith("sk_live");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (isRunProdMode()) {
            PriceConfigUtil.priceConfig = StripePriceLiveConfigLoader.getConfig();
            ProductCatalogFactory.INSTANCE = StripeLiveProductCatalogLoader.getConfig();
            ProductCatalogFactory.appendFeature();
        } else {
            PriceConfigUtil.priceConfig = StripePriceConfigLoader.getConfig();
            ProductCatalogFactory.INSTANCE = StripeProductCatalogLoader.getConfig();
            ProductCatalogFactory.appendFeature();
        }
    }
}
