package com.apitable.enterprise.apitablebilling.appsumo.core;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;

/**
 * redis config key.
 */
public class RedisConstants {
    /**
     * user email private key.
     */
    private static final String APPSUMO_EMAIL_PRIVATE_KEY = "cache:appsumo_user:email:{}";

    /**
     * get appsumo user email cache key.
     *
     * @param key key
     * @return cache key
     */
    public static String getAppsumoUserEmailKey(String key) {
        Assert.notBlank(key, "key does not exist");
        return StrUtil.format(APPSUMO_EMAIL_PRIVATE_KEY, key);
    }
}
