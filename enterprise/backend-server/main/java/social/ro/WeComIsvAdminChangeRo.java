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

package com.apitable.enterprise.social.ro;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.validation.annotation.Validated;

/**
 * <p>
 * Tenant space replacement master administrator.
 * </p>
 */
@Schema(description = "Tenant space replacement master administrator")
@Setter
@Getter
@Builder
@ToString
@EqualsAndHashCode
@Validated
public class WeComIsvAdminChangeRo {

    @Schema(description = "application package  ID", required = true)
    @NotNull
    private String suiteId;

    @Schema(description = "Authorized enterprises ID", required = true)
    @NotNull
    private String authCorpId;

    @Schema(description = "Space ID", required = true)
    @NotBlank
    private String spaceId;

    @Schema(description = "Members of the new master administrator ID", required = true)
    @NotNull
    private Long memberId;

}
