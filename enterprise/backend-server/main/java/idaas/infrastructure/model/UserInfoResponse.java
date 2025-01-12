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
 * Get user information
 * </p>
 *
 */
@Setter
@Getter
public class UserInfoResponse {

    /**
     * user unique ID
     */
    @JsonProperty("user_id")
    private String userId;

    /**
     * user's name
     */
    @JsonProperty("sub")
    private String sub;

    /**
     * user display name in idaas
     */
    @JsonProperty("name")
    private String name;

    /**
     * user's email in idaas
     */
    @JsonProperty("email")
    private String email;

    /**
     * user's phone number in idaas
     */
    @JsonProperty("phone_number")
    private String phoneNumber;

}
