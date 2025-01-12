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

import java.util.List;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;

import com.vikadata.social.service.dingtalk.entity.SocialTenantUserEntity;
import com.vikadata.social.service.dingtalk.mapper.SocialTenantUserMapper;
import com.vikadata.social.service.dingtalk.service.ISocialTenantUserService;
import com.vikadata.social.service.dingtalk.util.SqlTool;

import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SocialTenantUserServiceImpl extends ServiceImpl<SocialTenantUserMapper, SocialTenantUserEntity> implements ISocialTenantUserService {

    @Override
    public void create(String tenantId, String openId, String unionId) {
        SocialTenantUserEntity tenantUserEntity = new SocialTenantUserEntity();
        tenantUserEntity.setTenantId(tenantId);
        tenantUserEntity.setOpenId(openId);
        tenantUserEntity.setUnionId(unionId);
        save(tenantUserEntity);
    }

    @Override
    public void createBatch(List<SocialTenantUserEntity> entities) {
        if (CollUtil.isEmpty(entities)) {
            return;
        }
        baseMapper.insertBatch(entities);
    }

    @Override
    public boolean isTenantUserOpenIdExist(String tenantId, String openId) {
        return SqlTool.retCount(baseMapper.selectCountByTenantIdAndOpenId(tenantId, openId)) > 0;
    }

    @Override
    public void deleteByTenantIdAndOpenId(String tenantId, String openId) {
        baseMapper.deleteByTenantIdAndOpenId(tenantId, openId);
    }
}
