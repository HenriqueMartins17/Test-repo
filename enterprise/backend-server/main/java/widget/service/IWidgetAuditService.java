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

package com.apitable.enterprise.widget.service;

import java.util.List;

import com.apitable.enterprise.widget.ro.WidgetAuditGlobalIdRo;
import com.apitable.enterprise.widget.ro.WidgetAuditSubmitDataRo;
import com.apitable.widget.ro.WidgetStoreListRo;
import com.apitable.widget.vo.WidgetStoreListInfo;

public interface IWidgetAuditService {

    /**
     * issue global id
     *
     * @param opUserId       opUserId
     * @param body  request parameters
     * @return boolean
     */
    String issuedGlobalId(Long opUserId, WidgetAuditGlobalIdRo body);

    /**
     * query the list of small programs to be reviewed
     *
     * @param body  request parameters
     * @return list to be reviewed
     *
     *
     */
    List<WidgetStoreListInfo> waitReviewWidgetList(WidgetStoreListRo body);

    /**
     * audit global widgets
     *
     * @param opUserId  operating user
     * @param body      request parameters
     */
    void auditSubmitData(Long opUserId, WidgetAuditSubmitDataRo body);

}
