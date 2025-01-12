package cn.vika.starter.keycloak;

import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

import cn.vika.starter.keycloak.KeycloakProperties.Admin;
import cn.vika.starter.keycloak.providers.DefaultJsonConfigProviderFactory;
import org.keycloak.Config;
import org.keycloak.config.ConfigProviderFactory;
import org.keycloak.exportimport.ExportImportConfig;
import org.keycloak.exportimport.ExportImportManager;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakTransactionManager;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.managers.ApplianceBootstrap;
import org.keycloak.services.resources.KeycloakApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class EmbeddedKeycloakApplication extends KeycloakApplication {

    private static final Logger log = LoggerFactory.getLogger(EmbeddedKeycloakApplication.class);

    private final KeycloakProperties keycloakProperties;

    public EmbeddedKeycloakApplication(@Context ServletContext context) {
        this.keycloakProperties = WebApplicationContextUtils.getRequiredWebApplicationContext(context).getBean(KeycloakProperties.class);
    }

    @Override
    protected ExportImportManager bootstrap() {
        ExportImportManager exportImportManager = super.bootstrap();
        tryCreateMasterRealmAdminUser();
        tryImportRealm();
        return exportImportManager;
    }

    protected void loadConfig() {
        ConfigProviderFactory factory = new DefaultJsonConfigProviderFactory();
        Config.init(factory.create().orElseThrow(() -> new NoSuchElementException("没有提供服务配置文件 keycloak-server.json")));
    }

    protected void tryCreateMasterRealmAdminUser() {

        if (!keycloakProperties.getAdmin().isCreateAdminUserEnabled()) {
            log.warn("跳过创建keycloak master realm admin user.");
            return;
        }

        Admin admin = keycloakProperties.getAdmin();

        String username = admin.getUsername();
        if (!(StringUtils.hasLength(username) || StringUtils.hasText(username))) {
            return;
        }

        KeycloakSession session = KeycloakApplication.getSessionFactory().create();
        KeycloakTransactionManager transaction = session.getTransactionManager();
        try {
            transaction.begin();

            boolean randomPassword = false;
            String password = admin.getPassword();
            if (!StringUtils.hasLength(admin.getPassword())) {
                password = UUID.randomUUID().toString();
                randomPassword = true;
            }
            new ApplianceBootstrap(session).createMasterRealmUser(username, password);
            if (randomPassword) {
                log.info("随机生成admin密码: {}", password);
            }
            ServicesLogger.LOGGER.addUserSuccess(username, Config.getAdminRealm());
            transaction.commit();
        }
        catch (IllegalStateException e) {
            transaction.rollback();
            ServicesLogger.LOGGER.addUserFailedUserExists(username, Config.getAdminRealm());
        }
        catch (Throwable t) {
            transaction.rollback();
            ServicesLogger.LOGGER.addUserFailed(t, username, Config.getAdminRealm());
        }
        finally {
            session.close();
        }
    }

    protected void tryImportRealm() {

        Resource importLocation = new FileSystemResource(keycloakProperties.getRealmFile());

        if (!importLocation.exists()) {
            log.warn("找不到自定义realm文件 {}", importLocation);
            return;
        }
        // URL url;
        // try {
        //     url = importLocation.getURL();
        // }
        // catch (IOException e) {
        //     log.error("无法读取URL {}", importLocation, e);
        //     return;
        // }

        File file;
        try {
            // file = ResourceUtils.getFile(url.getPath());
            file = importLocation.getFile();
        }
        catch (IOException e) {
            log.error("无法读取自定义realm文件 {}", importLocation, e);
            return;
        }

        log.info("Keycloak Realm 配置插入开始 : {}", importLocation);

        KeycloakSession session = KeycloakApplication.getSessionFactory().create();

        ExportImportConfig.setAction("import");
        ExportImportConfig.setProvider("singleFile");
        ExportImportConfig.setFile(file.getAbsolutePath());

        ExportImportManager manager = new ExportImportManager(session);
        manager.runImport();

        session.close();

        log.info("Keycloak Realm 配置插入完成");
    }
}
