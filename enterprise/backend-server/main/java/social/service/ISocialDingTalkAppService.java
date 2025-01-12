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

import com.apitable.enterprise.ops.ro.SyncSocialDingTalkAppRo;
import com.apitable.enterprise.social.entity.SocialDingtalkAppEntity;


public interface ISocialDingTalkAppService extends IService<SocialDingtalkAppEntity> {

        void syncAppConfigInStorage(List<SyncSocialDingTalkAppRo> roList);

    /**
     * Bulk Insert
     *
     * @param dataList data
     */
    void createOrUpdateBatch(Long userId, List<SyncSocialDingTalkAppRo> dataList);
}
