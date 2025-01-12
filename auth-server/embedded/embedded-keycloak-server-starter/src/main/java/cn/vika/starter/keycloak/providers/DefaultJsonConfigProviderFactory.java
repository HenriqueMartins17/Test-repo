package cn.vika.starter.keycloak.providers;

import com.google.auto.service.AutoService;
import org.keycloak.config.ConfigProviderFactory;
import org.keycloak.services.util.JsonConfigProviderFactory;

/**
 * <p>
 *
 * </p>
 * @author Shawn Deng
 * @date 2021/9/15 00:49
 */
@AutoService(ConfigProviderFactory.class)
public class DefaultJsonConfigProviderFactory extends JsonConfigProviderFactory {
}
