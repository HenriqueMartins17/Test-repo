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

package com.apitable.enterprise.vika.autoconfigure;

import cn.vika.client.api.VikaApiClient;

import com.apitable.enterprise.vika.core.VikaOperations;
import com.apitable.enterprise.vika.core.VikaTemplate;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * autoconfiguration of vika sdk
 * </p>
 *
 * @author Chambers
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(VikaApiClient.class)
@EnableConfigurationProperties(VikaProperties.class)
@ConditionalOnProperty(value = "vika.enabled", havingValue = "true")
public class VikaAutoConfiguration {

    private final VikaProperties properties;

    public VikaAutoConfiguration(VikaProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean(VikaOperations.class)
    public VikaOperations vikaOperations() {
        return new VikaTemplate(properties.getHost(), properties.getToken());
    }
}
