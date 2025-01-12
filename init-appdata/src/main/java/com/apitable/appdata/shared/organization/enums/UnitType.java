package com.apitable.appdata.shared.organization.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UnitType {

    TEAM(1),

    ROLE(2),

    MEMBER(3),

    ;

    private final Integer type;
}
