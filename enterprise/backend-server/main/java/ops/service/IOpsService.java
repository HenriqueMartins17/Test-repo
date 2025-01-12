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

package com.apitable.enterprise.ops.service;

import com.apitable.enterprise.ops.ro.OpsWizardRo;

/**
 * <p>
 * Product Operation System Service.
 * </p>
 */
public interface IOpsService {

    /**
     * authorize for ops operation.
     *
     * @param token auth token
     * @author Chambers
     */
    void auth(String token);

    /**
     * mark template asset.
     *
     * @param templateId    template custom ID
     * @param isReversed    whether it is a reverse operation, that is, cancel the flag
     */
    void markTemplateAsset(String templateId, Boolean isReversed);

    /**
     * save or update wizard configuration.
     *
     * @param userId User ID
     * @param config Configuration parameters
     */
    void saveOrUpdateWizard(Long userId, OpsWizardRo config);
}
