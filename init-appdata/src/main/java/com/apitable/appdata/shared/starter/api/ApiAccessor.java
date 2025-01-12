package com.apitable.appdata.shared.starter.api;

import cn.vika.client.api.VikaApiClient;
import cn.vika.client.api.http.ApiCredential;

public abstract class ApiAccessor {

    private final String hostUrl;

    private final String token;

    public ApiAccessor(String hostUrl, String token) {
        this.hostUrl = hostUrl;
        this.token = token;
    }

    protected VikaApiClient getClient() {
        ApiCredential credential = new ApiCredential(token);
        return hostUrl != null && !hostUrl.isEmpty() ? new VikaApiClient(hostUrl, credential) : new VikaApiClient(credential);
    }

    protected VikaApiClient getClient(String host, String token) {
        ApiCredential credential = new ApiCredential(token);
        return host != null && !host.isEmpty() ? new VikaApiClient(host, credential) : this.getClient();
    }
}
