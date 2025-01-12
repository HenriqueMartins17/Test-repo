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

import java.util.List;

import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * Space Mapper
 * </p>
 */
public interface SpaceMapper {

    /**
     * Get a list of space id that need to be deleted
     *
     * @param deadline deleted deadline
     * @return space id list
     */
    List<String> findDelSpaceIds(@Param("deadline") String deadline);

    /**
     * update isDeleted status
     *
     * @param spaceIds space id list
     * @return number of execution results
     */
    int updateIsDeletedBySpaceIdIn(@Param("spaceIds") List<String> spaceIds);

    /**
     * Get the number to determine whether the space exists
     *
     * @param spaceId  space id
     * @param isPreDel Is it in pre-delete state（no require）
     * @return count
     */
    Integer countBySpaceId(@Param("spaceId") String spaceId, @Param("isPreDel") Boolean isPreDel);
}
