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

package com.vikadata.scheduler.space.mapper.space;

import java.util.Collection;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.vikadata.scheduler.space.model.SpaceAssetDto;
import com.vikadata.scheduler.space.pojo.SpaceAsset;

/**
 * <p>
 * Space Asset Mapper
 * </p>
 */
public interface SpaceAssetMapper {

    /**
     * Get space asset info
     *
     * @param nodeIds node id list
     * @return dto
     */
    List<SpaceAssetDto> selectDtoByNodeIds(@Param("nodeIds") Collection<String> nodeIds);

    /**
     * Change citation count
     *
     * @param ids  table id list
     * @param cite citation count
     */
    void updateCiteByIds(@Param("list") List<Long> ids, @Param("cite") Integer cite);

    /**
     * batch insert
     *
     * @param entities entities
     * @return number of execution results
     */
    int insertList(@Param("entities") List<SpaceAsset> entities);

}
