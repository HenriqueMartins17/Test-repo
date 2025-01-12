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

package com.apitable.enterprise.social.strategey;

import java.util.Map;
import java.util.Optional;

import jakarta.annotation.Resource;

import cn.hutool.core.map.MapUtil;

import com.apitable.enterprise.social.enums.SocialPlatformType;
import com.apitable.core.exception.BusinessException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Create a simple social user factory
 * </p>
 */
@Component
public class CreateSocialUserSimpleFactory implements InitializingBean {

    private static Map<Integer, CreateSocialUserStrategey> CREATE_SOCIAL_USER_STRATEGY_MAP =
        MapUtil.newHashMap(2);

    @Resource
    private SocialFeishuUserStrategey socialFeishuUserStrategey;

    @Resource
    private SocialWeComUserStrategey socialWeComUserStrategey;

    @Resource
    private SocialDingTalkUserStrategey socialDingTalkUserStrategey;

    @Override
    public void afterPropertiesSet() throws Exception {
        CREATE_SOCIAL_USER_STRATEGY_MAP.put(SocialPlatformType.FEISHU.getValue(),
            socialFeishuUserStrategey);
        CREATE_SOCIAL_USER_STRATEGY_MAP.put(SocialPlatformType.WECOM.getValue(),
            socialWeComUserStrategey);
        CREATE_SOCIAL_USER_STRATEGY_MAP.put(SocialPlatformType.DINGTALK.getValue(),
            socialDingTalkUserStrategey);
    }

    /**
     * Get the processing policy according to the type
     *
     * @param socialPlatformType Type of third-party social software platform
     */
    public CreateSocialUserStrategey getStrategy(Integer socialPlatformType) {
        CreateSocialUserStrategey strategy =
            CREATE_SOCIAL_USER_STRATEGY_MAP.get(socialPlatformType);
        return Optional.ofNullable(strategy)
            .orElseThrow(() -> new BusinessException("Unknown third-party account type"));
    }

}
