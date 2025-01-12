package com.apitable.enterprise.internal.ro;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Internal-update role parameters.
 */
@Data
@Schema(description = "Internal-modify role ro")
public class UpdateUnitRoleRo {

    @Schema(description = "Role name", type = "string", example = "Design Role")
    private String roleName;

    @Schema(description = "Role position", type = "java.lang.Integer", example = "0")
    private Integer position;
}
