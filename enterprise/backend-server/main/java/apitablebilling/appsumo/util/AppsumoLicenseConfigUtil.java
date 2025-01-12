package com.apitable.enterprise.apitablebilling.appsumo.util;

import com.apitable.enterprise.apitablebilling.appsumo.config.AppsumoLicenseConfig;
import com.apitable.enterprise.apitablebilling.appsumo.config.AppsumoLicenseObject;
import java.util.Optional;

/**
 * appsumo config util.
 */
public class AppsumoLicenseConfigUtil {

    public static volatile AppsumoLicenseConfig licenseConfig;

    /**
     * find config object through by plan id.
     *
     * @return AppsumoLicenseObject
     */
    public static Optional<AppsumoLicenseObject> findProductByPlanId(String planId) {
        return licenseConfig.values().stream()
            .filter(license -> license.getPlanId().equals(planId))
            .findFirst();
    }

    public static boolean isAppsumoPlan(String planId) {
        return licenseConfig.containsKey(planId);
    }

    /**
     * find product name.
     *
     * @param productId product id
     * @return name
     */
    public static String findProductNameByProductId(String productId) {
        for (AppsumoLicenseObject object : licenseConfig.values()) {
            if (object.getProductId().equals(productId)) {
                return object.getProductName();
            }
        }
        return "";
    }
}
