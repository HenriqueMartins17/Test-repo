package com.apitable.appdata.shared.base.enums;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LabFeatureScope {

    USER_SCOPE("user", 1),

    SPACE_SCOPE("space", 2),

    ;

    private final String scopeName;

    private final Integer scopeCode;

    public static LabFeatureScope of(String scopeName) {
        if (StrUtil.isBlank(scopeName)) {
            throw new RuntimeException("Feature scope cannot be null.");
        }
        for (LabFeatureScope scopeEnum : LabFeatureScope.values()) {
            if (scopeName.equalsIgnoreCase(scopeEnum.getScopeName())) {
                return scopeEnum;
            }
        }
        throw new RuntimeException("Feature scope mismatch.");
    }
}
