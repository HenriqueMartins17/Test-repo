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

package com.apitable.enterprise.vcode.service;

import com.apitable.enterprise.vcode.dto.VCodeDTO;

/**
 * <p>
 * VCode Usage Service
 * </p>
 */
public interface IVCodeUsageService {

    /**
     * Create usage record
     */
    void createUsageRecord(Long operator, String name, Integer type, String code);

    /**
     * Get the user ID of the inviter
     */
    VCodeDTO getInvitorUserId(Long userId);
}
