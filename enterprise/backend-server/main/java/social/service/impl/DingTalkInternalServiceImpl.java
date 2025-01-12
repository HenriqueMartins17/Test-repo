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

package com.apitable.enterprise.social.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.annotation.Resource;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import com.apitable.enterprise.grpc.CorpBizDataDto;
import com.apitable.enterprise.grpc.DingTalkUserDto;
import com.apitable.enterprise.grpc.DingTalkUserDto.Builder;
import com.apitable.enterprise.grpc.TenantInfoResult;
import com.apitable.enterprise.social.autoconfigure.dingtalk.DingTalkProperties;
import com.apitable.enterprise.social.autoconfigure.dingtalk.DingTalkProperties.IsvAppProperty;
import com.apitable.enterprise.social.entity.SocialUserBindEntity;
import com.apitable.enterprise.social.mapper.SocialTenantMapper;
import com.apitable.enterprise.social.mapper.SocialUserBindMapper;
import com.apitable.enterprise.social.service.IDingTalkGrpcClientService;
import com.apitable.enterprise.social.service.IDingTalkInternalService;
import com.apitable.user.entity.UserEntity;
import com.apitable.user.service.IUserService;
import com.vikadata.social.dingtalk.enums.DingTalkBizType;
import com.vikadata.social.dingtalk.enums.DingTalkMediaType;
import com.vikadata.social.dingtalk.exception.DingTalkApiException;
import com.vikadata.social.dingtalk.model.DingTalkAsyncSendCorpMessageResponse;
import com.vikadata.social.dingtalk.model.DingTalkCreateApaasAppRequest;
import com.vikadata.social.dingtalk.model.DingTalkCreateApaasAppResponse;
import com.vikadata.social.dingtalk.model.DingTalkInternalOrderResponse.InAppGoodsOrderVo;
import com.vikadata.social.dingtalk.model.DingTalkSsoUserInfoResponse;
import com.vikadata.social.dingtalk.model.DingTalkUserDetail;
import com.vikadata.social.dingtalk.model.DingTalkUserListResponse.UserPageResult;
import com.vikadata.social.dingtalk.model.UserInfoV2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.vikadata.social.dingtalk.constants.DingTalkApiConst.SEND_MESSAGE_BY_ID_MAX_COUNT;
import static com.vikadata.social.dingtalk.constants.DingTalkApiConst.SEND_MESSAGE_USER_MAX_COUNT;

/**
 * DingTalk Integrated service interface implementation
 */
@Service
@Slf4j
public class DingTalkInternalServiceImpl implements IDingTalkInternalService {

    @Resource
    private IDingTalkGrpcClientService iDingTalkGrpcClientService;

    @Resource
    private SocialTenantMapper socialTenantMapper;

    @Autowired(required = false)
    private DingTalkProperties dingTalkProperties;

    @Resource
    private SocialUserBindMapper socialUserBindMapper;

    @Resource
    private IUserService iUserService;

    @Override
    public UserInfoV2 getUserInfoByCode(String suiteId, String authCorpId, String code) {
        try {
            return iDingTalkGrpcClientService.getUserInfoByCode(suiteId, authCorpId, code);
        } catch (Exception e) {
            log.error("Failed to obtain user information according to temporary authorization code",
                e);
        }
        return null;
    }

    @Override
    public DingTalkSsoUserInfoResponse getSsoUserInfoByCode(String suiteId, String code) {
        try {
            return iDingTalkGrpcClientService.getSsoUserInfoByCode(suiteId, code);
        } catch (Exception e) {
            log.error(
                "Failed to obtain the background user information according to the temporary authorization code",
                e);
        }
        return null;
    }

    @Override
    public DingTalkUserDetail getUserDetailByCode(String suiteId, String authCorpId, String code) {
        UserInfoV2 userInfo = getUserInfoByCode(suiteId, authCorpId, code);
        if (userInfo != null) {
            try {
                return getUserDetailByUserId(suiteId, authCorpId, userInfo.getUserid());
            } catch (Exception e) {
                log.error("Failed to get user information according to user ID", e);
            }
        }
        return null;
    }

    @Override
    public DingTalkUserDetail getUserDetailByUserId(String suiteId, String authCorpId,
                                                    String userId) {
        return iDingTalkGrpcClientService.getIsvUserDetailByUserId(suiteId, authCorpId, userId);
    }


    @Override
    public DingTalkUserDto getIsvUserDetailByUserId(String suiteId, String authCorpId,
                                                    String userId) {
        DingTalkUserDetail userDetail = getUserDetailByUserId(suiteId, authCorpId, userId);
        return getIsvDingTalkUserInfo(userDetail);
    }

    @Override
    public Boolean getSocialTenantStatus(String suiteId, String authCorpId) {
        return iDingTalkGrpcClientService.getSocialTenantStatus(suiteId, authCorpId);
    }

    @Override
    public List<Long> getDepartmentSubIdList(String suiteId, String authCorpId, Long deptId) {
        try {
            return iDingTalkGrpcClientService.getDingTalkDepartmentSubIdList(suiteId, authCorpId,
                deptId.toString()).stream().filter(x -> x > 0).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Exception in obtaining sub department ID:{}:{}", authCorpId, deptId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public UserPageResult getDeptUserDetailList(String suiteId, String authCorpId, Long deptId,
                                                Integer cursor,
                                                Integer size) {
        return iDingTalkGrpcClientService.getDingTalkDeptUserList(suiteId, authCorpId,
            deptId.toString(), cursor, size);
    }

    @Override
    public HashMap<String, DingTalkUserDto> getAuthCorpUserDetailMap(String suiteId,
                                                                     String authCorpId,
                                                                     List<String> authDeptIds,
                                                                     List<String> authUserIds) {
        log.info("DingTalk ISV getting address book started:{}", authCorpId);
        long startAt = System.currentTimeMillis();
        HashMap<String, DingTalkUserDto> userMap = new HashMap<>();
        if (!authDeptIds.isEmpty()) {
            userMap.putAll(getUserTreeList(suiteId, authCorpId, authDeptIds));
        }
        if (!authUserIds.isEmpty()) {
            Set<String> existOpenIds =
                userMap.values().stream().map(DingTalkUserDto::getOpenId)
                    .collect(Collectors.toSet());
            List<String> openIds =
                authUserIds.stream().filter(i -> !existOpenIds.contains(i))
                    .collect(Collectors.toList());
            userMap.putAll(getAuthCorpUserDetailListByUserIds(suiteId, authCorpId, openIds));
        }
        log.info("DingTalk ISV time consuming to obtain address book:{}:{}:{}ms", authCorpId,
            userMap.size(), System.currentTimeMillis() - startAt);
        return userMap;
    }

    @Override
    public Map<String, DingTalkUserDto> getAuthCorpUserDetailListByUserIds(String suiteId,
                                                                           String authCorpId,
                                                                           List<String> userIds) {
        HashMap<String, DingTalkUserDto> userMap = MapUtil.newHashMap();
        for (String userId : userIds) {
            DingTalkUserDetail userDetail = getUserDetailByUserId(suiteId, authCorpId, userId);
            if (userDetail.getActive()) {
                userMap.put(userDetail.getUnionid(), getIsvDingTalkUserInfo(userDetail));
            }
        }
        return handleIsvDingTalkUserName(userMap);
    }

    @Override
    public List<DingTalkAsyncSendCorpMessageResponse> sendMessageToUserByTemplateId(String suiteId,
                                                                                    String authCorpId,
                                                                                    String templateId,
                                                                                    HashMap<String, String> data,
                                                                                    List<String> userIds) {
        String agentId = getIsvDingTalkAgentId(suiteId, authCorpId);
        return sendMessageToUserByTemplateId(suiteId, authCorpId, templateId, data, userIds,
            agentId);
    }

    @Override
    public List<DingTalkAsyncSendCorpMessageResponse> sendMessageToUserByTemplateId(String suiteId,
                                                                                    String authCorpId,
                                                                                    String templateId,
                                                                                    HashMap<String, String> data,
                                                                                    List<String> userIds,
                                                                                    String agentId) {
        List<DingTalkAsyncSendCorpMessageResponse> results = new ArrayList<>();
        if (agentId != null) {
            int maxSize = NumberUtil.ceilDiv(userIds.size(), SEND_MESSAGE_BY_ID_MAX_COUNT);
            Stream.iterate(0, n -> n + 1).limit(maxSize).forEach(i -> {
                List<String> tmpUserIds =
                    userIds.stream().skip((long) i * SEND_MESSAGE_BY_ID_MAX_COUNT)
                        .limit(SEND_MESSAGE_BY_ID_MAX_COUNT).collect(Collectors.toList());
                try {
                    DingTalkAsyncSendCorpMessageResponse response =
                        iDingTalkGrpcClientService.sendMessageToUserByTemplateId(suiteId,
                            authCorpId, agentId,
                            templateId, data, tmpUserIds);
                    results.add(response);
                } catch (DingTalkApiException e) {
                    log.error("Failed to send Ding Talk start notification:suiteId={},userIds={}",
                        suiteId, tmpUserIds, e);
                }
            });
        }
        return results;
    }

    @Override
    public IsvAppProperty getIsvAppConfig(String suiteId) {
        if (dingTalkProperties != null) {
            if (dingTalkProperties.getIsvAppList() != null) {
                for (IsvAppProperty isv : dingTalkProperties.getIsvAppList()) {
                    if (suiteId.equals(isv.getSuiteId())) {
                        return isv;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public IsvAppProperty getIsvAppConfigByDingDingDaKey(String dingDingDaKey) {
        if (dingTalkProperties.getIsvAppList() != null) {
            for (IsvAppProperty isv : dingTalkProperties.getIsvAppList()) {
                if (isv.getDingTalkDa() != null &&
                    dingDingDaKey.equals(isv.getDingTalkDa().getKey())) {
                    return isv;
                }
            }
        }
        return null;
    }

    @Override
    public String uploadMedia(String suiteId, String authCorpId, DingTalkMediaType mediaType,
                              byte[] file, String fileName) {
        return iDingTalkGrpcClientService.uploadMedia(suiteId, authCorpId, mediaType, file,
            fileName);
    }

    @Override
    public DingTalkCreateApaasAppResponse createMicroApaasApp(String suiteId, String authCorpId,
                                                              DingTalkCreateApaasAppRequest request) {
        return iDingTalkGrpcClientService.createMicroApaasApp(suiteId, authCorpId, request);
    }

    @Override
    public TenantInfoResult getSocialTenantInfo(String authCorpId, String suiteId) {
        return iDingTalkGrpcClientService.getSocialTenantInfo(authCorpId, suiteId);
    }

    @Override
    public String getInternalSkuPage(String suiteId, String authCorpId, String callbackPage,
                                     String extendParam) {
        IsvAppProperty app = getIsvAppConfig(suiteId);
        if (app != null && app.getGoodsCode() != null) {
            return iDingTalkGrpcClientService.getInternalSkuPage(suiteId, authCorpId,
                app.getGoodsCode(), callbackPage, extendParam);
        }
        return null;
    }

    @Override
    public Boolean internalOrderFinish(String suiteId, String authCorpId, String orderId) {
        try {
            return iDingTalkGrpcClientService.internalOrderFinish(suiteId, authCorpId, orderId);
        } catch (Exception e) {
            log.error("Failed to mark order processing completion:{}:{}", authCorpId, orderId, e);
        }
        return false;
    }

    @Override
    public InAppGoodsOrderVo getInternalOrder(String suiteId, String authCorpId, String orderId) {
        try {
            return iDingTalkGrpcClientService.getInternalOrder(suiteId, authCorpId, orderId);
        } catch (Exception e) {
            log.error("Failed to get DingTalk order data:{}", orderId, e);
        }
        return null;
    }

    @Override
    public String ddConfigSign(String suiteId, String authCorpId, String nonceStr, String timestamp,
                               String url) {
        return iDingTalkGrpcClientService.ddConfigSign(suiteId, authCorpId, nonceStr, timestamp,
            url);
    }

    @Override
    public String getIsvDingTalkAgentId(String suiteId, String authCorpId) {
        String result = socialTenantMapper.selectIsvAgentIdByTenantIdAndAppId(authCorpId, suiteId);
        if (result != null) {
            JSONArray jsonArray = JSONUtil.parseArray(result);
            Object agentId = jsonArray.get(0);
            if (agentId != null) {
                return agentId.toString();
            }
            log.error("Enterprise AgentId information error:{}:{}:{}", authCorpId, suiteId, result);
        }
        log.warn("Enterprise information error:{}:{}", authCorpId, suiteId);
        return null;
    }

    @Override
    public Integer getUserCount(String suiteId, String authCorpId, Boolean onlyActive) {
        return iDingTalkGrpcClientService.getDingTalkIsvUserCount(suiteId, authCorpId, onlyActive);
    }

    @Override
    public Integer getUserCountByDeptIds(String suiteId, String authCorpId, List<String> deptIds) {
        Set<String> userIds = new HashSet<>();
        for (String deptId : deptIds) {
            List<String> tmpUserIds =
                iDingTalkGrpcClientService.getDingTalkUserIdListByDeptId(suiteId, authCorpId,
                    Long.parseLong(deptId));
            userIds.addAll(tmpUserIds);
        }
        return userIds.size();
    }

    @Override
    public Integer getUserCountByDeptIdsAndUserIds(String suiteId, String authCorpId,
                                                   List<String> deptIds,
                                                   List<String> userIds) {
        Set<String> userIdSet = new HashSet<>(userIds);
        for (String deptId : deptIds) {
            List<String> tmpUserIds =
                iDingTalkGrpcClientService.getDingTalkUserIdListByDeptId(suiteId, authCorpId,
                    Long.parseLong(deptId));
            userIdSet.addAll(tmpUserIds);
        }
        return userIdSet.size();
    }

    @Override
    public Map<String, DingTalkUserDto> getUserTreeList(String suiteId, String authCorpId,
                                                        List<String> subDeptIds) {
        log.info("Getting the information of DingTalk enterprise authorized personnel starts-[{}]",
            authCorpId);
        long startedAt = System.currentTimeMillis();
        Map<String, DingTalkUserDto> userMap =
            iDingTalkGrpcClientService.getUserTreeList(suiteId, authCorpId, subDeptIds);
        log.info(
            "End of obtaining DingTalk enterprise authorized personnel information-[{}],Department:{},Total number of people:{},Time consuming:{}",
            authCorpId, subDeptIds, userMap.size(),
            System.currentTimeMillis() - startedAt);
        return handleIsvDingTalkUserName(userMap);
    }

    @Override
    public List<CorpBizDataDto> getCorpBizDataByBizTypes(String suiteId, String authCorpId,
                                                         List<DingTalkBizType> bizTypes) {
        List<Integer> types =
            bizTypes.stream().map(DingTalkBizType::getValue).collect(Collectors.toList());
        return iDingTalkGrpcClientService.getCorpBizDataByBizTypes(suiteId, authCorpId, types);
    }

    private DingTalkUserDto getIsvDingTalkUserInfo(DingTalkUserDetail userDetail) {
        Builder dto = DingTalkUserDto.newBuilder();
        dto.setOpenId(userDetail.getUserid());
        dto.setUnionId(userDetail.getUnionid());
        if (StrUtil.isNotBlank(userDetail.getAvatar())) {
            dto.setAvatar(userDetail.getAvatar());
        }
        if (StrUtil.isNotBlank(userDetail.getName())) {
            dto.setUserName(userDetail.getName());
        }
        return dto.build();
    }

    private Map<String, DingTalkUserDto> handleIsvDingTalkUserName(
        Map<String, DingTalkUserDto> users) {
        if (users.isEmpty()) {
            return users;
        }
        HashMap<String, DingTalkUserDto> userMap = new HashMap<>(users);
        int maxSize = NumberUtil.ceilDiv(userMap.size(), SEND_MESSAGE_USER_MAX_COUNT);
        // Find out the user's name according to the union ID
        Set<String> unionIds = userMap.keySet();
        Stream.iterate(0, n -> n + 1).limit(maxSize).forEach(i -> {
            List<String> tmpUnionIds =
                unionIds.stream().skip((long) i * SEND_MESSAGE_USER_MAX_COUNT)
                    .limit(SEND_MESSAGE_USER_MAX_COUNT).collect(Collectors.toList());
            HashMap<String, String> nickNameMap = getUserNameByUnionIds(tmpUnionIds);
            nickNameMap.forEach((k, v) -> {
                Builder userInfo = userMap.get(k).toBuilder();
                userInfo.setUserName(v);
                userMap.put(k, userInfo.build());
            });
        });
        return userMap;
    }

    @Override
    public HashMap<String, String> getUserNameByUnionIds(List<String> unionIds) {
        HashMap<String, String> nickNameMap = MapUtil.newHashMap();
        List<SocialUserBindEntity> entities = socialUserBindMapper.selectByUnionIds(unionIds);
        if (entities.isEmpty()) {
            return nickNameMap;
        }
        Map<Long, SocialUserBindEntity> bindMap =
            entities.stream()
                .collect(Collectors.toMap(SocialUserBindEntity::getUserId, a -> a, (k1, k2) -> k1));
        Set<Long> userIds = bindMap.keySet();
        List<UserEntity> users = iUserService.listByIds(userIds);
        for (UserEntity user : users) {
            if (bindMap.containsKey(user.getId())) {
                nickNameMap.put(bindMap.get(user.getId()).getUnionId(), user.getNickName());
            }
        }
        return nickNameMap;
    }
}
