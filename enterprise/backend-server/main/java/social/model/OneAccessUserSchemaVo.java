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
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * OneAccess UserSchema  define vo.
 */
@Data
@Schema(description = "OneAccess UserSchema  define vo")
public class OneAccessUserSchemaVo {

    @Schema(description = "The request ID sent by the platform each time the interface is called")
    private String bimRequestId;

    @Schema(description = "system account")
    private List<AttributeEntity> account;

    @Schema(description = "System organization")
    private List<AttributeEntity> organization;

    public OneAccessUserSchemaVo(String bimRequestId) {
        this.bimRequestId = bimRequestId;
    }

    /**
     * AttributeEntity.
     */
    @Builder(toBuilder = true)
    @Setter
    @Getter
    public static class AttributeEntity {

        @Schema(description = "Is it multi-valued", example = "false")
        private boolean multivalued;

        @Schema(description = "attribute field name", example = "uid")
        private String name;

        @Schema(description = "required", example = "false")
        private boolean required;

        // Optional values are String, int, double, float, long, byte, boolean. The field is of
        // type String。
        @Schema(description = "Field Type", example = "false")
        private String type;
    }
}
