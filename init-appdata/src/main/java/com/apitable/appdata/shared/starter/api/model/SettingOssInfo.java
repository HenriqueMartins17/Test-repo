package com.apitable.appdata.shared.starter.api.model;

public class SettingOssInfo {

    private String overrideKey;

    private String originAssetToken;

    private String mimeType;

    public String getOverrideKey() {
        return overrideKey;
    }

    public void setOverrideKey(String overrideKey) {
        this.overrideKey = overrideKey;
    }

    public String getOriginAssetToken() {
        return originAssetToken;
    }

    public void setOriginAssetToken(String originAssetToken) {
        this.originAssetToken = originAssetToken;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
