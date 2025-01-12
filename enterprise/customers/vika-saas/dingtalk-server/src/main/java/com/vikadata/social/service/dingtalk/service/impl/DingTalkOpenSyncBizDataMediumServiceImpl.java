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

import java.util.concurrent.locks.Lock;

import javax.annotation.Resource;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import lombok.extern.slf4j.Slf4j;

import com.vikadata.social.dingtalk.config.DingTalkRedisOperations;
import com.vikadata.social.service.dingtalk.entity.DingTalkOpenSyncBizDataMediumEntity;
import com.vikadata.social.service.dingtalk.mapper.DingTalkOpenSyncBizDataMediumMapper;
import com.vikadata.social.service.dingtalk.service.IDingTalkOpenSyncBizDataMediumService;

import org.springframework.stereotype.Service;

import static com.vikadata.social.service.dingtalk.util.RedisKey.getDingTalkSyncHttpEventLockKey;

/**
 * Third Party Integration - Medium Priority Data Service Class
 */
@Service
@Slf4j
public class DingTalkOpenSyncBizDataMediumServiceImpl extends ServiceImpl<DingTalkOpenSyncBizDataMediumMapper,
        DingTalkOpenSyncBizDataMediumEntity> implements IDingTalkOpenSyncBizDataMediumService {

    @Resource
    private DingTalkRedisOperations dingTalkRedisOperations;

    @Override
    public Boolean create(String subscribeId, String corpId, Integer bizType, String bizId, String bizData) {
        String lockKey = getDingTalkSyncHttpEventLockKey(subscribeId, corpId, bizId, bizType);
        // 60s
        Lock lock = dingTalkRedisOperations.getLock(lockKey);
        boolean locked = false;
        try {
            // Even if data is received, only one client can process it at the same time
            locked = lock.tryLock();
            if (locked) {
                DingTalkOpenSyncBizDataMediumEntity entity = new DingTalkOpenSyncBizDataMediumEntity();
                entity.setId(IdWorker.getId());
                entity.setCorpId(corpId);
                entity.setSubscribeId(subscribeId);
                entity.setBizId(bizId);
                entity.setBizData(bizData);
                entity.setBizType(bizType);
                int result = baseMapper.insert(entity);
                return SqlHelper.retBool(result);
            }
        }
        catch (Exception e) {
            log.error("Medium priority data exception for write push: {}:{}", bizId, bizType, e);
        }
        finally {
            if (locked) {
                lock.unlock();
            }
        }
        return false;
    }
}
