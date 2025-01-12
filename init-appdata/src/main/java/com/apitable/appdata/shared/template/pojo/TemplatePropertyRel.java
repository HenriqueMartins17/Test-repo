package com.apitable.appdata.shared.template.pojo;

import lombok.Data;

@Data
public class TemplatePropertyRel {

    private Long id;

    private String templateId;

    private String propertyCode;

    private Integer propertyOrder;

    public TemplatePropertyRel() {
    }

    public TemplatePropertyRel(String templateId, String propertyCode, Integer propertyOrder) {
        this.templateId = templateId;
        this.propertyCode = propertyCode;
        this.propertyOrder = propertyOrder;
    }
}
