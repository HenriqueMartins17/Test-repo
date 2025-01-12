package cn.vika.keycloak.authenticator;

import com.google.auto.service.AutoService;
import cn.vika.keycloak.constant.CommonAuthConstants;
import org.keycloak.Config;
import org.keycloak.OAuth2Constants;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.authentication.DisplayTypeAuthenticatorFactory;
import org.keycloak.authentication.authenticators.console.ConsoleUsernamePasswordAuthenticator;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

import java.util.List;

/**
 * <p>
 * 维格自定义校验器工厂
 * </p>
 *
 * @author Leo Zhao
 * @date 2021/08/30 11:34
 */
@AutoService(AuthenticatorFactory.class)
public class PhoneOrEmailFormFactory implements AuthenticatorFactory, DisplayTypeAuthenticatorFactory {

    public static final String PROVIDER_ID = "auth-email-or-phone-form";

    public static final PhoneOrEmailForm SINGLETON = new PhoneOrEmailForm();

    @Override
    public Authenticator create(KeycloakSession session) {
        return SINGLETON;
    }

    @Override
    public Authenticator createDisplay(KeycloakSession session, String displayType) {
        if (displayType == null) {
            return SINGLETON;
        }
        if (!OAuth2Constants.DISPLAY_CONSOLE.equalsIgnoreCase(displayType)) {
            return null;
        }
        return ConsoleUsernamePasswordAuthenticator.SINGLETON;
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getReferenceCategory() {
        return PasswordCredentialModel.TYPE;
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    public static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED
    };

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public String getDisplayType() {
        return "Email Or Phone Form";
    }

    @Override
    public String getHelpText() {
        return "Validates a phone or email and password or code from login form.";
    }

    private static final List<ProviderConfigProperty> configProperties;

    static {
        configProperties = ProviderConfigurationBuilder.create().property()
                .name(CommonAuthConstants.VIKA_REST_API_HOST).label("Vika host").type(ProviderConfigProperty.STRING_TYPE).helpText("Vika host")
                .add().property().name(CommonAuthConstants.AFS_REGION_ID).label("Afs regionId").type(ProviderConfigProperty.STRING_TYPE).helpText("Afs regionId")
                .add().property().name(CommonAuthConstants.AFS_ACCESS_KEY_ID).label("Afs access keyId").type(ProviderConfigProperty.STRING_TYPE).helpText("Afs access keyId")
                .add().property().name(CommonAuthConstants.AFS_ACCESS_KEY_SECRET).label("Afs access keySecret").type(ProviderConfigProperty.STRING_TYPE).helpText("Afs access keySecret")
                .add().build();
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

}
