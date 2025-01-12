package com.apitable.enterprise.ai.autoconfigure;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AI server properties.
 *
 * @author Shawn Deng
 */
@Data
@ConfigurationProperties(prefix = "ai")
public class AiServerProperties {

    /**
     * whether enabled this feature.
     */
    private boolean enabled = false;

    /**
     * server address url.
     */
    private String serverUrl;

    private OpenAi openai;

    @Getter
    @Setter
    public static class OpenAi {

        private String baseUrl;

        private String apiKey;
    }
}
