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

package com.apitable.enterprise.social.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;

import com.apitable.enterprise.social.entity.SocialUserEntity;
import com.apitable.enterprise.social.enums.SocialPlatformType;
import com.vikadata.social.dingtalk.model.DingTalkUserDetail;

/**
 * Third party platform integration - user service interface
 */
public interface ISocialUserService extends IService<SocialUserEntity> {

    /**
     * Bulk Insert
     *
     * @param entities Entity List
     */
    void createBatch(List<SocialUserEntity> entities);

    /**
     * Record third-party platform users
     *
     * @param unionId      Third party platform user ID
     * @param platformType Third party platform type
     */
    void create(String unionId, SocialPlatformType platformType);

    /**
     * Batch delete
     *
     * @param unionIds Third party platform user ID
     */
    void deleteBatchByUnionId(List<String> unionIds);

    /**
     * Member Activation
     *
     * @param userId User ID
     * @param spaceId Space ID
     * @param userDetail Third party user details
     */
    void dingTalkActiveMember(Long userId, String spaceId, DingTalkUserDetail userDetail);
}
