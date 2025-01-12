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

package com.apitable.enterprise.appstore.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import cn.hutool.core.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;

import com.apitable.enterprise.appstore.setting.App;
import com.apitable.enterprise.appstore.setting.MarketPlaceConfig;
import com.apitable.enterprise.appstore.setting.MarketPlaceConfigLoader;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class MarketPlaceInitEnvironment implements EnvironmentAware, InitializingBean {

    private Environment environment;

    private void init() {
        MarketPlaceConfig marketPlaceConfig = MarketPlaceConfigLoader.getConfig();
        String[] profiles = environment.getActiveProfiles();
        String[] defaultProfiles = new String[] { "integration", "staging", "production" };
        String profile = "";
        if (ArrayUtil.containsAny(defaultProfiles, profiles)) {
            for (String appProfile : defaultProfiles) {
                if (ArrayUtil.contains(profiles, appProfile)) {
                    profile = appProfile;
                    break;
                }
            }
        }
        else {
            profile = "integration";
        }
        List<String> removedKeys = new ArrayList<>();
        for (Entry<String, App> entry : marketPlaceConfig.entrySet()) {
            if (entry.getValue().getEnv().contains(profile)) {
                continue;
            }
            removedKeys.add(entry.getKey());
        }
        if (!removedKeys.isEmpty()) {
            removedKeys.forEach(marketPlaceConfig::remove);
        }
    }

    @Override
    public void setEnvironment(@NotNull Environment environment) {
        this.environment = environment;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }
}
