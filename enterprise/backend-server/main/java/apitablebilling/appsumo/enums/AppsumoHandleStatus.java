package com.apitable.enterprise.apitablebilling.appsumo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * appsumo event handle status.
 */
@Getter
@AllArgsConstructor
public enum AppsumoHandleStatus {

    HANDLING(0),

    SUCCESS(1),

    ERROR(2),

    SKIPPED(3),

    ;

    private final Integer status;


    public static boolean isEventHandled(Integer status) {
        return status.equals(SUCCESS.status) || status.equals(ERROR.status);
    }
}
