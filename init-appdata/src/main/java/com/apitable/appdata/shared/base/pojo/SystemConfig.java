package com.apitable.appdata.shared.base.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SystemConfig {

    private Long id;

    /**
     * Type(0:wizard; 1:template center recommend)
     */
    private Integer type;

    @JsonProperty("i18n_name")
    private String i18nName;

    @JsonProperty("config_map")
    private String configMap;

    public SystemConfig() {
    }

    public SystemConfig(Integer type, String i18nName, String configMap) {
        this.type = type;
        this.i18nName = i18nName;
        this.configMap = configMap;
    }
}
