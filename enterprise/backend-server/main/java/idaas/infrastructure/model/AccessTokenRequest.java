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

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * Access to access user information_ token
 * </p>
 *
 */
@Setter
@Getter
public class AccessTokenRequest {

    /**
     * way of authorization
     */
    @JsonProperty("grant_type")
    private String grantType = "authorization_code";

    /**
     * auth code
     */
    @JsonProperty("code")
    private String code;

    /**
     * token url
     */
    @JsonProperty("redirect_uri")
    private String redirectUri;

}
