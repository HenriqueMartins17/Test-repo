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

package com.apitable.enterprise.social.model;

import com.apitable.shared.support.serializer.NullBooleanSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Lark Login user identity.
 */
@Data
@Schema(description = "Lark Login user identity")
public class FeishuUserAuthVO {

    @Schema(description = "The user's unique ID in the vika application")
    private String openId;

    @Schema(description = "Enterprise ID of the login user")
    private String tenantKey;

    @Schema(description = "Login User Name")
    private String name;

    @Schema(description = "User avatar")
    private String avatarUrl;

    @Schema(description = "User avatar 72x72")
    private String avatarThumb;

    @Schema(description = "User avatar 240x240")
    private String avatarMiddle;

    @Schema(description = "User avatar 640x640")
    private String avatarBig;

    @Schema(description = "Bind user or not")
    @JsonSerialize(nullsUsing = NullBooleanSerializer.class)
    private Boolean bindUser;
}
