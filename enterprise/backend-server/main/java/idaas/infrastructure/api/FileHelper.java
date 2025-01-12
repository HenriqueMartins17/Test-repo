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

package com.apitable.enterprise.idaas.infrastructure.api;

import java.io.InputStream;

/**
 * Test file tool class
 */
public class FileHelper {

    /**
     * Get ClassPath resource file stream
     *
     * @param resourcePath Relative path under ClassPath
     * @return InputStream
     */
    public static InputStream getInputStreamFromResource(String resourcePath) {
        ClassLoader classLoader = FileHelper.class.getClassLoader();
        return classLoader.getResourceAsStream(resourcePath);
    }
}
