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

package com.apitable.enterprise.social.remind;

import java.util.List;
import java.util.Objects;

import jakarta.annotation.Resource;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import com.apitable.enterprise.social.entity.SocialTenantEntity;
import com.apitable.enterprise.social.model.TenantBindDTO;
import com.apitable.enterprise.social.service.ISocialTenantBindService;
import com.apitable.enterprise.social.service.ISocialTenantService;
import com.apitable.enterprise.social.enums.SocialAppType;
import com.apitable.workspace.observer.remind.AbstractRemind;
import com.apitable.workspace.observer.remind.NotifyDataSheetMeta;
import com.apitable.workspace.observer.remind.NotifyDataSheetMeta.RemindParameter;
import com.apitable.organization.entity.MemberEntity;

@Slf4j
public abstract class AbstractIMRemind extends AbstractRemind {

    protected String socialTenantId;

    protected String socialAppId;

    /**
     * app type(1: internal, 2: isv)
     */
    protected SocialAppType appType;

    protected String fromOpenId;

    protected List<String> toOpenIds;

    @Resource
    protected ISocialTenantService iSocialTenantService;

    @Resource
    protected ISocialTenantBindService iSocialTenantBindService;

    @Override
    protected void wrapperMeta(NotifyDataSheetMeta meta) {
        String nodeName = getNodeName(meta.getNodeId());
        // parameters to use when sending im
        // Query the third-party integrated user identity of the member
        fromOpenId = memberMapper.selectOpenIdByMemberId(meta.getFromMemberId());
        toOpenIds = memberMapper.selectOpenIdByMemberIds(meta.getToMemberIds());
        toOpenIds = CollUtil.removeEmpty(toOpenIds);
        log.info("[remind notification]- send user im information - fromOpenId：{} - sendOpenIds：{}",
            fromOpenId, toOpenIds);
        if (StrUtil.isNotBlank(fromOpenId) && CollUtil.isNotEmpty(toOpenIds)) {
            RemindParameter remindParameter = new RemindParameter();
            TenantBindDTO bindInfo =
                iSocialTenantBindService.getTenantBindInfoBySpaceId(meta.getSpaceId());
            socialAppId = bindInfo.getAppId();
            socialTenantId = bindInfo.getTenantId();
            SocialTenantEntity tenantEntity =
                iSocialTenantService.getByAppIdAndTenantId(bindInfo.getAppId(),
                    bindInfo.getTenantId());
            appType = SocialAppType.of(tenantEntity.getAppType());
            // sender name
            if (Objects.nonNull(appType) && appType == SocialAppType.ISV) {
                MemberEntity memberEntity = getMember(meta.getFromMemberId());
                remindParameter.setFromMemberName(memberEntity.getMemberName());
                Integer socialNameModified = memberEntity.getIsSocialNameModified();
                remindParameter.setFromMemberNameModified(
                    Objects.isNull(socialNameModified) || socialNameModified != 0);
            } else {
                remindParameter.setFromMemberName(getMemberName(meta.getFromMemberId()));
            }
            // node name
            remindParameter.setNodeName(nodeName);
            // notify url
            remindParameter.setNotifyUrl(buildNotifyUrl(meta, false));
            meta.setRemindParameter(remindParameter);
        }
    }
}
