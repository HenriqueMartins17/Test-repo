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

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * WeCom Profile Verification Result View.
 * </p>
 */
@Data
@Schema(description = "WeCom Profile Verification Result View")
public class WeComCheckConfigVo {

    @Schema(description = "Whether the configuration file passes the verification")
    private Boolean isPass;

    @Schema(description = "Shas generated after configuration file verification")
    private String configSha;

    @Schema(description = "WeCom exclusive domain name", example = "spcxqmlr2lusd.enp.vika.ltd")
    private String domainName;

}
