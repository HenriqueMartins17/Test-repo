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

package com.apitable.enterprise.vikabilling.autoconfigure;

import java.net.URL;
import java.nio.charset.StandardCharsets;

import cn.hutool.core.io.IoUtil;
import com.pingplusplus.Pingpp;

import com.apitable.enterprise.vikabilling.autoconfigure.properties.PingProperties;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;

/**
 * ping plus plus initial
 * @author Shawn Deng
 */
public class PingInit implements InitializingBean {

    private final PingProperties pingProperties;

    public PingInit(PingProperties pingProperties) {
        this.pingProperties = pingProperties;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Pingpp.apiKey = pingProperties.getApiKey();
        Pingpp.appId = pingProperties.getAppId();
        URL url = ClassPathResource.class.getClassLoader().getResource(pingProperties.getPrivateKeyPath());
        if (url == null) {
            throw new IllegalStateException("private key file not found");
        }
        Pingpp.privateKey = IoUtil.read(url.openStream(), StandardCharsets.UTF_8);
    }
}
