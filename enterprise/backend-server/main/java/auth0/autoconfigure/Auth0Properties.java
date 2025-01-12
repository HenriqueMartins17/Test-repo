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

package com.apitable.enterprise.auth0.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * auth0 properties.
 */
@ConfigurationProperties(prefix = "auth0")
public class Auth0Properties {

    /**
     * whether enabled this feature.
     */
    private boolean enabled = false;

    /**
     * client id.
     */
    private String clientId;

    /**
     * client secret.
     */
    private String clientSecret;

    private String domain;

    /**
     * issuer uri.
     */
    private String issuerUri;

    /**
     * which Redirect uri after Login.
     */
    private String redirectUri;

    /**
     * DB connection Id.
     */
    @Deprecated
    private String dbConnectionId;

    /**
     * DB Connection Name.
     */
    private String dbConnectionName;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getIssuerUri() {
        return issuerUri;
    }

    public void setIssuerUri(String issuerUri) {
        this.issuerUri = issuerUri;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getDbConnectionId() {
        return dbConnectionId;
    }

    public void setDbConnectionId(String dbConnectionId) {
        this.dbConnectionId = dbConnectionId;
    }

    public String getDbConnectionName() {
        return dbConnectionName;
    }

    public void setDbConnectionName(String dbConnectionName) {
        this.dbConnectionName = dbConnectionName;
    }
}
