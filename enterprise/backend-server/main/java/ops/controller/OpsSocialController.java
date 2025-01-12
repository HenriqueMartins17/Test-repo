/*
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up
 * license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its
 * subdirectories does not constitute permission to use this code or APITable Enterprise Edition
 * features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

package com.apitable.enterprise.ops.controller;

import com.apitable.core.support.ResponseData;
import com.apitable.enterprise.gm.vo.WeComIsvPermitNewOrderVo;
import com.apitable.enterprise.gm.vo.WeComIsvPermitRenewalVo;
import com.apitable.enterprise.ops.ro.OpsAuthRo;
import com.apitable.enterprise.ops.ro.WeComIsvEventRo;
import com.apitable.enterprise.ops.ro.WeComIsvNewSpaceRo;
import com.apitable.enterprise.ops.ro.WeComIsvPermitActivateRo;
import com.apitable.enterprise.ops.ro.WeComIsvPermitEnsureAllRo;
import com.apitable.enterprise.ops.ro.WeComIsvPermitNewOrderRo;
import com.apitable.enterprise.ops.ro.WeComIsvPermitRenewalRo;
import com.apitable.enterprise.ops.service.IOpsService;
import com.apitable.enterprise.ops.service.IOpsSocialService;
import com.apitable.enterprise.social.entity.SocialCpIsvEventLogEntity;
import com.apitable.enterprise.social.entity.SocialWecomPermitOrderEntity;
import com.apitable.enterprise.social.enums.SocialCpIsvMessageProcessStatus;
import com.apitable.enterprise.social.service.ISocialCpIsvMessageService;
import com.apitable.enterprise.social.service.ISocialCpIsvPermitService;
import com.apitable.enterprise.social.service.ISocialCpIsvService;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.PostResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * Product Operation System - Social API.
 * </p>
 */
@RestController
@ApiResource(path = "/ops/social")
@Tag(name = "Product Operation System - Social API")
public class OpsSocialController {

    @Resource
    private IOpsService iOpsService;

    @Resource
    private IOpsSocialService iOpsSocialService;

    @Resource
    private ISocialCpIsvService socialCpIsvService;

    @Resource
    private ISocialCpIsvPermitService socialCpIsvPermitService;

    @Resource
    private ISocialCpIsvMessageService socialCpIsvMessageService;

    /**
     * Manually execute compensation of feishu event.
     */
    @PostResource(path = "/feishu/tenants/{tenantId}/event", requiredLogin = false)
    @Operation(summary = "Manually execute compensation of feishu event")
    public ResponseData<Void> handleFeishuEvent(@PathVariable("tenantId") String tenantId,
                                                @RequestBody OpsAuthRo body) {
        iOpsService.auth(body.getToken());
        iOpsSocialService.handleFeishuEvent(tenantId);
        return ResponseData.success();
    }

    /**
     * Manually execute wecom isv event.
     */
    @PostResource(path = "/wecom/isv/event", requiredLogin = false)
    @Operation(summary = "Manually execute wecom isv event", hidden = true)
    public ResponseData<String> postWecomIsvEvent(@RequestBody @Validated WeComIsvEventRo body) {
        iOpsService.auth(body.getToken());

        SocialCpIsvEventLogEntity messageEntity =
            socialCpIsvMessageService.getById(body.getEventId());
        if (messageEntity == null) {
            return ResponseData.success("non");
        }
        try {
            socialCpIsvMessageService.doUnprocessedInfo(messageEntity);
            socialCpIsvMessageService.updateStatusById(messageEntity.getId(),
                SocialCpIsvMessageProcessStatus.SUCCESS);
            return ResponseData.success(messageEntity.getMessage());
        } catch (WxErrorException ex) {
            socialCpIsvMessageService.updateStatusById(messageEntity.getId(),
                SocialCpIsvMessageProcessStatus.REJECT_PERMANENTLY);
            return ResponseData.success(ex.getError().getErrorMsg());
        }
    }

    /**
     * Recreate wecom isv space.
     */
    @PostResource(path = "/wecom/isv/newSpace", requiredLogin = false)
    @Operation(summary = "Recreate wecom isv space", hidden = true)
    public ResponseData<Void> postWecomIsvNewSpace(
        @RequestBody @Validated WeComIsvNewSpaceRo body) {
        iOpsService.auth(body.getToken());
        socialCpIsvService.createNewSpace(body.getSuiteId(), body.getAuthCorpId());
        return ResponseData.success();
    }

    /**
     * Permit wecom isv new order.
     */
    @PostResource(path = "/wecom/isv/permit/newOrder", requiredLogin = false)
    @Operation(summary = "Permit wecom isv new order", hidden = true)
    public ResponseData<WeComIsvPermitNewOrderVo> postWecomIsvPermitNewOrder(
        @RequestBody @Validated WeComIsvPermitNewOrderRo body) {
        iOpsService.auth(body.getToken());

        SocialWecomPermitOrderEntity orderWecomEntity =
            socialCpIsvPermitService.createNewOrder(body.getSpaceId(), body.getDurationMonths());
        WeComIsvPermitNewOrderVo newOrderVo = new WeComIsvPermitNewOrderVo();
        newOrderVo.setId(orderWecomEntity.getId());
        newOrderVo.setOrderId(orderWecomEntity.getOrderId());
        return ResponseData.success(newOrderVo);
    }

    /**
     * Permit wecom isv activate.
     */
    @PostResource(path = "/wecom/isv/permit/activate", requiredLogin = false)
    @Operation(summary = "Permit wecom isv activate", hidden = true)
    public ResponseData<Void> postWecomIsvPermitActivate(
        @RequestBody @Validated WeComIsvPermitActivateRo body) {
        iOpsService.auth(body.getToken());
        socialCpIsvPermitService.activateOrder(body.getOrderId());
        return ResponseData.success();
    }

    /**
     * Permit wecom isv renewal.
     */
    @PostResource(path = "/wecom/isv/permit/renewal", requiredLogin = false)
    @Operation(summary = "Permit wecom isv renewal", hidden = true)
    public ResponseData<WeComIsvPermitRenewalVo> postWecomIsvPermitRenewal(
        @RequestBody @Validated WeComIsvPermitRenewalRo body) {
        iOpsService.auth(body.getToken());

        SocialWecomPermitOrderEntity orderWecomEntity =
            socialCpIsvPermitService.renewalCpUser(body.getSpaceId(), body.getCpUserIds(),
                body.getDurationMonths());
        WeComIsvPermitRenewalVo renewalVo = new WeComIsvPermitRenewalVo();
        renewalVo.setId(orderWecomEntity.getId());
        renewalVo.setOrderId(orderWecomEntity.getOrderId());
        return ResponseData.success(renewalVo);
    }

    /**
     * Ensure wecom isv account info.
     */
    @PostResource(path = "/wecom/isv/permit/ensureAll", requiredLogin = false)
    @Operation(summary = "Ensure wecom isv account info", hidden = true)
    public ResponseData<Void> postWecomIsvPermitEnsureAll(
        @RequestBody @Validated WeComIsvPermitEnsureAllRo body) {
        iOpsService.auth(body.getToken());
        socialCpIsvPermitService.ensureOrderAndAllActiveCodes(body.getOrderId());
        return ResponseData.success();
    }

}
