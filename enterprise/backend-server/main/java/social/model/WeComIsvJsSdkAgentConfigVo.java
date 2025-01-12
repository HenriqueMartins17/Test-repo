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
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.validation.annotation.Validated;

/**
 * <p>
 * JS-SDK Verify the configuration parameters of application identity and permission.
 * </p>
 */
@Schema(description = "JS-SDK Verify the configuration parameters of application identity and "
    + "permission")
@Setter
@Getter
@Builder
@ToString
@EqualsAndHashCode
@Validated
public class WeComIsvJsSdkAgentConfigVo {

    @Schema(description = "The corpId of the currently logged in WeCom", required = true)
    private String authCorpId;

    @Schema(description = "The application ID of the currently logged in WeCom", required = true)
    private String agentId;

    @Schema(description = "Time stamp of signature generation", required = true)
    private Long timestamp;

    @Schema(description = "Generate a random string of signatures", required = true)
    private String random;

    @Schema(description = "Generated Signature", required = true)
    private String signature;

}
