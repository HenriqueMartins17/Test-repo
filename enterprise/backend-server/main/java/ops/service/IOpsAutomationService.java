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

import com.apitable.enterprise.ops.ro.AutomationActionTypeCreateRO;
import com.apitable.enterprise.ops.ro.AutomationActionTypeEditRO;
import com.apitable.enterprise.ops.ro.AutomationServiceCreateRO;
import com.apitable.enterprise.ops.ro.AutomationServiceEditRO;
import com.apitable.enterprise.ops.ro.AutomationTriggerTypeCreateRO;
import com.apitable.enterprise.ops.ro.AutomationTriggerTypeEditRO;

/**
 * <p>
 * Product Operation System - Automation Service.
 * </p>
 */
public interface IOpsAutomationService {

    String createService(Long userId, AutomationServiceCreateRO ro);

    void editService(Long userId, String serviceId, AutomationServiceEditRO ro);

    void deleteService(Long userId, String serviceId);

    String createTriggerType(Long userId, AutomationTriggerTypeCreateRO ro);

    void editTriggerType(Long userId, String triggerTypeId, AutomationTriggerTypeEditRO ro);

    void deleteTriggerType(Long userId, String triggerTypeId);

    String createActionType(Long userId, AutomationActionTypeCreateRO ro);

    void editActionType(Long userId, String actionTypeId, AutomationActionTypeEditRO ro);

    void deleteActionType(Long userId, String actionTypeId);

}
