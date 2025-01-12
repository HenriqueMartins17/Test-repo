package cn.vika.keycloak.config;

import org.keycloak.common.util.MultivaluedHashMap;

import static cn.vika.keycloak.constants.CommonConstants.*;

/**
 * <p>
 * 维格表API客户端配置
 * </p>
 *
 * @author 胡海平(Humphrey Hu)
 * @date 2021/9/26 14:26:45
 */
public class VikaApiClientConfig {
    private final MultivaluedHashMap<String, String> config;

    public VikaApiClientConfig(MultivaluedHashMap<String, String> config) {
        this.config = config;
    }

    public String getVikaApiClientHost() {
        return config.getFirst(VIKA_CLIENT_HOST);
    }

    public String getVikaApiClientToken() {
        return config.getFirst(VIKA_CLIENT_TOKEN);
    }

    public String getVikaDataSheet() {
        return config.getFirst(VIKA_CLIENT_DATA_SHEET);
    }
}
