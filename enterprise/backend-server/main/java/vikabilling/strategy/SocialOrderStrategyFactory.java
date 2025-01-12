/*
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its subdirectories does not constitute permission to use this code or APITable Enterprise Edition features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

package com.apitable.enterprise.vikabilling.strategy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.hutool.core.lang.Assert;

import com.apitable.enterprise.social.enums.SocialPlatformType;

/**
 * <p>
 * third party orders strategy factory
 * </p>
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class SocialOrderStrategyFactory {

    private static final Map<Integer, ISocialOrderService> services = new ConcurrentHashMap<>();

    public static <T, R> ISocialOrderService<T, R> getService(SocialPlatformType platformType) {
        return services.get(platformType.getValue());
    }

    public static <T, R> void register(SocialPlatformType platformType, ISocialOrderService<T, R> socialOrderService) {
        Assert.notNull(platformType, "social platformType can't be null");
        services.put(platformType.getValue(), socialOrderService);
    }
}
