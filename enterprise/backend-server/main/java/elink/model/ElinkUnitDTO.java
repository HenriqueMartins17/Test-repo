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

package com.apitable.enterprise.elink.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Elink Unit Information.
 */
@Data
@Schema(description = "Elink unit information")
public class ElinkUnitDTO {

    /**
     * Unit Id
     */
    @NotBlank(message = "unit Id")
    private String unitId;

    /**
     * Parent Unit Id
     */
    private String parentUnitId;

    /**
     * Unit Name
     */
    @NotBlank(message = "unit name")
    private String name;

    /**
     * status
     * 0 valid
     * 1 disabled
     * 2 delete
     */
    private int status = 0;

}
