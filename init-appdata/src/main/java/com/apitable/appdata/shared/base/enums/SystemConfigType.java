package com.apitable.appdata.shared.base.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SystemConfigType {

    WIZARD_CONFIG(0),

    RECOMMEND_CONFIG(1),

    OPS_PERMISSION_CONFIG(2),

    ;

    private final int type;
}
