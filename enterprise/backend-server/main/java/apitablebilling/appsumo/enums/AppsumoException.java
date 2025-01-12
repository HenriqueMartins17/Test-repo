package com.apitable.enterprise.apitablebilling.appsumo.enums;

import com.apitable.core.exception.BaseException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AppsumoException implements BaseException {

    EVENT_NOT_FOUND(1701, "Appsumo event not found"),

    USER_EMAIL_NOT_FOUND(1702, "The activate email not bind any user"),

    USER_EMAIL_NOT_BIND_SPACE(1703, "The activate email not bind any space"),

    APPSUMO_BUNDLE_NOT_FOUND(1704, "The Appsumo bundle not found"),

    APPSUMO_SUBSCRIPTION_NOT_FOUND(1705, "The Appsumo subscription not found"),

    LICENSE_NOT_FOUND(1706, "License not found"),

    EMAIL_HAS_BIND(1707, "License not found"),

    USER_EMAIL_BOUNDED(1703, "The email address has been linked to an account"),

    NETWORK_ERROR(1704, "Network error, please try again"),

    USER_NOT_FOUND(1705, "User not found"),
    ;

    private final Integer code;

    private final String message;
}
