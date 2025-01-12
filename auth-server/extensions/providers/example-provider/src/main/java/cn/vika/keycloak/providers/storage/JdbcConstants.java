package cn.vika.keycloak.providers.storage;

/**
 * 定义
 * @author Shawn Deng
 * @date 2021-09-17 14:14:29
 */
public class JdbcConstants {

    public static final String JDBC_PROVIDER = "jdbc";

    // base config

    public static final String JDBC_DRIVER = "driver";

    public static final String JDBC_URL = "url";

    public static final String JDBC_USERNAME = "username";

    public static final String JDBC_PASSWORD = "password";

    // Hibernate 配置

    public static final String JDBC_DIALECT = "dialect";

    // 模式

    public static final String EDIT_MODE = "editMode";

    // hikariCP connection pool config

    public static final String HIKARICP_MINIMUM_IDLE = "minimumIdle";

    public static final String HIKARICP_MAXIMUM_POOL_SIZE = "maximumPoolSize";

    public static final String HIKARICP_IDLE_TIMEOUT = "idleTimeout";

    public static final String HIKARICP_CONNECTION_TIMEOUT = "connectionTimeout";
    public static final String SHOW_SQL = "showSql";
    public static final String FORMAT_SQL = "formatSql";
}
