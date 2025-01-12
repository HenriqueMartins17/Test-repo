package cn.vika.keycloak.provider;

import cn.vika.keycloak.adapters.VikaApiUserAdapter;
import cn.vika.keycloak.dto.VikaApiUserDto;
import cn.vika.keycloak.services.VikaApiUserService;
import cn.vika.keycloak.services.VikaApiUserServiceImpl;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.cache.CachedUserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * 维格表的Storage Provider
 * </p>
 *
 * @author 胡海平(Humphrey Hu)
 * @date 2021/9/22 10:15:58
 */
public class VikaSheetStorageProvider implements
        UserStorageProvider,
        UserLookupProvider,
        CredentialInputValidator {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final KeycloakSession session;
    private final ComponentModel model;
    private final VikaApiUserService vikaApiUserService;

    /**
     * 缓存用户对象，避免在一个登录事务中重复创建用户
     * */
    protected Map<String, UserModel> loadUsers = new HashMap<>();

    public VikaSheetStorageProvider(KeycloakSession keycloakSession,
                                    ComponentModel componentModel,
                                    VikaApiUserService vikaApiUserService) {
        log.info("initial VikaSheetStorageProvider...");
        this.session = keycloakSession;
        this.model = componentModel;
        this.vikaApiUserService = vikaApiUserService;
    }

    /**
     * close method for UserStorageProvider
     * */
    @Override
    public void close() {
        log.info("Close method execute...");
    }

    /**
     * 根据keycloak的externalId获取用户信息
     * */
    @Override
    public UserModel getUserById(String id, RealmModel realm) {
        log.info("getUserById: id: {}, realm: {}", id, realm.getName());
        StorageId storageId = new StorageId(id);
        String email = storageId.getExternalId();
        return getUserByEmail(email, realm);
    }

    @Override
    public UserModel getUserByUsername(String username, RealmModel realm) {
        return null;
    }

    /**
     * 根据邮箱获取用户信息
     * */
    @Override
    public UserModel getUserByEmail(String email, RealmModel realm) {
        log.info("getUserByEmail: email: {}, realm: {}", email, realm.getName());
        UserModel adapter = loadUsers.get(email);
        if (Objects.isNull(adapter)) {
            log.info("getUserByEmail - get user from self query service...");
            VikaApiUserDto userDto = vikaApiUserService.findUserByEmail(email);
            if (Objects.nonNull(userDto)) {
                adapter = new VikaApiUserAdapter(session, realm, model, userDto);
                loadUsers.put(email, adapter);
            }
        }
        log.info("getUserByEmail - get user from cache...");
        return adapter;
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return PasswordCredentialModel.TYPE.equals(credentialType);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realmModel, UserModel userModel, String s) {
        return false;
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput credentialInput) {
        log.info("isValid - input: {}, user is {}, realm is {}", credentialInput.getChallengeResponse(),
                user.getUsername(), realm.getName());
        if (!supportsCredentialType(credentialInput.getType())) {
            return false;
        }
        log.info("Get Cached user from keycloak context, user is {}", user.getUsername());
        VikaApiUserDto userDto = vikaApiUserService.findUserByEmail(user.getEmail());
        return Objects.nonNull(userDto) &&
                userDto.getPassword().equals(credentialInput.getChallengeResponse());
    }
}
