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

import java.util.LinkedHashMap;
import java.util.Set;

import com.apitable.enterprise.social.model.DingTalkContactDTO;

public interface IDingtalkInternalEventService {

    /**
     * DingTalk application binding space
     *
     * @param agentId  Applied agentId
     * @param spaceId    Space ID
     * @param operatorUserId The operation user ID of the bound space
     * @param contactMap Visible range of address book
     * @return DingTalk user ID successfully bound
     */
    Set<String> dingTalkAppBindSpace(String agentId, String spaceId, Long operatorUserId, LinkedHashMap<Long,
            DingTalkContactDTO> contactMap);

    /**
     * DingTalk application binding space
     *
     * @param agentId  Applied agent id
     * @param spaceId    Space ID
     * @param operatorOpenId Platform ID of the operating user of the bound space
     * @param contactMap contact map
     * @return DingTalk user ID successfully bound
     */
    Set<String> dingTalkRefreshContact(String spaceId, String agentId, String operatorOpenId, LinkedHashMap<Long,
            DingTalkContactDTO> contactMap);
}
