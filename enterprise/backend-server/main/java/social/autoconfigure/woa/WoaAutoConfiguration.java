/*
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up
 * license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its
 * subdirectories does not constitute permission to use this code or APITable Enterprise Edition
 * features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

package com.apitable.enterprise.social.autoconfigure.woa;

import com.apitable.enterprise.social.infrastructure.WoaTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * autoconfiguration of Woa
 * </p>
 * @author penglong feng
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(WoaProperties.class)
@ConditionalOnProperty(value = "woa.enabled", havingValue = "true")
public class WoaAutoConfiguration {

    private final WoaProperties woaProperties;

    public WoaAutoConfiguration(WoaProperties woaProperties) {
        this.woaProperties = woaProperties;
    }

    @Bean
    public WoaTemplate woaTemplate() {
        return new WoaTemplate(woaProperties.getBaseUrl(), woaProperties.getTenantId(),
            woaProperties.getQueryableUserStatus());
    }
}
