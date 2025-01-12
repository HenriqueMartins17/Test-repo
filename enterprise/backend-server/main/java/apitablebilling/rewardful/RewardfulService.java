package com.apitable.enterprise.apitablebilling.rewardful;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.apitable.enterprise.apitablebilling.model.dto.Referral;
import com.apitable.enterprise.apitablebilling.rewardful.model.RewardfulData;
import com.apitable.enterprise.stripe.core.StripeTemplate;
import com.stripe.model.Coupon;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Rewardful service.
 */
@Component
public class RewardfulService {

    @Autowired(required = false)
    private StripeTemplate stripeTemplate;

    /**
     * extract data from external property.
     *
     * @param externalProperty external property
     * @return RewardfulData
     */
    public RewardfulData extractData(Map<String, String> externalProperty) {
        RewardfulData rewardfulData = new RewardfulData();
        if (externalProperty != null && !externalProperty.isEmpty()) {
            if (externalProperty.containsKey("rewardful.referral")) {
                // from cookies
                String referralValue = externalProperty.get("rewardful.referral");
                if (StrUtil.isNotBlank(referralValue) && JSONUtil.isTypeJSON(referralValue)) {
                    // parse referral value json
                    Referral referral = JSONUtil.toBean(referralValue, Referral.class);
                    if (StrUtil.isNotBlank(referral.getId())) {
                        Map<String, String> customerMetadata = new HashMap<>(1);
                        customerMetadata.put("referral", referral.getId());
                        rewardfulData.setCustomerMetadata(customerMetadata);
                    }
                    if (referral.getCoupon() != null
                        && StrUtil.isNotBlank(referral.getCoupon().getId())) {
                        String couponId = referral.getCoupon().getId();
                        Coupon couponObj = stripeTemplate.retrieveCoupon(couponId);
                        if (couponObj != null) {
                            rewardfulData.setCoupon(couponObj.getId());
                        }
                    }
                }
            } else {
                if (externalProperty.containsKey("referral")) {
                    // from url
                    String referralId = externalProperty.get("referral");
                    if (StrUtil.isNotBlank(referralId)) {
                        Map<String, String> customerMetadata = new HashMap<>(1);
                        customerMetadata.put("referral", referralId);
                        rewardfulData.setCustomerMetadata(customerMetadata);
                    }
                }
                if (externalProperty.containsKey("coupon")) {
                    // from url
                    String couponId = externalProperty.get("coupon");
                    if (StrUtil.isNotBlank(couponId)) {
                        Coupon couponObj = stripeTemplate.retrieveCoupon(couponId);
                        if (couponObj != null) {
                            rewardfulData.setCoupon(couponObj.getId());
                        }
                    }
                }
            }
        }
        return rewardfulData;
    }
}
