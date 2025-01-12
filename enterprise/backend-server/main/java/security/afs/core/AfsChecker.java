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

package com.apitable.enterprise.security.afs.core;

/**
 * <p>
 * Man machine verification service interface.
 * </p>
 *
 * @author Chambers
 */
public interface AfsChecker {

    /**
     * Alibaba Cloud Shield Traceless Verification
     *
     * @param data         The front end obtains the value of the getNVCVal function
     * @param scoreJsonStr Mapping between "Back end call risk control return result" and "Client
     * execution operation"
     * @return Risk control return result
     */
    String noTraceCheck(String data, String scoreJsonStr);

}
