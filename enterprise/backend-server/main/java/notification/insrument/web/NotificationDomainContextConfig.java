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

package com.apitable.enterprise.notification.insrument.web;

import com.apitable.enterprise.notification.interfeces.facade.APITableMailFacadeImpl;
import com.apitable.enterprise.notification.interfeces.facade.VikaMailFacadeImpl;
import com.apitable.interfaces.notification.facade.MailFacade;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * notification domain context config.
 */
@Configuration(proxyBeanMethods = false)
public class NotificationDomainContextConfig {

    @Bean
    @Primary
    @ConditionalOnProperty(value = "edition", havingValue = "vika-saas")
    public MailFacade vikaSaasMailFacadeImpl() {
        return new VikaMailFacadeImpl();
    }

    @Bean
    @Primary
    @ConditionalOnProperty(value = "edition", havingValue = "apitable-saas")
    public MailFacade apiTableSaasMailFacadeImpl() {
        return new APITableMailFacadeImpl();
    }
}
