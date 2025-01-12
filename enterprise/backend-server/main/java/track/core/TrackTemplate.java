/*
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up
 * license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its
 * subdirectories does not constitute permission to use this code or APITable Enterprise Edition
 * features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

package com.apitable.enterprise.track.core;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import com.apitable.enterprise.shared.util.HttpUtil;
import com.apitable.enterprise.track.enums.TrackEventType;
import com.apitable.shared.util.information.ClientOriginInfo;
import com.apitable.user.mapper.UserMapper;
import java.util.HashMap;
import java.util.Map;
import jakarta.annotation.Resource;
import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Track Template.
 * </p>
 *
 * @author Chambers
 */
@Slf4j
@Component
public class TrackTemplate {

    @Resource
    private UserMapper userMapper;

    @Autowired(required = false)
    private DataTracker dataTracker;

    public void track(Long userId, TrackEventType type, String scene, ClientOriginInfo originInfo) {
        if (dataTracker == null) {
            log.info("Data tracking is not turned on.");
            return;
        }
        String distinctId = ObjectUtil.isNull(userId) ? null
            : userMapper.selectUuidById(userId);
        Map<String, Object> properties = MapUtil.newHashMap(2);
        Cookie[] cookies = null;
        String desktop = null;
        if (originInfo != null) {
            cookies = originInfo.getCookies();
            desktop = HttpUtil.getVikaDesktop(originInfo.getUserAgent(), false);
        }
        properties.put("desktop", desktop);
        switch (type) {
            case REGISTER:
                dataTracker.identify(distinctId, cookies);
                properties.put("registeredMethod", scene);
                // Automatic login after registration, need to record login events
                HashMap<String, Object> map = MapUtil.newHashMap(2);
                map.put("loginMethod", scene);
                map.put("desktop", desktop);
                dataTracker.track(distinctId, TrackEventType.LOGIN.getEventName(), map, cookies);
                break;
            case LOGIN:
                dataTracker.trackSignUp(distinctId, cookies);
                properties.put("loginMethod", scene);
                break;
            default:
                break;
        }
        dataTracker.track(distinctId, type.getEventName(), properties, cookies);
    }

    public void track(Long userId, TrackEventType eventType,
                      Map<String, Object> properties, ClientOriginInfo originInfo) {
        if (dataTracker == null) {
            log.info("Data tracking is not turned on.");
            return;
        }
        String distinctId = ObjectUtil.isNull(userId) ? null
            : userMapper.selectUuidById(userId);
        if (originInfo != null) {
            String desktop =
                HttpUtil.getVikaDesktop(originInfo.getUserAgent(), false);
            properties.put("desktop", desktop);
        }
        Cookie[] cookies = originInfo == null ? null : originInfo.getCookies();
        dataTracker.track(distinctId, eventType.getEventName(), properties, cookies);
    }

}
