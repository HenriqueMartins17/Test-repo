package com.apitable.enterprise.apitablebilling.appsumo.handler;

import static com.apitable.enterprise.apitablebilling.appsumo.core.RedisConstants.getAppsumoUserEmailKey;

import cn.hutool.core.util.StrUtil;
import com.apitable.core.exception.BusinessException;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.enterprise.apitablebilling.appsumo.annotation.AppsumoEventHandler;
import com.apitable.enterprise.apitablebilling.appsumo.enums.AppsumoAction;
import com.apitable.enterprise.apitablebilling.appsumo.enums.AppsumoException;
import com.apitable.enterprise.apitablebilling.appsumo.enums.AppsumoHandleStatus;
import com.apitable.enterprise.apitablebilling.appsumo.model.AppsumoEventDTO;
import com.apitable.enterprise.apitablebilling.appsumo.model.EventVO;
import com.apitable.enterprise.apitablebilling.appsumo.service.IAppsumoEventLogService;
import com.apitable.enterprise.apitablebilling.appsumo.service.IAppsumoService;
import com.apitable.enterprise.apitablebilling.service.IEntitlementService;
import com.apitable.enterprise.auth0.service.Auth0Service;
import com.apitable.enterprise.auth0.service.IUserBindService;
import com.apitable.interfaces.billing.model.SubscriptionInfo;
import com.apitable.shared.config.properties.ConstProperties;
import com.apitable.space.model.Space;
import com.apitable.space.service.ISpaceService;
import com.apitable.user.entity.UserEntity;
import com.apitable.user.service.IUserService;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.mgmt.users.User;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * appsumo event handler interface.
 */
@Slf4j
@AppsumoEventHandler(action = AppsumoAction.ACTIVATE)
@ConditionalOnProperty(value = "appsumo.enabled", havingValue = "true")
public class ActivateEventHandler implements IAppsumoEventHandler {
    @Resource
    private ConstProperties constProperties;

    @Resource
    private RedisTemplate<String, Long> redisTemplate;

    @Resource
    private IAppsumoEventLogService iAppsumoEventLogService;

    @Resource
    private ISpaceService iSpaceService;

    @Resource
    private Auth0Service auth0Service;

    @Resource
    private IUserBindService iUserBindService;

    @Resource
    private IAppsumoService iAppsumoService;

    @Resource
    private IUserService iUserService;

    @Resource
    private IEntitlementService iEntitlementService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public EventVO handle(Long eventLogId) {
        AppsumoEventDTO event = iAppsumoEventLogService.getSimpleInfoById(eventLogId);
        ExceptionUtil.isFalse(null == event, AppsumoException.EVENT_NOT_FOUND);
        // check email
        try {
            EventVO result = handleActivation(event);
            // set status success
            iAppsumoEventLogService.updateStatus(event.getId(), AppsumoHandleStatus.SUCCESS);
            return result;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            // get email false, should prevent to activate this account, try again, or contact up
            throw new BusinessException(AppsumoException.NETWORK_ERROR);
        }
    }

    private EventVO handleActivation(AppsumoEventDTO event) throws Auth0Exception {
        User user = auth0Service.userByEmail(event.getActivationEmail());
        // user not exits,should redirect to sign up page,register a new account
        if (null == user) {
            String key = getAppsumoUserEmailKey(event.getId().toString());
            if (Boolean.TRUE.equals(redisTemplate.boundValueOps(key)
                .setIfAbsent(event.getId(), 30, TimeUnit.DAYS))) {
                // set status success at sign up interface
                return EventVO.builder()
                    .message("product activated")
                    .redirectUrl(
                        StrUtil.format("{}/user/appsumo?state={}",
                            constProperties.getServerDomain(),
                            event.getId().toString()))
                    .build();
            }
            // an activation already exists, should finish the registration and then activate again
            throw new BusinessException(AppsumoException.USER_EMAIL_BOUNDED);
        }
        // already have an auth0 count
        Long userId = iUserBindService.getUserIdByExternalKey(user.getId());
        if (null == userId) {
            throw new BusinessException(AppsumoException.USER_NOT_FOUND);
        }
        List<String> spaceIds = iSpaceService.getSpaceIdsByCreatedBy(userId);
        String spaceId = null;
        // only have one space
        if (spaceIds.size() == 1) {
            // the only one space is a free plan, give the plan to this space
            SubscriptionInfo subscriptionInfo =
                iEntitlementService.getEntitlementBySpaceId(spaceIds.get(0));
            if (null == subscriptionInfo || subscriptionInfo.isFree()) {
                spaceId = spaceIds.get(0);
            }
        }
        if (StrUtil.isBlank(spaceId)) {
            UserEntity localUser = iUserService.getById(userId);
            // init one space for user
            String spaceName = String.format("%s's Space", localUser.getNickName());
            Space space = iSpaceService.createSpace(localUser, spaceName);
            spaceId = space.getId();
        }
        iAppsumoService.createSubscription(userId, spaceId, event);
        // have more than one space or haven't any space,
        // create another space and redirect to the new space
        // redirect to the space pace
        return EventVO.builder()
            .message("product activated")
            .redirectUrl(StrUtil.format("{}/workbench/spaceId={}",
                constProperties.getServerDomain(),
                spaceId))
            .build();
    }
}
