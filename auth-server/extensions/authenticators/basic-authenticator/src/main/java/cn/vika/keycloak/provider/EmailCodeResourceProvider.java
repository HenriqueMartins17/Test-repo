package cn.vika.keycloak.provider;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

/**
 * <p>
 * 邮箱rest接口提供者
 * </p>
 *
 * @author Leo Zhao
 * @date 2021/08/30 11:34
 */
public class EmailCodeResourceProvider implements RealmResourceProvider {

    private final KeycloakSession session;

    public EmailCodeResourceProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public Object getResource() {
        return new EmailCodeResource(session);
    }

    @Override
    public void close() {
    }

}
