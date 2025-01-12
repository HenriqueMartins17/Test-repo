package com.apitable.enterprise.apitablebilling.controller;

import static com.apitable.enterprise.apitablebilling.enums.BillingException.NOT_SUPPORT_PURCHASE;
import static com.apitable.space.enums.SpaceException.NOT_SPACE_MAIN_ADMIN;
import static com.apitable.user.enums.UserException.USER_NOT_BIND_EMAIL;

import cn.hutool.core.util.ObjectUtil;
import com.apitable.core.support.ResponseData;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.enterprise.apitablebilling.model.ro.CheckoutCreation;
import com.apitable.enterprise.apitablebilling.model.ro.OpsCheckoutCreationRO;
import com.apitable.enterprise.apitablebilling.model.vo.CheckoutCreationVO;
import com.apitable.enterprise.apitablebilling.service.ICheckoutService;
import com.apitable.enterprise.apitablebilling.service.IEntitlementService;
import com.apitable.enterprise.ops.service.IOpsService;
import com.apitable.interfaces.billing.model.SubscriptionInfo;
import com.apitable.organization.service.IMemberService;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.PostResource;
import com.apitable.shared.context.SessionContext;
import com.apitable.shared.holder.UserHolder;
import com.apitable.space.service.ISpaceService;
import com.apitable.user.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * checkout resource.
 */
@RestController
@Tag(name = "Checkout Api")
@ApiResource
@Slf4j
public class CheckoutController {

    @Resource
    private ICheckoutService iCheckoutService;

    @Resource
    private IMemberService iMemberService;

    @Resource
    private IOpsService iOpsService;

    @Resource
    private IUserService iUserService;

    @Resource
    private ISpaceService iSpaceService;

    @Resource
    private IEntitlementService iEntitlementService;

    /**
     * create checkout resource with Post Method.
     *
     * @param data request data
     * @return CheckoutCreationVO
     */
    @PostResource(path = "/checkout", requiredPermission = false)
    public CheckoutCreationVO createCheckout(@RequestBody @Valid CheckoutCreation data) {
        // Check Space Exist
        Long userId = SessionContext.getUserId();
        iMemberService.checkUserIfInSpace(userId, data.getSpaceId());
        String sessionUrl = iCheckoutService.createCheckoutSession(userId, data);
        return new CheckoutCreationVO(sessionUrl);
    }

    /**
     * Create checkout.
     */
    @PostResource(path = "/ops/checkout", requiredPermission = false, requiredLogin = false)
    @Operation(summary = "ops create checkout", hidden = true)
    public ResponseData<CheckoutCreationVO> opsCreateCheckout(
        @RequestBody @Valid OpsCheckoutCreationRO body) {
        iOpsService.auth(body.getToken());
        Long userId = iUserService.getUserIdByEmail(body.getEmail());
        ExceptionUtil.isFalse(userId == null, USER_NOT_BIND_EMAIL);
        Long spaceAdminUserId = iSpaceService.getSpaceMainAdminUserId(body.getSpaceId());
        // check email can control space
        ExceptionUtil.isTrue(ObjectUtil.equal(userId, spaceAdminUserId), NOT_SPACE_MAIN_ADMIN);
        CheckoutCreation checkoutCreation =
            CheckoutCreation.builder()
                .spaceId(body.getSpaceId())
                .priceId(body.getPriceId())
                .mode(body.getMode())
                .build();
        SubscriptionInfo subscriptionInfo =
            iEntitlementService.getEntitlementBySpaceId(body.getSpaceId());
        ExceptionUtil.isTrue(subscriptionInfo.isFree(), NOT_SUPPORT_PURCHASE);
        UserHolder.set(userId);
        String sessionUrl = iCheckoutService.createCheckoutSession(userId, checkoutCreation);
        return ResponseData.success(new CheckoutCreationVO(sessionUrl));
    }

}
