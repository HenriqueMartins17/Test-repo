package com.apitable.enterprise.airagent.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * air agent auto configuration.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AirAgentProperties.class)
@ConditionalOnProperty(value = "air-agent.enabled", havingValue = "true")
public class AirAgentAutoConfiguration {
}
