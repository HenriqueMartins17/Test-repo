package com.apitable.enterprise.apitablebilling.appsumo.handler;

import static com.apitable.core.constants.ResponseExceptionConstants.DEFAULT_SUCCESS_MESSAGE;

import com.apitable.core.exception.BusinessException;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.enterprise.apitablebilling.appsumo.annotation.AppsumoEventHandler;
import com.apitable.enterprise.apitablebilling.appsumo.config.AppsumoLicenseObject;
import com.apitable.enterprise.apitablebilling.appsumo.enums.AppsumoAction;
import com.apitable.enterprise.apitablebilling.appsumo.enums.AppsumoException;
import com.apitable.enterprise.apitablebilling.appsumo.enums.AppsumoHandleStatus;
import com.apitable.enterprise.apitablebilling.appsumo.model.AppsumoEventDTO;
import com.apitable.enterprise.apitablebilling.appsumo.model.EventVO;
import com.apitable.enterprise.apitablebilling.appsumo.service.IAppsumoEventLogService;
import com.apitable.enterprise.apitablebilling.appsumo.service.IAppsumoService;
import com.apitable.enterprise.apitablebilling.appsumo.util.AppsumoLicenseConfigUtil;
import com.apitable.enterprise.apitablebilling.core.Bundle;
import com.apitable.enterprise.apitablebilling.core.Subscription;
import com.apitable.enterprise.apitablebilling.entity.SubscriptionEntity;
import com.apitable.enterprise.apitablebilling.service.IBundleInApitableService;
import com.apitable.enterprise.apitablebilling.service.ISubscriptionInApitableService;
import com.apitable.space.service.ISpaceService;
import com.apitable.user.service.IUserService;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * appsumo event handler: enhance_tire(upgrade).
 */
@AppsumoEventHandler(action = AppsumoAction.ENHANCE_TIER)
@ConditionalOnProperty(value = "appsumo.enabled", havingValue = "true")
public class EnhanceTireEventHandler implements IAppsumoEventHandler {

    @Resource
    private IAppsumoEventLogService iAppsumoEventLogService;

    @Resource
    private ISubscriptionInApitableService iSubscriptionInApitableService;

    @Resource
    private IBundleInApitableService iBundleInApitableService;

    @Resource
    private ISpaceService iSpaceService;

    @Resource
    private IUserService iUserService;

    @Resource
    private IAppsumoService iAppsumoService;


    @Override
    public EventVO handle(Long eventLogId) {
        AppsumoEventDTO event = iAppsumoEventLogService.getSimpleInfoById(eventLogId);
        ExceptionUtil.isFalse(null == event, AppsumoException.EVENT_NOT_FOUND);
        if (AppsumoHandleStatus.isEventHandled(event.getHandleStatus())) {
            return EventVO.builder().message(DEFAULT_SUCCESS_MESSAGE).build();
        }
        String userEmail =
            iAppsumoEventLogService.getUserEmailByActivationEmailAndUuid(
                event.getActivationEmail(), event.getUuid());
        Long userId = iUserService.getUserIdByEmail(userEmail);
        if (null == userId) {
            EventVO.builder().message(DEFAULT_SUCCESS_MESSAGE).build();
        }
        List<String> spaceIds = iSpaceService.getSpaceIdsByCreatedBy(userId);
        if (spaceIds.isEmpty()) {
            return EventVO.builder().message(DEFAULT_SUCCESS_MESSAGE).build();
        }

        List<Bundle> bundles = iBundleInApitableService.getBundlesBySpaceIds(spaceIds);
        // allow user to enhance tire when not activated
        if (bundles.isEmpty()) {
            return EventVO.builder().message(DEFAULT_SUCCESS_MESSAGE).build();
        }
        List<String> bundleIds =
            bundles.stream().map(Bundle::getBundleId).collect(Collectors.toList());
        List<Subscription> subscriptions =
            iSubscriptionInApitableService.getSubscriptionsByBundleIds(bundleIds);
        ExceptionUtil.isFalse(subscriptions.isEmpty(),
            AppsumoException.APPSUMO_SUBSCRIPTION_NOT_FOUND);

        AppsumoLicenseObject license =
            AppsumoLicenseConfigUtil.findProductByPlanId(event.getPlanId())
                .orElseThrow(() -> new BusinessException(AppsumoException.LICENSE_NOT_FOUND));
        for (Subscription subscription : subscriptions) {
            // Only update AppSumo's subscription
            if (AppsumoLicenseConfigUtil.isAppsumoPlan(subscription.getPriceId())
                && iAppsumoService.isCurrentLicense(subscription.getMetadata(), event.getUuid())) {
                // Update subscription
                SubscriptionEntity updateSubscription = SubscriptionEntity.builder()
                    .priceId(license.getPlanId())
                    .productName(license.getProductId())
                    .build();
                iSubscriptionInApitableService.updateBySubscriptionId(
                    subscription.getSubscriptionId(),
                    updateSubscription);
            }
        }
        iAppsumoEventLogService.updateStatus(eventLogId, AppsumoHandleStatus.SUCCESS);
        return EventVO.builder().message(DEFAULT_SUCCESS_MESSAGE).build();
    }
}
