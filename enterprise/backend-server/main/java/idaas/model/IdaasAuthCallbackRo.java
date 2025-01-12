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

package com.apitable.enterprise.idaas.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.validation.annotation.Validated;

/**
 * <p>
 * The user completes subsequent operations after logging in to the I Daa S system.
 * </p>
 */
@Schema(description = "The user completes subsequent operations after logging in to the I Daa S "
    + "system")
@Setter
@Getter
@ToString
@EqualsAndHashCode
@Validated
public class IdaasAuthCallbackRo {

    @Schema(description = "Code returned by IDaaS login callback")
    @NotBlank
    private String code;

    @Schema(description = "The state returned by the IDaaS login callback")
    @NotBlank
    private String state;

}
