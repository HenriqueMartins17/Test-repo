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

import com.apitable.shared.support.serializer.NullBooleanSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

/**
 * Application Information View.
 */
@Data
@Schema(description = "Application Information View")
public class AppInfo {

    @Schema(description = "Application ID", type = "String", example = "app-jh1237123")
    private String appId;

    @Schema(description = "Apply name", type = "String", example = "Lark")
    private String name;

    @Schema(description = "Type(LARK、WECOM、DINGTALK)", type = "String", example = "LARK")
    private String type;

    @Schema(description = "Application Type(See the catalog for details)", type = "String",
        example = "SOCIAL")
    private String appType;

    @Schema(description = "Application status", type = "String", example = "ACTIVE")
    private String status;

    @Schema(description = "Application Introduction", type = "String", example = "Seamless "
        + "combination with Lark")
    private String intro;

    @Schema(description = "Help Document Path", type = "String", example = "/help/path")
    private String helpUrl;

    @Schema(description = "Application Description", type = "String", example = "long text")
    private String description;

    @Schema(description = "Display the picture list in order", type = "List", example = "[url1, "
        + "url2....]")
    private List<String> displayImages;

    @Schema(description = "Notes", type = "String", example = "Be careful：xxx")
    private String notice;

    @Schema(description = "Application LOGO address", type = "String", example = "feishu_logo")
    private String logoUrl;

    @Schema(description = "Whether configuration is required", type = "Boolean", example = "false")
    @JsonSerialize(nullsUsing = NullBooleanSerializer.class)
    private Boolean needConfigured;

    @Schema(description = "Configure Jump Path", type = "String", example = "/path")
    private String configureUrl;

    @Schema(description = "Disable the adjustment link. If there is no adjustment link, there is "
        + "no need to jump", type = "String", example = "https://feishu.cn/admin/xxx")
    private String stopActionUrl;

    @Schema(description = "Whether authorization enabling operation is required", type = "Boolean",
        example = "false")
    @JsonSerialize(nullsUsing = NullBooleanSerializer.class)
    private Boolean needAuthorize;
}
