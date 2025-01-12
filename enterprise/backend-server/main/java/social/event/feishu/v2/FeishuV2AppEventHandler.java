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

package com.apitable.enterprise.social.event.feishu.v2;

import jakarta.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import com.apitable.enterprise.social.service.IFeishuEventService;
import com.apitable.enterprise.social.service.ISocialFeishuEventLogService;
import com.apitable.enterprise.social.autoconfigure.feishu.annotation.FeishuEventHandler;
import com.apitable.enterprise.social.autoconfigure.feishu.annotation.FeishuEventListener;
import com.vikadata.social.feishu.event.app.AppOpenEvent;
import com.vikadata.social.feishu.event.app.AppStatusChangeEvent;
import com.vikadata.social.feishu.event.app.AppUninstalledEvent;
import com.vikadata.social.feishu.event.app.OrderPaidEvent;

import static com.vikadata.social.feishu.event.app.AppStatusChangeEvent.STATUS_START_BY_TENANT;
import static com.vikadata.social.feishu.event.app.AppStatusChangeEvent.STATUS_STOP_BY_PLATFORM;
import static com.vikadata.social.feishu.event.app.AppStatusChangeEvent.STATUS_STOP_BY_TENANT;

/**
 * Lark
 * Event subscription - application event
 */
@FeishuEventHandler
@Slf4j
public class FeishuV2AppEventHandler {

    @Resource
    private IFeishuEventService iFeishuEventService;

    @Resource
    private ISocialFeishuEventLogService iSocialFeishuEventLogService;

    /**
     * Event processing for the first time opening application
     *
     * @param event Event content
     * @return Response content
     */
    @FeishuEventListener
    public Object onAppOpenEvent(AppOpenEvent event) {
        // Tenant launches application for the first time
        log.info("[Lark] First time application, tenant:[{}]", event.getTenantKey());
        iSocialFeishuEventLogService.create(event);
        iFeishuEventService.handleAppOpenEvent(event);
        return "";
    }

    /**
     * Application Stop Enable Event Processing
     *
     * @param event Event content
     * @return Response content
     */
    @FeishuEventListener
    public Object onAppStatusChangeEvent(AppStatusChangeEvent event) {
        // Record Tenant
        log.info("[Lark] Enterprise Tenant:[{}], Application[{}], status change: {}",
            event.getTenantKey(), event.getAppId(), event.getStatus());
        iSocialFeishuEventLogService.create(event);
        switch (event.getStatus()) {
            case STATUS_STOP_BY_TENANT:
                log.info("[Lark] Tenant active deactivation");
                iFeishuEventService.handleAppStopEvent(event);
                break;
            case STATUS_START_BY_TENANT:
                log.info("[Lark] Enable after tenant deactivation");
                iFeishuEventService.handleAppRestartEvent(event);
                break;
            case STATUS_STOP_BY_PLATFORM:
                log.info("[Lark] Platform Deactivation");
                break;
            default:
                log.error("Unknown app status event notification");
                break;
        }
        return "";
    }

    /**
     * Application uninstallation event handling
     *
     * @param event Event content
     * @return Response content
     */
    @FeishuEventListener
    public Object onAppUninstalledEvent(AppUninstalledEvent event) {
        log.info("[Lark] Tenant uninstalls apps, Tenant: [{}]", event.getTenantKey());
        iSocialFeishuEventLogService.create(event);
        iFeishuEventService.handleAppUninstalledEvent(event);
        return "";
    }

    /**
     * Store app purchase event processing
     *
     * @param event Event content
     * @return Response content
     */
    @FeishuEventListener
    public Object onAppOrderPaidEvent(OrderPaidEvent event) {
        log.info("[Lark] Tenant store app purchase, tenant: [{}]", event.getTenantKey());
        iSocialFeishuEventLogService.create(event);
        iFeishuEventService.handleOrderPaidEvent(event);
        return "";
    }
}
