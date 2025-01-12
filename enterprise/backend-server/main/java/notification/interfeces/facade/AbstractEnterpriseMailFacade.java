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

package com.apitable.enterprise.notification.interfeces.facade;

import cn.hutool.core.util.StrUtil;
import com.apitable.interfaces.notification.facade.AbstractMailFacade;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.springframework.core.io.ClassPathResource;

/**
 * abstract class for mail facade.
 */
public abstract class AbstractEnterpriseMailFacade extends AbstractMailFacade {

    public Properties getProperties(String resourcePathPrefix, String locale,
        String defaultLocale) throws IOException {
        final Properties properties = new Properties();
        String path = StrUtil.format("{}/{}/subject.properties",
            resourcePathPrefix, locale);
        ClassPathResource resource = new ClassPathResource(path);
        if (resource.exists()) {
            try (InputStream in = resource.getInputStream()) {
                properties.load(in);
            }
            return properties;
        }

        String defaultPath = StrUtil.format("{}/{}/subject.properties",
            resourcePathPrefix, defaultLocale);
        ClassPathResource defaultResource = new ClassPathResource(defaultPath);
        try (InputStream in = defaultResource.getInputStream()) {
            properties.load(in);
        }
        return properties;
    }

    public String loadTemplateResourcePath(String resourcePathPrefix,
        String templateName, String locale, String defaultLocale) {
        String templatePath = StrUtil.format("{}/{}/{}",
            resourcePathPrefix, locale, templateName);
        ClassPathResource resource = new ClassPathResource(templatePath);
        if (resource.exists()) {
            // load locale priority
            return templatePath.substring(templatePath.indexOf("/") + 1);
        }

        String defaultPath = StrUtil.format("{}/{}/{}",
            resourcePathPrefix, defaultLocale, templateName);
        ClassPathResource defaultResource = new ClassPathResource(defaultPath);
        if (defaultResource.exists()) {
            return defaultPath.substring(defaultPath.indexOf("/") + 1);
        }

        return super.loadTemplateResourcePath(locale, templateName);
    }
}
