package com.apitable.appdata.shared.template.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TemplateAlbumRelType {

    TEMPLATE_CATEGORY(0),

    TEMPLATE(1),

    TEMPLATE_TAG(2),

    ;

    private int type;
}
