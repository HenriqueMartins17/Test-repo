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

package com.apitable.enterprise.social.autoconfigure.qq;

import com.vikadata.social.qq.AppConfig;
import com.vikadata.social.qq.QQTemplate;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * autoconfiguration of tencent open platform
 *
 * @author Chambers
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(QQTemplate.class)
@ConditionalOnProperty(value = "tencent.webapp.enabled", havingValue = "true")
@EnableConfigurationProperties(WebAppProperties.class)
public class WebAppServiceAutoConfiguration {

    private final WebAppProperties properties;

    public WebAppServiceAutoConfiguration(WebAppProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public QQTemplate qqTemplate() {
        return new QQTemplate(applyConfig());
    }

    private AppConfig applyConfig() {
        AppConfig config = new AppConfig();
        config.setAppId(properties.getAppId());
        config.setAppKey(properties.getAppKey());
        config.setRedirectUri(properties.getRedirectUri());
        config.setApplyUnion(properties.isApplyUnion());
        return config;
    }
}
