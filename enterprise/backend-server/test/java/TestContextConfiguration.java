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

package com.apitable.enterprise;

import com.apitable.enterprise.vikabilling.service.IBundleService;
import com.apitable.enterprise.vikabilling.service.IOrderV2Service;
import com.apitable.enterprise.vikabilling.service.ISpaceSubscriptionService;
import com.apitable.enterprise.vikabilling.util.EntitlementChecker;
import com.apitable.enterprise.vikabilling.util.OrderChecker;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestContextConfiguration {

    private final IBundleService iBundleService;

    private final IOrderV2Service iOrderV2Service;

    private final ISpaceSubscriptionService iSpaceSubscriptionService;

    public TestContextConfiguration(IBundleService iBundleService, IOrderV2Service iOrderV2Service,
                                    ISpaceSubscriptionService iSpaceSubscriptionService) {
        this.iBundleService = iBundleService;
        this.iOrderV2Service = iOrderV2Service;
        this.iSpaceSubscriptionService = iSpaceSubscriptionService;
    }

    @Bean
    public EntitlementChecker entitlementChecker() {
        return new EntitlementChecker(iBundleService, iSpaceSubscriptionService);
    }

    @Bean
    public OrderChecker orderChecker() {
        return new OrderChecker(iOrderV2Service);
    }
}
