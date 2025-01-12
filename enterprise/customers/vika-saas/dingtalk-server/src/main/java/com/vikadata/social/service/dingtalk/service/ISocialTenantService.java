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

import com.baomidou.mybatisplus.extension.service.IService;

import com.vikadata.social.dingtalk.event.sync.http.BaseOrgSuiteEvent;
import com.vikadata.social.service.dingtalk.entity.SocialTenantEntity;
import com.vikadata.social.service.dingtalk.enums.SocialAppType;
import com.vikadata.social.service.dingtalk.model.dto.SocialTenantDto;

public interface ISocialTenantService extends IService<SocialTenantEntity> {

    boolean isTenantAppExist(String tenantId, String appId);

    void createTenant(SocialAppType appType, String suiteId, Integer status, BaseOrgSuiteEvent suiteAuthEvent);

    void updateTenantStatus(String corpId, String suiteId, boolean enabled);

    void updateTenantIsDeleteStatus(String corpId, String suiteId, Boolean isDeleted);

    Boolean getTenantStatus(String corpId, String suiteId);

    Integer updateTenantAuthInfo(String tenantId, String appId, BaseOrgSuiteEvent changeEvent);
    
    SocialTenantDto getByTenantIdAndAppId(String tenantId, String appId);
}
