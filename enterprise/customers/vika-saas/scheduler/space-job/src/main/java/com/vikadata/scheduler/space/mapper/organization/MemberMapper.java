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

package com.vikadata.scheduler.space.mapper.organization;

import java.util.List;

import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * Member Mapper
 * </p>
 */
public interface MemberMapper {

    /**
     * Get the list of user IDs that are active in the space
     *
     * @param spaceIds space id list
     * @return user table ids
     */
    List<Long> selectUserIdBySpaceIds(@Param("spaceIds") List<String> spaceIds);

    /**
     * Update isDeleted status
     *
     * @param spaceIds space id list
     * @return number of execution results
     */
    int updateIsDeletedBySpaceIds(@Param("spaceIds") List<String> spaceIds);

}
