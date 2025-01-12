package cn.vika.starter.keycloak;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

@ConfigurationProperties(prefix = "keycloak")
public class KeycloakProperties {

    private String contextPath = "/auth";

    private Admin admin = new Admin();

    private String realmFile = "keycloak-realm-config.json";

    private Infinispan infinispan = new Infinispan();

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public String getRealmFile() {
        return realmFile;
    }

    public void setRealmFile(String realmFile) {
        this.realmFile = realmFile;
    }

    public Infinispan getInfinispan() {
        return infinispan;
    }

    public void setInfinispan(Infinispan infinispan) {
        this.infinispan = infinispan;
    }

    public static class Infinispan {

        Resource configLocation = new ClassPathResource("infinispan.xml");

        public Resource getConfigLocation() {
            return configLocation;
        }

        public void setConfigLocation(Resource configLocation) {
            this.configLocation = configLocation;
        }
    }

    public static class Admin {

        boolean createAdminUserEnabled = true;

        String username = "admin";

        String password;

        public boolean isCreateAdminUserEnabled() {
            return createAdminUserEnabled;
        }

        public void setCreateAdminUserEnabled(boolean createAdminUserEnabled) {
            this.createAdminUserEnabled = createAdminUserEnabled;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
