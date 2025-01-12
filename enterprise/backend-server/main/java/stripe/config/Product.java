package com.apitable.enterprise.stripe.config;

import com.apitable.enterprise.apitablebilling.enums.RecurringInterval;
import com.apitable.shared.support.serializer.NullCollectionSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * product object.
 */
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    private String id;

    private String name;

    private String description;

    private String type;

    private String catalog;

    private boolean free;

    private boolean customize;

    private boolean suggestion;

    private Long trialPeriodDays;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private boolean hidden;

    @JsonSerialize(nullsUsing = NullCollectionSerializer.class)
    private List<Price> prices;

    private PlanFeature feature;

    /**
     * find price in product.
     *
     * @param priceId price id
     * @return price optional
     */
    public Optional<Price> findPrice(String priceId) {
        if (prices == null) {
            return Optional.empty();
        }
        return prices.stream()
            .filter(price -> price.getId().equals(priceId))
            .findFirst();
    }

    /**
     * get price by interval.
     *
     * @param interval interval
     * @return price
     */
    public Price getPrice(String interval) {
        if (prices == null) {
            return null;
        }
        return prices.stream()
            .filter(price -> price.getInterval().equalsIgnoreCase(interval))
            .findFirst()
            .orElse(null);
    }

    /**
     * whether product has trial period days.
     *
     * @return true if it has trial period days
     */
    public boolean hasTrialPeriodDays() {
        return safeGetTrialPeriodDays() > 0;
    }

    /**
     * get trial period days.
     *
     * @return trial period days or 0
     */
    public long safeGetTrialPeriodDays() {
        return Optional.ofNullable(trialPeriodDays).orElse(0L);
    }

    /**
     * reverse price id by interval in product.
     *
     * @param interval interval
     * @return price id or null
     */
    public Optional<Price> reverseInterval(RecurringInterval interval) {
        return prices.stream()
            .filter(price -> !price.getInterval().equalsIgnoreCase(interval.getName()))
            .findFirst();
    }
}
