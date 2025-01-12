/*
 * APITable <https://github.com/apitable/apitable>
 * Copyright (C) 2022 APITable Ltd. <https://apitable.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.vikadata.social.service.dingtalk.autoconfigure;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vikadata.social.core.ServletUtil;
import com.vikadata.social.core.StringUtil;
import com.vikadata.social.dingtalk.DingTalkServiceProvider;
import com.vikadata.social.dingtalk.Jackson4DingTalkConverter;

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

    private static final long serialVersionUID = 6321762161018702174L;

    private ApplicationContext applicationContext;

    /**
     * dingtalk event post request
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        DingTalkProperties properties = applicationContext.getBean(DingTalkProperties.class);
        String path = parsePath(request);
        String[] paths = path.split("/");
        if (paths.length != 3 && !paths[0].equals(properties.getBasePath())) {
            LOGGER.error("The request address does not match the dingtalk request address:{}", properties.getBasePath());
            return;
        }
        String subscribeId = paths[paths.length - 1];
        String eventPath = paths[1];
        LOGGER.info("Dingtalk event path:{}/{}", eventPath, subscribeId);
        DingTalkServiceProvider provider = applicationContext.getBean(DingTalkServiceProvider.class);
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
        String requestData = ServletUtil.getRequestBody(request);
        Map<String, String> json;
        try {
            json = Jackson4DingTalkConverter.toObject(requestData, new TypeReference<Map<String, String>>() {});
        }
        catch (IOException e) {
            LOGGER.error("dingtalk init fail:{}", path);
            // don't worry this error
            throw new RuntimeException(e);
        }
        String encryptMsg = json.get("encrypt");
        String responseData = "";
        if (eventPath.equals(properties.getEventPath())) {
            // event subscribe
            responseData = provider.eventNotify(subscribeId, msgSignature, timestamp, nonce, encryptMsg);
        }
        else if (eventPath.equals(properties.getSyncEventPath())) {
            // sync event subscribe
            responseData = provider.syncHttpEventNotifyForIsv(subscribeId, msgSignature, timestamp, nonce, encryptMsg);
        }
        else {
            LOGGER.error("illegal event path:{}", path);
        }

        // response
        ServletUtil.toResponseData(response, responseData);
    }

    private String parsePath(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String ContentPath = request.getContextPath();
        return StringUtil.trimSlash(requestUri.substring(ContentPath.length()));
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
