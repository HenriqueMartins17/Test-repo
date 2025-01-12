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

package com.apitable.enterprise;

import com.apitable.enterprise.appstore.enums.AppType;
import com.apitable.enterprise.social.entity.SocialTenantBindEntity;
import com.apitable.enterprise.social.entity.SocialTenantEntity;
import com.apitable.enterprise.social.enums.SocialAppType;
import com.apitable.enterprise.social.enums.SocialPlatformType;
import com.apitable.enterprise.social.enums.SocialTenantAuthMode;
import com.apitable.enterprise.social.service.ISocialCpIsvPermitService;
import com.apitable.enterprise.social.service.ISocialCpIsvService;
import com.apitable.enterprise.social.service.ISocialWecomPermitOrderAccountService;
import com.apitable.enterprise.social.service.ISocialWecomPermitOrderService;
import com.apitable.enterprise.vikabilling.service.ISocialWecomOrderService;
import com.apitable.mock.bean.MockUserSpace;
import com.apitable.space.entity.SpaceEntity;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>
 * Unit test for wecom isv
 * </p>
 *
 * @author Codeman
 */
public abstract class AbstractIsvTest extends AbstractVikaSaasIntegrationTest {

    protected static final SocialAppType ISV = SocialAppType.ISV;

    @Autowired
    protected ISocialWecomOrderService iSocialWecomOrderService;

    @Autowired
    protected ISocialCpIsvService iSocialCpIsvService;

    @Autowired
    protected ISocialCpIsvPermitService realSocialCpIsvPermitService;

    @Autowired
    protected ISocialWecomPermitOrderService realSocialWecomPermitOrderService;

    @Autowired
    protected ISocialWecomPermitOrderAccountService realSocialWecomPermitOrderAccountService;

    protected MockUserSpace prepareSocialBindInfo(String tenantId, String appId,
                                                  SocialPlatformType platformType,
                                                  SocialAppType appType) {
        MockUserSpace userSpace = createSingleUserAndSpace();
        SocialTenantBindEntity entity =
            SocialTenantBindEntity.builder()
                .id(IdWorker.getId())
                .tenantId(tenantId)
                .appId(appId)
                .spaceId(userSpace.getSpaceId()).build();
        iSocialTenantBindService.save(entity);
        iSocialTenantService.save(SocialTenantEntity.builder()
            .id(IdWorker.getId())
            .tenantId(tenantId)
            .appId(appId)
            .platform(platformType.getValue())
            .appType(appType.getType()).build());
        return userSpace;
    }

    /**
     * Create wecom isv tenant without any subscriptions
     *
     * @param suiteId    Wecom isv suite ID
     * @param authCorpId Paid corporation ID
     * @return Related space ID
     * @author Codeman
     */
    protected String createWecomIsvTenant(String suiteId, String authCorpId) {
        String authCorpName = "test_corp";
        SocialTenantEntity tenantEntity = SocialTenantEntity.builder()
            .appId(suiteId)
            .appType(SocialAppType.ISV.getType())
            .tenantId(authCorpId)
            .contactAuthScope("{}")
            .authMode(SocialTenantAuthMode.ADMIN.getValue())
            .permanentCode("")
            .authInfo("{}")
            .platform(SocialPlatformType.WECOM.getValue())
            .status(true)
            .build();
        iSocialTenantService.createOrUpdateByTenantAndApp(tenantEntity);
        SpaceEntity spaceEntity = iSocialCpIsvService.createWeComIsvSpaceWithoutUser(
            String.format("%s'Space", authCorpName));
        String spaceId = spaceEntity.getSpaceId();
        iSocialTenantBindService.addTenantBind(tenantEntity.getAppId(), tenantEntity.getTenantId(),
            spaceId);
        iAppInstanceService.createInstanceByAppType(spaceId, AppType.WECOM_STORE.name());
        return spaceId;
    }

}
