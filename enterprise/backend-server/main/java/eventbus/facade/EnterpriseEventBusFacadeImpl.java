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

package com.apitable.enterprise.eventbus.facade;

import com.apitable.auth.enums.LoginType;
import com.apitable.enterprise.integral.service.IIntegralService;
import com.apitable.enterprise.track.core.TrackTemplate;
import com.apitable.enterprise.track.enums.TrackEventType;
import com.apitable.interfaces.eventbus.facade.EventBusFacade;
import com.apitable.interfaces.eventbus.model.CaptchaEvent;
import com.apitable.interfaces.eventbus.model.EventBusEvent;
import com.apitable.interfaces.eventbus.model.TemplateSearchEvent;
import com.apitable.interfaces.eventbus.model.UserInfoChangeEvent;
import com.apitable.interfaces.eventbus.model.UserLoginEvent;
import com.apitable.interfaces.eventbus.model.WizardActionEvent;
import com.apitable.shared.component.TaskManager;
import com.apitable.shared.util.information.ClientOriginInfo;
import java.util.HashMap;
import java.util.Map;

public class EnterpriseEventBusFacadeImpl implements EventBusFacade {

    private final TrackTemplate trackTemplate;

    private final IIntegralService integralService;

    public EnterpriseEventBusFacadeImpl(TrackTemplate trackTemplate,
                                        IIntegralService integralService) {
        this.trackTemplate = trackTemplate;
        this.integralService = integralService;
    }

    @Override
    public void onEvent(EventBusEvent event) {
        switch (event.getEventType()) {
            case USER_LOGIN:
                UserLoginEvent userLoginEvent = (UserLoginEvent) event;
                Long userId = userLoginEvent.getUserId();
                ClientOriginInfo originInfo = userLoginEvent.getClientOriginInfo();
                if (userLoginEvent.getScene() != null) {
                    TrackEventType trackEventType = userLoginEvent.isRegister()
                        ? TrackEventType.REGISTER : TrackEventType.LOGIN;
                    TaskManager.me().execute(() -> trackTemplate.track(userId,
                        trackEventType, userLoginEvent.getScene(), originInfo));
                    break;
                }
                if (userLoginEvent.getLoginType() == LoginType.PASSWORD) {
                    TaskManager.me().execute(() -> trackTemplate.track(userId,
                        TrackEventType.LOGIN, "Password", originInfo));
                } else if (userLoginEvent.getLoginType() == LoginType.SMS_CODE) {
                    TrackEventType trackEventType = userLoginEvent.isRegister()
                        ? TrackEventType.REGISTER : TrackEventType.LOGIN;
                    TaskManager.me().execute(() -> trackTemplate.track(userId,
                        trackEventType, "Mobile verification code", originInfo));
                } else if (userLoginEvent.getLoginType() == LoginType.EMAIL_CODE) {
                    TrackEventType trackEventType = userLoginEvent.isRegister()
                        ? TrackEventType.REGISTER : TrackEventType.LOGIN;
                    TaskManager.me().execute(() -> trackTemplate.track(userId,
                        trackEventType, "Email verification code", originInfo));
                }
                break;
            case USER_INFO_CHANGE:
                UserInfoChangeEvent userInfoChangeEvent = (UserInfoChangeEvent) event;
                TaskManager.me().execute(() -> trackTemplate.track(userInfoChangeEvent.getUserId(),
                    TrackEventType.SET_NICKNAME, "", userInfoChangeEvent.getClientOriginInfo()));
                break;
            case USER_WIZARD_CHANGE:
                WizardActionEvent wizardActionEvent = (WizardActionEvent) event;
                integralService.rewardWizard(wizardActionEvent.getUserId(), wizardActionEvent.getWizardId());
                break;
            case CAPTCHA_GET:
                CaptchaEvent captchaEvent = (CaptchaEvent) event;
                TaskManager.me().execute(() -> trackTemplate.track(null,
                    TrackEventType.GET_SMC_CODE, "", captchaEvent.getClientOriginInfo()));
                break;
            case TEMPLATE_SEARCH:
                TemplateSearchEvent templateSearchEvent = (TemplateSearchEvent) event;
                TaskManager.me().execute(() -> {
                    Map<String, Object> properties = new HashMap<>(3);
                    properties.put("keyword", templateSearchEvent.getSearchWord());
                    properties.put("albumNames", templateSearchEvent.getAlbumNames());
                    properties.put("templateName", templateSearchEvent.getTemplateName());
                    properties.put("tagName", templateSearchEvent.getTagName());
                    trackTemplate.track(templateSearchEvent.getUserId(),
                        TrackEventType.SEARCH_TEMPLATE, properties,
                        templateSearchEvent.getClientOriginInfo());
                });
                break;
            default:
                break;
        }
    }
}
