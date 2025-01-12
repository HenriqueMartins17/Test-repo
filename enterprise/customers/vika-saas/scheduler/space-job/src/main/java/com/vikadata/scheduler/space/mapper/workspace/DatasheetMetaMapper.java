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

package com.vikadata.scheduler.space.mapper.workspace;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.vikadata.scheduler.space.model.DataSheetMetaDto;
import com.vikadata.scheduler.space.model.ForeignDatasheetDto;

/**
 * <p>
 * Datasheet Meta Mapper
 * </p>
 */
public interface DatasheetMetaMapper {

    /**
     * Get datasheet id and meta
     *
     * @param nodeIds node id list
     * @return DataSheetMetaDto List
     */
    List<DataSheetMetaDto> selectDtoByNodeIds(@Param("nodeIds") List<String> nodeIds);

    /**
     * Query the max table ID
     *
     * @return ID
     */
    Long selectMaxId();

    /**
     * After getting the specified creation time, the minimum table ID
     *
     * @param createdAt createdAt
     * @return ID
     */
    Long selectMinIdAfterCreatedAt(@Param("createdAt") LocalDateTime createdAt);

    /**
     * Query all the magic linked data
     *
     * @param spaceId space id
     * @param nextId  table id to start querying next time
     * @param page    page object
     */
    IPage<ForeignDatasheetDto> selectForeignDatasheetIdsByPage(@Param("spaceId") String spaceId, @Param("nextId") Long nextId, Page<ForeignDatasheetDto> page);

    /**
     * Querying the configuration of the space station number table view
     *
     * @param spaceId           space id
     * @param selectFixDataMode mode
     * @return DataSheetMetaDto List
     */
    List<DataSheetMetaDto> selectMetaDataByFixMode(@Param("spaceId") String spaceId, @Param("selectFixDataMode") Integer selectFixDataMode);

    /**
     * Modify the sorting information of the template view
     *
     * @param dstId     datasheetId
     * @param viewIndex view index
     * @return number of execution results
     */
    int updateTemplateViewSortInfo(@Param("dstId") String dstId, @Param("viewIndex") Integer viewIndex);
}
