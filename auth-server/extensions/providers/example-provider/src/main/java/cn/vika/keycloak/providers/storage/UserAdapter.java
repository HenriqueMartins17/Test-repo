package cn.vika.keycloak.providers.storage;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;

public class UserAdapter extends AbstractUserAdapterFederatedStorage {

    String MOBILE_PHONE = "mobilePhone";

    String UUID = "uuid";

    String EMAIL = "email";

    String NICK_NAME = "nickName";

    String AVATAR = "avatar";

    protected UserEntity entity;

    protected String keycloakId;

    public UserAdapter(KeycloakSession session, RealmModel realm, ComponentModel model, UserEntity entity) {
        super(session, realm, model);
        this.entity = entity;
        this.keycloakId = StorageId.keycloakId(model, String.valueOf(entity.getId()));
    }

    public String getPassword() {
        return entity.getPassword();
    }

    public void setPassword(String password) {
        entity.setPassword(password);
    }

    @Override
    public String getUsername() {
        return entity.getNickName();
    }

    @Override
    public void setUsername(String username) {
        entity.setNickName(username);
    }

    @Override
    public void setEmail(String email) {
        entity.setEmail(email);
    }

    @Override
    public String getEmail() {
        return entity.getEmail();
    }

    @Override
    public String getId() {
        return keycloakId;
    }

    @Override
    public void setSingleAttribute(String name, String value) {
        if (name.equals(MOBILE_PHONE)) {
            entity.setMobilePhone(value);
        }
        else {
            super.setSingleAttribute(name, value);
        }
    }

    @Override
    public void removeAttribute(String name) {
        if (name.equals(MOBILE_PHONE)) {
            entity.setMobilePhone(null);
        }
        else {
            super.removeAttribute(name);
        }
    }

    @Override
    public void setAttribute(String name, List<String> values) {
        if (name.equals(MOBILE_PHONE)) {
            entity.setMobilePhone(values.get(0));
        }
        else {
            super.setAttribute(name, values);
        }
    }

    @Override
    public String getFirstAttribute(String name) {
        if (name.equals(MOBILE_PHONE)) {
            return entity.getMobilePhone();
        }
        else {
            return super.getFirstAttribute(name);
        }
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        Map<String, List<String>> attrs = super.getAttributes();
        MultivaluedHashMap<String, String> all = new MultivaluedHashMap<>();
        all.putAll(attrs);
        all.add(MOBILE_PHONE, entity.getMobilePhone());
        all.add(UUID, entity.getUuid());
        all.add(EMAIL, entity.getEmail());
        all.add(NICK_NAME, entity.getNickName());
        all.add(AVATAR, entity.getAvatar());
        return all;
    }

    @Override
    public List<String> getAttribute(String name) {
        if (name.equals(MOBILE_PHONE)) {
            List<String> phone = new LinkedList<>();
            phone.add(entity.getMobilePhone());
            return phone;
        }
        else {
            return super.getAttribute(name);
        }
    }
}
