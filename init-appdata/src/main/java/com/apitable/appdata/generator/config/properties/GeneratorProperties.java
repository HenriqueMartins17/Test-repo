package com.apitable.appdata.generator.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "generator")
public class GeneratorProperties {

    private String ossHost;

    private boolean skipWidgetCenter;

    private boolean skipTemplateCenter;

    private String templateSpaceId;

    private boolean skipAutomation;

    private boolean skipLabFeature;

    private boolean skipWizard;

}
