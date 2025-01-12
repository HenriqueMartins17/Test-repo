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

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.vikadata.scheduler.space.model.NodeDto;

/**
 * <p>
 * Node Mapper
 * </p>
 */
public interface NodeMapper {

    /**
     * Get node id list
     *
     * @param spaceIds space id list
     * @param type     node type（no require）
     * @return node id list
     */
    List<String> selectNodeIdBySpaceIds(@Param("list") List<String> spaceIds, @Param("type") Integer type);

    /**
     * Get node information
     * After specifying the changeset table id, the data changed and was not deleted
     *
     * @param changesetId changeset table id
     * @return NodeDto List
     */
    List<NodeDto> findChangedNodeIds(@Param("changesetId") Long changesetId);

}
