package com.apitable.appdata.shared.template.pojo;

import lombok.Data;

@Data
public class TemplateProperty {

    private Long id;

    /**
     * Type(0: template category, 1: template tag)
     */
    private Integer propertyType;

    private String propertyName;

    private String propertyCode;

    private String i18nName;

    public TemplateProperty() {
    }

    public TemplateProperty(Integer propertyType, String propertyName, String propertyCode, String i18nName) {
        this.propertyType = propertyType;
        this.propertyName = propertyName;
        this.propertyCode = propertyCode;
        this.i18nName = i18nName;
    }
}
