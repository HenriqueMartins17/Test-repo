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

package com.apitable.enterprise.integral.setting;


import java.io.IOException;
import java.io.InputStream;

import com.apitable.shared.sysconfig.Converter;

public class IntegralConfigLoader {

    public static IntegralRuleConfig getConfig() {
        return IntegralConfigLoader.Singleton.INSTANCE.getSingleton();
    }

    private enum Singleton {
        INSTANCE;

        private final IntegralRuleConfig singleton;

        Singleton() {
            try {
                InputStream resourceAsStream = IntegralConfigLoader.class.getResourceAsStream("/enterprise/config/integral.json");
                if (resourceAsStream == null) {
                    throw new IOException("System config file not found!");
                }
                singleton = Converter.getObjectMapper().readValue(resourceAsStream, IntegralRuleConfig.class);
            }
            catch (IOException e) {
                throw new RuntimeException("Failed to load system configuration!", e);
            }
        }

        public IntegralRuleConfig getSingleton() {
            return singleton;
        }
    }
}
