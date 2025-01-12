package com.apitable.enterprise.apitablebilling.service;

import com.apitable.enterprise.apitablebilling.model.ro.CheckoutCreation;

/**
 * stripe checkout service interface.
 */
public interface ICheckoutService {

    /**
     * create stripe checkout session.
     *
     * @param userId           user id
     * @param checkoutCreation checkout create param
     * @return checkout session url
     */
    String createCheckoutSession(Long userId, CheckoutCreation checkoutCreation);
}
