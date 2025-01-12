/*
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its subdirectories does not constitute permission to use this code or APITable Enterprise Edition features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

package com.apitable.enterprise.infoflow.service.impl;

import cn.hutool.core.lang.UUID;
import com.apitable.core.exception.BusinessException;
import com.apitable.enterprise.infoflow.model.PermissionResponse;
import com.apitable.enterprise.infoflow.service.IControlThirdPartService;
import com.apitable.enterprise.social.properties.OneAccessProperties;
import com.apitable.enterprise.social.service.ISocialUserBindService;
import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Control service for infoflow.
 */
@Service
@Slf4j
public class ControlInfoflowServiceImpl extends BaseThirdPartUtils
    implements IControlThirdPartService {

    @Resource
    private OneAccessProperties oneAccessProperties;

    @Resource
    private ISocialUserBindService iSocialUserBindService;

    @Resource
    private RestClient restClient;

    public int getPermission(String nodeId, Long userId) {
        log.debug("getPermission nodeId:{} , userId:{}", nodeId, userId);
        List<String> unionIds = iSocialUserBindService.getUnionIdsByUserId(userId);
        if (unionIds.isEmpty()) {
            return 0;
        }

        // get permission by infoflow
        String salt = UUID.fastUUID().toString(true);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("nodeId", nodeId);
        paramMap.put("uid", unionIds.get(0));
        paramMap.put("appKey", oneAccessProperties.getClientId());
        paramMap.put("timestamp", System.currentTimeMillis() / 1000);
        paramMap.put("salt", salt);
        String sign = infoflowSign(paramMap, oneAccessProperties.getClientSecret());
        paramMap.put("sign", sign);

        try {
            String url = oneAccessProperties.getIamHost() + "/callback/permission/verify";
            HttpHeaders header = new HttpHeaders();
            // post request needs to be set contentType
            header.setContentType(MediaType.APPLICATION_JSON);
            ResponseEntity<PermissionResponse> responseEntity = restClient
                .post()
                .uri(url)
                .headers(headers -> headers.addAll(header))
                .body(paramMap)
                .retrieve()
                .toEntity(PermissionResponse.class);
            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                throw new BusinessException("response http status not in 200");
            }
            if (null == responseEntity.getBody()) {
                throw new BusinessException("permission response body is null");
            }
            if (responseEntity.getBody().getReturnCode() != 200) {
                log.info("getPermission target:{} , responseEntity:{}", url,
                    responseEntity.getBody());
                throw new BusinessException("permission verify returnCode not in 200");
            }
            return responseEntity.getBody().getResult().getPermission();
        } catch (RestClientException e) {
            log.info("getPermission  Exception:{}", e.getMessage());
            throw new BusinessException(
                "Failed to request java server, please check network or parameters ");
        }
    }
}
