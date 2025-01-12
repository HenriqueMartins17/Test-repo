package cn.vika.keycloak.provider;

import cn.vika.core.utils.StringUtil;
import cn.vika.keycloak.config.VikaApiClientConfig;
import cn.vika.keycloak.services.VikaApiUserService;
import cn.vika.keycloak.services.VikaApiUserServiceImpl;
import cn.vika.keycloak.utils.VikaApiClientUtil;
import com.google.auto.service.AutoService;
import org.keycloak.Config;
import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.UserStorageProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static cn.vika.keycloak.constants.CommonConstants.*;

/**
 * <p>
 * 维格社区小号storage provider factory
 * </p>
 *
 * @author 胡海平(Humphrey Hu)
 * @date 2021/9/22 10:15:34
 */
@AutoService(UserStorageProviderFactory.class)
public class VikaSheetUserStorageProviderFactory implements
        UserStorageProviderFactory<VikaSheetStorageProvider> {
    private static final Logger log = LoggerFactory.getLogger(VikaSheetUserStorageProviderFactory.class);

    private static final String VIKA_SHEET_STORAGE_PROVIDER_NAME = "vika-sheet-storage-provider";

    protected static final List<ProviderConfigProperty> configMetadata;

    static {
        log.info("initial provider config builder start...");
        configMetadata = ProviderConfigurationBuilder.create()
                .property().name(VIKA_CLIENT_HOST)
                .label("客户端URL")
                .defaultValue(null)
                .type(ProviderConfigProperty.STRING_TYPE)
                .helpText("维格表API客户端的URL")
                .add()

                .property().name(VIKA_CLIENT_TOKEN)
                .label("客户端Token")
                .defaultValue(null)
                .type(ProviderConfigProperty.STRING_TYPE)
                .helpText("维格表的API Token")
                .add()

                .property().name(VIKA_CLIENT_DATA_SHEET)
                .label("维格表ID")
                .defaultValue(null)
                .type(ProviderConfigProperty.STRING_TYPE)
                .helpText("创建维格表生成的表格ID")
                .add()

                .build();
        log.info("initial provider config builder end...");
    }

    @Override
    public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel config) throws ComponentValidationException {
        VikaApiClientConfig apiClientConfig = new VikaApiClientConfig(config.getConfig());
        if (StringUtil.isEmpty(apiClientConfig.getVikaApiClientHost())) {
            throw new ComponentValidationException("请提供一个维格表的API Host");
        }
        if (StringUtil.isEmpty(apiClientConfig.getVikaApiClientToken())) {
            throw new ComponentValidationException("请提供一个维格表的API Token");
        }
        if (StringUtil.isEmpty(apiClientConfig.getVikaDataSheet())) {
            throw new ComponentValidationException("请提供一个维格表的ID");
        }
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configMetadata;
    }

    @Override
    public VikaSheetStorageProvider create(KeycloakSession keycloakSession, ComponentModel componentModel) {
        log.info("Initial VikaSheetStorageProvider start ...");
        VikaApiClientUtil vikaApiClientUtil = new VikaApiClientUtil(componentModel);
        VikaApiUserService vikaApiUserService = new VikaApiUserServiceImpl(vikaApiClientUtil);
        return new VikaSheetStorageProvider(keycloakSession, componentModel, vikaApiUserService);
    }

    @Override
    public String getId() {
        return VIKA_SHEET_STORAGE_PROVIDER_NAME;
    }
}
