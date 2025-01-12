package com.apitable.enterprise.apitablebilling.appsumo.model;

import lombok.Builder;
import lombok.Data;

/**
 * appsumo subscription metadata.
 */
@Data
@Builder(toBuilder = true)
public class AppsumoSubscriptionMetadata {

    private String invoiceItemUuid;

    private String uuid;
}
