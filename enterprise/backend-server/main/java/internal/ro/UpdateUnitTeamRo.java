package com.apitable.enterprise.internal.ro;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

/**
 * Internal-update team parameters.
 */
@Data
@Schema(description = "Internal-modify team ro")
public class UpdateUnitTeamRo {

    @Schema(description = "Department name", type = "string", example = "Design Department")
    private String teamName;

    @Schema(description = "Parent ID, 0 if the parent is root", type = "java.lang.String", example = "0")
    private String parentIdUnitId;

    @Schema(description = "role unit ids", type = "java.lang.String", example = "[\"aaa\"]")
    private List<String> roleUnitIds;

    @Schema(description = "team sequence", type = "java.lang.Integer", example = "0")
    private Integer sequence;
}
