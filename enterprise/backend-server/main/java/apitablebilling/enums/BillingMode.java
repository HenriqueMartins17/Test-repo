package com.apitable.enterprise.apitablebilling.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BillingMode {

    RECURRING("recurring");

    private final String name;
}
