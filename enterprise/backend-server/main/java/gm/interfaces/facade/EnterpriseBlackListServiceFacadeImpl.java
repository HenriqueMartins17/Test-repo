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

package com.apitable.enterprise.gm.interfaces.facade;

import com.apitable.enterprise.gm.service.IBlackListService;
import com.apitable.interfaces.security.facade.BlackListServiceFacade;

public class EnterpriseBlackListServiceFacadeImpl implements BlackListServiceFacade {

    private final IBlackListService iBlackListService;

    public EnterpriseBlackListServiceFacadeImpl(IBlackListService iBlackListService) {
        this.iBlackListService = iBlackListService;
    }

    @Override
    public void checkSpace(String spaceId) {
        iBlackListService.checkBlackSpace(spaceId);
    }

    @Override
    public void checkUser(Long userId) {
        iBlackListService.checkBlackUser(userId);
    }
}
