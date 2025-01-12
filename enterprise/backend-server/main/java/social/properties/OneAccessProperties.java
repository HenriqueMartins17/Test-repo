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

package com.apitable.enterprise.social.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import static com.apitable.enterprise.social.properties.OneAccessProperties.PREFIX;


@ConfigurationProperties(prefix = PREFIX)
public class OneAccessProperties {
    public static final String PREFIX = "oneaccess";

    /**
     * OneAccess function switch
     */
    private Boolean enabled = Boolean.FALSE;

    /**
     * Iam server Host
     */
    private String iamHost;

    /**
     * Client application registration ID (provided by IAM)
     */
    private String clientId;

    /**
     * Client application registration secret (provided by IAM)
     */
    private String clientSecret;

    /**
     *  encryption authentication key
     */
    private String encryptKey;

    /**
     * Encryption algorithm, only supports AES
     */
    private String encryptAlg;

    /**
     * Collaboration spaceId
     */
    private String collaborationSpaceId;

    /**
     * Government WeCom configuration
     */
    private WeCom weCom;

    public Boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getIamHost() {
        return iamHost;
    }

    public void setIamHost(String iamHost) {
        this.iamHost = iamHost;
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

    public String getEncryptKey() {
        return encryptKey;
    }

    public void setEncryptKey(String encryptKey) {
        this.encryptKey = encryptKey;
    }

    public String getEncryptAlg() {
        return encryptAlg;
    }

    public void setEncryptAlg(String encryptAlg) {
        this.encryptAlg = encryptAlg;
    }


    public String getCollaborationSpaceId() {
        return collaborationSpaceId;
    }

    public void setCollaborationSpaceId(String collaborationSpaceId) {
        this.collaborationSpaceId = collaborationSpaceId;
    }


    public WeCom getWeCom() {
        return weCom;
    }

    public void setWeCom(WeCom weCom) {
        this.weCom = weCom;
    }

    public static class WeCom {
        private String corpid;

        private String secret;

        private String baseApiUrl;

        private Integer agentId;

        public String getCorpid() {
            return corpid;
        }

        public void setCorpid(String corpid) {
            this.corpid = corpid;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public String getBaseApiUrl() {
            return baseApiUrl;
        }

        public void setBaseApiUrl(String baseApiUrl) {
            this.baseApiUrl = baseApiUrl;
        }

        public Integer getAgentId() {
            return agentId;
        }

        public void setAgentId(Integer agentId) {
            this.agentId = agentId;
        }
    }

}
