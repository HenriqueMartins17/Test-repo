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

import static com.apitable.enterprise.social.enums.SocialException.DING_TALK_DA_NOT_BIND;
import static com.apitable.enterprise.social.enums.SocialException.DING_TALK_DA_SIGNATURE_ERROR;
import static com.apitable.enterprise.social.enums.SocialException.DING_TALK_DA_TEMPLATE_NOT_EXITS;
import static com.apitable.enterprise.social.enums.SocialException.DING_TALK_DA_TENANT_NOT_EXITS;
import static com.apitable.enterprise.social.enums.SocialException.USER_NOT_EXIST;
import static com.apitable.enterprise.social.redis.RedisKey.getDingTalkTemplateIconKey;
import static com.apitable.shared.constants.NodeExtraConstants.DING_TALK_DA_STATUS;
import static com.apitable.shared.constants.NodeExtraConstants.DING_TALK_DA_TEMPLATE_KEY;
import static com.apitable.workspace.enums.PermissionException.NODE_OPERATION_DENIED;

import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.apitable.control.infrastructure.ControlTemplate;
import com.apitable.control.infrastructure.permission.NodePermission;
import com.apitable.core.exception.BusinessException;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.enterprise.social.autoconfigure.dingtalk.DingTalkProperties.IsvAppProperty;
import com.apitable.enterprise.social.enums.SocialPlatformType;
import com.apitable.enterprise.social.model.DingTalkDaCreateTemplateDTO;
import com.apitable.enterprise.social.model.DingTalkDaDTO;
import com.apitable.enterprise.social.model.DingTalkDaTemplateUpdateRo;
import com.apitable.enterprise.social.model.SocialUser;
import com.apitable.enterprise.social.model.TenantBindDTO;
import com.apitable.enterprise.social.service.IDingTalkDaService;
import com.apitable.enterprise.social.service.IDingTalkInternalService;
import com.apitable.enterprise.social.service.ISocialService;
import com.apitable.enterprise.social.service.ISocialTenantBindService;
import com.apitable.enterprise.social.service.ISocialTenantService;
import com.apitable.enterprise.social.service.ISocialTenantUserService;
import com.apitable.enterprise.social.service.ISocialUserBindService;
import com.apitable.enterprise.vika.core.model.DingTalkDaTemplateInfo;
import com.apitable.organization.entity.MemberEntity;
import com.apitable.organization.mapper.MemberMapper;
import com.apitable.shared.component.TaskManager;
import com.apitable.shared.config.properties.ConstProperties;
import com.apitable.shared.util.IdUtil;
import com.apitable.template.dto.TemplateInfo;
import com.apitable.template.mapper.TemplateMapper;
import com.apitable.user.service.IUserService;
import com.apitable.workspace.dto.NodeCopyOptions;
import com.apitable.workspace.entity.NodeEntity;
import com.apitable.workspace.enums.NodeType;
import com.apitable.workspace.mapper.NodeMapper;
import com.apitable.workspace.ro.NodeUpdateOpRo;
import com.apitable.workspace.service.INodeService;
import com.vikadata.social.dingtalk.enums.DingTalkMediaType;
import com.vikadata.social.dingtalk.model.DingTalkCreateApaasAppRequest;
import com.vikadata.social.dingtalk.model.DingTalkCreateApaasAppResponse;
import com.vikadata.social.dingtalk.model.DingTalkUserDetail;
import com.vikadata.social.dingtalk.util.DingTalkSignatureUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * DingTalk Integration Service Interface Implementation
 */
@Service
@Slf4j
public class DingTalkDaServiceImpl implements IDingTalkDaService {
    private static final String ISV_BIZ_APP_HOME_PAGE_TPL =
        "{}/user/dingtalk/social_bind_space?corpId={}&suiteId={}&bizAppId={}";

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private ISocialTenantUserService iSocialTenantUserService;

    @Resource
    private ISocialUserBindService iSocialUserBindService;

    @Resource
    private ISocialTenantBindService iSocialTenantBindService;

    @Resource
    private IDingTalkInternalService iDingTalkInternalService;

    @Resource
    private ISocialTenantService iSocialTenantService;

    @Resource
    private ControlTemplate controlTemplate;

    @Resource
    private INodeService iNodeService;

    @Resource
    private TemplateMapper templateMapper;

    @Resource
    private MemberMapper memberMapper;

    @Resource
    private ConstProperties constProperties;

    @Resource
    private NodeMapper nodeMapper;

    @Resource
    private IUserService iUserService;

    @Resource
    private ISocialService iSocialService;

    @Override
    public void validateSignature(String dingTalkDaKey, String corpId, String timestamp,
                                  String signature) {
        try {
            String calSignature = getSignature(dingTalkDaKey, corpId, timestamp);
            if (!signature.equals(calSignature)) {
                throw new BusinessException(DING_TALK_DA_SIGNATURE_ERROR);
            }
        } catch (Exception e) {
            throw new BusinessException(DING_TALK_DA_SIGNATURE_ERROR);
        }
    }

    @Override
    public String getSignature(String dingTalkDaKey, String corpId, String timestamp) {
        IsvAppProperty app = iDingTalkInternalService.getIsvAppConfigByDingDingDaKey(dingTalkDaKey);
        if (app != null) {
            String canonicalString =
                DingTalkSignatureUtil.getCanonicalStringForIsv(Long.parseLong(timestamp), corpId);
            return DingTalkSignatureUtil.computeSignature(app.getDingTalkDa().getSecret(),
                canonicalString);
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DingTalkDaCreateTemplateDTO dingTalkDaTemplateCreate(String dingDingDaKey,
                                                                String authCorpId,
                                                                String templateId, String opUserId,
                                                                String appName) {
        IsvAppProperty app = iDingTalkInternalService.getIsvAppConfigByDingDingDaKey(dingDingDaKey);
        ExceptionUtil.isNotNull(app, DING_TALK_DA_NOT_BIND);
        TemplateInfo info = templateMapper.selectInfoByTempId(templateId);
        ExceptionUtil.isNotNull(info, DING_TALK_DA_TEMPLATE_NOT_EXITS);
        String appId = app.getSuiteId();
        String homePageLink;
        MemberEntity member = null;
        String nodeId = IdUtil.createNodeId();
        String spaceId = iSocialTenantBindService.getTenantBindSpaceId(authCorpId, appId);
        if (StrUtil.isNotBlank(spaceId)) {
            member = memberMapper.selectBySpaceIdAndOpenId(spaceId, opUserId);
        } else {
            // todo
            log.error("Installing Ding Talk: The main application is not bound:{}:{}", authCorpId,
                appId);
        }
        if (member != null) {
            Long userId = member.getUserId();
            // Since Ding Talk may not open the main application, here we initialize the vika user ID according to the open ID
            if (userId == null) {
                DingTalkUserDetail userDetail =
                    iDingTalkInternalService.getUserDetailByUserId(appId, authCorpId, opUserId);
                ExceptionUtil.isFalse(userDetail == null, USER_NOT_EXIST);
                // Create or obtain user ID
                userId = iSocialService.createSocialUser(
                    new SocialUser(member.getMemberName(), userDetail.getAvatar(),
                        appId, authCorpId, userDetail.getUserid(), userDetail.getUnionid(),
                        SocialPlatformType.DINGTALK));
            }
            // Reference template
            info.setTemplateId(templateId);
            nodeId = quoteTemplate(info, spaceId, member.getId(), userId, appName);
        }
        homePageLink = dingTalkDaEntryPageUrl(authCorpId, appId, nodeId);
        DingTalkDaCreateTemplateDTO dto = new DingTalkDaCreateTemplateDTO();
        dto.setBizAppId(nodeId);
        dto.setHomepageLink(homePageLink);
        dto.setPcHomepageLink(homePageLink);
        dto.setHomepageEditLink(homePageLink);
        dto.setPcHomepageEditLink(homePageLink);
        return dto;
    }

    @Override
    public DingTalkCreateApaasAppResponse createApssApp(String suiteId, String authCorpId,
                                                        String bizAppId, TemplateInfo templateInfo,
                                                        String templateIconId, String opUserId) {
        String templateId = templateInfo.getTemplateId();
        // Create an application, which can be seen on the console
        DingTalkCreateApaasAppRequest request = new DingTalkCreateApaasAppRequest();
        String appDesc = templateInfo.getName().split(" ")[0];
        String homePageLink = dingTalkDaEntryPageUrl(authCorpId, suiteId, bizAppId);
        request.setAppName(templateInfo.getName());
        request.setAppDesc(appDesc);
        request.setAppIcon(templateIconId);
        request.setHomepageLink(homePageLink);
        request.setPcHomepageLink(homePageLink);
        request.setPcHomepageEditLink(homePageLink);
        request.setHomepageEditLink(homePageLink);
        request.setOmpLink(homePageLink);
        request.setBizAppId(bizAppId);
        request.setOpUserId(opUserId);
        request.setTemplateKey(templateId);
        return iDingTalkInternalService.createMicroApaasApp(suiteId, authCorpId, request);
    }

    @Override
    public String dingTalkDaTemplateIconMediaId(String suiteId, String authCorpId,
                                                String templateId) {
        String cacheKey = getDingTalkTemplateIconKey(templateId);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(cacheKey))) {
            DingTalkDaTemplateInfo info = JSONUtil.toBean(redisTemplate.opsForValue().get(cacheKey),
                DingTalkDaTemplateInfo.class);
            if (StrUtil.isNotBlank(info.getIconMediaId())) {
                return info.getIconMediaId();
            }
            try {
                byte[] fileData = HttpUtil.downloadBytes(info.getIconUrl());
                String mediaId = iDingTalkInternalService.uploadMedia(suiteId, authCorpId,
                    DingTalkMediaType.IMAGE,
                    fileData, info.getIconName());
                info.setIconMediaId(mediaId);
                redisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(info));
                return mediaId;
            } catch (Exception e) {
                log.error("DingTalk template icon upload failed:{}", templateId, e);
            }
            return null;
        }
        log.error(
            "The DingTalk template is not configured with icon, please confirm whether it is correct:{}",
            templateId);
        return null;
    }

    @Override
    public String quoteTemplate(TemplateInfo templateInfo, String spaceId, Long opMemberId,
                                Long opUserId,
                                String nodeName) {
        String rootNodeId = iNodeService.getRootNodeIdBySpaceId(spaceId);
        String templateId = templateInfo.getTemplateId();
        // Verify node permissions
        controlTemplate.checkNodePermission(opMemberId, rootNodeId, NodePermission.CREATE_NODE,
            status -> ExceptionUtil.isTrue(status, NODE_OPERATION_DENIED));
        String sourceNodeId = templateInfo.getNodeId();
        // Copy the node to the specified space directory
        NodeEntity shareNode = nodeMapper.selectByNodeId(sourceNodeId);
        NodeType nodeType = NodeType.toEnum(shareNode.getType());
        String nodeId = IdUtil.createNodeId(nodeType);
        TaskManager.me().execute(() -> {
            NodeCopyOptions options =
                NodeCopyOptions.builder().copyData(true).verifyNodeCount(false)
                    .nodeName(nodeName).nodeId(nodeId).dingTalkDaTemplateKey(templateId)
                    .sourceTemplateId(templateId).build();
            // Transfer node method, there are GRPC calls, if not asynchronous calls, ensure that the last call
            iNodeService.copyNodeToSpace(opUserId, spaceId, rootNodeId, templateInfo.getNodeId(),
                options);
        });
        // Cumulative template usage
        TaskManager.me().execute(() -> templateMapper.updateUsedTimesByTempId(templateId, 1));
        return nodeId;
    }

    @Override
    public DingTalkDaDTO getDingTalkDaInfoByBizAppId(String bizAppId) {
        String spaceId = iNodeService.getSpaceIdByNodeId(bizAppId);
        TenantBindDTO bindInfo = iSocialTenantBindService.getTenantBindInfoBySpaceId(spaceId);
        if (bindInfo != null && bindInfo.getAppId() != null) {
            if (!iSocialTenantService.isTenantActive(bindInfo.getTenantId(), bindInfo.getAppId())) {
                return null;
            }
            IsvAppProperty app = iDingTalkInternalService.getIsvAppConfig(bindInfo.getAppId());
            if (app != null) {
                DingTalkDaDTO dto = new DingTalkDaDTO();
                dto.setDingTalkSuiteKey(app.getSuiteKey());
                Integer status = nodeMapper.selectDingTalkDaStatusByNodeId(bizAppId);
                dto.setDingTalkDaStatus(status == null ? 0 : status);
                dto.setDingTalkCorpId(bindInfo.getTenantId());
                return dto;
            }
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dingTalkDaTemplateUpdate(String dingTalkDaKey,
                                         DingTalkDaTemplateUpdateRo updateRo) {
        IsvAppProperty app = iDingTalkInternalService.getIsvAppConfigByDingDingDaKey(dingTalkDaKey);
        ExceptionUtil.isNotNull(app, DING_TALK_DA_NOT_BIND);
        String appId = app.getSuiteId();
        String authCorpId = updateRo.getCorpId();
        String nodeId = updateRo.getBizAppId();
        String spaceId = iSocialTenantBindService.getTenantBindSpaceId(authCorpId, appId);
        ExceptionUtil.isNotNull(spaceId, DING_TALK_DA_TENANT_NOT_EXITS);
        String nodeName = nodeMapper.selectNodeNameByNodeId(nodeId);
        if (StrUtil.isNotBlank(updateRo.getName()) && !nodeName.equals(updateRo.getName())) {
            String unionId = iSocialTenantUserService.getUnionIdByOpenId(appId, authCorpId,
                updateRo.getOpUserId());
            Long userId = iSocialUserBindService.getUserIdByUnionId(unionId);
            if (userId != null) {
                NodeUpdateOpRo ro = new NodeUpdateOpRo();
                ro.setNodeName(updateRo.getName());
                iNodeService.edit(userId, nodeId, ro);
            }
        }
        dingTalkDaTemplateStatusUpdate(nodeId, updateRo.getAppStatus());
    }

    @Override
    public void dingTalkDaTemplateStatusUpdate(String nodeId, Integer status) {
        if (status != null) {
            // DingTalk Application status 0 means deactivated, 1 means enabled, 2 means deleted, and 3 means unpublished
            String extra = nodeMapper.selectExtraByNodeId(nodeId);
            if (StrUtil.isBlank(extra)) {
                Dict extraObj = Dict.create().set(DING_TALK_DA_STATUS, status);
                nodeMapper.insertExtraByNodeId(nodeId, JSONUtil.toJsonStr(extraObj));
            } else {
                nodeMapper.updateDingTalkDaStatusByNodeId(nodeId, status);
            }
        }
    }

    @Override
    public void handleTemplateQuoted(String spaceId, String nodeId, String templateId,
                                     Long memberId) {
        TenantBindDTO bindInfo = iSocialTenantBindService.getTenantBindInfoBySpaceId(spaceId);
        // Filter non nailed isv
        if (bindInfo == null || bindInfo.getAppId() == null) {
            return;
        }
        if (!iSocialTenantService.isTenantActive(bindInfo.getTenantId(), bindInfo.getAppId())) {
            return;
        }
        IsvAppProperty app = iDingTalkInternalService.getIsvAppConfig(bindInfo.getAppId());
        // It is not a third-party application. The third-party application is not integrated with Ding Talk
        if (app == null || app.getDingTalkDa() == null ||
            app.getDingTalkDa().getTemplate() == null) {
            return;
        }
        // Is not the specified template
        String cacheKey = getDingTalkTemplateIconKey(templateId);
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(cacheKey))) {
            return;
        }
        TemplateInfo info = templateMapper.selectInfoByTempId(templateId);
        // Template does not exist
        if (info == null) {
            return;
        }
        info.setName(nodeMapper.selectNodeNameByNodeId(nodeId));
        String openId = memberMapper.selectOpenIdById(memberId);
        if (StrUtil.isBlank(openId)) {
            log.error("Failed to synchronize DingTalk template: the user does not exist:{}:{}",
                templateId, memberId);
            return;
        }

        String mediaId = app.getDingTalkDa().getTemplate().get(templateId);
        try {
            if (StrUtil.isBlank(mediaId)) {
                mediaId = dingTalkDaTemplateIconMediaId(bindInfo.getAppId(), bindInfo.getTenantId(),
                    templateId);
            }
            DingTalkCreateApaasAppResponse response =
                createApssApp(bindInfo.getAppId(), bindInfo.getTenantId(), nodeId,
                    info, mediaId, openId);
            // Update node information
            if (response.getErrcode() == 0) {
                // DingTalk Application status 0 means deactivated, 1 means enabled, 2 means deleted, and 3 means unpublished
                String extra = nodeMapper.selectExtraByNodeId(nodeId);
                JSONObject extraObj =
                    StrUtil.isNotBlank(extra) ? JSONUtil.parseObj(extra) : JSONUtil.createObj();
                extraObj.set(DING_TALK_DA_STATUS, 1);
                extraObj.set(DING_TALK_DA_TEMPLATE_KEY, templateId);
                nodeMapper.insertExtraByNodeId(nodeId, JSONUtil.toJsonStr(extraObj));
            } else {
                log.error("Failed to synchronize DingTalk template:{}:[{}]", templateId, response);
            }
        } catch (Exception e) {
            log.error("Failed to synchronize DingTalk template:{}", templateId, e);
        }
    }

    private String dingTalkDaEntryPageUrl(String authCorpId, String suiteId, String bizAppId) {
        return StrUtil.format(ISV_BIZ_APP_HOME_PAGE_TPL, constProperties.getServerDomain(),
            authCorpId, suiteId,
            bizAppId);
    }
}
