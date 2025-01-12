package cn.vika.keycloak.social.provider;

import cn.hutool.core.util.URLUtil;
import cn.vika.keycloak.entity.UserEntity;
import cn.vika.keycloak.provider.JdbcUserStorageProviderFactory;
import cn.vika.keycloak.service.UserService;
import cn.vika.keycloak.service.impl.UserServiceImpl;
import cn.vika.keycloak.social.common.JustAuthKey;
import cn.vika.keycloak.social.common.JustIdentityProviderConfig;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthDefaultRequest;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.utils.UrlBuilder;
import org.jboss.resteasy.spi.HttpRequest;
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

/**
 * 飞书联合登录
 *
 * @author Leo Zhao
 * @date 2021/10/26 14:02
 */

public class FeishuIdentityProvider extends AbstractOAuth2IdentityProvider<JustIdentityProviderConfig> implements SocialIdentityProvider<JustIdentityProviderConfig> {
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
     * 授权地址
     */
    private final static String FEISHU_AUTHORIZE_URL = "https://open.feishu.cn/open-apis/authen/v1/index";

    public static final String OAUTH2_PARAMETER_CLIENT_ID = "app_id";

    public FeishuIdentityProvider(KeycloakSession session, JustIdentityProviderConfig config) {
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

        // 构造飞书授权链接
        String gotoUrl = UrlBuilder.fromBaseUrl(FEISHU_AUTHORIZE_URL)
                .queryParam(OAUTH2_PARAMETER_CLIENT_ID, authConfig.getClientId())
                .queryParam(OAUTH2_PARAMETER_STATE, request.getState().getEncoded())
                .queryParam(OAUTH2_PARAMETER_REDIRECT_URI, URLUtil.encode(authConfig.getRedirectUri()))
                .build();
        return UriBuilder.fromUri(gotoUrl);
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
        @Context
        protected HttpRequest request;

        public Endpoint(AuthenticationCallback callback, RealmModel realm, EventBuilder event) {
            this.callback = callback;
            this.realm = realm;
            this.event = event;
        }

        @GET
        public Response authResponse(@QueryParam(OAUTH2_PARAMETER_STATE) String state,
                                     @QueryParam(OAUTH2_PARAMETER_CODE) String code) {
            try {
                AuthCallback authCallback = AuthCallback.builder().code(code).state(state).build();

                // 没有check 不通过
                AuthRequest authRequest = IdentityProviderUtil.getAuthRequest(authConfig, "http://" + request.getUri().getBaseUri().getHost(), tClass);
                authConfig.setIgnoreCheckState(Boolean.TRUE);
                AuthResponse<AuthUser> response = authRequest.login(authCallback);

                if (response.ok()) {
                    String unionId = response.getData().getUuid();
                    UserEntity userEntity = userService.getSocialUserByUnionId(unionId);

                    if (Objects.isNull(userEntity)) {
                        return Response.status(Response.Status.FOUND).location(URI.create(config.getLoginUrl())).build();
                    }

                    BrokeredIdentityContext user = IdentityProviderUtil.getBrokeredIdentityContext(userEntity, getConfig(), FeishuIdentityProvider.this);

                    AuthenticationSessionModel authSession = IdentityProviderUtil.getAuthenticationSessionModel(state, realm, session, event);
                    if (authSession == null) {
                        return this.errorIdentityProviderLogin(Messages.IDENTITY_PROVIDER_UNEXPECTED_ERROR);
                    }
                    user.setAuthenticationSession(authSession);
                    return this.callback.authenticated(user);
                } else {
                    return this.errorIdentityProviderLogin(Messages.IDENTITY_PROVIDER_UNEXPECTED_ERROR);
                }
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
