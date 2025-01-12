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

package com.apitable.enterprise.apitablebilling.appsumo.autoconfigure;

import com.apitable.core.util.SpringContextHolder;
import com.apitable.enterprise.apitablebilling.appsumo.config.AppsumoLicenseConfigLoader;
import com.apitable.enterprise.apitablebilling.appsumo.enums.AppsumoAction;
import com.apitable.enterprise.apitablebilling.appsumo.handler.IAppsumoEventHandler;
import com.apitable.enterprise.apitablebilling.appsumo.model.EventVO;
import com.apitable.enterprise.apitablebilling.appsumo.util.AppsumoLicenseConfigUtil;
import java.util.HashMap;
import org.springframework.beans.factory.InitializingBean;

/**
 * appsumo event handler manager.
 */
public class AppsumoEventHandlerManager implements InitializingBean {
    private final HashMap<AppsumoAction, IAppsumoEventHandler> handlers = new HashMap<>();

    public static AppsumoEventHandlerManager me() {
        return SpringContextHolder.getBean(AppsumoEventHandlerManager.class);
    }

    /**
     * add handler.
     *
     * @param handler event handler
     */
    public void addHandler(AppsumoAction action, IAppsumoEventHandler handler) {
        if (!handlers.containsKey(action)) {
            handlers.put(action, handler);
        }
    }

    public EventVO handle(Long eventLogId, AppsumoAction action) {
        return handlers.get(action).handle(eventLogId);
    }


    /**
     * init appsumo product configuration.
     *
     * @throws Exception system exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        AppsumoLicenseConfigUtil.licenseConfig = AppsumoLicenseConfigLoader.getConfig();
    }
}
