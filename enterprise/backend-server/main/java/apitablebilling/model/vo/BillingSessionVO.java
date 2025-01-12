package com.apitable.enterprise.apitablebilling.model.vo;

import lombok.Data;

/**
 * billing session vo.
 *
 * @author Shawn Deng
 */
@Data
public class BillingSessionVO {

    private String url;

    public BillingSessionVO(String url) {
        this.url = url;
    }
}
