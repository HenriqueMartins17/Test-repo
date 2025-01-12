package com.apitable.appdata.generator.config.properties;


import com.apitable.appdata.shared.starter.api.model.ConfigDatasheet;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "generator.init-setting-oss")
public class InitSettingOssProperties {

    private boolean skip;

    private String host;

    private String token;

    private ConfigDatasheet base;

    private ConfigDatasheet customization;
}
