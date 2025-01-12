package com.apitable.enterprise.internal.ro;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Internal-update team parameters.
 */
@Data
@Schema(description = "Internal-modify team ro")
public class UpdateUnitMemberRo {

    @Schema(description = "Member Name", example = "Zhang San")
    @Size(max = 32, message = "The length cannot exceed 32 bits")
    private String memberName;

    @Schema(description = "Department unit id")
    private List<String> teamUnitIds;

    @Schema(description = "role unit ids", type = "java.lang.String", example = "[\"aaa\"]")
    private List<String> roleUnitIds;
}
