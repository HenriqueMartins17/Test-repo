package cn.vika.keycloak.provider;

import com.google.auto.service.AutoService;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

/**
 * <p>
 * 短信rest接口工厂
 * </p>
 *
 * @author Leo Zhao
 * @date 2021/08/30 11:34
 */
@AutoService(RealmResourceProviderFactory.class)
public class SmsCodeResourceProviderFactory implements RealmResourceProviderFactory {

    /**
     * 资源ID，也是rest接口url的一部分，https://localhost/auth/realms/$realm/sms
     */
    public static final String ID = "sms";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public RealmResourceProvider create(KeycloakSession session) {
        return new SmsCodeResourceProvider(session);
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
