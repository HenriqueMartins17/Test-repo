package cn.vika.keycloak.providers.storage;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputUpdater;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.cache.CachedUserModel;
import org.keycloak.models.cache.OnUserCache;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.ReadOnlyException;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.UserStorageProviderModel;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.utils.StringUtil;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 用户存储提供者
 * @author Shawn Deng
 * @date 2021-09-16 21:13:22
 */
public class JdbcUserStorageProvider implements
        UserStorageProvider, UserLookupProvider,
        CredentialInputUpdater, CredentialInputValidator, OnUserCache {

    private static final Logger logger = Logger.getLogger(JdbcUserStorageProvider.class);

    protected KeycloakSession session;

    protected UserStorageProviderModel model;

    protected PasswordEncoder passwordEncoder;

    public static final String PASSWORD_CACHE_KEY = UserAdapter.class.getName() + ".password";

    protected EntityManager em;

    protected UserRepository userRepository;

    public JdbcUserStorageProvider(KeycloakSession session, ComponentModel model, EntityManager em) {
        this.session = session;
        this.model = new UserStorageProviderModel(model);
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.userRepository = new UserRepository(em);
    }

    // UserStorageProvider


    @Override
    public void close() {
        logger.trace("关闭 EntityManager");
        em.close();
    }

    // UserLookupProvider

    @Override
    public UserModel getUserById(String id, RealmModel realm) {
        // 根据ID获取
        String persistenceId = StorageId.externalId(id);
        UserEntity entity = userRepository.findUserById(Long.valueOf(persistenceId));
        if (entity == null) {
            logger.error("无法找到用户: " + id);
            return null;
        }
        return new UserAdapter(session, realm, model, entity);
    }

    @Override
    public UserModel getUserByUsername(String username, RealmModel realm) {
        List<UserEntity> result = userRepository.findUserByMobilePhone(username);
        if (result.isEmpty()) {
            return null;
        }
        return new UserAdapter(session, realm, model, result.get(0));
    }

    @Override
    public UserModel getUserByEmail(String email, RealmModel realm) {
        List<UserEntity> result = userRepository.findUsersByEmail(email);
        if (result.isEmpty()) {
            return null;
        }
        return new UserAdapter(session, realm, model, result.get(0));
    }

    // CredentialInputValidator

    @Override
    public boolean supportsCredentialType(String credentialType) {
        // 凭证类型是否支持验证
        return PasswordCredentialModel.TYPE.equals(credentialType);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        // 此方法检查是否为用户设置了密码
        return supportsCredentialType(credentialType) && StringUtil.isNotBlank(getPassword(user));
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {
        // 验证密码
        if (!supportsCredentialType(input.getType())) {
            return false;
        }
        // 获取数据库中存储的密码，然后对比
        String password = getPassword(user);
        return StringUtil.isNotBlank(password) && passwordEncoder.matches(input.getChallengeResponse(), password);
    }

    public String getPassword(UserModel user) {
        String password = null;
        if (user instanceof CachedUserModel) {
            password = (String) ((CachedUserModel) user).getCachedWith().get(PASSWORD_CACHE_KEY);
        }
        else if (user instanceof UserAdapter) {
            password = ((UserAdapter) user).getPassword();
        }
        return password;
    }

    // CredentialInputUpdater

    @Override
    public boolean updateCredential(RealmModel realm, UserModel user, CredentialInput input) {
        // 不支持修改密码
        throw new ReadOnlyException("user is read only for this update");
    }

    @Override
    public void disableCredentialType(RealmModel realm, UserModel user, String credentialType) {

    }

    @Override
    public Set<String> getDisableableCredentialTypes(RealmModel realm, UserModel user) {
        // 设置用户
        return Collections.emptySet();
    }

    // OnUserCache

    @Override
    public void onCache(RealmModel realm, CachedUserModel user, UserModel delegate) {
        String password = ((UserAdapter) delegate).getPassword();
        if (password != null) {
            user.getCachedWith().put(PASSWORD_CACHE_KEY, password);
        }
    }
}
