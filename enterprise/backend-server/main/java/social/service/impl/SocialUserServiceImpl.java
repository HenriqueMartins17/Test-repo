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

import java.util.List;

import jakarta.annotation.Resource;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;

import com.apitable.base.enums.DatabaseException;
import com.apitable.enterprise.social.entity.SocialUserEntity;
import com.apitable.enterprise.social.enums.SocialPlatformType;
import com.apitable.enterprise.social.mapper.SocialUserMapper;
import com.apitable.enterprise.social.service.ISocialUserService;
import com.apitable.organization.entity.MemberEntity;
import com.apitable.organization.mapper.MemberMapper;
import com.apitable.core.util.ExceptionUtil;
import com.vikadata.social.dingtalk.model.DingTalkUserDetail;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Third party platform integration - user service interface implementation
 */
@Service
@Slf4j
public class SocialUserServiceImpl extends ServiceImpl<SocialUserMapper, SocialUserEntity>
    implements ISocialUserService {

    @Resource
    private SocialUserMapper socialUserMapper;

    @Resource
    private MemberMapper memberMapper;

    @Override
    public void createBatch(List<SocialUserEntity> entities) {
        if (CollUtil.isEmpty(entities)) {
            return;
        }
        socialUserMapper.insertBatch(entities);
    }

    @Override
    public void create(String unionId, SocialPlatformType platformType) {
        SocialUserEntity socialUser = new SocialUserEntity();
        socialUser.setUnionId(unionId);
        socialUser.setPlatform(platformType.getValue());
        boolean saveFlag = save(socialUser);
        ExceptionUtil.isTrue(saveFlag, DatabaseException.INSERT_ERROR);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatchByUnionId(List<String> unionIds) {
        if (CollUtil.isEmpty(unionIds)) {
            return;
        }
        socialUserMapper.deleteByUnionIds(unionIds);
    }

    @Override
    public void dingTalkActiveMember(Long userId, String spaceId, DingTalkUserDetail userDetail) {
        MemberEntity member =
            memberMapper.selectBySpaceIdAndOpenId(spaceId, userDetail.getUserid());
        // Activate User
        if (member != null) {
            // Correct member information
            if (StrUtil.isBlank(member.getEmail())) {
                member.setEmail(userDetail.getEmail());
            }
            if (StrUtil.isBlank(member.getMobile())) {
                member.setMobile(userDetail.getMobile());
            }
            if (StrUtil.isBlank(member.getPosition())) {
                member.setPosition(userDetail.getTitle());
            }
            member.setIsActive(true);
            member.setUserId(userId);
            member.setOpenId(userDetail.getUserid());
            memberMapper.updateById(member);
        }
    }
}
