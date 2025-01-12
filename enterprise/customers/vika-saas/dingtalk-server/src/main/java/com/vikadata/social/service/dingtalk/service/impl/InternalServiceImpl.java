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

import java.net.SocketTimeoutException;
import java.util.HashMap;

import javax.annotation.Resource;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import com.vikadata.social.service.dingtalk.util.SpringContextHolder;
import com.vikadata.social.dingtalk.DingTalkServiceProvider;
import com.vikadata.social.dingtalk.util.DingTalkCallbackCrypto;
import com.vikadata.social.service.dingtalk.autoconfigure.DingTalkProperties;
import com.vikadata.social.service.dingtalk.config.ConstProperties;
import com.vikadata.social.service.dingtalk.model.dto.DingTalkCallbackDto;
import com.vikadata.social.service.dingtalk.service.IInternalService;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import static com.vikadata.social.dingtalk.constants.DingTalkConst.DING_TALK_CALLBACK_SUCCESS;

/**
 * Internal service call interface implementation
 */
@Service
@Slf4j
public class InternalServiceImpl implements IInternalService {

    public static final String DING_TALK_CALLBACK_PATH = "/{}/{}/{}";

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private ConstProperties constProperties;

    @Resource
    private DingTalkProperties dingTalkProperties;

    @Override
    public void pushDingTalkSyncAction(String suiteId, String signature, String timestamp, String nonce,
            String encrypt) {
        // Retry mechanism to ensure synchronization and stability of asynchronous calls
        for (int i = 0; i < 3; i++) {
            try {
                HttpHeaders headers = new HttpHeaders();
                HashMap<String, String> map = new HashMap<>(1);
                map.put("encrypt", encrypt);
                String path = StrUtil.format(DING_TALK_CALLBACK_PATH,
                        dingTalkProperties.getBasePath(), dingTalkProperties.getSyncEventPath(), suiteId);
                String uri = UriComponentsBuilder.fromHttpUrl(constProperties.getVikaApiUrl() + path)
                        .queryParam("signature", signature)
                        .queryParam("nonce", nonce)
                        .queryParam("timestamp", timestamp)
                        .build().toString();
                HttpEntity<String> request = new HttpEntity<>(JSONUtil.toJsonStr(map), headers);
                ResponseEntity<DingTalkCallbackDto> response = restTemplate.postForEntity(uri, request, DingTalkCallbackDto.class);
                handleResponse(suiteId, response);
                break;
            }
            catch (Exception e) {
                log.error("call api exception", i, e);
                // timeout does not need to retry
                if (e.getCause() instanceof SocketTimeoutException) {
                    throw e;
                }
            }
        }
    }

    private <T extends DingTalkCallbackDto> void handleResponse(String suiteId, ResponseEntity<T> responseEntity) {
        if (responseEntity == null) {
            throw new RuntimeException("response message can not be null");
        }
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            if (responseEntity.getBody() != null) {
                throw new RuntimeException("response http status not in 200");
            }
            else {
                throw new RuntimeException("Failed to request java server, please check network or parameters");
            }
        }
        if (responseEntity.getBody() != null) {
            DingTalkCallbackDto body = responseEntity.getBody();
            if (body.getEncrypt() == null) {
                throw new RuntimeException("java failed to process the message, the service did not return");
            }
            DingTalkServiceProvider dingtalkServiceProvider = SpringContextHolder.getBean(DingTalkServiceProvider.class);
            DingTalkCallbackCrypto callbackCrypto;
            String decryptMsg;
            try {
                callbackCrypto = dingtalkServiceProvider.getIsvDingTalkCallbackCrypto(suiteId);
                decryptMsg = callbackCrypto.getDecryptMsg(body.getMsgSignature(), body.getTimeStamp(), body.getNonce(),
                        body.getEncrypt());
            }
            catch (Exception e) {
                log.error("Parse java returns data exception: {}:[{}]", suiteId, body, e);
                throw new RuntimeException("Parse java returns data exception");
            }
            if (!DING_TALK_CALLBACK_SUCCESS.equals(decryptMsg)) {
                log.error("java failed to process the message, return data: [{}]", decryptMsg);
                throw new RuntimeException("java processing message failed");
            }
        }
    }
}
