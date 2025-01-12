package cn.vika.keycloak.provider;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

/**
 * <p>
 * 短信rest接口提供者
 * </p>
 *
 * @author Leo Zhao
 * @date 2021/08/30 11:34
 */
public class SmsCodeResourceProvider implements RealmResourceProvider {

    private final KeycloakSession session;

    public SmsCodeResourceProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public Object getResource() {
        return new SmsCodeResource(session);
    }

    @Override
    public void close() {
    }

}
