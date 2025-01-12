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

package com.apitable.enterprise.appstore.autoconfigure.yozo;

import com.apitable.enterprise.appstore.component.yozo.YozoConfig;
import com.apitable.enterprise.appstore.component.yozo.YozoTemplate;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * autoconfiguration of yozo
 * @author Shawn Deng
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(YozoProperties.class)
@ConditionalOnClass(YozoTemplate.class)
@ConditionalOnProperty(value = "yozo.enabled", havingValue = "true")
public class YozoAutoConfiguration {

    private final YozoProperties properties;

    public YozoAutoConfiguration(YozoProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean(YozoTemplate.class)
    public YozoTemplate yozoTemplate() {
        YozoConfig config = new YozoConfig();
        config.setAppId(properties.getAppId());
        config.setKey(properties.getKey());
        YozoConfig.Uri uri = new YozoConfig.Uri();
        uri.setPreview(properties.getUri().getPreview());
        config.setUri(uri);
        return new YozoTemplate(config);
    }
}
