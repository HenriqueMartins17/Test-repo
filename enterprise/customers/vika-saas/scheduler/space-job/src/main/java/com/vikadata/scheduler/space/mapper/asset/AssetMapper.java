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

package com.vikadata.scheduler.space.mapper.asset;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.vikadata.scheduler.space.model.AssetDto;
import com.vikadata.scheduler.space.pojo.Asset;

/**
 * <p>
 * Asset Mapper
 * </p>
 */
public interface AssetMapper {

    /**
     * Get attachment resource information
     *
     * @param tokenList token list
     * @return dto
     */
    List<AssetDto> selectDtoByTokens(@Param("list") List<String> tokenList);

    /**
     * Insert entity
     *
     * @param entity entity
     * @return number of execution results
     */
    int insertEntity(@Param("entity") Asset entity);

}
