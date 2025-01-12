package com.apitable.appdata.initializer.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "initializer.space")
public class InitConfigSpaceProperties {

    private Boolean enabled = false;

    private String configSpaceId;

    private String adminUserCredential;

    private Boolean mandatoryCoverageEnabled = false;

    private Boolean createConfigTableEnabled = false;

}
