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

package com.apitable.enterprise.appstore.model;

import com.apitable.shared.support.serializer.ChinaLocalDateTimeToUtcSerializer;
import com.apitable.shared.support.serializer.NullBooleanSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * Space Application Information View.
 */
@Data
@Schema(description = "Space application instance view")
public class AppInstance {

    @Schema(description = "Space Id", example = "spc21u12h3")
    private String spaceId;

    @Schema(description = "Application logo of application store", example = "app-jh1237123")
    private String appId;

    @Schema(description = "Application instance ID", example = "ai-jh1237123")
    private String appInstanceId;

    @Schema(description = "Enable", example = "false")
    @JsonSerialize(nullsUsing = NullBooleanSerializer.class)
    private Boolean isEnabled;

    @Schema(description = "Type(LARK、WECOM、DINGTALK)", example = "LARK")
    private String type;

    @Schema(description = "Application instance configuration(Different types, different "
        + "configuration contents, and generic reception)")
    private InstanceConfig config;

    @Schema(description = "Creation time", type = "string", example = "2020-03-18T15:29:59.000")
    @JsonSerialize(using = ChinaLocalDateTimeToUtcSerializer.class)
    private LocalDateTime createdAt;
}
