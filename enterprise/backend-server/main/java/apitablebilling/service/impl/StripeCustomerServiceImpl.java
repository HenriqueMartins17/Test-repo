package com.apitable.enterprise.apitablebilling.service.impl;

import com.apitable.enterprise.apitablebilling.entity.StripeCustomerEntity;
import com.apitable.enterprise.apitablebilling.mapper.StripeCustomerMapper;
import com.apitable.enterprise.apitablebilling.service.IStripeCustomerService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;

/**
 * stripe customer service implement.
 */
@Service
public class StripeCustomerServiceImpl
    extends ServiceImpl<StripeCustomerMapper, StripeCustomerEntity>
    implements IStripeCustomerService {

    @Override
    public void createCustomer(String customerId, String customerEmail, String spaceId) {
        StripeCustomerEntity customer = new StripeCustomerEntity();
        customer.setStripeId(customerId);
        customer.setEmail(customerEmail);
        customer.setSpaceId(spaceId);
        save(customer);
    }

    @Override
    public String getFirstCustomerIdBySpaceId(String spaceId) {
        List<StripeCustomerEntity> stripeCustomerEntityList =
            list(
                new QueryWrapper<StripeCustomerEntity>().eq("space_id", spaceId)
            );
        return stripeCustomerEntityList.isEmpty()
            ? null : stripeCustomerEntityList.get(0).getStripeId();
    }

    @Override
    public <X extends Throwable> String getFirstCustomerIdOrThrow(
        String spaceId, Supplier<? extends X> exceptionSupplier)
        throws X {
        String customerId = getFirstCustomerIdBySpaceId(spaceId);
        if (customerId == null) {
            throw exceptionSupplier.get();
        }
        return customerId;
    }

    @Override
    public List<StripeCustomerEntity> getBySpaceId(String spaceId) {
        return list(
            new QueryWrapper<StripeCustomerEntity>().eq("space_id", spaceId)
        );
    }
}
