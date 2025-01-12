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

package com.apitable.enterprise.security.service;

/**
 * <p>
 * Alibaba Cloud Shield Human-Machine Authentication Interface
 * </p>
 *
 * @author Chambers
 */
public interface AfsCheckService {

    boolean getEnabledStatus();

    /**
     * incognito verification
     *
     * @param data The front end gets the value of the get NVC Val function
     */
    void noTraceCheck(String data);
}
