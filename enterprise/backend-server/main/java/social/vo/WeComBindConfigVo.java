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

package com.apitable.enterprise.social.vo;

import com.apitable.shared.support.serializer.DesensitizedSecretSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * WeCom Bound Profile View.
 * </p>
 */
@Data
@Schema(description = "WeCom Bound Profile View")
public class WeComBindConfigVo {

    @Schema(description = "Enterprise Id")
    private String corpId;

    @Schema(description = "Self built application ID")
    private Integer agentId;

    @Schema(description = "Self built application key")
    @JsonSerialize(using = DesensitizedSecretSerializer.class)
    private String agentSecret;

    @Schema(description = "Self built application status (0: enabled, 1: disabled)")
    private Integer agentStatu;

    @Schema(description = "Enterprise exclusive domain name", example = "spcxqmlr2lusd.enp.vika"
        + ".ltd")
    private String domainName;

}
