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
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Tolerate;

/**
 * <p>
 * IDaaS Create tenant.
 * </p>
 */
@Schema(description = "IDaaS Create tenant")
@Setter
@Getter
@Builder
@ToString
@EqualsAndHashCode
public class IdaasTenantCreateVo {

    /**
     * Primary key ID.
     */
    @Schema(description = "Primary key ID")
    private Long id;

    /**
     * tenant ID.
     */
    @Schema(description = "tenant ID")
    private String tenantId;

    /**
     * tenant name.
     */
    @Schema(description = "tenant name")
    private String tenantName;

    @Tolerate
    public IdaasTenantCreateVo() {
        // default constructor
    }

}
