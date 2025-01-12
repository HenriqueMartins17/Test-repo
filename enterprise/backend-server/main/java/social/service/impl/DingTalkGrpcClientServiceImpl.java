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

import static com.apitable.shared.constants.AssetsPublicConstants.CAPACITY_HEX;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.apitable.enterprise.grpc.AsyncSendMessageResult;
import com.apitable.enterprise.grpc.CorpBizDataDto;
import com.apitable.enterprise.grpc.CreateMicroApaasAppResult;
import com.apitable.enterprise.grpc.CreateMicroApaasAppRo;
import com.apitable.enterprise.grpc.DepartmentSubIdRo;
import com.apitable.enterprise.grpc.DeptUserListRo;
import com.apitable.enterprise.grpc.DingTalkServiceGrpc.DingTalkServiceBlockingStub;
import com.apitable.enterprise.grpc.DingTalkSsoUserInfoResult;
import com.apitable.enterprise.grpc.DingTalkUserDto;
import com.apitable.enterprise.grpc.GetCorpBizDataRo;
import com.apitable.enterprise.grpc.GetDdConfigSignRo;
import com.apitable.enterprise.grpc.GetInternalOrderRo;
import com.apitable.enterprise.grpc.GetInternalSkuPageRo;
import com.apitable.enterprise.grpc.GetSocialTenantStatusRo;
import com.apitable.enterprise.grpc.GetSsoUserInfoByCodeRo;
import com.apitable.enterprise.grpc.GetUserCountRo;
import com.apitable.enterprise.grpc.GetUserIdListByDeptIdRo;
import com.apitable.enterprise.grpc.GetUserInfoByCodeRo;
import com.apitable.enterprise.grpc.GetUserTreeListRo;
import com.apitable.enterprise.grpc.InternalOrderFinishRo;
import com.apitable.enterprise.grpc.RequestIdResult;
import com.apitable.enterprise.grpc.SendMessageToUserByTemplateIdRo;
import com.apitable.enterprise.grpc.TenantInfoResult;
import com.apitable.enterprise.grpc.UploadMediaRo;
import com.apitable.enterprise.grpc.UserDetailRo;
import com.apitable.enterprise.grpc.UserTreeListResult;
import com.apitable.enterprise.social.service.IDingTalkGrpcClientService;
import com.google.protobuf.BoolValue;
import com.google.protobuf.ByteString;
import com.google.protobuf.StringValue;
import com.google.protobuf.UInt32Value;
import com.vikadata.social.dingtalk.enums.DingTalkMediaType;
import com.vikadata.social.dingtalk.model.DingTalkAsyncSendCorpMessageResponse;
import com.vikadata.social.dingtalk.model.DingTalkCreateApaasAppRequest;
import com.vikadata.social.dingtalk.model.DingTalkCreateApaasAppResponse;
import com.vikadata.social.dingtalk.model.DingTalkDepartmentSubIdListResponse.DeptListSubIdResponse;
import com.vikadata.social.dingtalk.model.DingTalkDepartmentUserIdListResponse.ListUserByDeptResponse;
import com.vikadata.social.dingtalk.model.DingTalkInternalOrderResponse.InAppGoodsOrderVo;
import com.vikadata.social.dingtalk.model.DingTalkSsoUserInfoResponse;
import com.vikadata.social.dingtalk.model.DingTalkSsoUserInfoResponse.CorpInfo;
import com.vikadata.social.dingtalk.model.DingTalkSsoUserInfoResponse.UserInfo;
import com.vikadata.social.dingtalk.model.DingTalkUserDetail;
import com.vikadata.social.dingtalk.model.DingTalkUserListResponse.UserPageResult;
import com.vikadata.social.dingtalk.model.UserInfoV2;
import io.grpc.StatusRuntimeException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

/**
 * nest grpc client
 */
@Service
@Slf4j
public class DingTalkGrpcClientServiceImpl implements IDingTalkGrpcClientService {
    private static final Integer ERROR_CODE = 500;

    @GrpcClient("dingtalk-grpc-server")
    private DingTalkServiceBlockingStub dingTalkServiceBlockingStub;

    @Override
    public UserPageResult getDingTalkDeptUserList(String suiteId, String authCorpId, String deptId, Integer cursor, Integer size) {
        DeptUserListRo ro = DeptUserListRo.newBuilder()
                .setSuiteId(suiteId)
                .setAuthCorpId(authCorpId)
                .setDeptId(deptId)
                .setCursor(cursor)
                .setSize(size)
                .build();
        RequestIdResult result = dingTalkServiceBlockingStub.getDeptUserList(ro);
        return BeanUtil.toBean(JSONUtil.parseObj(result.getResult().getValue().toStringUtf8()), UserPageResult.class);
    }

    @Override
    public List<Long> getDingTalkDepartmentSubIdList(String suiteId, String authCorpId, String deptId) {
        DepartmentSubIdRo ro = DepartmentSubIdRo.newBuilder()
                .setSuiteId(suiteId)
                .setAuthCorpId(authCorpId)
                .setDeptId(deptId)
                .build();
        RequestIdResult result = dingTalkServiceBlockingStub.getDepartmentSubIdList(ro);
        DeptListSubIdResponse departmentSubIdList =
                BeanUtil.toBean(JSONUtil.parseObj(result.getResult().getValue().toStringUtf8()),
                        DeptListSubIdResponse.class);
        return departmentSubIdList.getDeptIdList();
    }

    @Override
    public DingTalkUserDetail getIsvUserDetailByUserId(String suiteId, String authCorpId, String userId) {
        UserDetailRo ro = UserDetailRo.newBuilder()
                .setSuiteId(suiteId)
                .setAuthCorpId(authCorpId)
                .setUserId(userId)
                .build();
        RequestIdResult result = dingTalkServiceBlockingStub.getUserDetailByUserId(ro);
        return BeanUtil.toBean(JSONUtil.parseObj(result.getResult().getValue().toStringUtf8()), DingTalkUserDetail.class);
    }

    @Override
    public Boolean getSocialTenantStatus(String suiteId, String authCorpId) {
        GetSocialTenantStatusRo ro =
                GetSocialTenantStatusRo.newBuilder().setAuthCorpId(authCorpId).setSuiteId(suiteId).build();
        BoolValue result = dingTalkServiceBlockingStub.getSocialTenantStatus(ro);
        return result.getValue();
    }

    @Override
    public UserInfoV2 getUserInfoByCode(String suiteId, String authCorpId, String code) {
        GetUserInfoByCodeRo ro =
                GetUserInfoByCodeRo.newBuilder().setAuthCorpId(authCorpId).setCode(code).setSuiteId(suiteId).build();
        RequestIdResult result = dingTalkServiceBlockingStub.getUserInfoByCode(ro);
        return BeanUtil.toBean(JSONUtil.parseObj(result.getResult().getValue().toStringUtf8()), UserInfoV2.class);
    }

    @Override
    public DingTalkSsoUserInfoResponse getSsoUserInfoByCode(String suiteId, String code) {
        GetSsoUserInfoByCodeRo ro = GetSsoUserInfoByCodeRo.newBuilder().setCode(code).setSuiteId(suiteId).build();
        DingTalkSsoUserInfoResult result = dingTalkServiceBlockingStub.getSsoUserInfoByCode(ro);
        DingTalkSsoUserInfoResponse response = new DingTalkSsoUserInfoResponse();
        response.setErrcode(result.getErrcode());
        response.setErrmsg(result.getErrmsg());
        response.setIsSys(result.getIsSys());
        response.setUserInfo(BeanUtil.toBean(JSONUtil.parseObj(result.getUserInfo().getValue().toStringUtf8()), UserInfo.class));
        response.setCorpInfo(BeanUtil.toBean(JSONUtil.parseObj(result.getCorpInfo().getValue().toStringUtf8()), CorpInfo.class));
        return response;
    }

    @Override
    public DingTalkAsyncSendCorpMessageResponse sendMessageToUserByTemplateId(String suiteId, String authCorpId, String agentId, String templateId, HashMap<String, String> data, List<String> userIds) {
        DingTalkAsyncSendCorpMessageResponse response = new DingTalkAsyncSendCorpMessageResponse();
        if (!userIds.isEmpty()) {
            SendMessageToUserByTemplateIdRo ro = SendMessageToUserByTemplateIdRo.newBuilder()
                    .setSuiteId(suiteId)
                    .setAuthCorpId(authCorpId)
                    .setAgentId(agentId)
                    .setTemplateId(templateId)
                    .addAllUserIds(userIds)
                    .setData(JSONUtil.toJsonStr(data)).build();
            try {
                AsyncSendMessageResult result = dingTalkServiceBlockingStub.sendMessageToUserByTemplateId(ro);
                response.setRequestId(result.getRequestId());
                response.setTaskId(result.getTaskId());
            }
            catch (StatusRuntimeException e) {
                log.warn("dingtalk send card message fail", e);
            }
        }
        return response;
    }

    @Override
    public String uploadMedia(String suiteId, String authCorpId, DingTalkMediaType mediaType, byte[] file, String fileName) {
        UploadMediaRo ro = UploadMediaRo.newBuilder()
                .setSuiteId(suiteId)
                .setAuthCorpId(authCorpId)
                .setMediaType(mediaType.getValue())
                .setFileBytes(ByteString.copyFrom(file))
                .setFileName(fileName).build();
        StringValue result = dingTalkServiceBlockingStub.uploadMedia(ro);
        return result.getValue();
    }

    @Override
    public DingTalkCreateApaasAppResponse createMicroApaasApp(String suiteId, String authCorpId, DingTalkCreateApaasAppRequest request) {
        CreateMicroApaasAppRo ro = CreateMicroApaasAppRo.newBuilder()
                .setSuiteId(suiteId)
                .setAuthCorpId(authCorpId)
                .setAppName(request.getAppName())
                .setAppDesc(request.getAppDesc())
                .setAppIcon(request.getAppIcon())
                .setHomepageLink(request.getHomepageLink())
                .setPcHomepageLink(request.getPcHomepageLink())
                .setOmpLink(request.getOmpLink())
                .setHomepageEditLink(request.getHomepageEditLink())
                .setPcHomepageEditLink(request.getPcHomepageEditLink())
                .setOpUserId(request.getOpUserId())
                .setBizAppId(request.getBizAppId())
                .setTemplateKey(request.getTemplateKey())
                .build();
        CreateMicroApaasAppResult result = dingTalkServiceBlockingStub.createMicroApaasApp(ro);
        DingTalkCreateApaasAppResponse response = new DingTalkCreateApaasAppResponse();
        response.setBizAppId(result.getBizAppId());
        response.setAgentId(result.getAgentId());
        return response;
    }

    @Override
    public TenantInfoResult getSocialTenantInfo(String authCorpId, String suiteId) {
        GetSocialTenantStatusRo ro = GetSocialTenantStatusRo.newBuilder()
                .setAuthCorpId(authCorpId)
                .setSuiteId(suiteId).build();
        return dingTalkServiceBlockingStub.getSocialTenantInfo(ro);
    }

    @Override
    public String getInternalSkuPage(String suiteId, String authCorpId, String goodsCode, String callbackPage, String extendParam) {
        GetInternalSkuPageRo ro = GetInternalSkuPageRo.newBuilder()
                .setSuiteId(suiteId)
                .setAuthCorpId(authCorpId)
                .setGoodsCode(goodsCode)
                .setCallbackPage(StrUtil.emptyToDefault(callbackPage, ""))
                .setExtendParam(StrUtil.emptyToDefault(extendParam, ""))
                .build();
        StringValue result = dingTalkServiceBlockingStub.getInternalSkuPage(ro);
        return result.getValue();
    }

    @Override
    public Boolean internalOrderFinish(String suiteId, String authCorpId, String orderId) {
        InternalOrderFinishRo ro = InternalOrderFinishRo.newBuilder()
                .setSuiteId(suiteId)
                .setAuthCorpId(authCorpId)
                .setOrderId(orderId).build();
        BoolValue result = dingTalkServiceBlockingStub.internalOrderFinish(ro);
        return result.getValue();
    }

    @Override
    public InAppGoodsOrderVo getInternalOrder(String suiteId, String authCorpId, String orderId) {
        GetInternalOrderRo ro = GetInternalOrderRo.newBuilder()
                .setSuiteId(suiteId)
                .setAuthCorpId(authCorpId)
                .setOrderId(orderId).build();
        RequestIdResult result = dingTalkServiceBlockingStub.getInternalOrder(ro);
        return BeanUtil.toBean(JSONUtil.parseObj(result.getResult().getValue().toStringUtf8()), InAppGoodsOrderVo.class);
    }

    @Override
    public String ddConfigSign(String suiteId, String authCorpId, String nonceStr, String timestamp, String url) {
        GetDdConfigSignRo ro = GetDdConfigSignRo.newBuilder()
                .setSuiteId(suiteId)
                .setAuthCorpId(authCorpId)
                .setNonceStr(nonceStr)
                .setTimestamp(timestamp)
                .setUrl(url).build();
        StringValue result = dingTalkServiceBlockingStub.getDdConfigSign(ro);
        return result.getValue();
    }

    @Override
    public Integer getDingTalkIsvUserCount(String suiteId, String authCorpId, Boolean onlyActive) {
        GetUserCountRo ro = GetUserCountRo.newBuilder()
                .setSuiteId(suiteId)
                .setAuthCorpId(authCorpId)
                .setOnlyActive(onlyActive).build();
        UInt32Value result = dingTalkServiceBlockingStub.getUserCount(ro);
        return result.getValue();
    }

    @Override
    public List<String> getDingTalkUserIdListByDeptId(String suiteId, String authCorpId, Long deptId) {
        GetUserIdListByDeptIdRo ro = GetUserIdListByDeptIdRo.newBuilder()
                .setSuiteId(suiteId)
                .setAuthCorpId(authCorpId)
                .setDeptId(deptId.toString()).build();
        RequestIdResult result = dingTalkServiceBlockingStub.getUserIdListByDeptId(ro);
        ListUserByDeptResponse departmentSubIdList =
                BeanUtil.toBean(JSONUtil.parseObj(result.getResult().getValue().toStringUtf8()),
                        ListUserByDeptResponse.class);
        return departmentSubIdList.getUseridList();
    }

    @Override
    public Map<String, DingTalkUserDto> getUserTreeList(String suiteId, String authCorpId, List<String> subDeptIds) {
        GetUserTreeListRo ro = GetUserTreeListRo.newBuilder()
                .setSuiteId(suiteId)
                .setAuthCorpId(authCorpId)
                .addAllSubDeptIds(subDeptIds).build();
        UserTreeListResult result = dingTalkServiceBlockingStub.withDeadlineAfter(1, TimeUnit.HOURS)
                .withMaxInboundMessageSize(CAPACITY_HEX.intValue()).getUerTreeList(ro);
        return result.getUserTreeListMap();
    }

    @Override
    public List<CorpBizDataDto> getCorpBizDataByBizTypes(String suiteId, String authCorpId, List<Integer> bizTypes) {
        GetCorpBizDataRo ro =
                GetCorpBizDataRo.newBuilder().setAuthCorpId(authCorpId).setSuiteId(suiteId).addAllBizTypes(bizTypes).build();
        return dingTalkServiceBlockingStub.getCorpBizData(ro).getResultList();
    }
}
