package com.apitable.appdata.initializer.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "initializer.user")
public class InitUserProperties {

    private Boolean batchEnabled;

    private Integer count;

    private String emailPrefix;

    private String emailSuffix;

}
