package cn.vika.keycloak.social.provider;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.useragent.Browser;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import cn.hutool.json.JSONUtil;
import cn.vika.keycloak.entity.UserEntity;
import cn.vika.keycloak.provider.JdbcUserStorageProviderFactory;
import cn.vika.keycloak.service.UserService;
import cn.vika.keycloak.service.impl.UserServiceImpl;
import cn.vika.keycloak.social.common.*;
import cn.vika.keycloak.social.service.WechatOpenService;
import cn.vika.keycloak.social.service.impl.WechatOpenServiceImpl;
import cn.vika.keycloak.social.util.RandomExtendUtil;
import cn.vika.keycloak.social.util.VikaResponse;
import cn.vika.keycloak.social.util.WxMpXStreamUtil;
import cn.vika.keycloak.social.util.WxOpenXStreamUtil;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.thoughtworks.xstream.XStream;
import lombok.extern.jbosslog.JBossLog;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import me.chanjar.weixin.open.bean.message.WxOpenXmlMessage;
import me.chanjar.weixin.open.bean.result.WxOpenQueryAuthResult;
import me.chanjar.weixin.open.util.WxOpenCryptUtil;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.exception.AuthException;
import me.zhyd.oauth.model.AuthToken;
import me.zhyd.oauth.request.AuthDefaultRequest;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.utils.HttpUtils;
import me.zhyd.oauth.utils.UrlBuilder;
import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.jboss.resteasy.spi.HttpRequest;
import org.keycloak.OAuth2Constants;
import org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider;
import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;
import org.keycloak.broker.provider.AuthenticationRequest;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.common.ClientConnection;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.ErrorPage;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.AuthenticationSessionModel;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.apache.http.protocol.HTTP.USER_AGENT;

/**
 * @author Leo Zhao
 * @date 2021/10/09 00:02
 */

@JBossLog
public class WechatOpenIdentityProvider extends AbstractOAuth2IdentityProvider<JustIdentityProviderConfig> implements SocialIdentityProvider<JustIdentityProviderConfig> {
    /**
     * 发起授权时默认请求作用域
     */
    private final String defaultScopes = "default";
    /**
     * 微信配置类
     */
    private final AuthConfig authConfig;
    /**
     * 扩展配置类
     */
    private final JustIdentityProviderConfig config;
    /**
     * request处理类
     */
    private final Class<? extends AuthDefaultRequest> tClass;
    /**
     * 微信开发平台服务
     */
    private WechatOpenService wechatOpenService;
    /**
     * 用户服务
     */
    private UserService userService;
    /**
     * 日期格式化
     */
    private final String TIME_SIMPLE_PATTERN = "yyyy-MM-dd HH:mm:ss";
    /**
     * vika 客户端标志
     */
    private final String VIKA_DESKTOP = "VikaDesktop";
    /**
     * 微信用户信息链接
     */
    private final String WECHAT_USER_INFO_URL = "https://api.weixin.qq.com/cgi-bin/user/info?access_token=%s&openid=%s&lang=zh_CN";
    /**
     * 微信access_token链接
     */
    private final String WECHAT_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/component/access_token";
    /**
     * 微信authorize链接
     */
    private final String WECHAT_AUTHORIZE_URL = "https://open.weixin.qq.com/connect/oauth2/authorize";
    /**
     * 微信二维码链接
     */
    private final String WECHAT_QRCODE_URL = "https://mp.weixin.qq.com/cgi-bin/showqrcode";

    public static final String OAUTH2_PARAMETER_CLIENT_ID = "appid";
    public static final String OAUTH2_PARAMETER_COMPONENT_APP_ID = "component_appid";
    public static final String OAUTH2_PARAMETER_COMPONENT_ACCESS_TOKEN = "component_access_token";
    public static final String OAUTH2_PARAMETER_SNS_API_USER_INFO = "snsapi_userinfo";

    public WechatOpenIdentityProvider(KeycloakSession session, JustIdentityProviderConfig config) {
        super(session, config);
        JustAuthKey justAuthKey = config.getJustAuthKey();
        this.authConfig = JustAuthKey.getAuthConfig(config);
        this.tClass = justAuthKey.getTClass();
        this.config = config;

        this.wechatOpenService = new WechatOpenServiceImpl(config);
        try {
            this.userService = new UserServiceImpl(JdbcUserStorageProviderFactory.getEntityManagerFactory(session));
        } catch (Exception e) {
            logger.error(e);
        }
    }

    @Override
    protected UriBuilder createAuthorizationUrl(AuthenticationRequest request) {
        String redirectUri = request.getRedirectUri();
        AuthRequest authRequest = IdentityProviderUtil.getAuthRequest(authConfig, redirectUri, tClass);
        String uri = authRequest.authorize(request.getState().getEncoded());

        String ua = request.getHttpRequest().getHttpHeaders().getHeaderString(USER_AGENT).toLowerCase();
        if (!isWechatBrowser(ua)) {
            // 微信扫码
            String mark = RandomExtendUtil.randomString(12);
            String qrUrl = getQrCodeUrl(mark);
            uri = qrUrl + "&" + uri.substring(uri.indexOf("?") + 1);

            ClientOriginInfo info = new ClientOriginInfo();
            info.setUserAgent(ua);
            info.setIp(session.getContext().getConnection().getRemoteHost());
            wechatOpenService.cacheScanInfo(info, mark);
        } else {
            // 微信应用内
            uri = UrlBuilder.fromBaseUrl(WECHAT_AUTHORIZE_URL)
                    .queryParam(OAUTH2_PARAMETER_CLIENT_ID, config.getAuthAppId())
                    .queryParam(OAUTH2_PARAMETER_REDIRECT_URI, authConfig.getRedirectUri())
                    .queryParam(OAUTH2_PARAMETER_RESPONSE_TYPE, OAUTH2_PARAMETER_CODE)
                    .queryParam(OAUTH2_PARAMETER_SCOPE, OAUTH2_PARAMETER_SNS_API_USER_INFO)
                    .queryParam(OAUTH2_PARAMETER_STATE, request.getState().getEncoded())
                    .queryParam(OAUTH2_PARAMETER_COMPONENT_APP_ID, authConfig.getClientId())
                    .build();
        }

        return UriBuilder.fromUri(uri);
    }

    private String getQrCodeUrl(String mark) {
        // 生成随机字符串作为二维码的唯一标识
        if (StringUtils.isBlank(mark)) {
            mark = RandomExtendUtil.randomString(12);
        }
        // 生成二维码
        WxMpQrCodeTicket qrCodeTicket = wechatOpenService.getWxMpQrCodeTicket(mark, config.getAuthAppId());
        String qrUrl = UrlBuilder.fromBaseUrl(WECHAT_QRCODE_URL)
                .queryParam("ticket", qrCodeTicket.getTicket())
                .queryParam("mark", mark)
                .build();
        return qrUrl;
    }

    @Override
    public Response performLogin(AuthenticationRequest request) {
        String ua = request.getHttpRequest().getHttpHeaders().getHeaderString(USER_AGENT).toLowerCase();
        if (!isWechatBrowser(ua)) {
            URI authorizationUrl = createAuthorizationUrl(request).build();
            return Response.ok().entity(authorizationUrl.toString()).build();
        } else {
            return super.performLogin(request);
        }
    }

    /**
     * 判断是否在微信浏览器里面请求
     *
     * @param ua 浏览器user-agent
     * @return
     */
    private boolean isWechatBrowser(String ua) {
        if (ua.indexOf("micromessenger") > 0) {
            return true;
        }
        return false;
    }

    protected BrokeredIdentityContext extractIdentityFromProfile(EventBuilder event, JsonNode profile) {
        String unionId = getJsonProperty(profile, "unionid");
        if (Objects.isNull(userService)) {
            this.userService = new UserServiceImpl(JdbcUserStorageProviderFactory.getEntityManagerFactory(session));
        }
        UserEntity userEntity = userService.getUserByUnionId(unionId);

        if (Objects.isNull(userEntity)) {
            return null;
        }

        BrokeredIdentityContext user = IdentityProviderUtil.getBrokeredIdentityContext(userEntity, getConfig(), this);
        AbstractJsonUserAttributeMapper.storeUserProfileForMapper(user, profile, getConfig().getAlias());
        return user;
    }

    @Override
    protected String getDefaultScopes() {
        return defaultScopes;
    }

    @Override
    public Object callback(RealmModel realm, AuthenticationCallback callback, EventBuilder event) {
        return new Endpoint(callback, realm, event);
    }

    protected class Endpoint {
        protected AuthenticationCallback callback;
        protected RealmModel realm;
        protected EventBuilder event;
        @Context
        protected KeycloakSession session;
        @Context
        protected ClientConnection clientConnection;
        @Context
        protected HttpHeaders headers;
        @Context
        protected HttpRequest request;

        public Endpoint(AuthenticationCallback callback, RealmModel realm, EventBuilder event) {
            this.callback = callback;
            this.realm = realm;
            this.event = event;
        }

        @GET
        public Response authResponse(@QueryParam("state") String state,
                                     @QueryParam("code") String authorizationCode,
                                     @QueryParam("error") String error,
                                     @QueryParam(OAuth2Constants.SCOPE_OPENID) String openid) {
            try {
                if (StringUtils.isBlank(openid)) {
                    AuthToken authToken = this.getToken(accessTokenUrl(authorizationCode));
                    openid = authToken.getOpenId();
                }
                BrokeredIdentityContext federatedIdentity = authByOpenId(openid);
                if (Objects.isNull(federatedIdentity)) {
                    return Response.status(302).location(URI.create(config.getLoginUrl())).build();
                }
                AuthenticationSessionModel authSession = IdentityProviderUtil.getAuthenticationSessionModel(state, realm, session, event);
                if (authSession == null) {
                    return this.errorIdentityProviderLogin(Messages.IDENTITY_PROVIDER_UNEXPECTED_ERROR);
                }
                federatedIdentity.setAuthenticationSession(authSession);
                return this.callback.authenticated(federatedIdentity);
            } catch (Exception e) {
                logger.error(e);
                return this.errorIdentityProviderLogin(Messages.IDENTITY_PROVIDER_UNEXPECTED_ERROR);
            }
        }

        /**
         * 生成访问令牌链接
         *
         * @param code
         * @return
         * @throws WxErrorException
         */
        private String accessTokenUrl(String code) throws WxErrorException {
            return UrlBuilder.fromBaseUrl(WECHAT_ACCESS_TOKEN_URL)
                    .queryParam(OAUTH2_PARAMETER_CLIENT_ID, config.getAuthAppId())
                    .queryParam(OAUTH2_PARAMETER_CODE, code)
                    .queryParam(OAUTH2_PARAMETER_GRANT_TYPE, OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE)
                    .queryParam(OAUTH2_PARAMETER_COMPONENT_APP_ID, authConfig.getClientId())
                    .queryParam(OAUTH2_PARAMETER_COMPONENT_ACCESS_TOKEN, wechatOpenService.getWxOpenService().getWxOpenComponentService().getComponentAccessToken(false))
                    .build();
        }

        /**
         * 获取访问令牌
         *
         * @param accessTokenUrl
         * @return
         */
        private AuthToken getToken(String accessTokenUrl) {
            String response = (new HttpUtils(authConfig.getHttpConfig())).get(accessTokenUrl).getBody();
            JSONObject accessTokenObject = JSONObject.parseObject(response);
            if (accessTokenObject.containsKey("errcode")) {
                throw new AuthException(accessTokenObject.getIntValue("errcode"), accessTokenObject.getString("errmsg"));
            }
            return AuthToken.builder()
                    .accessToken(accessTokenObject.getString("access_token"))
                    .refreshToken(accessTokenObject.getString("refresh_token"))
                    .expireIn(accessTokenObject.getIntValue("expires_in"))
                    .openId(accessTokenObject.getString("openid"))
                    .build();
        }

        /**
         * 获取验证票据，授权事件接收URL
         *
         * @return
         */
        @POST
        @Path("/receiveTicket")
        @NoCache
        public Response getComponentVerifyTicket(String requestBody,
                                                 @QueryParam("timestamp") String timestamp,
                                                 @QueryParam("nonce") String nonce,
                                                 @QueryParam("signature") String signature,
                                                 @QueryParam("encrypt_type") String encType,
                                                 @QueryParam("msg_signature") String msgSignature) {
            log.infov("RecieveTicket接口接收微信请求：[signature=[{0}], encType=[{1}], msgSignature=[{2}],"
                            + " timestamp=[{3}], nonce=[{4}], requestBody=[\n{5}\n]",
                    signature, encType, msgSignature, timestamp, nonce, requestBody);

            if (!StrUtil.equalsIgnoreCase("aes", encType)) {
                throw new RuntimeException("非法请求，可能属于伪造的请求");
            }
            if (!wechatOpenService.getWxOpenService().getWxOpenComponentService().checkSignature(timestamp, nonce, signature)) {
                throw new RuntimeException("非法请求，可能属于伪造的请求");
            }

            XStream xs = new XStream();
            WxOpenXStreamUtil.setXStreamAlias(xs);
            WxOpenCryptUtil cryptUtil = new WxOpenCryptUtil(wechatOpenService.getWxOpenService().getWxOpenConfigStorage());
            String plainText = cryptUtil.decrypt(msgSignature, timestamp, nonce, requestBody);
            WxOpenXmlMessage inMessage = (WxOpenXmlMessage) xs.fromXML(plainText);
            log.infov("\n消息解密后内容为：\n{0} ", inMessage.toString());
            String out = "success";
            try {
                //获取ticket放入微信开放平台配置WxOpenConfigStorage中
                out = wechatOpenService.getWxOpenService().getWxOpenComponentService().route(inMessage);
            } catch (WxErrorException e) {
                log.error("receive_ticket", e);
            }

            log.infov("receiveTicket组装回复信息：{0}", out);

            return Response.ok().entity(out).build();
        }

        /**
         * 获取微信二维码链接
         *
         * @return
         */
        @GET
        @Path("/qrcode")
        @NoCache
        public Response getQrCode(@QueryParam("mark") String mark, @Context HttpServletRequest servletRequest) {
            String qrCodeUrl = getQrCodeUrl(mark);
            ClientOriginInfo info = new ClientOriginInfo();
            info.setUserAgent(servletRequest.getHeader(USER_AGENT));
            info.setIp(session.getContext().getConnection().getRemoteHost());
            wechatOpenService.cacheScanInfo(info, mark);
            return Response.ok().entity(qrCodeUrl).type(MediaType.TEXT_PLAIN).build();
        }

        /**
         * 创建预授权链接
         *
         * @return
         */
        @GET
        @Path("/createPreAuthUrl")
        @NoCache
        public Response createPreAuthUrl(@QueryParam("authType") String authType,
                                         @QueryParam("appId") String appId,
                                         @QueryParam("realm") String realm) throws WxErrorException {
            String preAuthUrl = wechatOpenService.getWxOpenService().getWxOpenComponentService().getPreAuthUrl("http://" + request.getUri().getBaseUri().getHost() + "/auth/realms/" + realm + "/broker/wechat_open/endpoint/getQueryAuth", authType, appId);
            String authPage = "<a referrerpolicy=\"origin\" href=\"" + preAuthUrl + "\">立即授权</a>";
            return Response.ok().entity(authPage).type(MediaType.TEXT_HTML).build();
        }

        /**
         * 获取授权码获取授权信息
         *
         * @return
         */
        @GET
        @Path("/getQueryAuth")
        @NoCache
        @Produces(MediaType.APPLICATION_JSON)
        public Response getQueryAuth(@QueryParam("auth_code") String authCode) throws WxErrorException {
            log.infov("getQueryAuth, authCode={0}", authCode);
            WxOpenQueryAuthResult result = wechatOpenService.getWxOpenService().getWxOpenComponentService().getQueryAuth(authCode);
            log.infov("获取微信授权信息：{0}", result);
            return Response.ok().entity(JSONUtil.toJsonStr(result.getAuthorizationInfo())).build();
        }

        /**
         * 微信回调事件
         *
         * @param requestBody
         * @param appId
         * @param signature
         * @param timestamp
         * @param nonce
         * @param openid
         * @param encType
         * @param msgSignature
         * @return
         */
        @POST
        @Path("/callback/{appId}")
        public Response callback(String requestBody,
                                 @PathParam("appId") String appId,
                                 @QueryParam("signature") String signature,
                                 @QueryParam("timestamp") String timestamp,
                                 @QueryParam("nonce") String nonce,
                                 @QueryParam("openid") String openid,
                                 @QueryParam("encrypt_type") String encType,
                                 @QueryParam("msg_signature") String msgSignature) {
            log.infov("\n接收微信回调消息请求：[appId=[{0}], openid=[{1}], signature=[{2}], encType=[{3}], msgSignature=[{4}],"
                            + " timestamp=[{5}], nonce=[{6}], requestBody=[\n{7}\n]",
                    appId, openid, signature, encType, msgSignature, timestamp, nonce, requestBody);

            if (!StrUtil.equalsIgnoreCase("aes", encType)) {
                throw new RuntimeException("非法请求，可能属于伪造的请求");
            }
            if (!wechatOpenService.getWxOpenService().getWxOpenComponentService().checkSignature(timestamp, nonce, signature)) {
                throw new RuntimeException("非法请求，可能属于伪造的请求");
            }

            XStream xs = new XStream();
            WxMpXStreamUtil.setXStreamAlias(xs);
            WxOpenCryptUtil cryptUtil = new WxOpenCryptUtil(wechatOpenService.getWxOpenService().getWxOpenConfigStorage());
            String plainText = cryptUtil.decrypt(msgSignature, timestamp, nonce, requestBody);
            WxMpXmlMessage inMessage = (WxMpXmlMessage) xs.fromXML(plainText);
            log.infov("\n消息解密后内容为：\n{0} ", inMessage.toString());

            if (!appId.equals(config.getAuthAppId())) {
                return Response.ok().entity("success").build();
            }

            String out = null;
            if (StringUtils.equalsIgnoreCase(inMessage.getMsgType(), WechatMessageType.EVENT.name())) {
                String eventKey = inMessage.getEventKey();
                if (StringUtils.isNotBlank(eventKey) && eventKey.contains(WechatOpenServiceImpl.MARK_PRE)) {
                    if (StringUtils.equalsIgnoreCase(inMessage.getEvent(), WechatEventType.SUBSCRIBE.name())) {
                        eventKey = eventKey.substring(WechatOpenServiceImpl.QR_SCENE_PRE.length());
                    }
                    String mark = eventKey.substring(WechatOpenServiceImpl.MARK_PRE.length());
                    wechatOpenService.cacheScanMark(mark, openid);
                    log.infov("cache wechat mark {0}", mark);
                    out = qrCodeReply(inMessage, mark);
                }
            }
            log.infov("回复消息内容：{0}", out);
            out = StrUtil.isNotBlank(out) ? new WxOpenCryptUtil(wechatOpenService.getWxOpenService().getWxOpenComponentService().getWxOpenConfigStorage()).encrypt(out) : "success";
            return Response.ok().entity(out).build();
        }

        /**
         * 生成微信扫码模板消息
         *
         * @param inMessage
         * @param mark
         * @return
         */
        private String qrCodeReply(WxMpXmlMessage inMessage, String mark) {
            try {
                ClientOriginInfo info = wechatOpenService.getScanInfo(mark);
                if (Objects.isNull(info) || StringUtils.isBlank(info.getIp())) {
                    return null;
                }
                // 发送公众号模板消息
                List<WxMpTemplateData> data = new ArrayList<>();
                data.add(new WxMpTemplateData("first", "扫码成功"));
                data.add(new WxMpTemplateData("keyword1", LocalDateTime.now(ZoneId.of("Asia/Shanghai")).format(DateTimeFormatter.ofPattern(TIME_SIMPLE_PATTERN))));
                data.add(new WxMpTemplateData("keyword2", "微信扫码"));
                data.add(new WxMpTemplateData("keyword3", info.getIp()));
                String desktop = getVikaDesktop(info.getUserAgent(), true);
                data.add(new WxMpTemplateData("keyword4", desktop));
                data.add(new WxMpTemplateData("remark", config.getMessageRemark()));
                WxMpTemplateMessage msg = WxMpTemplateMessage.builder()
                        .toUser(inMessage.getFromUser())
                        .templateId(config.getMessageTemplateId())
                        .data(data)
                        .build();
                WxMpService wxMpService = wechatOpenService.getWxOpenService().getWxOpenComponentService().getWxMpServiceByAppid(config.getAuthAppId());

                wxMpService.getTemplateMsgService().sendTemplateMsg(msg);
            } catch (WxErrorException e) {
                log.infov("模板消息发送失败, Message:{0}", e.getMessage());
            }
            return null;
        }

        /**
         * 获取 Vika 客户端信息
         */
        public String getVikaDesktop(String userAgent, boolean browser) {
            if (StrUtil.isBlank(userAgent)) {
                return null;
            }
            UserAgent ua = UserAgentUtil.parse(userAgent);
            // 优先返回客户端信息
            if (StrUtil.containsIgnoreCase(userAgent, VIKA_DESKTOP)) {
                int start = StrUtil.indexOfIgnoreCase(userAgent, VIKA_DESKTOP);
                return StrUtil.subBefore(userAgent.substring(start), ' ', false) +
                        StrUtil.format(" ({})", ua.getPlatform());
            }
            // 否则返回平台类型
            StringBuilder platform = new StringBuilder(ua.getPlatform().toString());
            if (browser && !ua.getBrowser().equals(Browser.Unknown)) {
                platform.append(" ").append(ua.getBrowser().toString());
            }
            return platform.toString();
        }

        /**
         * 扫码结果轮询
         *
         * @return
         */
        @POST
        @Path("/poll")
        @NoCache
        public Response checkScanResult(@QueryParam("mark") String mark) {
            VikaResponse<String> response = new VikaResponse();
            String openid = wechatOpenService.getScanMarkCache(mark);
            if (StringUtils.isBlank(openid)) {
                response.setCode(HttpURLConnection.HTTP_NOT_FOUND);
                response.setMessage("未查询到扫码记录");
            } else {
                response.setCode(HttpURLConnection.HTTP_OK);
                response.setMessage("扫码成功");
                response.setData(openid);
                wechatOpenService.deleteScanMarkCache(mark);
            }
            return Response.status(Response.Status.OK).entity(response).build();
        }

        private Response errorIdentityProviderLogin(String message) {
            this.event.event(EventType.LOGIN);
            this.event.error(Errors.IDENTITY_PROVIDER_LOGIN_FAILURE);
            return ErrorPage.error(this.session, null, Response.Status.BAD_GATEWAY, message);
        }

        /**
         * 根据openId获取用户信息
         *
         * @param openid 微信开放id
         * @return
         * @throws IOException
         */
        public BrokeredIdentityContext authByOpenId(String openid) throws IOException {
            String accessToken = wechatOpenService.getWxOpenService().getWxOpenConfigStorage().getAuthorizerAccessToken(config.getAuthAppId());
            JsonNode profile = SimpleHttp.doGet(String.format(WECHAT_USER_INFO_URL, accessToken, openid), this.session).asJson();

            BrokeredIdentityContext context = extractIdentityFromProfile(null, profile);
            if (Objects.nonNull(context)) {
                context.getContextData().put(FEDERATED_ACCESS_TOKEN, accessToken);
            }
            return context;
        }
    }
}
