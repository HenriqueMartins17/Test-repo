/*
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its subdirectories does not constitute permission to use this code or APITable Enterprise Edition features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

package com.apitable.enterprise.vika.core;

import cn.vika.client.api.VikaApiClient;
import cn.vika.client.api.http.ApiCredential;

/**
 * <p>
 * vika sdk abstract class
 * </p>
 *
 */
public abstract class VikaAccessor {

    private final String hostUrl;

    private final String token;

    public VikaAccessor(String hostUrl, String token) {
        this.hostUrl = hostUrl;
        this.token = token;
    }

    protected VikaApiClient getClient() {
        ApiCredential credential = new ApiCredential(token);
        return hostUrl != null && !hostUrl.isEmpty() ? new VikaApiClient(hostUrl, credential) : new VikaApiClient(credential);
    }

    protected VikaApiClient getClient(String host, String token) {
        ApiCredential credential = new ApiCredential(token);
        return new VikaApiClient(host, credential);
    }

    protected String getHostUrl() {
        return hostUrl;
    }
}
