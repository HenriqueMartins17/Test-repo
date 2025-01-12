package com.apitable.enterprise.apitablebilling.model.vo;

import com.apitable.enterprise.stripe.config.Price;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * subscription info.
 *
 * @author Shawn Deng
 */
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class BillingInfo {

    private String planName;

    private String interval;

    private Long chargedThroughDate;

    private boolean trial;

    private long credit;

    private Price price;

    private String subscriptionId;

    private BillingDetail billingDetail;

    private PaymentMethodDetail paymentMethodDetail;
}
