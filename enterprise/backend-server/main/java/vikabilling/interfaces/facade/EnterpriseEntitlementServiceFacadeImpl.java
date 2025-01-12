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

package com.apitable.enterprise.vikabilling.interfaces.facade;

import com.apitable.enterprise.vikabilling.service.ISpaceSubscriptionService;
import com.apitable.interfaces.billing.facade.EntitlementServiceFacade;
import com.apitable.interfaces.billing.model.EntitlementRemark;
import com.apitable.interfaces.billing.model.SubscriptionFeature;
import com.apitable.interfaces.billing.model.SubscriptionInfo;
import java.util.List;
import java.util.Map;

/**
 * entitlement service facade for enterprise edition.
 */
public class EnterpriseEntitlementServiceFacadeImpl implements EntitlementServiceFacade {

    private final ISpaceSubscriptionService iSpaceSubscriptionService;

    public EnterpriseEntitlementServiceFacadeImpl(
        ISpaceSubscriptionService spaceSubscriptionService) {
        this.iSpaceSubscriptionService = spaceSubscriptionService;
    }

    @Override
    public SubscriptionInfo getSpaceSubscription(String spaceId) {
        return iSpaceSubscriptionService.getPlanInfoBySpaceId(spaceId);
    }

    @Override
    public Map<String, SubscriptionFeature> getSpaceSubscriptions(List<String> spaceIds) {
        return iSpaceSubscriptionService.getSubscriptionFeatureBySpaceIds(spaceIds);
    }

    @Override
    public void rewardGiftCapacity(String spaceId, EntitlementRemark remark) {
        iSpaceSubscriptionService.createAddOnWithGiftCapacity(remark.getUserId(),
            remark.getUserName(), spaceId);
    }
}
