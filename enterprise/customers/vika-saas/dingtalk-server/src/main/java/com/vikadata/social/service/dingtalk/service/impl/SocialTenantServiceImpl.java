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

package com.vikadata.social.service.dingtalk.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import lombok.extern.slf4j.Slf4j;

import com.vikadata.social.dingtalk.event.sync.http.BaseOrgSuiteEvent;
import com.vikadata.social.service.dingtalk.entity.SocialTenantEntity;
import com.vikadata.social.service.dingtalk.enums.SocialAppType;
import com.vikadata.social.service.dingtalk.mapper.SocialTenantMapper;
import com.vikadata.social.service.dingtalk.model.dto.SocialTenantDto;
import com.vikadata.social.service.dingtalk.service.ISocialTenantService;
import com.vikadata.social.service.dingtalk.util.SqlTool;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class SocialTenantServiceImpl extends ServiceImpl<SocialTenantMapper, SocialTenantEntity> implements ISocialTenantService {

    @Override
    public boolean isTenantAppExist(String tenantId, String appId) {
        return SqlTool.retCount(baseMapper.selectCountByTenantIdAndAppId(tenantId, appId)) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createTenant(SocialAppType appType, String suiteId, Integer status, BaseOrgSuiteEvent suiteAuthEvent) {
        String authCorpid = suiteAuthEvent.getAuthCorpInfo().getCorpid();
        log.info("DingTalk application activation:[{}]:{}", suiteId, authCorpid);
        boolean exist = isTenantAppExist(authCorpid, suiteId);
        if (exist) {
            log.warn("tenant already exists");
            return;
        }

        SocialTenantEntity tenant = new SocialTenantEntity();
        tenant.setAppId(suiteId);
        tenant.setAppType(appType.getType());
        tenant.setTenantId(authCorpid);
        tenant.setStatus(status);
        tenant.setContactAuthScope(JSONUtil.toJsonStr(suiteAuthEvent.getAuthScope().getAuthOrgScopes()));
        tenant.setAuthInfo(JSONUtil.toJsonStr(suiteAuthEvent));
        boolean flag = SqlHelper.retBool(baseMapper.insert(tenant));
        if (!flag) {
            throw new RuntimeException("[DingTalk] Failed to add tenant");
        }
    }

    @Override
    public void updateTenantStatus(String corpId, String suiteId, boolean enabled) {
        baseMapper.updateTenantStatusByTenantIdAndAppId(corpId, suiteId, enabled);
    }

    @Override
    public void updateTenantIsDeleteStatus(String corpId, String suiteId, Boolean isDeleted) {
        baseMapper.updateIsDeletedByTenantIdAndAppId(corpId, suiteId, isDeleted);
    }

    @Override
    public Boolean getTenantStatus(String corpId, String suiteId) {
        return SqlTool.retCount(baseMapper.selectStatusByTenantIdAndAppId(corpId, suiteId)) > 0;
    }

    @Override
    public Integer updateTenantAuthInfo(String tenantId, String appId, BaseOrgSuiteEvent changeEvent) {
        String authInfo = JSONUtil.toJsonStr(changeEvent);
        String authScope = JSONUtil.toJsonStr(changeEvent.getAuthScope().getAuthOrgScopes());
        return baseMapper.updateTenantAuthInfoByTenantIdAndAppId(tenantId, appId, authInfo, authScope);
    }


    @Override
    public SocialTenantDto getByTenantIdAndAppId(String tenantId, String appId) {
        return baseMapper.selectByTenantIdAndAppId(tenantId, appId);
    }
}
