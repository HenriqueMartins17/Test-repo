package com.apitable.enterprise.apitablebilling.model.dto;

import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class Order {

    private String spaceId;

    private String currency;

    private Integer amount;

    private String state;

    private Map<String, String> metadata;

    private List<OrderItem> items;
}
