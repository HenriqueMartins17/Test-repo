package com.apitable.enterprise.apitablebilling.appsumo.controller;

import static com.apitable.enterprise.apitablebilling.appsumo.core.RedisConstants.getAppsumoUserEmailKey;

import com.apitable.auth.vo.LoginResultVO;
import com.apitable.core.exception.BusinessException;
import com.apitable.core.support.ResponseData;
import com.apitable.enterprise.apitablebilling.appsumo.enums.AppsumoAction;
import com.apitable.enterprise.apitablebilling.appsumo.enums.AppsumoException;
import com.apitable.enterprise.apitablebilling.appsumo.enums.AppsumoHandleStatus;
import com.apitable.enterprise.apitablebilling.appsumo.model.AppsumoEventDTO;
import com.apitable.enterprise.apitablebilling.appsumo.model.AppsumoSignupRO;
import com.apitable.enterprise.apitablebilling.appsumo.model.EventVO;
import com.apitable.enterprise.apitablebilling.appsumo.model.OpsAppsumoSubscriptionRO;
import com.apitable.enterprise.apitablebilling.appsumo.model.TokenVO;
import com.apitable.enterprise.apitablebilling.appsumo.model.UserEmailVO;
import com.apitable.enterprise.apitablebilling.appsumo.service.IAppsumoEventLogService;
import com.apitable.enterprise.apitablebilling.appsumo.service.IAppsumoService;
import com.apitable.enterprise.ops.service.IOpsService;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.GetResource;
import com.apitable.shared.component.scanner.annotation.PostResource;
import com.apitable.shared.context.SessionContext;
import com.auth0.exception.Auth0Exception;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Appsumo callback controller.
 */
@RestController
@Tag(name = "Appsumo")
@ApiResource(path = "/appsumo")
@Slf4j
public class AppsumoController {
    @Resource
    private IOpsService iOpsService;
    @Resource
    private IAppsumoService iAppsumoService;

    @Resource
    private IAppsumoEventLogService iAppsumoEventLogService;

    @Resource
    private RedisTemplate<String, Long> redisTemplate;

    /**
     * generate token for appsumo.
     */
    @PostResource(path = "/callback/token", requiredLogin = false, requiredPermission = false)
    @Operation(summary = "Appsumo generate token")
    @Parameters({
        @Parameter(name = "username", description = "app id", required = true, schema = @Schema(type = "string"), in = ParameterIn.QUERY, example = "test"),
        @Parameter(name = "password", description = "app secret", required = true, schema = @Schema(type = "string"), in = ParameterIn.QUERY, example = "test_secret"),
    })
    public TokenVO generateToken(@RequestParam("username") String username,
                                 @RequestParam("password") String password) {
        String token = iAppsumoService.getAccessToken(username, password);
        log.info(" =============== get appsumo access token =============== ");
        return TokenVO.builder().access(token).build();
    }

    /**
     * appsumo event callback.
     */
    @PostResource(path = "/callback/event", requiredLogin = false, requiredPermission = false)
    @Operation(summary = "Appsumo event callback")
    @Parameters({
        @Parameter(name = HttpHeaders.AUTHORIZATION, description = "access token", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER, example = "Bear ***"),
        @Parameter(name = "action", description = "action:activate,enhance_tier,reduce_tier,refund,update", required = true, schema = @Schema(type = "string"), in = ParameterIn.QUERY, example = "update"),
        @Parameter(name = "action", description = "action:activate,enhance_tier,reduce_tier,refund,update", required = true, schema = @Schema(type = "string"), in = ParameterIn.QUERY, example = "update"),
        @Parameter(name = "plan_id", description = "our plan id", required = true, schema = @Schema(type = "string"), in = ParameterIn.QUERY, example = "our_plan1"),
        @Parameter(name = "uuid", description = "appsumo product key", required = true, schema = @Schema(type = "string"), in = ParameterIn.QUERY, example = "appsumo_plan1"),
        @Parameter(name = "activation_email", description = "user email", required = true, schema = @Schema(type = "string"), in = ParameterIn.QUERY, example = "test@com"),
        @Parameter(name = "invoice_item_uuid", description = "purchase id", schema = @Schema(type = "string"), in = ParameterIn.QUERY, example = "purchase_id"),
    })
    public EventVO handleEvent(@RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
                               @RequestParam("action") String action,
                               @RequestParam("plan_id") String planId,
                               @RequestParam("uuid") String uuid,
                               @RequestParam("activation_email") String activationEmail,
                               @RequestParam(value = "invoice_item_uuid", required = false)
                               String invoiceItemUuid,
                               HttpServletResponse response) {
        boolean isTokenUseful = iAppsumoService.verifyAccessToken(token);
        if (!isTokenUseful) {
            log.error("Appsumo token invalid: {}:{}:{}", action, activationEmail, token);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return EventVO.builder().message(HttpStatus.UNAUTHORIZED.getReasonPhrase()).build();
        }
        Long logId = iAppsumoEventLogService.create(action, planId, uuid, activationEmail,
            invoiceItemUuid);
        EventVO vo;
        try {
            vo = iAppsumoService.handleEvent(logId, AppsumoAction.toEnum(action));
            if (action.equals(AppsumoAction.ACTIVATE.getAction())) {
                response.setStatus(HttpStatus.CREATED.value());
            }
        } catch (BusinessException e) {
            log.error("handle appsumo event error:{}", logId, e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            iAppsumoEventLogService.updateStatus(logId, AppsumoHandleStatus.ERROR);
            vo = EventVO.builder().message(e.getMessage()).build();
        }
        return vo;
    }

    /**
     * get email.
     */
    @GetResource(path = "/email/{state}", requiredLogin = false, requiredPermission = false)
    @Operation(summary = "Get appsumo email")
    @Parameter(name = "state", description = "key", required = true, schema = @Schema(type = "string"), in = ParameterIn.PATH, example = "**")
    public ResponseData<UserEmailVO> getUserEmail(@PathVariable("state") String state) {
        String cacheKey = getAppsumoUserEmailKey(state);
        if (Boolean.FALSE.equals(redisTemplate.hasKey(cacheKey))) {
            throw new BusinessException(AppsumoException.EMAIL_HAS_BIND);
        }
        Long eventId = redisTemplate.opsForValue().get(cacheKey);
        AppsumoEventDTO event = iAppsumoEventLogService.getSimpleInfoById(eventId);
        if (event == null) {
            throw new BusinessException(AppsumoException.EVENT_NOT_FOUND);
        }
        return ResponseData.success(
            UserEmailVO.builder().email(event.getActivationEmail()).build());

    }

    /**
     * signup api and auto login.
     */
    @PostResource(path = "/signup", requiredLogin = false, requiredPermission = false)
    @Operation(summary = "Sign Up", description = "auth0 signup router with auto login")
    public ResponseData<LoginResultVO> signup(@RequestBody @Valid final AppsumoSignupRO data) {
        String cacheKey = getAppsumoUserEmailKey(data.getState());
        if (Boolean.FALSE.equals(redisTemplate.hasKey(cacheKey))) {
            throw new BusinessException(AppsumoException.EMAIL_HAS_BIND);
        }
        try {
            Long eventId = redisTemplate.opsForValue().get(cacheKey);
            Long userId = iAppsumoService.userSignup(eventId, data.getPassword());
            // save session
            SessionContext.setUserId(userId);
            // remove link email cache
            redisTemplate.delete(cacheKey);
            return ResponseData.success(
                LoginResultVO.builder().userId(userId).isNewUser(true).build());
        } catch (Auth0Exception e) {
            log.error("appsumo user signup error", e);
            throw new BusinessException(e.getMessage());
        }
    }

    @PostResource(path = "/ops/subscription", requiredPermission = false, requiredLogin = false)
    @Operation(summary = "handle subscription for appsumo user")
    public ResponseData<Void> executeAppsumoSubscription(@RequestBody OpsAppsumoSubscriptionRO ro) {
        iOpsService.auth(ro.getToken());
        iAppsumoService.manuHandleSubscription(ro.getEventId(), ro.getSpaceId());
        return ResponseData.success();
    }
}
