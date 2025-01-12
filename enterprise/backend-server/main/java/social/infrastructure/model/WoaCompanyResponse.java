/*
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up
 * license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its
 * subdirectories does not constitute permission to use this code or APITable Enterprise Edition
 * features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

package com.apitable.enterprise.social.infrastructure.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class WoaCompanyResponse {

    /**
     * Status code, non-zero means failure
     */
    private int result;

    /**
     * Credential information
     */
    private Token token;

    @Setter
    @Getter
    public static class Token {

        @JsonProperty("company_token")
        private String companyToken;

        /**
         * Credential validity period, in seconds.
         */
        @JsonProperty("expires_in")
        private int expiresIn;
    }
}
