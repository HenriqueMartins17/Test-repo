/*
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up
 * license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its
 * subdirectories does not constitute permission to use this code or APITable Enterprise Edition
 * features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

package com.apitable.enterprise.security.afs.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>
 * AfsProperties.
 * </p>
 *
 * @author Chambers
 */
@ConfigurationProperties(prefix = "afs")
public class AfsProperties {

    private boolean enabled = false;

    private AfsType type;

    private Aliyun aliyun;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public AfsType getType() {
        return type;
    }

    public void setType(AfsType type) {
        this.type = type;
    }

    public Aliyun getAliyun() {
        return aliyun;
    }

    public void setAliyun(Aliyun aliyun) {
        this.aliyun = aliyun;
    }

    public enum AfsType {

        ALIYUN
    }

    /**
     * Aliyun afs properties
     */
    public static class Aliyun {

        private String regionId;

        private String accessKeyId;

        private String secret;

        public String getRegionId() {
            return regionId;
        }

        public void setRegionId(String regionId) {
            this.regionId = regionId;
        }

        public String getAccessKeyId() {
            return accessKeyId;
        }

        public void setAccessKeyId(String accessKeyId) {
            this.accessKeyId = accessKeyId;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }
    }
}
