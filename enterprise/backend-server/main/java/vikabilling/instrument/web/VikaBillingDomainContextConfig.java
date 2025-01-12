/*
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its subdirectories does not constitute permission to use this code or APITable Enterprise Edition features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

package com.apitable.enterprise.vikabilling.instrument.web;

import com.apitable.enterprise.vikabilling.interfaces.facade.EnterpriseEntitlementServiceFacadeImpl;
import com.apitable.enterprise.vikabilling.service.ISpaceSubscriptionService;
import com.apitable.interfaces.billing.facade.EntitlementServiceFacade;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Vika SaaS Billing domain configuration.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "billing.vika.enabled", havingValue = "true")
public class VikaBillingDomainContextConfig {

    @Bean
    @Primary
    @ConditionalOnBean(ISpaceSubscriptionService.class)
    public EntitlementServiceFacade enterpriseEntitlementServiceFacadeImpl(
        ISpaceSubscriptionService spaceSubscriptionService) {
        return new EnterpriseEntitlementServiceFacadeImpl(spaceSubscriptionService);
    }
}
