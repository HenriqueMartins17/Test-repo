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
 * Well-known interface return result
 * </p>
 *
 */
@Setter
@Getter
public class WellKnowResponse {

    @JsonProperty("issuer")
    private String issuer;

    @JsonProperty("authorization_endpoint")
    private String authorizationEndpoint;

    @JsonProperty("revocation_endpoint")
    private String revocationEndpoint;

    @JsonProperty("token_endpoint")
    private String tokenEndpoint;

    @JsonProperty("userinfo_endpoint")
    private String userinfoEndpoint;

    @JsonProperty("jwks_uri")
    private String jwksUri;

}
