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

package com.apitable.enterprise.wechat.controller;


import static com.apitable.base.enums.ParameterException.INCORRECT_ARG;
import static com.apitable.base.enums.ParameterException.NO_ARG;
import static com.apitable.enterprise.wechat.enums.WechatException.ILLEGAL_REQUEST;
import static com.apitable.user.enums.UserException.CANCEL_OPERATION;
import static com.apitable.user.enums.UserException.MA_CODE_INVALID;
import static com.apitable.user.enums.UserException.SCAN_SUCCESS;
import static com.apitable.user.enums.UserException.USER_CHECK_FAILED;
import static com.apitable.user.enums.UserException.WECHAT_LINK_OTHER;
import static com.apitable.user.enums.UserException.WECHAT_NO_LINK;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.apitable.core.exception.BusinessException;
import com.apitable.core.support.ResponseData;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.enterprise.wechat.mapper.ThirdPartyMemberMapper;
import com.apitable.enterprise.wechat.service.IThirdPartyMemberService;
import com.apitable.enterprise.wechat.service.IWechatMaService;
import com.apitable.enterprise.wechat.vo.WeChatLoginResultVo;
import com.apitable.enterprise.wechat.vo.WechatInfoVo;
import com.apitable.interfaces.eventbus.facade.EventBusFacade;
import com.apitable.interfaces.eventbus.model.UserLoginEvent;
import com.apitable.shared.component.TaskManager;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.GetResource;
import com.apitable.shared.context.SessionContext;
import com.apitable.shared.util.information.ClientOriginInfo;
import com.apitable.shared.util.information.InformationUtil;
import com.apitable.user.enums.LinkType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * WeChat MiniApp API.
 * </p>
 */
@RestController
@Tag(name = "WeChat MiniApp API")
@ApiResource(path = "/wechat/miniapp")
@Slf4j
public class WechatMaController {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired(required = false)
    private WxMaService wxMaService;

    @Resource
    private IWechatMaService iWechatMaService;

    @Resource
    private IThirdPartyMemberService iThirdPartyMemberService;

    @Resource
    private ThirdPartyMemberMapper thirdPartyMemberMapper;

    @Resource
    private EventBusFacade eventBusFacade;

    /**
     * WeChat widget, get session information after WeChat login.
     */
    private static final String WECHAT_MINIAPP_AUTH_RESULT =
        "vikadata:wechat:miniapp:sessionInfo:{}";

    /**
     * WeChat widget, generates a unique identity for the scan login or associated widget code.
     */
    private static final String WECHAT_MINIAPP_CODE_MARK = "vikadata:wechat:miniapp:qrcode:{}";

    /**
     * Authorized Login(wx.login user).
     */
    @GetResource(path = "/authorize", requiredLogin = false)
    @Operation(summary = "Authorized Login(wx.login user)", description = "Mini Program "
        + "Authorized Login (Silent Authorization)")
    @Parameter(name = "code", description = "Wechat login credentials obtained by wx.login",
        schema = @Schema(type = "string"), required = true, in = ParameterIn.QUERY)
    public ResponseData<WeChatLoginResultVo> authorize(@RequestParam(value = "code") String code) {
        log.info("WeChat user login,code:{}", code);
        if (wxMaService == null) {
            throw new BusinessException("WeChat applet component is not enabled.");
        }
        // Get the information in the cache first to avoid code reuse
        BoundValueOperations<String, Object> ops =
            redisTemplate.boundValueOps(StrUtil.format(WECHAT_MINIAPP_AUTH_RESULT, code));
        WxMaJscode2SessionResult result;
        if (ObjectUtil.isNotNull(ops.get())) {
            result = (WxMaJscode2SessionResult) ops.get();
        } else {
            // Get WeChat user identity
            try {
                result = wxMaService.jsCode2SessionInfo(code);
                ops.set(result, 2, TimeUnit.HOURS);
            } catch (WxErrorException e) {
                e.printStackTrace();
                throw new BusinessException(ILLEGAL_REQUEST);
            }
        }
        // Login processing, if there is a bound user to automatically enter the workbench,
        // otherwise add or update WeChat member information
        WeChatLoginResultVo vo = iWechatMaService.login(result);
        return ResponseData.success(vo);
    }

    /**
     * User authorized to use WeChat mobile number.
     */
    @GetResource(path = "/phone", requiredLogin = false)
    @Operation(summary = "User authorized to use WeChat mobile number")
    @Parameters({
        @Parameter(name = "mark", description = "mini program code unique identifier", schema =
        @Schema(type = "string"), in = ParameterIn.QUERY),
        @Parameter(name = "encryptedData", description = "encrypted data", schema = @Schema(type
            = "string"), required = true, in = ParameterIn.QUERY),
        @Parameter(name = "iv", description = "initial vector for encryption algorithm", schema =
        @Schema(type = "string"), required = true, in = ParameterIn.QUERY)
    })
    public ResponseData<WeChatLoginResultVo> phone(
        @RequestParam(value = "encryptedData", required = false) String encryptedData,
        @RequestParam(value = "iv", required = false) String iv,
        @RequestParam(value = "mark", required = false) String mark) {
        if (wxMaService == null) {
            throw new BusinessException("WeChat applet component is not enabled");
        }
        ExceptionUtil.isFalse(encryptedData == null || iv == null, NO_ARG);
        // Get sessionKey
        Long wechatMemberId = SessionContext.getWechatMemberId();
        String sessionKey = thirdPartyMemberMapper.selectSessionKeyById(wechatMemberId);
        // decrypt
        WxMaPhoneNumberInfo phoneNoInfo =
            wxMaService.getUserService().getPhoneNoInfo(sessionKey, encryptedData, iv);
        // Login processing
        WeChatLoginResultVo vo = iWechatMaService.signIn(wechatMemberId, phoneNoInfo);
        // Sensors - Register/Login
        ClientOriginInfo origin =
            InformationUtil.getClientOriginInfoInCurrentHttpContext(false, true);
        TaskManager.me().execute(() ->
            eventBusFacade.onEvent(new UserLoginEvent(vo.getUserId(),
                "WeChat Applet", vo.isNewUser(), origin)));
        // Agree to the web scan code login processing
        if (StrUtil.isNotBlank(mark)) {
            // Read the applet code to uniquely identify the cache
            String key = StrUtil.format(WECHAT_MINIAPP_CODE_MARK, mark);
            BoundValueOperations<String, Object> opts = redisTemplate.boundValueOps(key);
            ExceptionUtil.isNotNull(opts.get(), MA_CODE_INVALID);
            // Put the bound Vig account ID into the cache, and let the web side poll the result
            // to log in
            Long userId = SessionContext.getUserId();
            opts.set(userId, Objects.requireNonNull(opts.getExpire()) + 5, TimeUnit.SECONDS);
        }
        return ResponseData.success(vo);
    }

    /**
     * Synchronize WeChat User Information.
     */
    @GetResource(path = "/info", requiredLogin = false)
    @Operation(summary = "Synchronize WeChat User Information")
    @Parameters({
        @Parameter(name = "signature", description = "signature", schema = @Schema(type = "string"),
            required = true, in = ParameterIn.QUERY),
        @Parameter(name = "rawData", description = "data", schema = @Schema(type = "string"),
            required = true, in = ParameterIn.QUERY),
        @Parameter(name = "encryptedData", description = "encrypted data", schema = @Schema(type
            = "string"), required = true, in = ParameterIn.QUERY),
        @Parameter(name = "iv", description = "initial vector for encryption algorithm", schema =
        @Schema(type = "string"), required = true, in = ParameterIn.QUERY)
    })
    public ResponseData<Void> info(
        @RequestParam(value = "signature", required = false) String signature,
        @RequestParam(value = "rawData", required = false) String rawData,
        @RequestParam(value = "encryptedData", required = false) String encryptedData,
        @RequestParam(value = "iv", required = false) String iv) {
        if (wxMaService == null) {
            throw new BusinessException("WeChat applet component is not enabled");
        }
        // Get sessionKey
        Long wechatMemberId = SessionContext.getWechatMemberId();
        String sessionKey = thirdPartyMemberMapper.selectSessionKeyById(wechatMemberId);
        // User information verification
        if (!wxMaService.getUserService().checkUserInfo(sessionKey, rawData, signature)) {
            throw new BusinessException(USER_CHECK_FAILED);
        }
        // Decrypt user information
        WxMaUserInfo userInfo =
            wxMaService.getUserService().getUserInfo(sessionKey, encryptedData, iv);
        // information processing
        iThirdPartyMemberService.editMiniAppMember(wechatMemberId, null, null, userInfo);
        return ResponseData.success();
    }

    /**
     * Get User Information.
     */
    @GetResource(path = "/getInfo", requiredPermission = false)
    @Operation(summary = "Get User Information")
    public ResponseData<WechatInfoVo> getInfo() {
        // Obtain the user ID of the bound account in real time through the WeChat member ID,
        // so as to avoid that the userId in the session is no longer the bound account
        Long wechatMemberId = SessionContext.getWechatMemberId();
        Long userId = thirdPartyMemberMapper.selectUserIdByIdAndLinkType(wechatMemberId,
            LinkType.WECHAT.getType());
        WechatInfoVo vo = iWechatMaService.getUserInfo(userId);
        return ResponseData.success(vo);
    }

    /**
     * The Operation of The Applet Code.
     */
    @GetResource(path = "/operate", requiredLogin = false)
    @Operation(summary = "The Operation of The Applet Code")
    @Parameters({
        @Parameter(name = "type", description = "type (0: Enter verification validity; 1: Confirm"
            + " the login (the WeChat account of the Weige account is bound); 2: Cancel the "
            + "login/bind the account; 3: Confirm the binding account)", schema = @Schema(type =
            "integer"), required = true, in = ParameterIn.QUERY),
        @Parameter(name = "mark", description = "mini program code unique identifier", schema =
        @Schema(type = "string"), required = true, in = ParameterIn.QUERY)
    })
    public ResponseData<Void> operate(@RequestParam(value = "type", required = false) Integer type,
                                      @RequestParam(value = "mark", required = false) String mark) {
        ExceptionUtil.isFalse(type == null || mark == null, NO_ARG);
        log.info("After scanning the code, the operation of the applet code page，mark:{},type:{}",
            mark, type);
        // Read the applet code to uniquely identify the cache
        String key = StrUtil.format(WECHAT_MINIAPP_CODE_MARK, mark);
        BoundValueOperations<String, Object> opts = redisTemplate.boundValueOps(key);
        Object code = opts.get();
        switch (type) {
            case 0:
                ExceptionUtil.isNotNull(code, MA_CODE_INVALID);
                opts.set(SCAN_SUCCESS.getCode(), Objects.requireNonNull(opts.getExpire()),
                    TimeUnit.SECONDS);
                break;
            case 1:
                log.info("Scan code to authorize login，mark:{}. Cache get status value：{}", mark,
                    code);
                // Confirm the login, put the bound Vig account ID into the cache, and let the
                // web side poll the result to realize the login
                ExceptionUtil.isNotNull(code, MA_CODE_INVALID);
                Long wechatMemberId = SessionContext.getWechatMemberId();
                Long userId = thirdPartyMemberMapper.selectUserIdByIdAndLinkType(wechatMemberId,
                    LinkType.WECHAT.getType());
                ExceptionUtil.isNotNull(userId, WECHAT_NO_LINK);
                opts.set(userId, Objects.requireNonNull(opts.getExpire()) + 5, TimeUnit.SECONDS);
                break;
            case 2:
                // If it is still within the valid time, update the status of canceling the
                // login/binding of the applet
                if (ObjectUtil.isNotNull(code)) {
                    opts.set(CANCEL_OPERATION.getCode(), Objects.requireNonNull(opts.getExpire()),
                        TimeUnit.SECONDS);
                }
                break;
            case 3:
                log.info("Bind account，mark:{}. Cache get status value：{}", mark, code);
                // Put the WeChat member ID in the cache, and let the web side poll the results
                // to associate with the account
                wechatMemberId = SessionContext.getWechatMemberId();
                userId = thirdPartyMemberMapper.selectUserIdByIdAndLinkType(wechatMemberId,
                    LinkType.WECHAT.getType());
                if (ObjectUtil.isNotNull(userId)) {
                    // If the WeChat account has been bound to another Weige account,
                    // save the result to the cache so that the web side can also prompt
                    // synchronously
                    opts.set(WECHAT_LINK_OTHER.getCode(),
                        Objects.requireNonNull(opts.getExpire()) + 5, TimeUnit.SECONDS);
                    throw new BusinessException(WECHAT_LINK_OTHER);
                }
                opts.set(wechatMemberId, Objects.requireNonNull(opts.getExpire()) + 5,
                    TimeUnit.SECONDS);
                break;
            default:
                throw new BusinessException(INCORRECT_ARG);
        }
        return ResponseData.success();
    }
}
