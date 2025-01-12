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
import com.apitable.shared.support.serializer.NullNumberSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Third party integrated configuration information view.
 */
@Data
@Schema(description = "Third party integrated configuration information view")
public class SocialConfigVO {

    @Schema(description = "Platform( 1: WeCom, 2: DingTalk, 3: Lark)")
    @JsonSerialize(nullsUsing = NullNumberSerializer.class)
    private Integer platform;

    @Schema(description = "Enable")
    @JsonSerialize(nullsUsing = NullBooleanSerializer.class)
    private Boolean enable;

    @Schema(description = "Application ID")
    private String appId;

    @Schema(description = "Service provider application or not")
    @JsonSerialize(nullsUsing = NullBooleanSerializer.class)
    private Boolean isv;
}
