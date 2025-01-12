package com.apitable.enterprise.ai.server;

import lombok.Getter;

/**
 * AI Server.
 *
 * @author Shawn Deng
 */
public class AiServer {

    public static final String LIVE_BASE_URL = "https://aitable.ai";

    @Getter
    private static volatile String baseUrl = LIVE_BASE_URL;

    public static void overrideBaseUrl(final String overriddenApiBase) {
        baseUrl = overriddenApiBase;
    }
}
