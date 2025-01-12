package com.apitable.enterprise.selfhostbilling.instrument.web;

import com.apitable.enterprise.selfhostbilling.interfaces.facade.SelfhostEntitleServiceFacadeImpl;
import com.apitable.enterprise.selfhostbilling.service.ISelfhostEntitlementService;
import com.apitable.interfaces.billing.facade.EntitlementServiceFacade;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Self-host Billing domain configuration.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "billing.selfhost.enabled", havingValue = "true")
public class SelfHostBillingDomainContextConfig {

    @Bean
    @Primary
    public EntitlementServiceFacade selfhostEntitlementServiceFacade(
        ISelfhostEntitlementService entitlementService) {

        return new SelfhostEntitleServiceFacadeImpl(entitlementService);
    }
}
