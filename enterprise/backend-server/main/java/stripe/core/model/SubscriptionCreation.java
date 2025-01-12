package com.apitable.enterprise.stripe.core.model;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * subscription create params.
 *
 * @author Shawn Deng
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionCreation {

    private String stripeCustomerId;

    private String stripePriceId;

    private long trialPeriodDays;

    private Map<String, String> metadata;
}
