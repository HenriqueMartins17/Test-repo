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

package com.vikadata.social.service.dingtalk.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;

import com.vikadata.social.service.dingtalk.entity.SocialTenantUserEntity;

public interface ISocialTenantUserService extends IService<SocialTenantUserEntity> {


    void create(String tenantId, String openId, String unionId);

    void createBatch(List<SocialTenantUserEntity> entities);

    boolean isTenantUserOpenIdExist(String tenantId, String openId);
    
    void deleteByTenantIdAndOpenId(String tenantId, String openId);
}
