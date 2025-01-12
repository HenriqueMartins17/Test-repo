package com.apitable.enterprise.aliyun.instrument.web;

import com.apitable.enterprise.aliyun.interfaces.facade.AliyunEntitleServiceFacadeImpl;
import com.apitable.interfaces.billing.facade.EntitlementServiceFacade;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Aliyun Billing domain configuration.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "billing.aliyun.enabled", havingValue = "true")
public class AliyunBillingDomainContextConfig {

    @Bean
    @Primary
    public EntitlementServiceFacade aliyunEntitlementServiceFacade() {
        return new AliyunEntitleServiceFacadeImpl();
    }
}
