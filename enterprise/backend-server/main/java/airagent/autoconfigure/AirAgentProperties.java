package com.apitable.enterprise.airagent.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * airagent properties.
 */
@Data
@ConfigurationProperties(prefix = "air-agent")
public class AirAgentProperties {

    /**
     * whether enabled this feature.
     */
    private boolean enabled = false;

    private String homePagePath = "home";
}
