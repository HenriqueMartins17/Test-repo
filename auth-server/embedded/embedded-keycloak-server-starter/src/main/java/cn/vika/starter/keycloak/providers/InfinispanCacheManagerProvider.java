package cn.vika.starter.keycloak.providers;

import javax.naming.NamingException;

import cn.vika.starter.keycloak.DynamicJndiContextFactoryBuilder;
import com.google.auto.service.AutoService;
import org.infinispan.manager.DefaultCacheManager;
import org.keycloak.Config;
import org.keycloak.cluster.ManagedCacheManagerProvider;

import org.springframework.jndi.JndiTemplate;

@AutoService(ManagedCacheManagerProvider.class)
public class InfinispanCacheManagerProvider implements ManagedCacheManagerProvider {

    @Override
    @SuppressWarnings("unchecked")
    public <C> C getCacheManager(Config.Scope config) {
        try {
            return (C) new JndiTemplate().lookup(DynamicJndiContextFactoryBuilder.JNDI_CACHE_MANAGER, DefaultCacheManager.class);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }
}
