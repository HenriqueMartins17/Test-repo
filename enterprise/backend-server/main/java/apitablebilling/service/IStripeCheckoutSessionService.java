package com.apitable.enterprise.apitablebilling.service;

import com.apitable.enterprise.apitablebilling.entity.StripeCheckoutSessionEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.stripe.model.checkout.Session;

public interface IStripeCheckoutSessionService extends IService<StripeCheckoutSessionEntity> {

    /**
     * save checkout session record.
     *
     * @param spaceId space id
     * @param session stripe checkout session
     */
    void createSession(String spaceId, Session session);
}
