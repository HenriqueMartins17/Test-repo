package com.apitable.enterprise.apitablebilling.rewardful.model;

import java.util.Map;
import lombok.Data;

/**
 * Rewardful data.
 */
@Data
public class RewardfulData {

    private Map<String, String> customerMetadata;

    private String coupon;
}
