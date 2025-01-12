package cn.vika.keycloak.provider;

import cn.vika.keycloak.constant.CommonConstants;
import cn.vika.keycloak.dao.UserDao;
import cn.vika.keycloak.entity.UserEntity;
import cn.vika.keycloak.adapter.UserAdapter;
import lombok.extern.jbosslog.JBossLog;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputUpdater;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.cache.CachedUserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.ReadOnlyException;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserRegistrationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

/**
 * <p>
 * JDBC 用户联合
 * </p>
 *
 * @author Leo Zhao
 * @date 2021/08/14 21:22
 */
@JBossLog
public class JdbcUserStorageProvider implements UserStorageProvider,
        UserLookupProvider,
        UserRegistrationProvider,
        CredentialInputUpdater,
        CredentialInputValidator {

    private final KeycloakSession session;

    private final UserDao userDao;

    private final ComponentModel componentModel;

    private final PasswordEncoder passwordEncoder;

    public JdbcUserStorageProvider(KeycloakSession session,
                                   ComponentModel componentModel,
                                   PasswordEncoder passwordEncoder,
                                   UserDao userDao) {
        this.session = session;
        this.componentModel = componentModel;
        this.passwordEncoder = passwordEncoder;
        this.userDao = userDao;
    }

    @Override
    public UserModel getUserByUsername(String username, RealmModel realm) {
        log.infov("getUserByUsername(username: {0}, realm: {1})", username, realm.getName());
        Optional<UserEntity> optionalUser = userDao.getUserByUsername(username);
        UserAdapter userAdapter = optionalUser.map(user -> getUserAdapter(user, realm)).orElse(null);
        return userAdapter;
    }

    @Override
    public UserModel getUserById(String keycloakId, RealmModel realm) {
        log.infov("getUserById(keycloakId: {0}, realm: {1})", keycloakId, realm.getName());
        String id = StorageId.externalId(keycloakId);
        UserEntity userEntity = userDao.getUserById(Long.valueOf(id));
        UserAdapter userAdapter = new UserAdapter(session, realm, componentModel, userEntity);
        userAdapter.setAttribute(CommonConstants.USER_MOBILE_PHONE, Collections.singletonList(userEntity.getMobilePhone()));
        userAdapter.setAttribute(CommonConstants.USER_UUID, Collections.singletonList(userEntity.getUuid()));
        userAdapter.setAttribute(CommonConstants.USER_EMAIL, Collections.singletonList(userEntity.getEmail()));
        userAdapter.setAttribute(CommonConstants.USER_NICK_NAME, Collections.singletonList(userEntity.getNickName()));
        userAdapter.setAttribute(CommonConstants.USER_AVATAR, Collections.singletonList(userEntity.getAvatar()));
        return userAdapter;
    }

    @Override
    public UserModel getUserByEmail(String email, RealmModel realm) {
        log.infov("getUserByEmail(keycloakId: {0}, realm: {1})", email, realm.getName());
        Optional<UserEntity> optionalUser = userDao.getUserByEmail(email);
        return optionalUser.map(user -> getUserAdapter(user, realm)).orElse(null);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        return supportsCredentialType(credentialType) && Objects.nonNull(getPassword(user));
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return PasswordCredentialModel.TYPE.equals(credentialType);
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {
        if (!supportsCredentialType(input.getType())) {
            return false;
        }

        String password = getPassword(user);
        if (password == null) {
            return false;
        }

        return passwordEncoder.matches(input.getChallengeResponse(), password);
    }

    @Override
    public boolean updateCredential(RealmModel realm, UserModel user, CredentialInput input) {
        if (!supportsCredentialType(input.getType()) || !(input instanceof UserCredentialModel)) {
            return false;
        } else {
            throw new ReadOnlyException("User is read only for this update");
        }
    }

    @Override
    public void disableCredentialType(RealmModel realm, UserModel user, String credentialType) {
        if (!supportsCredentialType(credentialType)) {
            return;
        }
        getUserAdapter(user).setPassword(null);
    }

    @Override
    public Set<String> getDisableableCredentialTypes(RealmModel realm, UserModel user) {
        if (Objects.nonNull(getUserAdapter(user).getPassword())) {
            Set<String> set = new HashSet<>();
            set.add(PasswordCredentialModel.TYPE);
            return set;
        } else {
            return Collections.emptySet();
        }
    }

    @Override
    public void close() {
        userDao.close();
    }

    @Override
    public UserModel addUser(RealmModel realm, String username) {
        // TODO
        return null;
    }

    @Override
    public boolean removeUser(RealmModel realm, UserModel user) {
        return false;
    }

    public UserAdapter getUserAdapter(UserModel user) {
        UserAdapter userAdapter = null;
        if (user instanceof CachedUserModel) {
            userAdapter = (UserAdapter) ((CachedUserModel) user).getDelegateForUpdate();
        } else {
            userAdapter = (UserAdapter) user;
        }
        return userAdapter;
    }

    public UserAdapter getUserAdapter(UserEntity user, RealmModel realm) {
        return new UserAdapter(session, realm, componentModel, user);
    }

    public String getPassword(UserModel user) {
        String password = null;
        if (user instanceof CachedUserModel) {
            Optional<String> optionalPwd = Optional.empty();
            if (StringUtils.isNotBlank(user.getUsername())) {
                optionalPwd = userDao.getPasswordByUsername(user.getUsername());
            }
            if (!optionalPwd.isPresent() && StringUtils.isNotBlank(user.getEmail())) {
                optionalPwd = userDao.getPasswordByEmail(user.getEmail());
            }
            password = optionalPwd.isPresent() ? optionalPwd.get() : null;
        } else if (user instanceof UserAdapter) {
            password = ((UserAdapter) user).getPassword();
        }
        return password;
    }
}
