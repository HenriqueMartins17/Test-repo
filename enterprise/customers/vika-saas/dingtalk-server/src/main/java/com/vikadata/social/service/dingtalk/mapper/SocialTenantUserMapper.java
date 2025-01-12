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

package com.vikadata.social.service.dingtalk.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import com.vikadata.social.service.dingtalk.entity.SocialTenantUserEntity;

public interface SocialTenantUserMapper extends BaseMapper<SocialTenantUserEntity> {
    /**
     * fast batch insert
     *
     * @param entities list
     * @return number of execution results
     */
    int insertBatch(@Param("entities") List<SocialTenantUserEntity> entities);

    /**
     * query whether the user under the tenant exists
     *
     * @param tenantId Tenant ID
     * @param openId   user id under the tenant
     * @return total
     */
    Integer selectCountByTenantIdAndOpenId(@Param("tenantId") String tenantId, @Param("openId") String openId);

    /**
     * delete a user under a tenant
     *
     * @param tenantId Tenant ID
     * @param openId   user id under the tenant
     * @return results
     */
    int deleteByTenantIdAndOpenId(@Param("tenantId") String tenantId, @Param("openId") String openId);
}
