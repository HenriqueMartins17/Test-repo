package cn.vika.keycloak.provider;

import cn.vika.keycloak.dao.UserDao;
import cn.vika.keycloak.entity.SocialUserBindEntity;
import cn.vika.keycloak.entity.UserEntity;
import cn.vika.keycloak.entity.UserLinkEntity;
import com.google.auto.service.AutoService;
import lombok.extern.jbosslog.JBossLog;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.cfg.Environment;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.UserStorageProviderFactory;
import org.keycloak.storage.UserStorageProviderModel;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.EntityManagerFactory;
import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Stream;

import static cn.vika.keycloak.constant.CommonConstants.*;

/**
 * <p>
 * RDBMS用户联合provider
 * </p>
 *
 * @author Leo Zhao
 * @date 2021/08/14 21:22
 */
@JBossLog
@AutoService(UserStorageProviderFactory.class)
public class JdbcUserStorageProviderFactory implements UserStorageProviderFactory<JdbcUserStorageProvider> {
    /**
     * provider配置信息
     */
    protected static final List<ProviderConfigProperty> configMetadata;
    /**
     * provider唯一标识
     */
    public static final String PROVIDER_ID = "jdbc-storage";

    private static volatile Map<String, EntityManagerFactory> entityManagerFactories = new HashMap<>();

    static {
        configMetadata = ProviderConfigurationBuilder.create()
                /**
                 * JDBC URL
                 */
                .property()
                .name(HIKARI_JDBC_URL)
                .label("JDBC URL")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("jdbc:mysql://localhost/db")
                .helpText("JDBC Connection String")
                /**
                 * JDBC user
                 */
                .add().property()
                .name(HIKARI_USERNAME)
                .label("JDBC User")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("root")
                .helpText("JDBC Connection User")
                /**
                 * JDBC password
                 */
                .add().property()
                .name(HIKARI_PASSWORD)
                .label("JDBC Password")
                .type(ProviderConfigProperty.PASSWORD)
                .defaultValue("")
                .helpText("JDBC Connection Password")
                /**
                 * HikariCP Minimum idle
                 */
                .add().property()
                .name(HIKARICP_MINIMUM_IDLE)
                .label("HikariCP Minimum idle")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("10")
                .helpText("HikariCP Minimum idle")
                /**
                 * HikariCP Maximum Pool Size
                 */
                .add().property()
                .name(HIKARICP_MAXIMUM_POOL_SIZE)
                .label("HikariCP Maximum Pool Size")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("20")
                .helpText("HikariCP Maximum Pool Size")
                /**
                 * HikariCP idle Timeout
                 */
                .add().property()
                .name(HIKARICP_IDLE_TIMEOUT)
                .label("HikariCP idle Timeout")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("600000")
                .helpText("HikariCP idle Timeout")
                /**
                 * HikariCP Max Lifetime
                 */
                .add().property()
                .name(HIKARICP_MAX_LIFETIME)
                .label("HikariCP Max Lifetime")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("36000000")
                .helpText("HikariCP Max Lifetime")
                /**
                 * HikariCP Connection Timeout
                 */
                .add().property()
                .name(HIKARICP_CONNECTION_TIMEOUT)
                .label("HikariCP Connection Timeout")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("60000")
                .helpText("HikariCP Connection Timeout")
                /**
                 * HikariCP Auto Commit
                 */
                .add().property()
                .name(HIKARICP_AUTO_COMMIT)
                .label("HikariCP Auto Commit")
                .type(ProviderConfigProperty.LIST_TYPE)
                .options(Arrays.asList(Boolean.TRUE.toString(), Boolean.FALSE.toString()))
                .defaultValue(Boolean.TRUE.toString())
                .helpText("HikariCP Auto Commit")
                /**
                 * HikariCP Pool Name
                 */
                .add().property()
                .name(HIKARICP_POOL_NAME)
                .label("HikariCP Pool Name")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("HikariCP")
                .helpText("HikariCP Pool Name")

                .add().build();
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configMetadata;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel componentModel)
            throws ComponentValidationException {
        MultivaluedHashMap<String, String> configMap = componentModel.getConfig();
        if (StringUtils.isBlank(configMap.getFirst(HIKARI_JDBC_URL))) {
            throw new ComponentValidationException("JDBC URL is empty.");
        }
        if (StringUtils.isBlank(configMap.getFirst(HIKARI_USERNAME))) {
            throw new ComponentValidationException("JDBC User is empty.");
        }
        if (StringUtils.isBlank(configMap.getFirst(HIKARI_PASSWORD))) {
            throw new ComponentValidationException("JDBC Password is empty.");
        }
    }

    @Override
    public JdbcUserStorageProvider create(KeycloakSession session, ComponentModel componentModel) {
        EntityManagerFactory entityManagerFactory = getEntityMangerFactory(componentModel, componentModel.getParentId());
        UserDao userDao = new UserDao(entityManagerFactory.createEntityManager());
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return new JdbcUserStorageProvider(session, componentModel, passwordEncoder, userDao);
    }

    public static EntityManagerFactory getEntityMangerFactory(ComponentModel componentModel, String parentId) {
        log.infov("getEntityMangerFactory parentId={0}", parentId);
        EntityManagerFactory entityManagerFactory = entityManagerFactories.get(parentId);
        // 因为每次登录请求都会调用这个方法，所以把EntityManagerFactory缓存，防止每次重复初始化连接池
        if (Objects.isNull(entityManagerFactory)) {
            synchronized (JdbcUserStorageProviderFactory.class) {
                if (Objects.isNull(entityManagerFactory)) {
                    Map<String, Object> properties = new HashMap();
                    MultivaluedHashMap<String, String> config = componentModel.getConfig();
                    properties.put(Environment.SHOW_SQL, Boolean.TRUE.toString());
                    properties.put(Environment.SCANNER_DISCOVERY, "class, hbm");
                    properties.put(Environment.HBM2DDL_AUTO, "none");
                    properties.put(Environment.AUTOCOMMIT, "true");
                    properties.put(Environment.CONNECTION_PROVIDER, CONNECTION_PROVIDER);

                    properties.put(HIKARI_JDBC_URL, config.getFirst(HIKARI_JDBC_URL));
                    properties.put(HIKARI_USERNAME, config.getFirst(HIKARI_USERNAME));
                    properties.put(HIKARI_PASSWORD, config.getFirst(HIKARI_PASSWORD));
                    properties.put(HIKARICP_MINIMUM_IDLE, config.getFirst(HIKARICP_MINIMUM_IDLE));
                    properties.put(HIKARICP_MAXIMUM_POOL_SIZE, config.getFirst(HIKARICP_MAXIMUM_POOL_SIZE));
                    properties.put(HIKARICP_IDLE_TIMEOUT, config.getFirst(HIKARICP_IDLE_TIMEOUT));
                    properties.put(HIKARICP_MAX_LIFETIME, config.getFirst(HIKARICP_MAX_LIFETIME));
                    properties.put(HIKARICP_CONNECTION_TIMEOUT, config.getFirst(HIKARICP_CONNECTION_TIMEOUT));
                    properties.put(HIKARICP_AUTO_COMMIT, config.getFirst(HIKARICP_AUTO_COMMIT));
                    properties.put(HIKARICP_CONNECTION_TEST_QUERY, "SELECT 1");

                    entityManagerFactory = new HibernatePersistenceProvider()
                            .createContainerEntityManagerFactory(getPersistenceUnitInfo(PERSISTENCE_UNIT_NAME), properties);
                    entityManagerFactories.put(parentId, entityManagerFactory);
                }
            }
        }
        return entityManagerFactory;
    }

    public static EntityManagerFactory getEntityManagerFactory(KeycloakSession session) {
        RealmModel realm = session.getContext().getRealm();
        Optional<ComponentModel> componentModelOptional = session.getContext().getRealm().getComponentsStream(realm.getId())
                .filter(componentModel -> "jdbc-storage".equals(componentModel.getProviderId()))
                .findFirst();
        return getEntityMangerFactory(componentModelOptional.get(), realm.getId());
    }

    private static PersistenceUnitInfo getPersistenceUnitInfo(String name) {
        return new PersistenceUnitInfo() {
            @Override
            public String getPersistenceUnitName() {
                return name;
            }

            @Override
            public String getPersistenceProviderClassName() {
                return PERSISTENCE_PROVIDER_CLASS_NAME;
            }

            @Override
            public PersistenceUnitTransactionType getTransactionType() {
                return PersistenceUnitTransactionType.RESOURCE_LOCAL;
            }

            @Override
            public DataSource getJtaDataSource() {
                return null;
            }

            @Override
            public DataSource getNonJtaDataSource() {
                return null;
            }

            @Override
            public List<String> getMappingFileNames() {
                return Collections.emptyList();
            }

            @Override
            public List<URL> getJarFileUrls() {
                try {
                    return Collections.list(this.getClass()
                            .getClassLoader()
                            .getResources(""));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }

            @Override
            public URL getPersistenceUnitRootUrl() {
                return null;
            }

            @Override
            public List<String> getManagedClassNames() {
                List<String> managedClasses = new LinkedList<>();
                managedClasses.add(UserEntity.class.getName());
                managedClasses.add(UserLinkEntity.class.getName());
                managedClasses.add(SocialUserBindEntity.class.getName());
                return managedClasses;
            }

            @Override
            public boolean excludeUnlistedClasses() {
                return false;
            }

            @Override
            public SharedCacheMode getSharedCacheMode() {
                return SharedCacheMode.UNSPECIFIED;
            }

            @Override
            public ValidationMode getValidationMode() {
                return ValidationMode.AUTO;
            }

            @Override
            public Properties getProperties() {
                return new Properties();
            }

            @Override
            public String getPersistenceXMLSchemaVersion() {
                return "2.1";
            }

            @Override
            public ClassLoader getClassLoader() {
                return null;
            }

            @Override
            public void addTransformer(ClassTransformer transformer) {
            }

            @Override
            public ClassLoader getNewTempClassLoader() {
                return null;
            }
        };
    }
}