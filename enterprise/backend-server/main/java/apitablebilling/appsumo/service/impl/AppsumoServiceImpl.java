package com.apitable.enterprise.apitablebilling.appsumo.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.apitable.core.exception.BusinessException;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.enterprise.apitablebilling.appsumo.autoconfigure.AppsumoEventHandlerManager;
import com.apitable.enterprise.apitablebilling.appsumo.config.AppsumoLicenseObject;
import com.apitable.enterprise.apitablebilling.appsumo.core.AppsumoTemplate;
import com.apitable.enterprise.apitablebilling.appsumo.enums.AppsumoAction;
import com.apitable.enterprise.apitablebilling.appsumo.enums.AppsumoException;
import com.apitable.enterprise.apitablebilling.appsumo.enums.AppsumoHandleStatus;
import com.apitable.enterprise.apitablebilling.appsumo.model.AppsumoEventDTO;
import com.apitable.enterprise.apitablebilling.appsumo.model.AppsumoSubscriptionMetadata;
import com.apitable.enterprise.apitablebilling.appsumo.model.EventVO;
import com.apitable.enterprise.apitablebilling.appsumo.service.IAppsumoEventLogService;
import com.apitable.enterprise.apitablebilling.appsumo.service.IAppsumoService;
import com.apitable.enterprise.apitablebilling.appsumo.util.AppsumoLicenseConfigUtil;
import com.apitable.enterprise.apitablebilling.entity.BundleEntity;
import com.apitable.enterprise.apitablebilling.entity.SubscriptionEntity;
import com.apitable.enterprise.apitablebilling.enums.BillingPeriod;
import com.apitable.enterprise.apitablebilling.enums.BundleState;
import com.apitable.enterprise.apitablebilling.enums.ProductEnum;
import com.apitable.enterprise.apitablebilling.enums.SubscriptionState;
import com.apitable.enterprise.apitablebilling.service.IBundleInApitableService;
import com.apitable.enterprise.apitablebilling.service.ISubscriptionInApitableService;
import com.apitable.enterprise.auth0.model.UserSpaceDTO;
import com.apitable.enterprise.auth0.service.Auth0Service;
import com.apitable.space.entity.SpaceEntity;
import com.apitable.space.enums.SpaceException;
import com.apitable.space.service.ISpaceService;
import com.apitable.user.enums.UserException;
import com.apitable.user.service.IUserService;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.mgmt.users.User;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Appsumo service implements.
 */
@Service
@Slf4j
public class AppsumoServiceImpl implements IAppsumoService {

    @Autowired(required = false)
    private AppsumoTemplate appsumoTemplate;

    @Resource
    private IBundleInApitableService iBundleInApitableService;

    @Resource
    private ISubscriptionInApitableService iSubscriptionInApitableService;

    @Resource
    private Auth0Service auth0Service;

    @Resource
    private IAppsumoEventLogService iAppsumoEventLogService;

    @Autowired(required = false)
    private AppsumoEventHandlerManager appsumoEventHandlerManager;

    @Resource
    private ISpaceService iSpaceService;

    @Resource
    private IUserService iUserService;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public String getAccessToken(String userName, String password) {
        validateUser(userName, password);
        if (null != appsumoTemplate) {
            return appsumoTemplate.generateToken();
        }
        return null;
    }

    @Override
    public boolean verifyAccessToken(String token) {
        if (null != appsumoTemplate) {
            return appsumoTemplate.verifyToken(StrUtil.removeAll(token, "Bearer "));
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EventVO handleEvent(Long eventLogId, AppsumoAction action) {
        return appsumoEventHandlerManager.handle(eventLogId, action);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createSubscription(Long userId, String spaceId, AppsumoEventDTO event) {
        LocalDateTime startAt = LocalDateTime.now();
        LocalDateTime endAt = startAt.plusYears(100);
        // Create a space station subscription bundle
        BundleEntity bundleEntity = new BundleEntity();
        bundleEntity.setBundleId(UUID.randomUUID().toString());
        bundleEntity.setSpaceId(spaceId);
        bundleEntity.setState(BundleState.ACTIVATED.name());
        bundleEntity.setStartDate(startAt);
        bundleEntity.setEndDate(endAt);
        bundleEntity.setCreatedBy(userId);
        bundleEntity.setUpdatedBy(userId);
        final List<SubscriptionEntity> subscriptionEntities = new ArrayList<>();
        AppsumoSubscriptionMetadata metadata = AppsumoSubscriptionMetadata.builder()
            .invoiceItemUuid(event.getInvoiceItemUuid())
            .uuid(event.getUuid())
            .build();
        AppsumoLicenseObject license =
            AppsumoLicenseConfigUtil.findProductByPlanId(event.getPlanId())
                .orElseThrow(() -> new BusinessException(AppsumoException.LICENSE_NOT_FOUND));
        // Create base type subscription
        String subscriptionId = UUID.randomUUID().toString();
        SubscriptionEntity base = new SubscriptionEntity();
        base.setSpaceId(spaceId);
        base.setBundleId(bundleEntity.getBundleId());
        base.setSubscriptionId(subscriptionId);
        base.setProductName(license.getProductId());
        ProductEnum productEnum = ProductEnum.of(license.getProductId());
        base.setProductCategory(productEnum.getCategory().name());
        base.setPriceId(license.getPlanId());
        base.setPeriod(BillingPeriod.UNLIMITED.name());
        base.setState(SubscriptionState.ACTIVATED.name());
        base.setBundleStartDate(startAt);
        base.setExpireDate(endAt);
        base.setStartDate(startAt);
        base.setMetadata(JSONUtil.toJsonStr(metadata));
        base.setCreatedBy(userId);
        base.setUpdatedBy(userId);
        base.setQuantity(0);
        subscriptionEntities.add(base);
        iBundleInApitableService.create(bundleEntity);
        iSubscriptionInApitableService.createBatch(subscriptionEntities);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long userSignup(Long eventId, String password) throws Auth0Exception {
        AppsumoEventDTO event = iAppsumoEventLogService.getSimpleInfoById(eventId);
        if (null == event) {
            throw new BusinessException(AppsumoException.EVENT_NOT_FOUND);
        }
        String email = event.getActivationEmail();
        User user = auth0Service.userByEmail(email);
        // throw when user exists
        ExceptionUtil.isFalse(null != user, UserException.REGISTER_EMAIL_HAS_EXIST);
        // create an account
        user = auth0Service.createUserWithoutEmailVerification(email, password);
        UserSpaceDTO userSpace = auth0Service.createUserByAuth0IfNotExist(user);
        // handle appsumo activation event
        createSubscription(userSpace.getUserId(), userSpace.getSpaceId(), event);
        iAppsumoEventLogService.updateStatus(event.getId(), AppsumoHandleStatus.SUCCESS);
        return userSpace.getUserId();
    }

    @Override
    public void linkAppsumoActivationEmail(String userEmail, String activationEmail) {
        iAppsumoEventLogService.updateUserEmail(activationEmail, userEmail);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void manuHandleSubscription(Long eventId, String spaceId) {
        SpaceEntity space = iSpaceService.getEntityBySpaceId(spaceId);
        if (null == space) {
            throw new BusinessException(SpaceException.SPACE_NOT_EXIST);
        }
        AppsumoEventDTO event = iAppsumoEventLogService.getSimpleInfoById(eventId);
        if (null == event) {
            throw new BusinessException("wrong event id");
        }
        List<BundleEntity> bundles = iBundleInApitableService.getBySpaceId(spaceId);
        if (!bundles.isEmpty()) {
            throw new BusinessException("wrong space id");
        }
        String email = iUserService.getEmailByUserId(space.getCreatedBy());
        if (!event.getActivationEmail().equals(email)) {
            throw new BusinessException("wrong space creator's id");
        }
        createSubscription(space.getCreatedBy(), spaceId, event);
        iAppsumoEventLogService.updateStatus(event.getId(), AppsumoHandleStatus.SUCCESS);
    }

    @Override
    public boolean isCurrentLicense(String metadataStr, String uuid) {
        AppsumoSubscriptionMetadata metadata =
            JSONUtil.toBean(metadataStr, AppsumoSubscriptionMetadata.class);
        // blank means old license,have not saved in subscription
        return uuid.equals(metadata.getUuid()) || StrUtil.isBlank(metadata.getUuid());

    }

    private void validateUser(String userName, String password) {
        if (null == appsumoTemplate) {
            throw new IllegalStateException("Appsumo not configured");
        }
        if (!appsumoTemplate.isUserMatch(userName, password)) {
            throw new IllegalStateException("Appsumo user config matched");
        }
    }
}

