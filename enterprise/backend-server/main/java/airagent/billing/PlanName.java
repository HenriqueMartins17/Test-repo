package com.apitable.enterprise.airagent.billing;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PlanName {

    FREE("Free"),
    PLUS("Plus"),
    PRO("Pro"),
    ENTERPRISE("Enterprise");

    private final String value;
}
