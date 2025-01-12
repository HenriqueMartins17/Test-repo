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

package com.vikadata.social.service.dingtalk.controller;

import java.util.Map;

import javax.annotation.Resource;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

import com.vikadata.social.dingtalk.DingTalkServiceProvider;
import com.vikadata.social.service.dingtalk.component.TaskManager;
import com.vikadata.social.service.dingtalk.model.ro.DingTalkCallbackRo;
import com.vikadata.social.service.dingtalk.service.IInternalService;
import com.vikadata.social.service.dingtalk.util.SpringContextHolder;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * dingtalk callback interface entry
 */
@Slf4j
@Api(tags = "Third-party platform integration interface--DingTalk")
@RequestMapping(value = "/dingtalk")
@RestController
public class CallBackController {
    @Resource
    private IInternalService iInternalService;

    @PostMapping(name = "dingtalk third party application callback address", value = "/callback/{suiteId}")
    @ApiOperation(value = "DingTalk third-party application callback address")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "signature", value = "signature", required = true, dataTypeClass = String.class, paramType = "query", example = "111108bb8e6dbc2xxxx"),
            @ApiImplicitParam(name = "timestamp", value = "timestamp", required = true, dataTypeClass = String.class, paramType = "query", example = "1783610513"),
            @ApiImplicitParam(name = "nonce", value = "nonce", required = true, dataTypeClass = String.class, paramType = "query", example = "380320111"),
            @ApiImplicitParam(name = "suiteId", value = "suiteId", required = true, dataTypeClass = String.class, paramType = "path", example = "380320111"),
    })
    public Map<String, Object> callback(@PathVariable("suiteId") String suiteId,
            @RequestParam("signature") String signature,
            @RequestParam("timestamp") String timestamp,
            @RequestParam("nonce") String nonce,
            @RequestBody DingTalkCallbackRo ro) {
        log.info("callBack: {},{},{},{},{}", suiteId, signature, timestamp, nonce, ro);
        DingTalkServiceProvider dingtalkServiceProvider = SpringContextHolder.getBean(DingTalkServiceProvider.class);
        String result = dingtalkServiceProvider.syncHttpEventNotifyForIsv(suiteId, signature, timestamp, nonce,
                ro.getEncrypt());
        if (StrUtil.isNotBlank(result)) {
            TaskManager.me().execute(() -> iInternalService.pushDingTalkSyncAction(suiteId, signature, timestamp, nonce,
                    ro.getEncrypt()));
        }
        return BeanUtil.beanToMap(JSONUtil.parseObj(result));
    }
}
