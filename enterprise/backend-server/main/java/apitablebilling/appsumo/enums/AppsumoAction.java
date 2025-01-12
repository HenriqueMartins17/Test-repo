package com.apitable.enterprise.apitablebilling.appsumo.enums;

import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * appsumo event action type.
 */
@Getter
@AllArgsConstructor
public enum AppsumoAction {

    ACTIVATE("activate"),

    ENHANCE_TIER("enhance_tier"),

    REDUCE_TIER("reduce_tier"),

    REFUND("refund"),

    UPDATE("update");

    private final String action;

    /**
     * to enum.
     *
     * @param action action
     * @return AppsumoAction
     */
    public static AppsumoAction toEnum(String action) {
        for (AppsumoAction e : AppsumoAction.values()) {
            if (Objects.equals(e.getAction(), action)) {
                return e;
            }
        }
        throw new RuntimeException("unknown action type");
    }
}
