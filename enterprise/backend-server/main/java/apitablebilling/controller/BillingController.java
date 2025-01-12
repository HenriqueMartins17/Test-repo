package com.apitable.enterprise.apitablebilling.controller;

import com.apitable.core.support.ResponseData;
import com.apitable.enterprise.apitablebilling.enums.UpdateSubscriptionAction;
import com.apitable.enterprise.apitablebilling.model.dto.PaginationRequest;
import com.apitable.enterprise.apitablebilling.model.vo.BillingInfo;
import com.apitable.enterprise.apitablebilling.model.vo.BillingSessionVO;
import com.apitable.enterprise.apitablebilling.model.vo.CustomerInvoices;
import com.apitable.enterprise.apitablebilling.service.IBillingService;
import com.apitable.enterprise.auth0.autoconfigure.Auth0Template;
import com.apitable.enterprise.auth0.service.IUserBindService;
import com.apitable.organization.service.IMemberService;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.GetResource;
import com.apitable.shared.component.scanner.annotation.PostResource;
import com.apitable.shared.context.SessionContext;
import com.apitable.shared.util.HttpServletUtil;
import com.apitable.space.service.ISpaceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * billing info controller.
 *
 * @author Shawn Deng
 */
@RestController
@Tag(name = "Billing Api")
@ApiResource
public class BillingController {

    @Resource
    private IBillingService iBillingService;

    @Resource
    private ISpaceService iSpaceService;

    @Resource
    private IMemberService iMemberService;

    @Resource
    private IUserBindService iUserBindService;

    @Autowired(required = false)
    private Auth0Template auth0Template;

    /**
     * get billing info.
     *
     * @param spaceId space id
     * @return BillingInfo
     */
    @GetResource(path = "/billing/subscriptions", requiredPermission = false)
    public ResponseData<BillingInfo> getSubscriptions(
        @RequestParam(name = "spaceId") String spaceId) {
        // check main admin
        Long userId = SessionContext.getUserId();
        Long memberId = iMemberService.getMemberIdByUserIdAndSpaceId(userId, spaceId);
        iSpaceService.checkMemberIsAdmin(spaceId, memberId);
        BillingInfo billingInfo = iBillingService.getBillingInfo(spaceId);
        return ResponseData.success(billingInfo);
    }

    /**
     * get invoices.
     *
     * @param spaceId       space id
     * @param startingAfter starting after
     * @param endingBefore  ending before
     * @param limit         limit
     * @return CustomerInvoices
     */
    @GetResource(path = "/billing/invoices", requiredPermission = false)
    public ResponseData<CustomerInvoices> getInvoices(
        @RequestParam(name = "spaceId") String spaceId,
        @RequestParam(name = "startingAfter", required = false) String startingAfter,
        @RequestParam(name = "endingBefore", required = false) String endingBefore,
        @RequestParam(name = "limit", required = false, defaultValue = "10") long limit) {
        // check main admin
        Long userId = SessionContext.getUserId();
        Long memberId = iMemberService.getMemberIdByUserIdAndSpaceId(userId, spaceId);
        // check main admin
        iSpaceService.checkMemberIsAdmin(spaceId, memberId);
        PaginationRequest paginationRequest =
            PaginationRequest.of(startingAfter, endingBefore, limit);
        CustomerInvoices customerInvoices = iBillingService.getInvoices(spaceId, paginationRequest);
        return ResponseData.success(customerInvoices);
    }

    private Map<String, String> extractExternalProperty(HttpServletRequest request, Long userId) {
        Map<String, String> externalProperty = HttpServletUtil.getParameterAsMap(request, true);
        String externalKey = iUserBindService.getExternalKeyByUserId(userId);
        Map<String, Object> userAppMetadata = auth0Template.getUserAppMetadata(externalKey);
        // merge map
        userAppMetadata.forEach(
            (key, value) -> externalProperty.merge(key, value.toString(), (v1, v2) -> v2));
        return externalProperty;
    }

    /**
     * billing customer portal.
     *
     * @param spaceId space id
     * @return BillingSessionVO
     */
    @PostResource(path = "/billing/customers/portal", requiredPermission = false)
    public ResponseData<BillingSessionVO> customerPortalUrl(
        @RequestParam(name = "spaceId") String spaceId, HttpServletRequest request) {
        // check main admin
        Long userId = SessionContext.getUserId();
        Long memberId = iMemberService.getMemberIdByUserIdAndSpaceId(userId, spaceId);
        // check main admin
        iSpaceService.checkMemberIsAdmin(spaceId, memberId);
        Map<String, String> externalProperty = extractExternalProperty(request, userId);
        String url = iBillingService.getCustomerPortalUrl(spaceId, externalProperty);
        return ResponseData.success(new BillingSessionVO(url));
    }

    /**
     * change payment method for subscription.
     *
     * @param spaceId space id
     * @return BillingSessionVO
     */
    @PostResource(path = "/billing/changePaymentMethod", requiredPermission = false)
    public ResponseData<BillingSessionVO> changePaymentMethod(
        @RequestParam(name = "spaceId") String spaceId, HttpServletRequest request) {
        // check main admin
        Long userId = SessionContext.getUserId();
        Long memberId = iMemberService.getMemberIdByUserIdAndSpaceId(userId, spaceId);
        // check main admin
        iSpaceService.checkMemberIsAdmin(spaceId, memberId);
        Map<String, String> externalProperty = extractExternalProperty(request, userId);
        String url = iBillingService.getChangePaymentMethodUrl(spaceId, externalProperty);
        return ResponseData.success(new BillingSessionVO(url));
    }

    /**
     * update subscription for confirm price.
     *
     * @param spaceId        space id
     * @param subscriptionId subscription id
     * @param action         interval or null
     * @return BillingSessionVO
     */
    @PostResource(path = "/billing/updateSubscription", requiredPermission = false)
    public ResponseData<BillingSessionVO> updateSubscription(
        @RequestParam(name = "spaceId") String spaceId,
        @RequestParam(name = "subscriptionId") String subscriptionId,
        @RequestParam(name = "action", required = false) String action,
        HttpServletRequest request) {
        // check main admin
        Long userId = SessionContext.getUserId();
        Long memberId = iMemberService.getMemberIdByUserIdAndSpaceId(userId, spaceId);
        iSpaceService.checkMemberIsAdmin(spaceId, memberId);
        UpdateSubscriptionAction subscriptionAction = UpdateSubscriptionAction.of(action);
        Map<String, String> externalProperty = extractExternalProperty(request, userId);
        String url =
            iBillingService.getCustomerUpdateSubscriptionLink(spaceId, subscriptionId,
                subscriptionAction, externalProperty);
        return ResponseData.success(new BillingSessionVO(url));
    }

    /**
     * update subscription for confirm price.
     *
     * @param spaceId        space id
     * @param subscriptionId subscription id
     * @param priceId        price id
     * @return BillingSessionVO
     */
    @Deprecated
    @PostResource(path = "/billing/updateSubscriptionConfirm", requiredPermission = false)
    public ResponseData<BillingSessionVO> updateSubscriptionConfirm(
        @RequestParam(name = "spaceId") String spaceId,
        @RequestParam(name = "subscriptionId") String subscriptionId,
        @RequestParam(name = "priceId") String priceId) {
        // check main admin
        Long userId = SessionContext.getUserId();
        Long memberId = iMemberService.getMemberIdByUserIdAndSpaceId(userId, spaceId);
        iSpaceService.checkMemberIsAdmin(spaceId, memberId);
        String url =
            iBillingService.getCustomerUpdateSubscriptionConfirmLink(spaceId, subscriptionId,
                priceId);
        return ResponseData.success(new BillingSessionVO(url));
    }

    /**
     * cancel subscription.
     *
     * @param spaceId        space id
     * @param subscriptionId subscription id
     * @return BillingSessionVO
     */
    @PostResource(path = "/billing/cancelSubscription", requiredPermission = false)
    public ResponseData<BillingSessionVO> cancelSubscription(
        @RequestParam(name = "spaceId") String spaceId,
        @RequestParam(name = "subscriptionId") String subscriptionId) {
        // check main admin
        Long userId = SessionContext.getUserId();
        Long memberId = iMemberService.getMemberIdByUserIdAndSpaceId(userId, spaceId);
        iSpaceService.checkMemberIsAdmin(spaceId, memberId);
        String url = iBillingService.getCancelSubscriptionLink(spaceId, subscriptionId);
        return ResponseData.success(new BillingSessionVO(url));
    }
}
