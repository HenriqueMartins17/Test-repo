package com.apitable.enterprise.stripe.autoconfigure;

import cn.hutool.core.util.StrUtil;
import com.apitable.enterprise.stripe.core.StripeTemplate;
import com.stripe.Stripe;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * stripe auto configuration.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(Stripe.class)
@ConditionalOnProperty(value = "stripe.enabled", havingValue = "true")
public class StripeAutoConfiguration {

    private final StripeProperties stripeProperties;

    public StripeAutoConfiguration(StripeProperties stripeProperties) {
        this.stripeProperties = stripeProperties;
    }

    /**
     * stripe api template.
     *
     * @return StripeTemplate
     */
    @Bean
    public StripeTemplate stripeTemplate() {
        if (StrUtil.isBlank(stripeProperties.getApiKey())) {
            throw new RuntimeException("Please Set Stripe Api Key");
        }
        Stripe.apiKey = stripeProperties.getApiKey();
        return new StripeTemplate(stripeProperties);
    }
}
