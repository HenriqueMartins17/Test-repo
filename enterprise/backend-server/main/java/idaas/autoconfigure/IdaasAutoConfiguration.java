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

package com.apitable.enterprise.idaas.autoconfigure;

import com.apitable.enterprise.idaas.infrastructure.IdaasConfig;
import com.apitable.enterprise.idaas.infrastructure.IdaasTemplate;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "idaas.enabled", havingValue = "true")
public class IdaasAutoConfiguration {

    @Bean
    public IdaasTemplate idaasTemplate(IdaasProperties idaasProperties) {
        IdaasConfig idaasConfig = new IdaasConfig();
        idaasConfig.setSystemHost(idaasProperties.getManageHost());
        idaasConfig.setContactHost(idaasProperties.getContactHost());

        return new IdaasTemplate(idaasConfig);
    }

}
