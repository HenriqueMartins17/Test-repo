package com.apitable.enterprise.apitablebilling.model.dto;

import lombok.Data;

/**
 * rewardful referral bean.
 *
 * @author Shawn Deng
 */
@Data
public class Referral {

    private String id;

    private ReferralCoupon coupon;
}
