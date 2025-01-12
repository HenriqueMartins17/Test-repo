package cn.vika.starter.keycloak;

import java.util.Hashtable;
import java.util.concurrent.ExecutorService;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;
import javax.naming.spi.NamingManager;
import javax.sql.DataSource;

import org.infinispan.manager.DefaultCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;

public class DynamicJndiContextFactoryBuilder implements InitialContextFactoryBuilder, InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(DynamicJndiContextFactoryBuilder.class);

    public static final String JNDI_SPRING_DATASOURCE = "spring/datasource";

    public static final String JNDI_CACHE_MANAGER = "spring/infinispan/cacheManager";

    public static final String JNDI_EXECUTOR_SERVICE = "java:jboss/ee/concurrency/executor/storage-provider-threads";

    private final InitialContext fixedInitialContext;

    public DynamicJndiContextFactoryBuilder(DataSource dataSource, DefaultCacheManager cacheManager, ExecutorService executorService) {
        fixedInitialContext = createFixedInitialContext(dataSource, cacheManager, executorService);
    }

    protected static InitialContext createFixedInitialContext(DataSource dataSource, DefaultCacheManager cacheManager, ExecutorService executorService) {

        Hashtable<Object, Object> jndiEnv = new Hashtable<>();
        jndiEnv.put(JNDI_SPRING_DATASOURCE, dataSource);
        jndiEnv.put(JNDI_CACHE_MANAGER, cacheManager);
        jndiEnv.put(JNDI_EXECUTOR_SERVICE, executorService);

        try {
            return new KeycloakInitialContext(jndiEnv);
        }
        catch (NamingException ne) {
            throw new RuntimeException("Could not create KeycloakInitialContext", ne);
        }
    }

    @Override
    public InitialContextFactory createInitialContextFactory(Hashtable<?, ?> environment) {

        if (environment == null || environment.isEmpty()) {
            return env -> fixedInitialContext;
        }

        String factoryClassName = (String) environment.get("java.naming.factory.initial");
        if (factoryClassName != null) {
            try {
                Class<?> factoryClass = Thread.currentThread().getContextClassLoader().loadClass(factoryClassName);
                return BeanUtils.instantiateClass(factoryClass, InitialContextFactory.class);
            }
            catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    public void afterPropertiesSet() {
        try {
            NamingManager.setInitialContextFactoryBuilder(this);
        }
        catch (NamingException e) {
            LOG.error("Could not configure InitialContextFactoryBuilder", e);
        }
    }
}