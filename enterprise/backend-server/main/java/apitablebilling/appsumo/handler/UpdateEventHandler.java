package com.apitable.enterprise.apitablebilling.appsumo.handler;

import static com.apitable.core.constants.ResponseExceptionConstants.DEFAULT_SUCCESS_MESSAGE;

import cn.hutool.json.JSONUtil;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.enterprise.apitablebilling.appsumo.annotation.AppsumoEventHandler;
import com.apitable.enterprise.apitablebilling.appsumo.enums.AppsumoAction;
import com.apitable.enterprise.apitablebilling.appsumo.enums.AppsumoException;
import com.apitable.enterprise.apitablebilling.appsumo.enums.AppsumoHandleStatus;
import com.apitable.enterprise.apitablebilling.appsumo.model.AppsumoEventDTO;
import com.apitable.enterprise.apitablebilling.appsumo.model.AppsumoSubscriptionMetadata;
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
 * appsumo event handler: update.
 */
@AppsumoEventHandler(action = AppsumoAction.UPDATE)
@ConditionalOnProperty(value = "appsumo.enabled", havingValue = "true")
public class UpdateEventHandler implements IAppsumoEventHandler {
    @Resource
    private IAppsumoEventLogService iAppsumoEventLogService;

    @Resource
    private ISubscriptionInApitableService iSubscriptionInApitableService;

    @Resource
    private IBundleInApitableService iBundleInApitableService;

    @Resource
    private IUserService iUserService;

    @Resource
    private ISpaceService iSpaceService;

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
            iAppsumoEventLogService.getUserEmailByActivationEmailAndUuid(event.getActivationEmail(),
                event.getUuid());
        Long userId = iUserService.getUserIdByEmail(userEmail);
        ExceptionUtil.isFalse(null == userId, AppsumoException.USER_EMAIL_NOT_FOUND);

        List<String> spaceIds = iSpaceService.getSpaceIdsByCreatedBy(userId);
        ExceptionUtil.isFalse(spaceIds.isEmpty(), AppsumoException.USER_EMAIL_NOT_BIND_SPACE);

        List<Bundle> bundles = iBundleInApitableService.getBundlesBySpaceIds(spaceIds);
        ExceptionUtil.isFalse(bundles.isEmpty(), AppsumoException.APPSUMO_BUNDLE_NOT_FOUND);

        List<String> bundleIds =
            bundles.stream().map(Bundle::getBundleId).collect(Collectors.toList());
        List<Subscription> subscriptions =
            iSubscriptionInApitableService.getSubscriptionsByBundleIds(bundleIds);
        ExceptionUtil.isFalse(subscriptions.isEmpty(),
            AppsumoException.APPSUMO_SUBSCRIPTION_NOT_FOUND);

        for (Subscription subscription : subscriptions) {
            if (AppsumoLicenseConfigUtil.isAppsumoPlan(subscription.getPriceId())
                && iAppsumoService.isCurrentLicense(subscription.getMetadata(), event.getUuid())) {
                AppsumoSubscriptionMetadata metadata = AppsumoSubscriptionMetadata.builder()
                    .invoiceItemUuid(event.getInvoiceItemUuid())
                    .uuid(event.getUuid())
                    .build();
                // Update subscription
                SubscriptionEntity updateSubscription = SubscriptionEntity.builder()
                    .metadata(JSONUtil.toJsonStr(metadata))
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
