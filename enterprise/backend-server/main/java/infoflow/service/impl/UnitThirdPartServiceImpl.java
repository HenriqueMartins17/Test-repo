package com.apitable.enterprise.infoflow.service.impl;

import cn.hutool.core.lang.UUID;
import com.apitable.core.exception.BusinessException;
import com.apitable.enterprise.infoflow.mapper.InfoflowUnitMapper;
import com.apitable.enterprise.infoflow.model.UserRecentResponse;
import com.apitable.enterprise.infoflow.service.IUnitThirdPartService;
import com.apitable.enterprise.social.entity.SocialUserBindEntity;
import com.apitable.enterprise.social.properties.OneAccessProperties;
import com.apitable.enterprise.social.service.ISocialUserBindService;
import com.apitable.shared.config.properties.LimitProperties;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
@Slf4j
public class UnitThirdPartServiceImpl extends BaseThirdPartUtils implements IUnitThirdPartService {

    @Resource
    private OneAccessProperties oneAccessProperties;

    @Resource
    private RestClient restClient;

    @Resource
    private ISocialUserBindService iSocialUserBindService;

    @Resource
    private InfoflowUnitMapper infoflowUnitMapper;

    @Resource
    private LimitProperties limitProperties;

    @Override
    public List<String> getUserRecent(Long userId) {
        List<String> unionIds = iSocialUserBindService.getUnionIdsByUserId(userId);
        if (unionIds.isEmpty()) {
            log.error("getUserRecent userId:{} not bind unitId", userId);
            throw new BusinessException("userId not bind unitId");
        }
        HttpHeaders header = new HttpHeaders();
        // post request needs to be set contentType
        header.setContentType(MediaType.APPLICATION_JSON);
        String salt = UUID.fastUUID().toString(true);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("uid", unionIds.get(0));
        paramMap.put("appKey", oneAccessProperties.getClientId());
        paramMap.put("timestamp", System.currentTimeMillis() / 1000);
        paramMap.put("salt", salt);
        String sign = infoflowSign(paramMap, oneAccessProperties.getClientSecret());
        paramMap.put("sign", sign);
        try {
            String url = oneAccessProperties.getIamHost() + "/callback/user/recent";
            // post request needs to be set contentType
            header.setContentType(MediaType.APPLICATION_JSON);
            ResponseEntity<UserRecentResponse> responseEntity = restClient
                .post()
                .uri(url)
                .headers(headers -> headers.addAll(header))
                .body(paramMap)
                .retrieve()
                .toEntity(UserRecentResponse.class);
            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                throw new BusinessException("response http status not in 200");
            }
            if (null == responseEntity.getBody()) {
                throw new BusinessException("getUserRecent response body is null");
            }
            if (responseEntity.getBody().getReturnCode() != 200) {
                log.info("getUserRecent target:{} , responseEntity:{}", url,
                    responseEntity.getBody());
                throw new BusinessException("getUserRecent response code not eq 200");
            }
            return responseEntity.getBody().getResult().getUserIds();
        } catch (RestClientException e) {
            throw new BusinessException(
                "Failed to request java server, please check network or parameters ");
        }
    }

    @Override
    public List<Long> getUnitIdsByUnionIdsAndSpaceId(String spaceId, List<String> unionIds) {
        List<Long> unitIds = new ArrayList<>();
        List<SocialUserBindEntity> socialUserBindEntities =
            iSocialUserBindService.getEntitiesByUnionId(unionIds);
        if (socialUserBindEntities.isEmpty()) {
            return unitIds;
        }
        socialUserBindEntities = socialUserBindEntities.stream().
            limit(limitProperties.getMemberFieldMaxLoadCount()).toList();
        List<Long> userIds = socialUserBindEntities.stream().map(SocialUserBindEntity::getUserId).
            collect(Collectors.toList());
        Map<Long, Map<String, Long>> unitIdsMap =
            infoflowUnitMapper.selectUnitIdsBySpaceIdAndUserIds(spaceId, userIds);
        //Sort by unionIds
        Map<String, Long> socialUserBindMap = socialUserBindEntities.stream().collect(
            Collectors.toMap(SocialUserBindEntity::getUnionId,
                SocialUserBindEntity::getUserId));

        unionIds.stream().distinct().forEach(unionId -> {
            Long userId = socialUserBindMap.getOrDefault(unionId, 0L);
            if (userId != 0L) {
                Map<String, Long> unitInfo = unitIdsMap.getOrDefault(userId, null);
                if (unitInfo != null) {
                    unitIds.add(unitInfo.get("unitId"));
                }
            }
        });
        return unitIds;
    }
}
