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

package com.apitable.enterprise.automation.service;

import com.apitable.core.support.ResponseData;
import com.apitable.enterprise.automation.model.AutomationApiTriggerCreateRo;
import com.apitable.enterprise.automation.model.AutomationTriggerCreateVo;

public interface IAutomationOpenService {

    /**
     * Create or update robot info.
     *
     * @param data          trigger info
     * @param xServiceToken service token
     * @return automation trigger
     */
    ResponseData<AutomationTriggerCreateVo> upsert(AutomationApiTriggerCreateRo data,
        String xServiceToken);

}
