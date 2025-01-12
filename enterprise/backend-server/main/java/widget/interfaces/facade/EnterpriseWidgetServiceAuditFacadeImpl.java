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

package com.apitable.enterprise.widget.interfaces.facade;

import java.util.List;

import com.apitable.enterprise.widget.service.IWidgetAuditService;
import com.apitable.interfaces.widget.facade.WidgetServiceAuditFacade;
import com.apitable.widget.ro.WidgetStoreListRo;
import com.apitable.widget.vo.WidgetStoreListInfo;

public class EnterpriseWidgetServiceAuditFacadeImpl implements WidgetServiceAuditFacade {

    private final IWidgetAuditService iWidgetAuditService;

    public EnterpriseWidgetServiceAuditFacadeImpl(IWidgetAuditService iWidgetAuditService) {
        this.iWidgetAuditService = iWidgetAuditService;
    }

    @Override
    public List<WidgetStoreListInfo> getWaitReviewWidgetList(WidgetStoreListRo body) {
        return iWidgetAuditService.waitReviewWidgetList(body);
    }
}
