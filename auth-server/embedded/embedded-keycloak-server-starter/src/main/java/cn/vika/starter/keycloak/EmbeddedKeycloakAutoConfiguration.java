package cn.vika.starter.keycloak;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.Filter;
import javax.sql.DataSource;

import cn.vika.starter.keycloak.providers.SpringBootPlatformProvider;
import org.infinispan.configuration.parsing.ConfigurationBuilderHolder;
import org.infinispan.configuration.parsing.ParserRegistry;
import org.infinispan.manager.DefaultCacheManager;
import org.jboss.resteasy.plugins.server.servlet.HttpServlet30Dispatcher;
import org.jboss.resteasy.plugins.server.servlet.ResteasyContextParameters;
import org.keycloak.platform.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(KeycloakProperties.class)
public class EmbeddedKeycloakAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(EmbeddedKeycloakAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean(name = "springBootPlatform")
    public SpringBootPlatformProvider springBootPlatform() {
        return (SpringBootPlatformProvider) Platform.getPlatform();
    }

    @Bean
    @ConditionalOnMissingBean(name = "springBeansJndiContextFactory")
    public DynamicJndiContextFactoryBuilder springBeansJndiContextFactory(DataSource dataSource, DefaultCacheManager cacheManager, @Qualifier("fixedThreadPool") ExecutorService executorService) {
        return new DynamicJndiContextFactoryBuilder(dataSource, cacheManager, executorService);
    }

    @Bean("fixedThreadPool")
    public ExecutorService fixedThreadPool() {
        return Executors.newFixedThreadPool(5);
    }

    @Bean
    @ConditionalOnMissingBean(name = "keycloakInfinispanCacheManager")
    public DefaultCacheManager keycloakInfinispanCacheManager(KeycloakProperties keycloakProperties) throws Exception {

        KeycloakProperties.Infinispan infinispan = keycloakProperties.getInfinispan();
        Resource configLocation = infinispan.getConfigLocation();
        log.info("Using infinispan configuration from {}", configLocation.getURI());

        ConfigurationBuilderHolder configBuilder = new ParserRegistry().parse(configLocation.getURL());
        DefaultCacheManager defaultCacheManager = new DefaultCacheManager(configBuilder, false);
        defaultCacheManager.start();
        return defaultCacheManager;
    }

    @Bean
    @ConditionalOnMissingBean(name = "keycloakJaxRsApplication")
    public ServletRegistrationBean<HttpServlet30Dispatcher> keycloakJaxRsApplication(KeycloakProperties keycloakProperties) {

        initKeycloakEnvironmentFromProfiles();

        ServletRegistrationBean<HttpServlet30Dispatcher> servlet = new ServletRegistrationBean<>(new HttpServlet30Dispatcher());
        servlet.addInitParameter("javax.ws.rs.Application", EmbeddedKeycloakApplication.class.getName());

        servlet.addInitParameter("resteasy.allowGzip", "true");
        servlet.addInitParameter("keycloak.embedded", "true");
        servlet.addInitParameter(ResteasyContextParameters.RESTEASY_EXPAND_ENTITY_REFERENCES, "false");
        servlet.addInitParameter(ResteasyContextParameters.RESTEASY_SECURE_PROCESSING_FEATURE, "true");
        servlet.addInitParameter(ResteasyContextParameters.RESTEASY_DISABLE_DTDS, "true");
        servlet.addInitParameter(ResteasyContextParameters.RESTEASY_SERVLET_MAPPING_PREFIX, keycloakProperties.getContextPath());
        servlet.addInitParameter(ResteasyContextParameters.RESTEASY_USE_CONTAINER_FORM_PARAMS, "false");
        servlet.addInitParameter(ResteasyContextParameters.RESTEASY_DISABLE_HTML_SANITIZER, "true");
        servlet.addUrlMappings(keycloakProperties.getContextPath() + "/*");

        servlet.setLoadOnStartup(2);
        servlet.setAsyncSupported(true);

        return servlet;
    }

    private void initKeycloakEnvironmentFromProfiles() {

        try (InputStream in = getClass().getClassLoader().getResourceAsStream("profile.properties")) {

            if (in == null) {
                log.info("Could not find profile.properties on classpath.");
                return;
            }

            Properties profile = new Properties();
            profile.load(in);

            log.info("Found profile.properties on classpath.");
            String profilePrefix = "keycloak.profile.";
            for (Object key : profile.keySet()) {
                String value = (String) profile.get(key);
                String featureName = key.toString().toLowerCase();
                String currentValue = System.getProperty(profilePrefix + featureName);
                if (currentValue == null) {
                    System.setProperty(profilePrefix + featureName, value);
                }
            }
        }
        catch (IOException ioe) {
            log.warn("Could not read profile.properties.", ioe);
        }
    }

    @Bean
    @ConditionalOnMissingBean(name = "keycloakSessionManagement")
    public FilterRegistrationBean<Filter> keycloakSessionManagement(KeycloakProperties keycloakProperties) {

        FilterRegistrationBean<Filter> filter = new FilterRegistrationBean<>();
        filter.setName("Keycloak Session Management");
        filter.setFilter(new KeycloakRequestFilter());
        filter.addUrlPatterns(keycloakProperties.getContextPath() + "/*");

        return filter;
    }

}
