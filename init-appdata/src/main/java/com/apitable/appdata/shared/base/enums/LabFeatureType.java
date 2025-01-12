package com.apitable.appdata.shared.base.enums;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LabFeatureType {

    STATIC("static", 1),

    REVIEW("review", 2),

    NORMAL("normal", 3),

    NORMAL_PERSIST("normal_persist", 4),

    GLOBAL("global", 5),

    ;

    private final String featureType;

    private final Integer type;

    public static LabFeatureType of(String featureType) {
        if (StrUtil.isBlank(featureType)) {
            throw new RuntimeException("Feature type cannot be null.");
        }
        for (LabFeatureType featureEnum : LabFeatureType.values()) {
            if (featureType.equalsIgnoreCase(featureEnum.getFeatureType())) {
                return featureEnum;
            }
        }
        throw new RuntimeException("Feature type mismatch.");
    }
}
