package cn.vika.keycloak.social.provider;

import cn.hutool.core.util.URLUtil;
import cn.vika.keycloak.entity.UserEntity;
import cn.vika.keycloak.provider.JdbcUserStorageProviderFactory;
import cn.vika.keycloak.service.UserService;
import cn.vika.keycloak.service.impl.UserServiceImpl;
import cn.vika.keycloak.social.common.JustAuthKey;
import cn.vika.keycloak.social.common.JustIdentityProviderConfig;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.request.OapiSnsGetuserinfoBycodeRequest;
import com.dingtalk.api.response.OapiSnsGetuserinfoBycodeResponse;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.request.AuthDefaultRequest;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.utils.UrlBuilder;
import org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider;
import org.keycloak.broker.provider.AuthenticationRequest;
import org.keycloak.broker.provider.BrokeredIdentityContext;
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

import javax.ws.rs.GET;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Objects;

import static org.apache.http.protocol.HTTP.USER_AGENT;

/**
 * 钉钉联合登录
 *
 * @author Leo Zhao
 * @date 2021/10/26 14:02
 */

public class DingTalkIdentityProvider extends AbstractOAuth2IdentityProvider<JustIdentityProviderConfig> implements SocialIdentityProvider<JustIdentityProviderConfig> {
    /**
     * 发起授权时默认请求作用域
     */
    public final String defaultScopes = "default";
    /**
     * 钉钉配置类
     */
    public final AuthConfig authConfig;
    /**
     * request处理类
     */
    public final Class<? extends AuthDefaultRequest> tClass;
    /**
     * 用户服务类
     */
    private final UserService userService;
    /**
     * 扩展配置类
     */
    private final JustIdentityProviderConfig config;
    /**
     * 用户信息地址
     */
    private final static String DING_TALK_USER_INFO_URL = "https://oapi.dingtalk.com/sns/getuserinfo_bycode";
    /**
     * 授权地址
     */
    private final static String DING_TALK_AUTHORIZE_URL = "https://oapi.dingtalk.com/connect/oauth2/sns_authorize";
    /**
     * 钉钉浏览器标识
     */
    private final static String DING_TALK_USER_AGENT = "dingtalk";

    public static final String OAUTH2_PARAMETER_CLIENT_ID = "appid";
    public static final String OAUTH2_PARAMETER_SCOPE_LOGIN = "snsapi_login";
    public static final String OAUTH2_PARAMETER_SCOPE_AUTH = "snsapi_auth";

    public DingTalkIdentityProvider(KeycloakSession session, JustIdentityProviderConfig config) {
        super(session, config);
        JustAuthKey justAuthKey = config.getJustAuthKey();
        this.authConfig = JustAuthKey.getAuthConfig(config);
        this.tClass = justAuthKey.getTClass();
        this.userService = new UserServiceImpl(JdbcUserStorageProviderFactory.getEntityManagerFactory(session));
        this.config = config;
    }

    /**
     * 创建授权链接
     *
     * @param request
     * @return
     */
    @Override
    protected UriBuilder createAuthorizationUrl(AuthenticationRequest request) {
        // 初始化request处理类
        String redirectUri = request.getRedirectUri();
        AuthRequest authRequest = IdentityProviderUtil.getAuthRequest(authConfig, redirectUri, tClass);
        authRequest.authorize(request.getState().getEncoded());

        // 默认扫码授权登录
        String scope = OAUTH2_PARAMETER_SCOPE_LOGIN;
        String ua = request.getHttpRequest().getHttpHeaders().getHeaderString(USER_AGENT).toLowerCase();
        if (isDingTalkBrowser(ua)) {
            // 钉钉应用内一键登录
            scope = OAUTH2_PARAMETER_SCOPE_AUTH;
        }

        // 构造钉钉授权链接
        String gotoUrl = UrlBuilder.fromBaseUrl(DING_TALK_AUTHORIZE_URL)
                .queryParam(OAUTH2_PARAMETER_CLIENT_ID, authConfig.getClientId())
                .queryParam(OAUTH2_PARAMETER_RESPONSE_TYPE, OAUTH2_PARAMETER_CODE)
                .queryParam(OAUTH2_PARAMETER_SCOPE, scope)
                .queryParam(OAUTH2_PARAMETER_STATE, request.getState().getEncoded())
                .queryParam(OAUTH2_PARAMETER_REDIRECT_URI, URLUtil.encode(authConfig.getRedirectUri()))
                .build();
        return UriBuilder.fromUri(gotoUrl);
    }

    /**
     * 执行登录
     *
     * @param request
     * @return
     */
    @Override
    public Response performLogin(AuthenticationRequest request) {
        String ua = request.getHttpRequest().getHttpHeaders().getHeaderString(USER_AGENT).toLowerCase();
        if (!isDingTalkBrowser(ua)) {
            // 扫码登录返回JSON数据
            URI authorizationUrl = createAuthorizationUrl(request).build();
            return Response.ok().entity(authorizationUrl.toString()).build();
        } else {
            return super.performLogin(request);
        }
    }

    /**
     * 判断是否在钉钉浏览器里面请求
     *
     * @param ua 浏览器user-agent
     * @return
     */
    private boolean isDingTalkBrowser(String ua) {
        if (ua.indexOf(DING_TALK_USER_AGENT) > 0) {
            return true;
        }
        return false;
    }

    @Override
    protected String getDefaultScopes() {
        return defaultScopes;
    }

    @Override
    public Object callback(RealmModel realm, AuthenticationCallback callback, EventBuilder event) {
        return new Endpoint(callback, realm, event);
    }

    /**
     * 回调处理类
     */
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

        public Endpoint(AuthenticationCallback callback, RealmModel realm, EventBuilder event) {
            this.callback = callback;
            this.realm = realm;
            this.event = event;
        }

        @GET
        public Response authResponse(@QueryParam(OAUTH2_PARAMETER_STATE) String state,
                                     @QueryParam(OAUTH2_PARAMETER_CODE) String code) {
            try {
                DefaultDingTalkClient client = new DefaultDingTalkClient(DING_TALK_USER_INFO_URL);
                OapiSnsGetuserinfoBycodeRequest request = new OapiSnsGetuserinfoBycodeRequest();
                request.setTmpAuthCode(code);
                OapiSnsGetuserinfoBycodeResponse response = client.execute(request, authConfig.getClientId(), authConfig.getClientSecret());

                String unionId = response.getUserInfo().getUnionid();
                UserEntity userEntity = userService.getUserByUnionId(unionId);

                if (Objects.isNull(userEntity)) {
                    return Response.status(302).location(URI.create(config.getLoginUrl())).build();
                }

                BrokeredIdentityContext user = IdentityProviderUtil.getBrokeredIdentityContext(userEntity, getConfig(), DingTalkIdentityProvider.this);

                AuthenticationSessionModel authSession = IdentityProviderUtil.getAuthenticationSessionModel(state, realm, session, event);
                if (authSession == null) {
                    return this.errorIdentityProviderLogin(Messages.IDENTITY_PROVIDER_UNEXPECTED_ERROR);
                }
                user.setAuthenticationSession(authSession);
                return this.callback.authenticated(user);
            } catch (Exception e) {
                logger.error(e);
                return this.errorIdentityProviderLogin(Messages.IDENTITY_PROVIDER_UNEXPECTED_ERROR);
            }
        }

        private Response errorIdentityProviderLogin(String message) {
            this.event.event(EventType.LOGIN);
            this.event.error(Errors.IDENTITY_PROVIDER_LOGIN_FAILURE);
            return ErrorPage.error(this.session, null, Response.Status.BAD_GATEWAY, message);
        }
    }
}
