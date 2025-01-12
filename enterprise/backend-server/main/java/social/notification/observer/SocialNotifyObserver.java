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

package com.apitable.enterprise.social.notification.observer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.annotation.Resource;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;

import com.apitable.player.ro.NotificationCreateRo;
import com.apitable.organization.service.IMemberService;
import com.apitable.organization.service.IUnitService;
import com.apitable.shared.component.notification.observer.AbstractNotifyObserver;

public abstract class SocialNotifyObserver<M, T> extends AbstractNotifyObserver<M, T> {

    @Resource
    private IMemberService iMemberService;

    @Resource
    private IUnitService iUnitService;

    @Override
    public List<String> toUser(NotificationCreateRo ro) {
        String fromOpenId = "";
        if (StrUtil.isNotBlank(ro.getFromUserId())) {
            fromOpenId =
                CollUtil.getFirst(iMemberService.getOpenIdByUserIds(
                    Collections.singletonList(Long.valueOf(ro.getFromUserId()))));
        }
        if (CollUtil.isNotEmpty(ro.getToUserId())) {
            String finalFromOpenId = fromOpenId;
            return iMemberService.getOpenIdByUserIds(
                ro.getToUserId().stream().filter(i -> ObjectUtil.notEqual(i,
                    finalFromOpenId)).map(Long::parseLong).collect(Collectors.toList()));
        }
        if (CollUtil.isNotEmpty(ro.getToMemberId())) {
            String finalFromOpenId1 = fromOpenId;
            return iMemberService.getOpenIdByIds(
                ro.getToMemberId().stream().filter(i -> ObjectUtil.notEqual(i,
                    finalFromOpenId1)).map(Long::parseLong).collect(Collectors.toList()));
        }
        if (CollUtil.isNotEmpty(ro.getToUnitId())) {
            String finalFromOpenId2 = fromOpenId;
            List<Long> memberIds =
                iUnitService.getMembersIdByUnitIds(
                    ro.getToUnitId().stream().map(Long::parseLong).collect(Collectors.toList()));
            return iMemberService.getOpenIdByIds(memberIds).stream()
                .filter(i -> ObjectUtil.notEqual(i,
                    finalFromOpenId2)).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
