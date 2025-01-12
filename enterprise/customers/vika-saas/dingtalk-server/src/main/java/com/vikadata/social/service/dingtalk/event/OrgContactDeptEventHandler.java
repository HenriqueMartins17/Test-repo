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

package com.vikadata.social.service.dingtalk.event;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import com.vikadata.social.dingtalk.event.sync.http.contact.OrgDeptCreateEvent;
import com.vikadata.social.dingtalk.event.sync.http.contact.OrgDeptModifyEvent;
import com.vikadata.social.dingtalk.event.sync.http.contact.OrgDeptRemoveEvent;
import com.vikadata.social.service.dingtalk.autoconfigure.annotation.DingTalkEventHandler;
import com.vikadata.social.service.dingtalk.autoconfigure.annotation.DingTalkEventListener;

import org.springframework.data.redis.core.RedisTemplate;

import static com.vikadata.social.dingtalk.constants.DingTalkConst.DING_TALK_CALLBACK_SUCCESS;

/**
 * Event Subscriptions -- general priority data, the latest status of the corporate sector
 */
@DingTalkEventHandler
@Slf4j
public class OrgContactDeptEventHandler {
    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @DingTalkEventListener
    public Object onOrgDeptCreateEvent(String deptId, OrgDeptCreateEvent event) {
        log.info("Received DingTalk push event: [{}:{}]", event.getEventType(), event.getSyncAction());
        return DING_TALK_CALLBACK_SUCCESS;
    }

    @DingTalkEventListener
    public Object onOrgDeptModifyEvent(String deptId, OrgDeptModifyEvent event) {
        log.info("Received DingTalk push event: [{}:{}]", event.getEventType(), event.getSyncAction());
        return DING_TALK_CALLBACK_SUCCESS;
    }

    @DingTalkEventListener
    public Object onOrgDeptRemoveEvent(String deptId, OrgDeptRemoveEvent event) {
        log.info("Receive DingTalk push event: [{}:{}]", event.getEventType(), event.getSyncAction());
        return DING_TALK_CALLBACK_SUCCESS;
    }
}
