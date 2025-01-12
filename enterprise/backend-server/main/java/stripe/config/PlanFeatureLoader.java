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

package com.apitable.enterprise.stripe.config;

import com.apitable.shared.sysconfig.Converter;
import java.io.IOException;
import java.io.InputStream;
import lombok.Getter;

/**
 * stripe product catalog loader in test mode.
 */
public class PlanFeatureLoader {

    public static PlanFeatures getConfig() {
        return Singleton.INSTANCE.getSingleton();
    }

    @Getter
    private enum Singleton {

        INSTANCE;

        private final PlanFeatures singleton;

        Singleton() {
            try {
                InputStream resourceAsStream = PlanFeatureLoader.class.getResourceAsStream(
                    "/enterprise/stripe/feature.json");
                if (resourceAsStream == null) {
                    throw new IOException("config file not found!");
                }
                singleton = Converter.getObjectMapper()
                    .readValue(resourceAsStream, PlanFeatures.class);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load system configuration!", e);
            }
        }

    }
}
