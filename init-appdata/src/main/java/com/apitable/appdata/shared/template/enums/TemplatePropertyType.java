package com.apitable.appdata.shared.template.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TemplatePropertyType {

    CATEGORY(0),

    TAG(1),

    ;

    private final int type;
}
