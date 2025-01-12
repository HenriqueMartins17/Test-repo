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

package com.apitable.enterprise.idaas.infrastructure.model;

import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * open enterprise
 * </p>
 *
 */
@Setter
@Getter
public class TenantRequest {

    /**
     * business name is displayed in the url link
     */
    private String name;

    /**
     * enterprise display name
     */
    private String displayName;

    /**
     * Tenant type, PROD or TEST, refers to formal customer and test customer. Default PROD
     */
    private String env = "PROD";

    /**
     * initial Administrator
     */
    private Admin admin;

    /**
     * configuration information
     */
    private Config config;

    @Setter
    @Getter
    public static class Admin {

        private String username;

        private String password;

    }

    @Setter
    @Getter
    public static class Config {

        /**
         * configure the authentication source
         */
        private IdpInfo idpInfo;

        /**
         * configuration related to single sign on application
         */
        private SsoInfo ssoInfo;

        @Setter
        @Getter
        public static class  IdpInfo {

            private String appCode;

            private AppConfig appConfig;

            @Setter
            @Getter
            public static class AppConfig {

                private String abc;

                private String bcd;

            }

        }

        @Setter
        @Getter
        public static class SsoInfo {

            private String appCode;

            private AppConfig appConfig;

            @Setter
            @Getter
            public static class AppConfig {

                private String abc;

                private String bcd;

            }

        }

    }

}
