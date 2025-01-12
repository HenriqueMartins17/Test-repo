package cn.vika.keycloak.social.provider;

import cn.vika.keycloak.entity.UserEntity;
import lombok.extern.jbosslog.JBossLog;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.request.AuthDefaultRequest;
import me.zhyd.oauth.request.AuthRequest;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityProvider;
import org.keycloak.broker.provider.util.IdentityBrokerState;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.ClientModel;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.managers.ClientSessionCode;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.sessions.RootAuthenticationSessionModel;
import org.keycloak.sessions.StickySessionEncoderProvider;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 代理身份提供者工具类
 * </p>
 *
 * @author Leo Zhao
 * @date 2021/11/01 17:39
 */
@JBossLog
public class IdentityProviderUtil {

    /**
     * 获取授权请求处理类
     *
     * @param authConfig
     * @param redirectUri
     * @param tClass
     * @return
     */
    public static AuthRequest getAuthRequest(AuthConfig authConfig, String redirectUri, Class<? extends AuthDefaultRequest> tClass) {
        AuthRequest authRequest = null;
        authConfig.setRedirectUri(redirectUri);
        try {
            Constructor<? extends AuthDefaultRequest> constructor = tClass.getConstructor(AuthConfig.class);
            authRequest = constructor.newInstance(authConfig);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return authRequest;
    }

    /**
     * 获取session
     *
     * @param session
     * @param realm
     * @param state
     * @param authSessionCookies
     * @return
     */
    public static AuthenticationSessionModel getCurrentAuthenticationSession(KeycloakSession session, RealmModel realm, String state, List<String> authSessionCookies) {
        IdentityBrokerState idpState = IdentityBrokerState.encoded(state);
        String tabId = idpState.getTabId();
        String clientId = idpState.getClientId();
        ClientModel client = realm.getClientByClientId(clientId);

        return authSessionCookies.stream().map(oldEncodedId -> {
            StickySessionEncoderProvider encoder = session.getProvider(StickySessionEncoderProvider.class);
            String decodedAuthSessionId = encoder.decodeSessionId(oldEncodedId);
            String reEncoded = encoder.encodeSessionId(decodedAuthSessionId);

            AuthSessionId authSessionId = new AuthSessionId(decodedAuthSessionId, reEncoded);
            String sessionId = authSessionId.getDecodedId();

            RootAuthenticationSessionModel rootAuthSession = session.authenticationSessions().getRootAuthenticationSession(realm, sessionId);
            AuthenticationSessionModel authSession = rootAuthSession == null ? null : rootAuthSession.getAuthenticationSession(client, tabId);

            if (authSession != null) {
                return authSession;
            }

            return null;
        }).filter(authSession -> Objects.nonNull(authSession)).findFirst().orElse(null);
    }

    /**
     * 获取授权session对象
     *
     * @param state
     * @return
     */
    public static AuthenticationSessionModel getAuthenticationSessionModel(String state, RealmModel realm, KeycloakSession session, EventBuilder event) {
        IdentityBrokerState idpState = IdentityBrokerState.encoded(state);
        String clientId = idpState.getClientId();
        String tabId = idpState.getTabId();
        if (clientId == null || tabId == null) {
            log.errorf("Invalid state parameter: %s", state);
            return null;
        }

        ClientModel client = realm.getClientByClientId(clientId);
        return ClientSessionCode.getClientSession(state, tabId, session, realm, client, event, AuthenticationSessionModel.class);
    }

    /**
     * 获取代理身份上下文
     *
     * @param userEntity
     * @param idpConfig
     * @param idp
     * @return
     */
    public static BrokeredIdentityContext getBrokeredIdentityContext(UserEntity userEntity, IdentityProviderModel idpConfig, IdentityProvider idp) {
        BrokeredIdentityContext identityContext = new BrokeredIdentityContext(String.valueOf(userEntity.getId()));
        identityContext.setUsername(userEntity.getMobilePhone());
        identityContext.setBrokerUserId(String.valueOf(userEntity.getId()));
        identityContext.setEmail(userEntity.getEmail());
        identityContext.setFirstName(userEntity.getNickName());
        identityContext.setLastName(userEntity.getNickName());
        identityContext.setIdpConfig(idpConfig);
        identityContext.setIdp(idp);
        return identityContext;
    }

   static class AuthSessionId {

        // Decoded ID of authenticationSession WITHOUT route attached (EG. "5e161e00-d426-4ea6-98e9-52eb9844e2d7")
        private final String decodedId;

        // Encoded ID of authenticationSession WITH route attached (EG. "5e161e00-d426-4ea6-98e9-52eb9844e2d7.node1")
        private final String encodedId;

        AuthSessionId(String decodedId, String encodedId) {
            this.decodedId = decodedId;
            this.encodedId = encodedId;
        }


        public String getDecodedId() {
            return decodedId;
        }

        public String getEncodedId() {
            return encodedId;
        }
    }
}
