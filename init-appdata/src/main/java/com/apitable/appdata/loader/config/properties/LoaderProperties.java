package com.apitable.appdata.loader.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "loader")
public class LoaderProperties {

    private String ossBucketName;

    private boolean skipAssetLoad;

    private TemplateCenter templateCenter = new TemplateCenter();

    @Data
    public static class TemplateCenter {

        private boolean skip;

        private String templateSpaceId;

        private boolean skipConfig;
    }

    private WidgetCenter widgetCenter = new WidgetCenter();

    @Data
    public static class WidgetCenter {

        private boolean skip;

        private String widgetSpaceId;
    }

    private BaseOption automation = new BaseOption();

    private BaseOption labFeature = new BaseOption();

    private BaseOption wizard = new BaseOption();

    @Data
    public static class BaseOption {

        private boolean skip;
    }
}
