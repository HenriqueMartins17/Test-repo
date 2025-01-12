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

package com.apitable.enterprise.k11.autoconfigure;

import com.apitable.enterprise.k11.infrastructure.K11Connector;
import com.apitable.enterprise.k11.infrastructure.K11Template;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * k11 connector autoconfiguration
 * </p>
 *
 * @author Chambers
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "connector.k11.enabled", havingValue = "true")
@EnableConfigurationProperties(K11Properties.class)
public class K11AutoConfiguration {

    private final K11Properties properties;

    public K11AutoConfiguration(K11Properties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public K11Connector connector() {
        return new K11Template(properties.getDomain(), properties.getAppId(), properties.getAppSecret(), properties.getSmsTempCode());
    }
}
