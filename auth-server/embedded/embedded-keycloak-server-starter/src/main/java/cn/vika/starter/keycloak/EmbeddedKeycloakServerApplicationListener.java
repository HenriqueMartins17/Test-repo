package cn.vika.starter.keycloak;

import org.jboss.logging.Logger;
import org.keycloak.common.Profile;
import org.keycloak.common.Version;

import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

/**
 * 启动完成监听器
 * @author Shawn Deng
 * @date 2021-09-16 16:55:33
 */
public class EmbeddedKeycloakServerApplicationListener implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = Logger.getLogger(EmbeddedKeycloakServerApplicationListener.class);

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        ServerProperties serverProperties = event.getApplicationContext().getBean(ServerProperties.class);
        KeycloakProperties keycloakProperties = event.getApplicationContext().getBean(KeycloakProperties.class);
        log.infof("Using Keycloak Version: %s", Version.VERSION_KEYCLOAK);
        log.infof("Enabled Keycloak Features (Deprecated): %s", Profile.getDeprecatedFeatures());
        log.infof("Enabled Keycloak Features (Preview): %s", Profile.getPreviewFeatures());
        log.infof("Enabled Keycloak Features (Experimental): %s", Profile.getExperimentalFeatures());
        log.infof("Enabled Keycloak Features (Disabled): %s", Profile.getDisabledFeatures());
        log.infof("Embedded Keycloak started: Browse to <http://localhost:%d%s> to use keycloak", serverProperties.getPort(), keycloakProperties.getContextPath());
    }
}
