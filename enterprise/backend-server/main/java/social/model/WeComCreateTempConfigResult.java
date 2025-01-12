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

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * WeCom Create Temporary Authorization Configuration Result View.
 * </p>
 */
@Data
@Accessors(chain = true)
@Schema(description = "WeCom Create Temporary Authorization Configuration Result View")
public class WeComCreateTempConfigResult {

    @Schema(description = "Profile sha")
    private String configSha;

    @Schema(description = "WeCom exclusive domain name", example = "spcxqmlr2lusd.enp.vika.ltd")
    private String domainName;

}
