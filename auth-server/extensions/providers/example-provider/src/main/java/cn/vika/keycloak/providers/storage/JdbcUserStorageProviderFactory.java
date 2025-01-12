package cn.vika.keycloak.providers.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.spi.PersistenceUnitTransactionType;

import com.google.auto.service.AutoService;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.jpa.boot.internal.ParsedPersistenceXmlDescriptor;
import org.hibernate.jpa.boot.internal.PersistenceXmlParser;
import org.hibernate.jpa.boot.spi.Bootstrap;
import org.jboss.logging.Logger;
import org.keycloak.common.util.StackUtil;
import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.UserStorageProviderFactory;
import org.keycloak.storage.UserStorageProviderModel;

/**
 * 实现 UserStorageProviderFactory
 * @author Shawn Deng
 * @date 2021-09-16 21:18:48
 */
@AutoService(UserStorageProviderFactory.class)
public class JdbcUserStorageProviderFactory implements UserStorageProviderFactory<JdbcUserStorageProvider> {

    private static final Logger logger = Logger.getLogger(JdbcUserStorageProviderFactory.class);

    public static final String PROVIDER_ID = JdbcConstants.JDBC_PROVIDER;

    protected static final List<ProviderConfigProperty> configProperties;

    private volatile EntityManagerFactory emf;

    static {
        configProperties = getConfigProps(null);
    }

    private static List<ProviderConfigProperty> getConfigProps(ComponentModel parent) {
        return ProviderConfigurationBuilder.create()
                // 当前只支持MYSQL
                .property().name(JdbcConstants.JDBC_DRIVER)
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("com.mysql.cj.jdbc.Driver")
                .label("Driver")
                .helpText("目前支持MySQL驱动，不支持其他的数据库类型")
                .add()
                // Dialect
                .property().name(JdbcConstants.JDBC_DIALECT)
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("org.hibernate.dialect.MySQL8Dialect")
                .label("Dialect")
                .helpText("数据库方言，暂时不支持其他方言")
                .add()
                // JDBC URL, example: jdbc:{dbType}://{hostname}:{port}/{database}
                .property().name(JdbcConstants.JDBC_URL)
                .type(ProviderConfigProperty.STRING_TYPE)
                .label("JDBC URL")
                .helpText("JDBC连接地址，通常是:jdbc:{dbType}://{hostname}:{port}/{database}")
                .add()
                // JDBC 用户名
                .property().name(JdbcConstants.JDBC_USERNAME)
                .type(ProviderConfigProperty.STRING_TYPE)
                .label("Username")
                .helpText("数据库用户名")
                .add()
                // JDBC 密码
                .property().name(JdbcConstants.JDBC_PASSWORD)
                .type(ProviderConfigProperty.STRING_TYPE)
                .secret(true)
                .label("Password")
                .helpText("数据库密码")
                .add()
                // HikariCP连接池配置
                .property().name(JdbcConstants.HIKARICP_MINIMUM_IDLE)
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("20")
                .label("最小空闲连接数")
                .helpText("连接池中维护的最小空闲连接数")
                .add()
                .property().name(JdbcConstants.HIKARICP_MAXIMUM_POOL_SIZE)
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("100")
                .label("最大连接数")
                .helpText("连接池中允许的最大连接数，包括闲置和使用中的连接")
                .add()
                .property().name(JdbcConstants.HIKARICP_IDLE_TIMEOUT)
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("200000")
                .label("连接存活的最长时间(毫秒)")
                .helpText("每个连接状态的最大时长（毫秒），超时则被释放")
                .add()
                .property().name(JdbcConstants.HIKARICP_CONNECTION_TIMEOUT)
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("10000")
                .label("连接超时时间(毫秒)")
                .helpText("等待连接池分配连接的最大时长（毫秒），超过这个时长还没可用的连接则发生SQLException")
                .add()
                .property().name(JdbcConstants.SHOW_SQL)
                .type(ProviderConfigProperty.BOOLEAN_TYPE)
                .defaultValue("false")
                .label("是否打印SQL语句")
                .helpText("控制台打印SQL语句")
                .add()
                .property().name(JdbcConstants.FORMAT_SQL)
                .type(ProviderConfigProperty.BOOLEAN_TYPE)
                .defaultValue("false")
                .label("是否格式化SQL语句")
                .helpText("SQL语句格式化")
                .add()
                .build();
    }

    @Override
    public JdbcUserStorageProvider create(KeycloakSession session, ComponentModel model) {
        initEntityManagerFactory(model);
        EntityManager em = emf.createEntityManager();
        return new JdbcUserStorageProvider(session, model, em);
    }

    @Override
    public void close() {
        if (emf != null) {
            emf.close();
        }
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getHelpText() {
        return "JDBC User Storage Provider";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    @Override
    public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel config) throws ComponentValidationException {
        // 校验输入的配置信息
        JdbcConfig jdbcConfig = new JdbcConfig(config.getConfig());
        UserStorageProviderModel userStorageModel = new UserStorageProviderModel(config);
        if (!userStorageModel.isImportEnabled() && jdbcConfig.getEditMode() == UserStorageProvider.EditMode.UNSYNCED) {
            // import users 未开启时，不能开启 unsynced 模式
            throw new ComponentValidationException("Error Cant Enable Unsynced mode And ImportOff");
        }
    }

    private Connection getConnection(JdbcConfig config) {
        try {
            Class.forName(config.getDriver());
            return DriverManager.getConnection(
                    config.getConnectionUrl(), config.getUsername(), config.getPassword());
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    private void initEntityManagerFactory(ComponentModel model) {
        JdbcConfig jdbcConfig = new JdbcConfig(model.getConfig());
        if (emf == null) {
            synchronized (this) {
                if (emf == null) {
                    logger.debugf("Initializing JPA connections%s", StackUtil.getShortStackTrace());
                    Map<String, Object> properties = new HashMap<>();

                    String unitName = "jdbc-unit";

                    properties.put(AvailableSettings.JPA_JDBC_URL, jdbcConfig.getConnectionUrl());
                    properties.put(AvailableSettings.JPA_JDBC_DRIVER, jdbcConfig.getDriver());

                    String user = jdbcConfig.getUsername();
                    if (user != null) {
                        properties.put(AvailableSettings.JPA_JDBC_USER, user);
                    }
                    String password = jdbcConfig.getPassword();
                    if (password != null) {
                        properties.put(AvailableSettings.JPA_JDBC_PASSWORD, password);
                    }
                    properties.put(AvailableSettings.DIALECT, jdbcConfig.getDialect());
                    properties.put(AvailableSettings.SHOW_SQL, jdbcConfig.isShowSql());
                    properties.put(AvailableSettings.FORMAT_SQL, jdbcConfig.isFormatSql());

                    String driverDialect = jdbcConfig.getDialect();
                    if (driverDialect != null) {
                        properties.put(AvailableSettings.DIALECT, driverDialect);
                    }

                    Connection connection = getConnection(jdbcConfig);
                    try {
                        emf = Persistence.createEntityManagerFactory(unitName, properties);
                        logger.trace("EntityManagerFactory created");
                    }
                    finally {
                        // 初始化EntityManagerFactory后，必须关闭connection
                        if (connection != null) {
                            try {
                                connection.close();
                            }
                            catch (SQLException e) {
                                logger.warn("不能关闭连接", e);
                            }
                        }
                    }
                }
            }
        }
    }

    private EntityManagerFactory createEntityManagerFactory(String unitName, Map<String, Object> properties) {
        PersistenceUnitTransactionType txType = PersistenceUnitTransactionType.RESOURCE_LOCAL;
        List<ParsedPersistenceXmlDescriptor> persistenceUnits = PersistenceXmlParser.locatePersistenceUnits(properties);
        for (ParsedPersistenceXmlDescriptor persistenceUnit : persistenceUnits) {
            if (persistenceUnit.getName().equals(unitName)) {
                // Now build the entity manager factory
                persistenceUnit.setTransactionType(txType);
                return Bootstrap.getEntityManagerFactoryBuilder(persistenceUnit, properties).build();
            }
        }
        throw new RuntimeException("Persistence unit '" + unitName + "' not found");
    }
}
