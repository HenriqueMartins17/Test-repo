package com.apitable.enterprise.apitablebilling.service;

/**
 * billing task interface.
 *
 * @author Shawn Deng
 */
public interface IBillingTaskService {

    /**
     * report space subscription seats to stripe.
     */
    void reportSpaceSubscriptionSeats();
}
