package cn.vika.keycloak.constant;

/**
 * <p>
 * 公共常量
 * </p>
 *
 * @author Leo Zhao
 * @date 2021/08/15 21:52
 */
public class CommonConstants {
    private CommonConstants() {
    }

    /**
     * 维格用户表字段
     */
    public static final String USER_UUID = "uuid";
    public static final String USER_EMAIL = "email";
    public static final String USER_NICK_NAME = "nick_name";
    public static final String USER_MOBILE_PHONE = "mobile_phone";
    public static final String USER_AVATAR = "avatar";

    /**
     * 数据库连接配置
     */
    public static final String HIKARI_USERNAME = "hibernate.hikari.username";
    public static final String HIKARI_PASSWORD = "hibernate.hikari.password";
    public static final String HIKARI_JDBC_URL = "hibernate.hikari.jdbcUrl";
    public static final String HIKARICP_MINIMUM_IDLE = "hibernate.hikari.minimumIdle";
    public static final String HIKARICP_CONNECTION_TEST_QUERY = "hibernate.hikari.connectionTestQuery";
    public static final String HIKARICP_MAXIMUM_POOL_SIZE = "hibernate.hikari.maximumPoolSize";
    public static final String HIKARICP_IDLE_TIMEOUT = "hibernate.hikari.idleTimeout";
    public static final String HIKARICP_MAX_LIFETIME = "hibernate.hikari.maxLifetime";
    public static final String HIKARICP_CONNECTION_TIMEOUT = "hibernate.hikari.connectionTimeout";
    public static final String HIKARICP_AUTO_COMMIT = "hibernate.hikari.autoCommit";
    public static final String HIKARICP_POOL_NAME = "hibernate.hikari.poolName";
    public static final String CONNECTION_PROVIDER = "org.hibernate.hikaricp.internal.HikariCPConnectionProvider";
    public static final String PERSISTENCE_UNIT_NAME = "jdbc-user-storage";
    public static final String PERSISTENCE_PROVIDER_CLASS_NAME = "org.hibernate.jpa.HibernatePersistenceProvider";

    /**
     * 维格表
     * */
    public static final String VIKA_TYPE_USER_NAME = "username";
    public static final String VIKA_TYPE_EMAIL = "email";
}
