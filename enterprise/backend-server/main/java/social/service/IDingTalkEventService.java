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

package com.apitable.enterprise.social.service;

/**
 * DingTalk Event Service Interface
 */
public interface IDingTalkEventService {
    /**
     * DingTalk User Status Change - Employee Activation
     *
     * @param agentId  DingTalk Apply enterprise identity
     * @param tenantKey Enterprise ID
     * @param userOpenId DingTalk Application user's unique identification of the application
     */
    void handleUserActiveOrg(String agentId, String tenantKey, String userOpenId);

    /**
     * DingTalk User Status Change - Employee Resignation
     *
     * @param agentId  DingTalk Apply enterprise identity
     * @param tenantKey Enterprise ID
     * @param userOpenId DingTalk Application user's unique identification of the application
     */
    void handUserLeaveOrg(String agentId, String tenantKey, String userOpenId);

    /**
     * User status change - address book user change
     *
     * @param agentId  DingTalk Application agent Id
     * @param tenantKey Enterprise ID
     * @param userOpenId DingTalk Application user's unique identification of the application
     */
    void handleUserModifyOrg(String agentId, String tenantKey, String userOpenId);

    /**
     * Processing the synchronous address book of the new department of DingTalk
     *
     * @param agentId  DingTalk Application agent Id
     * @param tenantKey Enterprise ID
     * @param openDepartmentId Applied department ID
     */
    void handleOrgDeptCreate(String agentId, String tenantKey, Long openDepartmentId);

    /**
     * Processing DingTalk modification department
     *
     * @param agentId  DingTalk Application agentId
     * @param tenantKey Enterprise ID
     * @param departmentId Applied department ID
     */
    void handleOrgDeptModify(String agentId, String tenantKey, Long departmentId);

    /**
     * Processing DingTalk to delete department
     *
     * @param agentId  DingTalk Application agentId
     * @param tenantKey Enterprise ID
     * @param departmentId Applied department ID
     */
    void handleOrgDeptRemove(String agentId, String tenantKey, Long departmentId);
}
