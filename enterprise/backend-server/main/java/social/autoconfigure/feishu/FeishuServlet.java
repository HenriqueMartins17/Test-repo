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

package com.apitable.enterprise.social.autoconfigure.feishu;

import com.apitable.enterprise.social.util.HttpServletUtil;
import com.vikadata.social.core.StringUtil;
import com.vikadata.social.feishu.FeishuServiceProvider;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * feishu servlet
 *
 * @author Shawn Deng
 */
public class FeishuServlet extends HttpServlet implements ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeishuServlet.class);

    private ApplicationContext applicationContext;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        FeishuProperties properties = applicationContext.getBean(FeishuProperties.class);
        String path = parsePath(request);
        String[] paths = path.split("/");
        if (paths.length != 2 && !paths[0].equals(properties.getBasePath())) {
            LOGGER.error("The request address does not match the request address :{}",
                properties.getBasePath());
            return;
        }
        String eventType = paths[1];
        FeishuServiceProvider provider = applicationContext.getBean(FeishuServiceProvider.class);
        String requestData = HttpServletUtil.getRequestBody(request);
        LOGGER.info("event type：{}", eventType);
        String responseData = null;
        if (eventType.equals(properties.getEventPath())) {
            // event subscribe
            responseData = provider.wrapperEventNotify(requestData);
        } else if (eventType.equals(properties.getCardEventPath())) {
            // card event subscribe
            responseData = provider.cardNotify(requestData);
        } else {
            LOGGER.error("Illegal event type: {}", eventType);
        }
        HttpServletUtil.toResponseData(response, responseData);
    }

    private String parsePath(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String basePath = request.getServletPath();
        return StringUtil.trimSlash(requestUri.substring(basePath.length()));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
