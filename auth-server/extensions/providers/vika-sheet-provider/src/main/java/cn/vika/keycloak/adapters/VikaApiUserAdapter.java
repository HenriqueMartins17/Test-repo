package cn.vika.keycloak.adapters;

import cn.vika.keycloak.dto.VikaApiUserDto;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.adapter.AbstractUserAdapter;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.vika.keycloak.constants.CommonConstants.*;

/**
 * <p>
 * Vika user info federated storage
 * </p>
 *
 * @author 胡海平(Humphrey Hu)
 * @date 2021/9/23 20:05:51
 */
public class VikaApiUserAdapter extends AbstractUserAdapter {
    private final VikaApiUserDto vikaApiUserDto;

    public VikaApiUserAdapter(KeycloakSession session, RealmModel realm,
                              ComponentModel storageProviderModel,
                              VikaApiUserDto vikaApiUserDto) {
        super(session, realm, storageProviderModel);
        this.vikaApiUserDto = vikaApiUserDto;
    }

    @Override
    public String getUsername() {
        return this.vikaApiUserDto.getNickName();
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        Map<String, List<String>> attributes = super.getAttributes();
        attributes.put(USER_NICK_NAME, Collections.singletonList(vikaApiUserDto.getNickName()));
        attributes.put(USER_EMAIL, Collections.singletonList(vikaApiUserDto.getEmail()));
        attributes.put(USER_UUID, Collections.singletonList(vikaApiUserDto.getUuid()));
        return attributes;
    }

    @Override
    public void setUsername(String username) {
        vikaApiUserDto.setNickName(username);
    }

    @Override
    public String getEmail() {
        return vikaApiUserDto.getEmail();
    }

    @Override
    public void setEmail(String email) {
        super.setEmail(email);
    }

    @Override
    public String getId() {
        return StorageId.keycloakId(storageProviderModel, vikaApiUserDto.getEmail());
    }
}
