package cn.vika.keycloak.social.provider;

import cn.vika.keycloak.social.common.JustAuthKey;
import cn.vika.keycloak.social.common.JustIdentityProviderConfig;
import com.google.auto.service.AutoService;
import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.broker.social.SocialIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;

/**
 * QQ联合登录
 *
 * @author Leo Zhao
 * @date 2021/10/26 14:02
 */

@AutoService(SocialIdentityProviderFactory.class)
public class QqIdentityProviderFactory extends
        AbstractIdentityProviderFactory<QqIdentityProvider>
        implements SocialIdentityProviderFactory<QqIdentityProvider> {

    public static final JustAuthKey JUST_AUTH_KEY = JustAuthKey.QQ;

    @Override
    public String getName() {
        return JUST_AUTH_KEY.getName();
    }

    @Override
    public QqIdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
        return new QqIdentityProvider(session, new JustIdentityProviderConfig(model, JUST_AUTH_KEY));
    }

    @Override
    public OAuth2IdentityProviderConfig createConfig() {
        return new OAuth2IdentityProviderConfig();
    }

    @Override
    public String getId() {
        return JUST_AUTH_KEY.getId();
    }
}
