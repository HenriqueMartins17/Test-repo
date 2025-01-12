package cn.vika.keycloak.provider;

import com.google.auto.service.AutoService;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

/**
 * <p>
 * 邮箱rest接口工厂
 * </p>
 *
 * @author Leo Zhao
 * @date 2021/08/30 11:34
 */
@AutoService(RealmResourceProviderFactory.class)
public class EmailCodeResourceProviderFactory implements RealmResourceProviderFactory {

    /**
     * 资源ID，也是rest接口url的一部分，https://localhost/auth/realms/$realm/mail
     */
    public static final String ID = "mail";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public RealmResourceProvider create(KeycloakSession session) {
        return new EmailCodeResourceProvider(session);
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }
}
