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

package com.apitable.enterprise.k11.instrument.web;

import com.apitable.enterprise.k11.interfaces.facade.K11AuthServiceFacadeImpl;
import com.apitable.enterprise.k11.interfaces.facade.K11CaptchaServiceFacadeImpl;
import com.apitable.enterprise.k11.service.K11Service;
import com.apitable.interfaces.auth.facade.AuthServiceFacade;
import com.apitable.interfaces.security.facade.CaptchaServiceFacade;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "connector.k11.enabled", havingValue = "true")
public class K11ContextConfig {

    @Bean
    @Primary
    public AuthServiceFacade k11AuthServiceFacade(K11Service k11Service) {
        return new K11AuthServiceFacadeImpl(k11Service);
    }

    @Bean
    @Primary
    public CaptchaServiceFacade k11CaptchaServiceFacade(K11Service k11Service) {
        return new K11CaptchaServiceFacadeImpl(k11Service);
    }
}
