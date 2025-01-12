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
 * 飞书联合登录
 *
 * @author Leo Zhao
 * @date 2021/10/26 14:02
 */

@AutoService(SocialIdentityProviderFactory.class)
public class FeishuIdentityProviderFactory extends
        AbstractIdentityProviderFactory<FeishuIdentityProvider>
        implements SocialIdentityProviderFactory<FeishuIdentityProvider> {

    public static final JustAuthKey JUST_AUTH_KEY = JustAuthKey.FEISHU;

    @Override
    public String getName() {
        return JUST_AUTH_KEY.getName();
    }

    @Override
    public FeishuIdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
        return new FeishuIdentityProvider(session, new JustIdentityProviderConfig(model, JUST_AUTH_KEY));
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
