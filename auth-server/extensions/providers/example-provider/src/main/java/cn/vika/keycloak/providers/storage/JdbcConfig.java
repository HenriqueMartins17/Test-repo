package cn.vika.keycloak.providers.storage;

import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.UserStorageProvider.EditMode;

/**
 * 配置获取简化
 * @author Shawn Deng
 * @date 2021-09-17 14:42:49
 */
public class JdbcConfig {

    private final MultivaluedHashMap<String, String> config;

    public JdbcConfig(MultivaluedHashMap<String, String> config) {
        this.config = config;
    }

    public String getDriver() {
        return config.getFirst(JdbcConstants.JDBC_DRIVER);
    }

    /**
     * 获取连接URL
     */
    public String getConnectionUrl() {
        return config.getFirst(JdbcConstants.JDBC_URL);
    }

    public String getDialect() {
        return config.getFirst(JdbcConstants.JDBC_DIALECT);
    }

    public String getUsername() {
        return config.getFirst(JdbcConstants.JDBC_USERNAME);
    }

    public String getPassword() {
        return config.getFirst(JdbcConstants.JDBC_PASSWORD);
    }

    public boolean isShowSql() {
        String showSql = config.getFirst(JdbcConstants.SHOW_SQL);
        return Boolean.parseBoolean(showSql);
    }

    public boolean isFormatSql() {
        String formatSql = config.getFirst(JdbcConstants.FORMAT_SQL);
        return Boolean.parseBoolean(formatSql);
    }

    /**
     * 编辑模式
     */
    public EditMode getEditMode() {
        String editModeString = config.getFirst(JdbcConstants.EDIT_MODE);
        if (editModeString == null) {
            return UserStorageProvider.EditMode.READ_ONLY;
        }
        else {
            return UserStorageProvider.EditMode.valueOf(editModeString);
        }
    }
}
