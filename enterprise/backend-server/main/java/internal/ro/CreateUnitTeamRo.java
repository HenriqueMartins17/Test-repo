/*
 * APITable <https://github.com/apitable/apitable>
 * Copyright (C) 2022 APITable Ltd. <https://apitable.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.apitable.enterprise.internal.ro;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * <p>
 * New department request parameter.
 * </p>
 */
@Data
@Schema(description = "New department request parameter")
public class CreateUnitTeamRo {
    @NotBlank
    @Size(min = 1, max = 100, message = "Department name cannot exceed 100 characters")
    @Schema(description = "Department name", requiredMode = Schema.RequiredMode.REQUIRED, example = "Finance Department")
    private String teamName;

    @NotNull
    @Schema(description = "Parent ID, 0 if the parent is root", type = "java.lang.String", example = "0")
    private String parentIdUnitId;

    @Schema(description = "role unit ids", type = "java.lang.String", example = "[\"aaa\"]")
    private List<String> roleUnitIds;

    @Schema(description = "team sequence", type = "java.lang.Integer", example = "0")
    private Integer sequence;

}
