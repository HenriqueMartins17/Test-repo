package com.apitable.enterprise.stripe.service;

import com.apitable.enterprise.stripe.entity.StripeEventEntity;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * stripe event service.
 */
public interface IStripeEventService extends IService<StripeEventEntity> {

    /**
     * Check if the event exists
     *
     * @param eventId event id
     * @return true if the event exists
     */
    boolean isExist(String eventId);

    /**
     * Create a new event
     *
     * @param eventId   event id
     * @param eventType event type
     * @param eventData event data
     */
    void createEvent(String eventId, String eventType, String eventData);
}
