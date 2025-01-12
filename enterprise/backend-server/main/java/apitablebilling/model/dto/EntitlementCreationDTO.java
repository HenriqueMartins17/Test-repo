package com.apitable.enterprise.apitablebilling.model.dto;

import com.apitable.enterprise.apitablebilling.enums.BillingPeriod;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * entitlement create parameter.
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class EntitlementCreationDTO {

    private String spaceId;

    private String stripeId;

    private String stripeSubId;

    private String productName;

    private String priceId;

    private int quantity;

    private BillingPeriod period;

    private boolean trial;

    private LocalDateTime startDate;

    private LocalDateTime endDate;
}
