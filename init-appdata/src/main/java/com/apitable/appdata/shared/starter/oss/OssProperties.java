package com.apitable.appdata.shared.starter.oss;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "starter.oss")
public class OssProperties {

    private boolean enabled = false;

    private String accessKeyId;

    private String accessKeySecret;

    private String endpoint;

    private String region;

    private String bucketPolicy;

    private Signature signature;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getBucketPolicy() {
        return bucketPolicy;
    }

    public void setBucketPolicy(String bucketPolicy) {
        this.bucketPolicy = bucketPolicy;
    }

    public Signature getSignature() {
        return signature;
    }

    public void setSignature(Signature signature) {
        this.signature = signature;
    }

    public static class Signature {

        private boolean enabled = false;

        /**
         * timestamp anti leech encrypt key
         */
        private String encryptKey;

        private Integer expireSecond;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getEncryptKey() {
            return encryptKey;
        }

        public void setEncryptKey(String encryptKey) {
            this.encryptKey = encryptKey;
        }

        public Integer getExpireSecond() {
            return expireSecond;
        }

        public void setExpireSecond(Integer expireSecond) {
            this.expireSecond = expireSecond;
        }
    }

    public enum SignatureModel {

        // PRIVATE_BUCKET_CDN_TOKEN,

        CDN_TIMESTAMP_ANTI_LEECH,

    }
}
