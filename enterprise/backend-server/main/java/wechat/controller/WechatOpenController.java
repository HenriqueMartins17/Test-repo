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

import static com.apitable.enterprise.wechat.enums.WechatException.ILLEGAL_REQUEST;
import static com.apitable.enterprise.wechat.enums.WechatException.UPDATE_AUTO_REPLY_ERROR;
import static com.apitable.shared.constants.PageConstants.PAGE_DESC;
import static com.apitable.shared.constants.PageConstants.PAGE_PARAM;
import static com.apitable.shared.constants.PageConstants.PAGE_SIMPLE_EXAMPLE;
import static com.apitable.shared.constants.WechatConstants.MARK_PRE;
import static com.apitable.user.enums.UserException.QR_CODE_GET_ERROR;
import static com.apitable.user.enums.UserException.SCENE_EMPTY;

import cn.hutool.core.util.StrUtil;
import com.apitable.core.exception.BusinessException;
import com.apitable.core.support.ResponseData;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.enterprise.gm.enums.GmAction;
import com.apitable.enterprise.gm.service.IGmService;
import com.apitable.enterprise.wechat.autoconfigure.mp.WxMpProperties;
import com.apitable.enterprise.wechat.entity.WechatAuthorizationEntity;
import com.apitable.enterprise.wechat.entity.WechatKeywordReplyEntity;
import com.apitable.enterprise.wechat.enums.WechatMessageType;
import com.apitable.enterprise.wechat.enums.WechatMpQrcodeType;
import com.apitable.enterprise.wechat.mapper.WechatKeywordReplyMapper;
import com.apitable.enterprise.wechat.service.IWechatMpQrcodeService;
import com.apitable.enterprise.wechat.service.IWechatOpenService;
import com.apitable.enterprise.wechat.vo.QrCodePageVo;
import com.apitable.enterprise.wechat.vo.QrCodeVo;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.GetResource;
import com.apitable.shared.component.scanner.annotation.PostResource;
import com.apitable.shared.context.SessionContext;
import com.apitable.shared.util.page.PageHelper;
import com.apitable.shared.util.page.PageInfo;
import com.apitable.shared.util.page.PageObjectParam;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.result.WxMpCurrentAutoReplyInfo;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import me.chanjar.weixin.open.api.WxOpenService;
import me.chanjar.weixin.open.bean.message.WxOpenXmlMessage;
import me.chanjar.weixin.open.bean.result.WxOpenAuthorizerListResult;
import me.chanjar.weixin.open.util.WxOpenCryptUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * WeChat Open Platform API.
 * </p>
 */
@Tag(name = "WeChat Open Platform API")
@ApiResource(path = "/wechat/open")
@RestController
@Slf4j
public class WechatOpenController {

    @Value("${OPEN_REDIRECT_URI:}")
    private String openRedirectUri;

    @Autowired(required = false)
    private WxOpenService wxOpenService;

    @Resource
    private IWechatOpenService iWechatOpenService;

    @Resource
    private IWechatMpQrcodeService iWechatMpQrcodeService;

    @Resource
    private IGmService iGmService;

    @Autowired(required = false)
    private WxMpProperties wxMpProperties;

    @Resource
    private WechatKeywordReplyMapper keywordReplyMapper;

    /**
     * Receive Verification Ticket.
     */
    @PostResource(path = "/receiveTicket", requiredLogin = false)
    @Operation(summary = "Receive Verification Ticket")
    public String getComponentVerifyTicket(@RequestBody(required = false) String requestBody,
                                           @RequestParam("timestamp") String timestamp,
                                           @RequestParam("nonce") String nonce,
                                           @RequestParam("signature") String signature,
                                           @RequestParam(name = "encrypt_type", required = false)
                                           String encType,
                                           @RequestParam(name = "msg_signature", required = false)
                                           String msgSignature,
                                           HttpServletRequest request) {
        log.info("Receive Verification Ticket: [signature=[{}], encType=[{}], msgSignature=[{}],"
                + " timestamp=[{}], nonce=[{}], requestBody=[\n{}\n], remoteAddr=[{}]",
            signature, encType, msgSignature, timestamp, nonce, requestBody,
            request.getRemoteAddr());
        if (wxOpenService == null) {
            throw new BusinessException("Wechat developer platform components are not enabled");
        }
        ExceptionUtil.isTrue(StrUtil.equalsIgnoreCase("aes", encType), ILLEGAL_REQUEST);
        ExceptionUtil.isTrue(
            wxOpenService.getWxOpenComponentService().checkSignature(timestamp, nonce, signature),
            ILLEGAL_REQUEST);

        // aes encrypted message
        WxOpenXmlMessage inMessage =
            WxOpenXmlMessage.fromEncryptedXml(requestBody, wxOpenService.getWxOpenConfigStorage(),
                timestamp, nonce, msgSignature);
        if (log.isDebugEnabled()) {
            log.debug("\nThe decrypted content of the receiveTicket message is：\n{} ",
                inMessage.toString());
        }
        String out = "success";
        try {
            out = wxOpenService.getWxOpenComponentService().route(inMessage);
        } catch (WxErrorException e) {
            log.error("receive_ticket", e);
        }
        log.info("Receive Ticket assembly reply message：{}", out);
        return out;
    }

    /**
     * Create Pre-authorization URL.
     */
    @GetResource(path = "/createPreAuthUrl", requiredLogin = false)
    @Operation(summary = "Create Pre-authorization URL")
    @Parameters({
        @Parameter(name = "auth_type", description = "Authorized account type, 1. Only the "
            + "official account authorization list is displayed, 2. Only the applet authorization"
            + " list is displayed, 3. Both are displayed", required = true, schema =
        @Schema(type = "string"), in = ParameterIn.QUERY, example = "3"),
        @Parameter(name = "component_appid", description = "Authorized Official Account or Mini "
            + "Program AppId", required = true, schema = @Schema(type = "string"), in =
            ParameterIn.QUERY, example = "wx3ccd2f6264309a7c")
    })
    public ResponseData<String> createPreAuthUrl(
        @RequestParam(name = "auth_type", required = false) String authType,
        @RequestParam(name = "component_appid") String componentAppid) throws WxErrorException {
        if (wxOpenService == null) {
            throw new BusinessException("Wechat developer platform components are not enabled");
        }
        return ResponseData.success(wxOpenService.getWxOpenComponentService()
            .getPreAuthUrl(openRedirectUri, authType, componentAppid));
    }

    /**
     * Get Authorization Code Get Authorization Information.
     */
    @GetResource(path = "/getQueryAuth", requiredLogin = false)
    @Operation(summary = "Get Authorization Code Get Authorization Information")
    public ResponseData<WechatAuthorizationEntity> getQueryAuth(
        @RequestParam(name = "auth_code", required = false) String authorizationCode) {
        return ResponseData.success(iWechatOpenService.addAuthInfo(authorizationCode));
    }

    /**
     * Obtain the basic information of the authorized account.
     */
    @GetResource(path = "/createAuthorizerInfo", requiredLogin = false)
    @Operation(summary = "Obtain the basic information of the authorized account")
    public ResponseData<WechatAuthorizationEntity> getAuthorizerInfo(
        @RequestParam(name = "authorizerAppid", required = false) String authorizerAppid) {
        return ResponseData.success(iWechatOpenService.addAuthorizeInfo(authorizerAppid));
    }

    /**
     * Get All Authorized Account Information.
     */
    @GetResource(path = "/getAuthorizerList", requiredLogin = false)
    @Operation(summary = "Get All Authorized Account Information")
    public ResponseData<WxOpenAuthorizerListResult> getAuthorizerList() throws WxErrorException {
        if (wxOpenService == null) {
            throw new BusinessException("Wechat developer platform components are not enabled");
        }
        WxOpenAuthorizerListResult wxOpenAuthorizerListResult =
            wxOpenService.getWxOpenComponentService().getAuthorizerList(0, 100);
        return ResponseData.success(wxOpenAuthorizerListResult);
    }

    /**
     * WeChat Message Push Callback.
     */
    @PostResource(path = "/callback/{appId}", requiredLogin = false)
    @Operation(summary = "WeChat Message Push Callback")
    public Object callback(@RequestBody(required = false) String requestBody,
                           @PathVariable("appId") String appId,
                           @RequestParam("signature") String signature,
                           @RequestParam("timestamp") String timestamp,
                           @RequestParam("nonce") String nonce,
                           @RequestParam("openid") String openid,
                           @RequestParam("encrypt_type") String encType,
                           @RequestParam("msg_signature") String msgSignature,
                           HttpServletRequest request)
        throws WxErrorException {
        log.info(
            "\nReceive WeChat callback message request. [appId=[{}], openid=[{}], signature=[{}],"
                + " encType=[{}], msgSignature=[{}],"
                + " timestamp=[{}], nonce=[{}], requestBody=[\n{}\n], remoteAddr=[{}]",
            appId, openid, signature, encType, msgSignature, timestamp, nonce, requestBody,
            request.getRemoteAddr());

        // request verification
        ExceptionUtil.isTrue(StrUtil.equalsIgnoreCase("aes", encType), ILLEGAL_REQUEST);
        ExceptionUtil.isTrue(
            wxOpenService.getWxOpenComponentService().checkSignature(timestamp, nonce, signature),
            ILLEGAL_REQUEST);
        // Reply processing of WeChat keyword messages and events
        String out = null;
        // aes encrypted message
        WxMpXmlMessage inMessage =
            WxOpenXmlMessage.fromEncryptedMpXml(requestBody, wxOpenService.getWxOpenConfigStorage(),
                timestamp, nonce, msgSignature);
        log.info("\nThe content of the decrypted message is: \n{} ", inMessage.toString());
        // Determine whether it is your own public account
        boolean self = appId.equals(wxMpProperties.getAppId());
        // text message processing
        if (StringUtils.equalsIgnoreCase(inMessage.getMsgType(), WechatMessageType.TEXT.name())) {
            if (self) {
                out = iWechatOpenService.mpTextMessageProcess(appId, openid, inMessage);
            }
        } else if (StringUtils.equalsIgnoreCase(inMessage.getMsgType(),
            WechatMessageType.EVENT.name())) {
            // WeChat event handling
            if (self) {
                out = iWechatOpenService.mpEventProcess(appId, openid, inMessage);
            }
        }
        log.info("Reply message content: " + out);
        // Encrypt the reply message
        return StrUtil.isNotBlank(out) ? new WxOpenCryptUtil(
            wxOpenService.getWxOpenConfigStorage()).encrypt(out) : "success";
    }

    /**
     * Get WeChat server IP list.
     */
    @GetResource(path = "/getWechatIpList", requiredLogin = false)
    @Operation(summary = "Get WeChat server IP list")
    public ResponseData<List<String>> getWechatIpList(
        @RequestParam(name = "appId", required = false) String appId) throws WxErrorException {
        WxMpService wxMpService =
            wxOpenService.getWxOpenComponentService().getWxMpServiceByAppid(appId);
        return ResponseData.success(Arrays.asList(wxMpService.getCallbackIP()));
    }

    /**
     * Generates Qrcode.
     */
    @PostResource(path = "/createWxQrCode", requiredPermission = false)
    @Operation(summary = "Generates Qrcode", description = "The scene value cannot be passed at "
        + "all, and the string type is preferred.")
    @Parameters({
        @Parameter(name = "type", description = "qrcode type, type value (temporary integer "
            + "value: QR_SCENE, temporary string value: QR_STR_SCENE; permanent integer value: "
            + "QR_LIMIT_SCENE, permanent string value: QR_LIMIT_STR_SCENE)", schema =
        @Schema(type = "string"), in = ParameterIn.QUERY, example = "QR_LIMIT_STR_SCENE"),
        @Parameter(name = "expireSeconds", description = "the valid time of the QR code, in "
            + "seconds. The maximum is not more than 2592000 (that is, 30 days), and the default "
            + "is 30 seconds.", schema = @Schema(type = "integer"), in = ParameterIn.QUERY,
            example = "2592000"),
        @Parameter(name = "sceneId", description = "scene value ID, a 32-bit non-zero integer for"
            + " a temporary QR code, and a maximum value of 100000 for a permanent QR code "
            + "(current parameters only support 1--100000)", schema = @Schema(type = "integer"),
            in = ParameterIn.QUERY, example = "1"),
        @Parameter(name = "sceneStr", description = "Scene value ID (ID in string form), string "
            + "type, length limited from 1 to 64.", schema = @Schema(type = "string"), in =
            ParameterIn.QUERY, example = "weibo"),
        @Parameter(name = "appId", description = "wechat public account appId", schema =
        @Schema(type = "string"), in = ParameterIn.QUERY, example = "wx73eb141189")
    })
    public ResponseData<QrCodeVo> createWxQrCode(
        @RequestParam(name = "type", defaultValue = "QR_LIMIT_STR_SCENE") String type,
        @RequestParam(name = "expireSeconds", required = false) Integer expireSeconds,
        @RequestParam(name = "sceneId", required = false) Integer sceneId,
        @RequestParam(name = "sceneStr", required = false) String sceneStr,
        @RequestParam(name = "appId", required = false) String appId) {
        Long userId = SessionContext.getUserId();
        iGmService.validPermission(userId, GmAction.WECHAT_QRCODE__MANAGE);
        if (StrUtil.isBlank(appId)) {
            appId = wxMpProperties.getAppId();
            // Restrict your own official account, generate the scene value of the Qrcode, and
            // affect your own business such as scanning the code to log in
            if (StrUtil.isNotBlank(sceneStr) && sceneStr.startsWith(MARK_PRE)) {
                throw new BusinessException(StrUtil.format(
                    "The scene value should not start with 「{}」, otherwise it will affect scenes "
                        + "such as scan code login.",
                    MARK_PRE));
            }
        }
        WxMpService wxMpService =
            wxOpenService.getWxOpenComponentService().getWxMpServiceByAppid(appId);
        WxMpQrCodeTicket qrCodeCreateResult;
        // Determine the type of Qrcode and the type of scene value
        try {
            if (type.equals(WechatMpQrcodeType.QR_LIMIT_STR_SCENE.name())) {
                qrCodeCreateResult =
                    wxMpService.getQrcodeService().qrCodeCreateLastTicket(sceneStr);
            } else if (type.equals(WechatMpQrcodeType.QR_STR_SCENE.name())) {
                qrCodeCreateResult =
                    wxMpService.getQrcodeService().qrCodeCreateTmpTicket(sceneStr, expireSeconds);
            } else if (type.equals(WechatMpQrcodeType.QR_LIMIT_SCENE.name())) {
                qrCodeCreateResult = wxMpService.getQrcodeService().qrCodeCreateLastTicket(sceneId);
                ExceptionUtil.isNotNull(sceneId, SCENE_EMPTY);
                sceneStr = sceneId.toString();
            } else {
                qrCodeCreateResult =
                    wxMpService.getQrcodeService().qrCodeCreateTmpTicket(sceneId, expireSeconds);
                ExceptionUtil.isNotNull(sceneId, SCENE_EMPTY);
                sceneStr = sceneId.toString();
            }
        } catch (WxErrorException e) {
            e.printStackTrace();
            throw new BusinessException(QR_CODE_GET_ERROR);
        }
        iWechatMpQrcodeService.save(appId, type, sceneStr, qrCodeCreateResult);
        QrCodeVo vo = QrCodeVo.builder().image(qrCodeCreateResult.getTicket())
            .url(qrCodeCreateResult.getUrl()).build();
        // After generating the Qrcode, actively update the keyword automatic reply rules
        this.updateWxReply(appId);
        return ResponseData.success(vo);
    }

    /**
     * Query Qrcode pagination list.
     */
    @GetResource(path = "/getQrCodePage", requiredPermission = false)
    @Operation(summary = "Query Qrcode pagination list", description = PAGE_DESC)
    @Parameters({
        @Parameter(name = "appId", description = "wechat public account appId", schema =
        @Schema(type = "string"), in = ParameterIn.QUERY, example = "wx73eb141189"),
        @Parameter(name = PAGE_PARAM, description = "page params", required = true, schema =
        @Schema(type = "string"), in = ParameterIn.QUERY, example = PAGE_SIMPLE_EXAMPLE)
    })
    @SuppressWarnings({"rawtypes", "unchecked"})
    public ResponseData<PageInfo<QrCodePageVo>> getQrCodePage(
        @RequestParam(name = "appId", required = false) String appId, @PageObjectParam Page page) {
        Long userId = SessionContext.getUserId();
        iGmService.validPermission(userId, GmAction.WECHAT_QRCODE_QUERY);
        return ResponseData.success(PageHelper.build(iWechatMpQrcodeService.getQrCodePageVo(page,
            Optional.ofNullable(appId).orElse(wxMpProperties.getAppId()))));
    }

    /**
     * Delete Qrcode.
     */
    @PostResource(path = "/delQrCode", method = {RequestMethod.DELETE,
        RequestMethod.POST}, requiredPermission = false)
    @Operation(summary = "Delete Qrcode")
    @Parameters({
        @Parameter(name = "qrCodeId", description = "qrcode ID", required = true, schema =
        @Schema(type = "string"), in = ParameterIn.QUERY, example = "12345"),
        @Parameter(name = "appId", description = "wechat public account appId", schema =
        @Schema(type = "string"), in = ParameterIn.QUERY, example = "wx73eb141189")
    })
    public ResponseData<Void> delQrCode(@RequestParam(name = "qrCodeId") Long qrCodeId,
                                        @RequestParam(name = "appId", required = false)
                                        String appId) {
        Long userId = SessionContext.getUserId();
        iGmService.validPermission(userId, GmAction.WECHAT_QRCODE__MANAGE);
        iWechatMpQrcodeService.delete(userId, qrCodeId,
            Optional.ofNullable(appId).orElse(wxMpProperties.getAppId()));
        return ResponseData.success();
    }

    /**
     * Synchronously update WeChat keyword automatic reply rules.
     */
    @GetResource(path = "/updateWxReply", requiredPermission = false)
    @Operation(summary = "Synchronously update WeChat keyword automatic reply rules",
        description = "Be sure to add keyword replies first in the background of the official "
            + "account")
    @Transactional(rollbackFor = Exception.class)
    public ResponseData<Void> updateWxReply(
        @RequestParam(name = "appId", required = false) String appId) {
        Long userId = SessionContext.getUserId();
        iGmService.validPermission(userId, GmAction.WECHAT_REPLY_RULE_REFRESH);
        appId = Optional.ofNullable(appId).orElse(wxMpProperties.getAppId());
        WxMpService wxMpService =
            wxOpenService.getWxOpenComponentService().getWxMpServiceByAppid(appId);
        try {
            // Get the current official account keyword automatic reply rules
            WxMpCurrentAutoReplyInfo autoReplyInfo = wxMpService.getCurrentAutoReplyInfo();
            WxMpCurrentAutoReplyInfo.KeywordAutoReplyInfo keywordAutoReplyInfo =
                autoReplyInfo.getKeywordAutoReplyInfo();
            List<WxMpCurrentAutoReplyInfo.AutoReplyRule> keywordAutoReplyRules =
                keywordAutoReplyInfo.getList();
            if (keywordAutoReplyRules.size() > 0) {
                // 1.Delete all keyword auto-reply rules first
                keywordReplyMapper.deleteKeywordReplies(appId);

                List<WechatKeywordReplyEntity> keywordReplies = new ArrayList<>();

                // 2.Traverse autoresponder rules
                String finalAppId = appId;
                keywordAutoReplyRules.forEach(autoReplyRule -> {
                    List<WxMpCurrentAutoReplyInfo.KeywordInfo> keywordList =
                        autoReplyRule.getKeywordListInfo();
                    List<WxMpCurrentAutoReplyInfo.ReplyInfo> replyListInfo =
                        autoReplyRule.getReplyListInfo();
                    // Traverse the keyword list and the reply content list
                    keywordList.forEach(keywordInfo ->
                        replyListInfo.forEach(replyInfo -> {
                            WechatKeywordReplyEntity reply = new WechatKeywordReplyEntity();
                            reply.setAppId(finalAppId);
                            reply.setRuleName(autoReplyRule.getRuleName());
                            reply.setReplyMode(autoReplyRule.getReplyMode());
                            reply.setMatchMode(keywordInfo.getMatchMode());
                            reply.setKeyword(keywordInfo.getContent());
                            String replyType = replyInfo.getType();
                            // The storage value of the image type under conversion is: image
                            if (replyType.equalsIgnoreCase(WechatMessageType.IMG.name())) {
                                replyType = WechatMessageType.IMAGE.name().toLowerCase();
                            }

                            // If the reply type is news, get the newsInfo content, otherwise get
                            // the content content
                            if (replyType.equalsIgnoreCase(WechatMessageType.NEWS.name())) {
                                reply.setNewsInfo(StrUtil.utf8Str(replyInfo.getNewsInfo()));
                            } else {
                                reply.setContent(replyInfo.getContent());
                            }

                            reply.setType(replyType);
                            keywordReplies.add(reply);

                        })
                    );
                });
                keywordReplyMapper.insertBatchWechatKeywordReply(appId, keywordReplies);
            }

        } catch (WxErrorException e) {
            e.printStackTrace();
            throw new BusinessException(UPDATE_AUTO_REPLY_ERROR);
        }
        return ResponseData.success();
    }
}
