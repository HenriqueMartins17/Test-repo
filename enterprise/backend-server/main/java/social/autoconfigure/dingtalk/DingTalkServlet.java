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

package com.apitable.enterprise.social.autoconfigure.dingtalk;

import cn.hutool.core.util.StrUtil;
import com.apitable.enterprise.social.util.HttpServletUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.vikadata.social.core.StringUtil;
import com.vikadata.social.dingtalk.DingTalkServiceProvider;
import com.vikadata.social.dingtalk.Jackson4DingTalkConverter;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serial;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;

/**
 * dingtalk servlet
 *
 * @author Shawn Deng
 */
public class DingTalkServlet extends HttpServlet implements ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(DingTalkServlet.class);

    @Serial
    private static final long serialVersionUID = 6321762161018702174L;

    private ApplicationContext applicationContext;

    /**
     * dingtalk event post request
     *
     * @param request  servlet request
     * @param response servlet response
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        DingTalkProperties properties = applicationContext.getBean(DingTalkProperties.class);
        String path = parsePath(request);
        String[] paths = path.split("/");
        if (paths.length != 3 && !paths[0].equals(properties.getBasePath())) {
            LOGGER.error("The request address does not match the dingtalk request address:{}",
                properties.getBasePath());
            return;
        }
        String subscribeId = paths[paths.length - 1];
        String eventPath = paths[1];
        LOGGER.info("Dingtalk event path:{}/{}", eventPath, subscribeId);
        DingTalkServiceProvider provider =
            applicationContext.getBean(DingTalkServiceProvider.class);
        String msgSignature = request.getParameter("msg_signature");
        if (msgSignature == null) {
            msgSignature = request.getParameter("signature");
        }
        String timestamp = request.getParameter("timestamp");
        String nonce = request.getParameter("nonce");
        if (StrUtil.isBlank(msgSignature) || StrUtil.isBlank(timestamp) || StrUtil.isBlank(nonce)) {
            LOGGER.error("Lost parameter in request: [{}]", request.getParameterMap());
            return;
        }
        String requestData = HttpServletUtil.getRequestBody(request);
        Map<String, String> json;
        try {
            TypeReference<Map<String, String>> typeReference = new TypeReference<>() {
            };
            json = Jackson4DingTalkConverter.toObject(requestData, typeReference);
        } catch (IOException e) {
            LOGGER.error("dingtalk init fail:{}", path);
            // don't worry this error
            throw new RuntimeException(e);
        }
        String encryptMsg = json.get("encrypt");
        String responseData = "";
        if (eventPath.equals(properties.getEventPath())) {
            // event subscribe
            responseData =
                provider.eventNotify(subscribeId, msgSignature, timestamp, nonce, encryptMsg);
        } else if (eventPath.equals(properties.getSyncEventPath())) {
            // sync event subscribe
            responseData =
                provider.syncHttpEventNotifyForIsv(subscribeId, msgSignature, timestamp, nonce,
                    encryptMsg);
        } else {
            LOGGER.error("illegal event path:{}", path);
        }

        // response
        HttpServletUtil.toResponseData(response, responseData);
    }

    private String parsePath(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String ContentPath = request.getContextPath();
        return StringUtil.trimSlash(requestUri.substring(ContentPath.length()));
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext)
        throws BeansException {
        this.applicationContext = applicationContext;
    }
}
