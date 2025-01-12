/*
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License
 *  and conditional on having a fully paid-up license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory
 * and its subdirectories does not constitute permission to use this code
 * or APITable Enterprise Edition features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

package com.apitable.enterprise.gm.interfaces.facade;

import com.apitable.enterprise.gm.enums.GmAction;
import com.apitable.enterprise.gm.service.IGmService;
import com.apitable.interfaces.security.facade.WhiteListServiceFacade;

public class EnterpriseWhiteListServiceFacadeImpl implements WhiteListServiceFacade {

    private final IGmService iGmService;

    public EnterpriseWhiteListServiceFacadeImpl(IGmService iGmService) {
        this.iGmService = iGmService;
    }

    @Override
    public void checkWidgetPermission(Long userId) {
        iGmService.validPermission(userId, GmAction.WIDGET_MANAGE);
    }
}
