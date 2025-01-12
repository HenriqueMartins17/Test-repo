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

package com.apitable.enterprise.idaas.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "idaas")
public class IdaasProperties {
    private boolean enabled = false;

    /**
     * Whether to privatize the deployment. Default false
     */
    private boolean selfHosted = false;

    /**
     * Domain name of Yufu management interface, example：https://demo-admin.cig.tencentcs.com
     */
    private String manageHost;

    /**
     * Domain name of Yufu address book interface, example：https://{tenantName}-admin.cig.tencentcs.com
     */
    private String contactHost;

    /**
     * the host
     */
    private String serverHost;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isSelfHosted() {
        return selfHosted;
    }

    public void setSelfHosted(boolean selfHosted) {
        this.selfHosted = selfHosted;
    }

    public String getManageHost() {
        return manageHost;
    }

    public void setManageHost(String manageHost) {
        this.manageHost = manageHost;
    }

    public String getContactHost() {
        return contactHost;
    }

    public void setContactHost(String contactHost) {
        this.contactHost = contactHost;
    }

    public String getServerHost() {
        return serverHost;
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

}
