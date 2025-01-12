package com.apitable.enterprise.apitablebilling.enums;

/**
 * action enum for update subscription operation.
 *
 * @author Shawn Deng
 */
public enum UpdateSubscriptionAction {

    INTERVAL;

    /**
     * transform by action name.
     *
     * @param name lower name
     * @return UpdateSubscriptionAction
     */
    public static UpdateSubscriptionAction of(String name) {
        for (UpdateSubscriptionAction value : UpdateSubscriptionAction.values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }
}
