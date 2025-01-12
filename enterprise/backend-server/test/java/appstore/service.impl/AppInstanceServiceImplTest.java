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

package com.apitable.enterprise.appstore.service.impl;

import com.apitable.enterprise.AbstractVikaSaasIntegrationTest;
import com.apitable.enterprise.appstore.model.AppInstance;
import com.apitable.mock.bean.MockUserSpace;
import org.junit.jupiter.api.Test;

public class AppInstanceServiceImplTest extends AbstractVikaSaasIntegrationTest {

    @Test
    public void testAppInstanceStop() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        String appId = "app-80c8b19b1d8c4e40a1f4a9854b71ceb0";
        AppInstance appInstance = iAppInstanceService.createInstance(userSpace.getSpaceId(), appId);
        iAppInstanceService.deleteAppInstance(userSpace.getUserId(), appInstance.getAppInstanceId());
    }
}
