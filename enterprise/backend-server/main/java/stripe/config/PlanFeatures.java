package com.apitable.enterprise.stripe.config;

import com.apitable.enterprise.apitablebilling.enums.ProductEnum;
import java.util.HashMap;

/**
 * plan feature json object.
 */
public class PlanFeatures extends HashMap<ProductEnum, PlanFeature> {

    public PlanFeature free() {
        return this.get(ProductEnum.FREE);
    }

    public PlanFeature starter() {
        return this.get(ProductEnum.STARTER);
    }

    public PlanFeature plus() {
        return this.get(ProductEnum.PLUS);
    }

    public PlanFeature pro() {
        return this.get(ProductEnum.PRO);
    }

    public PlanFeature business() {
        return this.get(ProductEnum.BUSINESS);
    }

    public PlanFeature enterprise() {
        return this.get(ProductEnum.ENTERPRISE);
    }
}
