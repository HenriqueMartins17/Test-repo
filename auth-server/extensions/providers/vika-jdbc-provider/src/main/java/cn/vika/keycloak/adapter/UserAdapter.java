package cn.vika.keycloak.adapter;

import cn.vika.keycloak.constant.CommonConstants;
import cn.vika.keycloak.dao.UserDao;
import cn.vika.keycloak.entity.UserEntity;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class UserAdapter extends AbstractUserAdapterFederatedStorage {
    private final UserEntity userEntity;

    public UserAdapter(KeycloakSession session,
                       RealmModel realm,
                       ComponentModel storageProviderModel,
                       UserEntity userEntity) {
        super(session, realm, storageProviderModel);
        this.userEntity = userEntity;
    }

    @Override
    public String getUsername() {
        return userEntity.getMobilePhone();
    }

    @Override
    public void setUsername(String username) {
        // TODO 修改用户名
    }

    @Override
    public String getEmail() {
        return userEntity.getEmail();
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        Map<String, List<String>> attributes = super.getAttributes();
        attributes.put(CommonConstants.USER_MOBILE_PHONE, Collections.singletonList(userEntity.getMobilePhone()));
        attributes.put(CommonConstants.USER_UUID, Collections.singletonList(userEntity.getUuid()));
        attributes.put(CommonConstants.USER_EMAIL, Collections.singletonList(userEntity.getEmail()));
        attributes.put(CommonConstants.USER_NICK_NAME, Collections.singletonList(userEntity.getNickName()));
        attributes.put(CommonConstants.USER_AVATAR, Collections.singletonList(userEntity.getAvatar()));
        return attributes;
    }

    @Override
    public String getId() {
        return StorageId.keycloakId(storageProviderModel, userEntity.getId().toString());
    }

    public String getPassword() {
        return userEntity.getPassword();
    }

    public void setPassword(String password) {
        // TODO 修改密码
    }
}
