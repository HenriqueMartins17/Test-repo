package com.apitable.enterprise.apitablebilling.service.impl;

import com.apitable.enterprise.apitablebilling.entity.StripeCheckoutSessionEntity;
import com.apitable.enterprise.apitablebilling.mapper.StripeCheckoutSessionMapper;
import com.apitable.enterprise.apitablebilling.service.IStripeCheckoutSessionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stripe.model.checkout.Session;
import org.springframework.stereotype.Service;

@Service
public class StripeCheckoutSessionServiceImpl
    extends ServiceImpl<StripeCheckoutSessionMapper, StripeCheckoutSessionEntity>
    implements IStripeCheckoutSessionService {

    @Override
    public void createSession(String spaceId, Session session) {
        StripeCheckoutSessionEntity checkoutSession = new StripeCheckoutSessionEntity();
        checkoutSession.setSpaceId(spaceId);
        checkoutSession.setSessionId(session.getId());
        checkoutSession.setAdditionalData(session.toJson());
        save(checkoutSession);
    }
}
