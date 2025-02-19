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

import com.vikadata.social.service.dingtalk.entity.DingTalkOpenSyncBizDataEntity;
import com.vikadata.social.service.dingtalk.model.dto.SocialTenantBizDataDto;

/**
 * Third-party platform integration - high-priority push data mapper interface
 */
public interface DingTalkOpenSyncBizDataMapper extends BaseMapper<DingTalkOpenSyncBizDataEntity> {

    /**
     * Get event data
     * @param subscribeId Subscription ID
     * @param bizTypes category type
     * @param corpId Tenant ID
     * @return List<SocialTenantBizDataDto>
     */
    List<SocialTenantBizDataDto> selectBySubscribeIdAndCorpIdAndBizTypes(@Param("subscribeId") String subscribeId,
            @Param("corpId") String corpId, @Param("bizTypes") List<Integer> bizTypes);
}
