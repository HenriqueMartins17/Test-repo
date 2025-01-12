package com.apitable.appdata.shared.starter.api;

import cn.vika.client.api.VikaApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(VikaApiClient.class)
@EnableConfigurationProperties(ApiProperties.class)
@ConditionalOnProperty(value = "starter.api.enabled", havingValue = "true")
public class ApiAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiAutoConfiguration.class);

    private final ApiProperties properties;

    public ApiAutoConfiguration(ApiProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean(ApiTemplate.class)
    public ApiTemplate apiTemplate() {
        LOGGER.info("Api starter autoconfiguration finish.");
        return new ApiTemplate(properties.getHost(), properties.getToken());
    }
}
