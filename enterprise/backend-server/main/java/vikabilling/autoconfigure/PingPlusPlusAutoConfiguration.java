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

package com.apitable.enterprise.vikabilling.autoconfigure;

import com.pingplusplus.Pingpp;

import com.apitable.enterprise.vikabilling.autoconfigure.properties.PingProperties;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(Pingpp.class)
@ConditionalOnProperty(value = "pingpp.enabled", havingValue = "true")
public class PingPlusPlusAutoConfiguration {

    private final PingProperties pingProperties;

    public PingPlusPlusAutoConfiguration(PingProperties pingProperties) {
        this.pingProperties = pingProperties;
    }

    @Bean
    public PingInit pingInit() {
        return new PingInit(pingProperties);
    }
}
