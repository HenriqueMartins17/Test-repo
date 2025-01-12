package com.apitable.enterprise.apitablebilling.model.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class OrderItem {

    private String productId;

    /**
     * Product Name
     */
    private String productName;

    /**
     * Product Type
     */
    private String productCategory;

    private String priceId;

    private Integer quantity;

    /**
     * Subscription ID
     */
    private String subscriptionId;

    /**
     * Start Date
     */
    private LocalDateTime startDate;

    /**
     * End Date
     */
    private LocalDateTime endDate;

    /**
     * ISO currency code (upper case letters)
     */
    private String currency;

    /**
     * Payment scheme amount (unit: cents)
     */
    private Integer amount;

    private String itemDetails;
}
