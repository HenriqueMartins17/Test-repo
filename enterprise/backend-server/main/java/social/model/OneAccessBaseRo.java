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
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Huawei OneAccess Creation Information.
 */
@Data
@Schema(description = "Huawei OneAccess Creation Information")
public class OneAccessBaseRo {

    @Schema(description = "The request ID sent by the One Access platform each time the interface"
        + " is called")
    private String bimRequestId;

    @Schema(description = "The authorized account for the platform to call the third-party "
        + "application interface")
    @NotBlank(message = "bimRemoteUser Can not be empty")
    private String bimRemoteUser;

    @Schema(description = "The password for the platform to call the third-party application "
        + "interface")
    @NotBlank(message = "bimRemotePwd Can not be empty")
    private String bimRemotePwd;

    @Schema(description = "request data signature")
    private String signature;

}
