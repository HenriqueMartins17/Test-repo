package com.apitable.enterprise.apitablebilling.appsumo.model;

import lombok.Builder;
import lombok.Data;

/**
 * Appsumo event VO.
 */
@Data
@Builder(toBuilder = true)
public class AppsumoEventDTO {
    private Long id;
    private String action;

    private String uuid;

    private String planId;

    private String activationEmail;

    private String invoiceItemUuid;

    private Integer handleStatus;
}
