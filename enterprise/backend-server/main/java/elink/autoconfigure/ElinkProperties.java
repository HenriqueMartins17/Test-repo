/*
 * APITable <https://github.com/apitable/apitable>
 * Copyright (C) 2022 APITable Ltd. <https://apitable.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.apitable.enterprise.elink.autoconfigure;

import java.util.Collections;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "connector.elink")
public class ElinkProperties {
    //enabled
    private boolean enabled = false;

    // Elink corpId
    private String corpId;

    /**
     * Third-party enterprise application customization service application.
     */
    private List<AgentAppProperty> agentApp = Collections.emptyList();

    //Be careful not to add / at the end
    private String baseUrl;


    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getCorpId() {
        return corpId;
    }

    public void setCorpId(String corpId) {
        this.corpId = corpId;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public List<AgentAppProperty> getAgentApp() {
        return agentApp;
    }

    public void setAgentApp(List<AgentAppProperty> agentApp) {
        this.agentApp = agentApp;
    }

    /**
     * Check whether the configuration is filled
     *
     * @param properties ElinkProperties
     */
    public static void checkAppProperties(ElinkProperties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("Dingtalk properties should not be null, must be set");
        }

        if (properties.getAgentApp().size() == 0) {
            throw new IllegalArgumentException("illegal elink App info: " + properties);
        }
    }

    public static class AgentAppProperty {

        /**
         * Agent Id
         */
        private String agentId;

        /**
         * Application key
         */
        private  String agentSecret;

        /**
         * Callback domain name, different agentId configuration domain names are different.
         */
        private String callbackDomain;

        private String qrDomain;

        public String getAgentSecret() {
            return agentSecret;
        }

        public void setAgentSecret(String agentSecret) {
            this.agentSecret = agentSecret;
        }

        public String getAgentId() {
            return agentId;
        }

        public void setAgentId(String agentId) {
            this.agentId = agentId;
        }

        public String getCallbackDomain() {
            return callbackDomain;
        }

        public void setCallbackDomain(String callbackDomain) {
            this.callbackDomain = callbackDomain;
        }

        public String getQrDomain() {
            return qrDomain;
        }

        public void setQrDomain(String qrDomain) {
            this.qrDomain = qrDomain;
        }
    }
}
