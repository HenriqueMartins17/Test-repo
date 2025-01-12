package com.apitable.enterprise.apitablebilling.service;

import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;

/**
 * stripe webhook event handler.
 */
public interface IStripeEventHandler {

    /**
     * fulfill order.
     *
     * @param session stripe checkout session
     */
    void fulfillOrder(Session session);

    /**
     * handle subscription create event.
     *
     * @param subscription stripe subscription object
     */
    void subscriptionCreated(Subscription subscription);

    /**
     * handle subscription updated event.
     *
     * @param subscription stripe subscription object
     */
    void subscriptionUpdated(Subscription subscription);

    /**
     * handle subscription deleted event.
     *
     * @param subscription stripe subscription object
     */
    void subscriptionDeleted(Subscription subscription);
}
