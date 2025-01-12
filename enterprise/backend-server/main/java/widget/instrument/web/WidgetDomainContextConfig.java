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

package com.apitable.enterprise.widget.instrument.web;

import com.apitable.enterprise.widget.interfaces.facade.EnterpriseWidgetServiceAuditFacadeImpl;
import com.apitable.enterprise.widget.service.IWidgetAuditService;
import com.apitable.interfaces.widget.facade.WidgetServiceAuditFacade;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration(proxyBeanMethods = false)
public class WidgetDomainContextConfig {

    @Bean
    @Primary
    public WidgetServiceAuditFacade enterpriseWidgetServiceAuditFacadeImpl(IWidgetAuditService iWidgetAuditService) {
        return new EnterpriseWidgetServiceAuditFacadeImpl(iWidgetAuditService);
    }
}
