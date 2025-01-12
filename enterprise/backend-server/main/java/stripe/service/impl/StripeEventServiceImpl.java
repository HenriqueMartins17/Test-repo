package com.apitable.enterprise.stripe.service.impl;

import com.apitable.enterprise.stripe.entity.StripeEventEntity;
import com.apitable.enterprise.stripe.mapper.StripeEventMapper;
import com.apitable.enterprise.stripe.service.IStripeEventService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * stripe event service impl.
 */
@Service
public class StripeEventServiceImpl extends ServiceImpl<StripeEventMapper, StripeEventEntity>
    implements IStripeEventService {

    @Override
    public boolean isExist(String eventId) {
        return count(new QueryWrapper<StripeEventEntity>().eq("event_id", eventId)) > 0;
    }

    @Override
    public void createEvent(String eventId, String eventType, String eventData) {
        StripeEventEntity eventEntity = new StripeEventEntity();
        eventEntity.setEventId(eventId);
        eventEntity.setEventType(eventType);
        eventEntity.setEventData(eventData);
        save(eventEntity);
    }
}
