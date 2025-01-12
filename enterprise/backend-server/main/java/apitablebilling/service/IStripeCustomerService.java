package com.apitable.enterprise.apitablebilling.service;

import com.apitable.enterprise.apitablebilling.entity.StripeCustomerEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;
import java.util.function.Supplier;

/**
 * stripe customer service.
 *
 * @author Shawn Deng
 */
public interface IStripeCustomerService extends IService<StripeCustomerEntity> {

    /**
     * create customer relates in system.
     *
     * @param customerId    customer id
     * @param customerEmail customer email
     * @param spaceId       space id
     */
    void createCustomer(String customerId, String customerEmail, String spaceId);

    /**
     * get bind stripe customer id.
     *
     * @param spaceId space id
     * @return customer id
     */
    String getFirstCustomerIdBySpaceId(String spaceId);

    /**
     * Return the customer id, if present,
     * otherwise throw an exception to be created by the provided supplier.
     *
     * @param spaceId           space id
     * @param exceptionSupplier exception supplier
     * @param <X>               exception class
     * @return customer id
     * @throws X if there is no value present
     *           NullPointerException – if no value is present and exceptionSupplier is null
     */
    <X extends Throwable> String getFirstCustomerIdOrThrow(String spaceId,
                                                           Supplier<? extends X> exceptionSupplier)
        throws X;

    /**
     * get space customer.
     *
     * @param spaceId space id
     * @return StripeCustomerEntity List
     */
    List<StripeCustomerEntity> getBySpaceId(String spaceId);
}
