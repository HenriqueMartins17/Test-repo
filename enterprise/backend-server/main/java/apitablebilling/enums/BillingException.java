package com.apitable.enterprise.apitablebilling.enums;

import com.apitable.core.exception.BaseException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BillingException implements BaseException {

    NOT_SUPPORT_DOWNGRADE(1101, "Not Support To Downgrade"),
    NOT_SUPPORT_ACTION(1102, "Not Support Action"),
    PRICE_NOT_FOUND(1103, "price not found"),
    NOT_SUPPORT_PURCHASE(1104, "product not support purchase");

    private final Integer code;

    private final String message;
}
