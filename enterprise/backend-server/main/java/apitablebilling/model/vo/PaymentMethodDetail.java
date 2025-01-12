package com.apitable.enterprise.apitablebilling.model.vo;

import lombok.Data;

/**
 * payment method setting.
 *
 * @author Shawn Deng
 */
@Data
public class PaymentMethodDetail {

    private String last4;

    private String type;

    private String brand;

}
